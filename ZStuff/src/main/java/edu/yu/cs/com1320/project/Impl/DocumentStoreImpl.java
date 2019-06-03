package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.*;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.bzip2.*;
import org.apache.commons.compress.compressors.gzip.*;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.function.Function;

/**
 * @author Zechariah-Rosenthal
 */
public class DocumentStoreImpl implements DocumentStore {
    private BTreeImpl<URI, DocumentImpl> docStorage;
    private CompressionFormat defaultFormat;
    private Stack<Command> commandStack;
    private TrieImpl<URI> documentTrie;
    private URIToDocComparator comparator;
    private MinHeapImpl<URIWrapper> leastUsedDocHeap;
    private UndoIO undoIO;
    private int maxDocCount;
    private int maxTotalDocBytes;
    private int currentTotalDocBytes;

    public DocumentStoreImpl() {
        docStorage = new BTreeImpl<>();
        leastUsedDocHeap = new MinHeapImpl<>();
        defaultFormat = CompressionFormat.ZIP;
        commandStack = new StackImpl<>();
        comparator = new URIToDocComparator(docStorage);
        documentTrie = new TrieImpl<>(comparator);
        undoIO = new UndoIO();
        maxDocCount = maxTotalDocBytes = Integer.MAX_VALUE;
        currentTotalDocBytes = 0;
        URIWrapper.setBTree(docStorage);
    }

    /**
     * set maximum number of documents that may be stored.
     * Passing 0 results in resetting the limit to its default of Integer.MAX_VALUE
     *
     * @param limit
     * @throws IllegalArgumentException if passed a negative limit
     */
    public void setMaxDocumentCount(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Max Document Count cannot be negative.");
        } else if (limit == 0) {
            maxDocCount = Integer.MAX_VALUE;
        } else {
            maxDocCount = limit;
            manageMemory();
        }
    }

    /**
     * set maximum number of bytes of memory that may be used by all the compressed documents in memory combined
     * Passing 0 results in resetting the limit to its default of Integer.MAX_VALUE
     *
     * @param limit
     * @throws IllegalArgumentException if passed a negative limit
     */
    public void setMaxDocumentBytes(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Max Document Bytes cannot be negative.");
        } else if (limit == 0) {
            maxTotalDocBytes = Integer.MAX_VALUE;
        } else {
            maxTotalDocBytes = limit;
            manageMemory();
        }
    }


    /**
     * Retrieve all documents that contain the given key word.
     * Documents are returned in sorted, in descending order, by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     *
     * @param keyword
     * @return A List<String> of the uncompressed text of each Document, or null if not found
     */
    @Override
    public List<String> search(String keyword) {
        keyword = fixKey(keyword);
        comparator.setKeyword(keyword);
        List<URI> URISearchResults = documentTrie.getAllSorted(keyword);
        if (URISearchResults.isEmpty()) return new ArrayList<>();

        List<DocumentImpl> docSearchResults = new ArrayList<>();
        for (URI uri : URISearchResults) {
            docSearchResults.add(docStorage.get(uri));
        }
        List<String> fullDocTexts = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (DocumentImpl doc : docSearchResults) {
            checkIfJustPulledFromDisc(doc);
            doc.setLastUsedTime(currentTime);
            leastUsedDocHeap.reHeapify(new URIWrapper(doc.getKey()));
            String docText = decompress(doc.getDocument(), doc.getCompressionFormat());
            fullDocTexts.add(docText);
        }
        return fullDocTexts;
    }

    protected void setBaseDir(File baseDir) {
        this.docStorage.setBaseDir(baseDir);
    }

    private void checkIfJustPulledFromDisc(DocumentImpl doc) {
        if (doc == null) return;
        URIWrapper uri = new URIWrapper(doc.getKey());
        int index = leastUsedDocHeap.getArrayIndex(uri);
        if (index == -1) {
            leastUsedDocHeap.insert(uri);
            currentTotalDocBytes += doc.getDocument().length;
            manageMemory();
        }
    }

    /**
     * Retrieve the compressed form of all documents that contain the given key word.
     * Documents are returned in sorted, in descending order, by the number of times the keyword appears in the document
     * Search is CASE INSENSITIVE.
     *
     * @param keyword
     * @return
     */
    @Override
    public List<byte[]> searchCompressed(String keyword) {
        keyword = fixKey(keyword);
        comparator.setKeyword(keyword);
        List<URI> URISearchResults = documentTrie.getAllSorted(keyword);
        if (URISearchResults.isEmpty()) return new ArrayList<>();

        List<DocumentImpl> docSearchResults = new ArrayList<>();
        for (URI uri : URISearchResults) {
            docSearchResults.add(docStorage.get(uri));
        }
        List<byte[]> compressedDocs = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (DocumentImpl doc : docSearchResults) {
            checkIfJustPulledFromDisc(doc);
            doc.setLastUsedTime(currentTime);
            leastUsedDocHeap.reHeapify(new URIWrapper(doc.getKey()));
            byte[] docBytes = doc.getDocument();
            compressedDocs.add(docBytes);
        }
        return compressedDocs;
    }

    /**
     * specify which compression format should be used if none is specified on a putDocument,
     * either because the two-argument putDocument method is used or the CompressionFormat is
     * null in the three-argument version of putDocument
     *
     * @param format
     * @see DocumentStore#putDocument(InputStream, URI)
     * @see DocumentStore#putDocument(InputStream, URI, CompressionFormat)
     */
    public void setDefaultCompressionFormat(CompressionFormat format) {
        if (checkFormat(format)) {
            this.defaultFormat = format;
        }
    }

    @Override
    public CompressionFormat getDefaultCompressionFormat() {
        return this.defaultFormat;
    }


    /**
     * @param uri the unique identifier of the document to get
     * @return the <B>compressed</B> version of the document
     */
    public byte[] getCompressedDocument(URI uri) {
        DocumentImpl doc = this.docStorage.get(uri);

        if (doc == null) {
            return null;
        } else {
            checkIfJustPulledFromDisc(doc);
            doc.setLastUsedTime(System.currentTimeMillis());
            leastUsedDocHeap.reHeapify(new URIWrapper(uri));
            return doc.getDocument();
        }
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the <B>uncompressed</B> document as a String, or null if no such document exists
     */
    public String getDocument(URI uri) {
        DocumentImpl doc = this.docStorage.get(uri);
        if (doc == null) {
            return null;
        } else {
            checkIfJustPulledFromDisc(doc);
            doc.setLastUsedTime(System.currentTimeMillis());
            leastUsedDocHeap.reHeapify(new URIWrapper(uri));
            return decompress(doc.getDocument(), doc.getCompressionFormat());
        }
    }

    /**
     * since the user does not specify a compression format, use the default compression format
     *
     * @param input the document being put
     * @param uri   unique identifier for the document
     * @return the hashcode of the document
     */
    public int putDocument(InputStream input, URI uri) {
        return putDocument(input, uri, defaultFormat);
    }

    /**
     * @param input  the document being put
     *               If null, calls deleteDocument(uri)
     * @param uri    unique identifier for the document
     *               If null, returns Integer.MIN_VALUE
     * @param format compression format to use for compressing this document
     * @return the hashcode of the document
     */
    public int putDocument(InputStream input, URI uri, CompressionFormat format) {
        if (uri == null) return Integer.MIN_VALUE; //If there was an error reading the file, what should I do here?
        if (input == null) return this.deleteDocument(uri) ? Integer.MIN_VALUE : 1;
        try {
            byte[] uncompressedBytes = IOUtils.toByteArray(input);
            input.close();
            String uncompressedFile = new String(uncompressedBytes);
            int fileHashCode = Math.abs(uncompressedFile.hashCode());
            if (checkDuplicateDoc(uri, fileHashCode)) {
                return Integer.MIN_VALUE; //If there was a duplicate, what should I return?
            }
            if (!checkFormat(format)) {
                format = this.defaultFormat;
            }
            byte[] compressedBytes = compress(uncompressedBytes, format);
            if (compressedBytes == null) return -1;
            if (compressedBytes.length > maxTotalDocBytes) return -1; //too big
            HashMap<String, Integer> wordsCounted = getHashMapOfWords(uncompressedFile);
            DocumentImpl doc = new DocumentImpl(compressedBytes, fileHashCode, uri, format, wordsCounted);
            pushPutCommand(uri, doc);
            addWordsToTrie(doc);
            this.docStorage.put(uri, doc);
            addDocToHeap(doc);
            manageMemory();
            return fileHashCode;
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
            return Integer.MIN_VALUE;
        }
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean deleteDocument(URI uri) {
        DocumentImpl toDelete = docStorage.get(uri);
        if (toDelete == null) {
            return false;
        }
        checkIfJustPulledFromDisc(toDelete);
        pushDelCommand(uri, toDelete);
        deleteWordsFromTrie(toDelete);
        removeDocFromHeap(toDelete);
        docStorage.put(uri, null);
        return true;
    }

    /**
     * undo the last put or delete command
     *
     * @return true if successfully undid command, false if not successful
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public boolean undo() throws IllegalStateException {
        Command toUndo = this.commandStack.pop();
        if (toUndo == null) {
            throw new IllegalStateException("No actions to be undone.");
        }
        boolean result = toUndo.undo();
        manageMemory();
        return result;
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     *
     * @param uri
     * @return
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    public boolean undo(URI uri) throws IllegalStateException {
        Stack<Command> tempStack = new StackImpl<>();
        while (commandStack.size() > 0) {
            Command current = commandStack.pop();
            if (current == null) break;
            if (current.getUri().equals(uri)) {
                boolean result = current.undo();
                manageMemory();
                refillCommandStack(tempStack);
                manageMemory();
                return result;
            }
            current.undo();
            tempStack.push(current);
        }
        refillCommandStack(tempStack);
        throw new IllegalStateException("No actions on the commandStack for given URI.");
    }

    private void manageMemory() {
        while (this.docStorage.size() > maxDocCount || currentTotalDocBytes > maxTotalDocBytes) {
            URI uri = leastUsedDocHeap.removeMin().getUri();
            DocumentImpl toMoveToDisk = docStorage.get(uri);
            currentTotalDocBytes -= toMoveToDisk.getDocument().length;
            try {
                this.docStorage.moveToDisk(uri);
            } catch (Exception e) {
            }
        }
    }

    private boolean checkDuplicateDoc(URI uri, int hashCode) {
        DocumentImpl duplicate = this.docStorage.get(uri);
        if (duplicate == null) {
            return false;
        } else {
            checkIfJustPulledFromDisc(duplicate);
            return hashCode == duplicate.getDocumentHashCode();
        }
    }

    protected HashMap<String, Integer> getHashMapOfWords(String text) {
        if (text == null || text.equals("")) {
            throw new IllegalArgumentException("Text cannot be empty.");
        }
        text = text.trim();
        text = text.replaceAll("[\\p{P}\\p{S}0-9]", "");
        if (text.equals("")) {
            throw new IllegalArgumentException("Key must be a word.");
        }
        text = text.toLowerCase();
        String[] words = text.split(" ");
        HashMap<String, Integer> wordsCounted = new HashMap<>();
        for (String word : words) {
            if (word != "") {
                int count = wordsCounted.getOrDefault(word, 0);
                count++;
                wordsCounted.put(word, count);
            }
        }
        return wordsCounted;
    }


    private void addWordsToTrie(DocumentImpl doc) {
        URI uri = doc.getKey();
        for (String word : doc.getWords().keySet()) {
            documentTrie.put(word, uri);
        }
    }

    private void deleteWordsFromTrie(DocumentImpl doc) {
        URI uri = doc.getKey();
        for (String word : doc.getWords().keySet()) {
            documentTrie.delete(word, uri);
        }
    }

    private String fixKey(String key) {
        if (key == null || key.equals("")) {
            throw new IllegalArgumentException("Key cannot be empty.");
        }
        key = key.trim();
        if (key.contains(" ")) {
            throw new IllegalArgumentException("Key must be a single word.");
        }
        key = key.replaceAll("[\\p{P}\\p{S}0-9]", "");
        if (key.equals("")) {
            throw new IllegalArgumentException("Key must be a word.");
        }
        key = key.toLowerCase();
        return key;
    }


    private void pushPutCommand(URI uri, DocumentImpl doc) {
        undoIO.serialize(doc);
        if (docStorage.get(uri) != null) {
            checkIfJustPulledFromDisc(doc);
            pushOverwriteCommand(uri, doc);
            return;
        }
        int hashcode = doc.getDocumentHashCode();
        long timeAtPut = doc.getLastUsedTime();
        Function<URI, Boolean> putUndo = (u) -> {
            DocumentImpl document = (DocumentImpl) undoIO.deserialize(uri, hashcode);
            deleteWordsFromTrie(document);
            removeDocFromHeap(document);
            return this.docStorage.put(u, null) != null;
        };
        Function<URI, Boolean> putRedo = (u) -> {
            DocumentImpl document = (DocumentImpl) undoIO.deserialize(uri, hashcode);
            boolean b = this.docStorage.put(u, document) == null;
            addWordsToTrie(document);
            addDocToHeap(document, timeAtPut);
            return b;
        };
        Command toPush = new Command(uri, putUndo, putRedo);
        commandStack.push(toPush);
    }

    private void pushOverwriteCommand(URI uri, DocumentImpl newDoc) {
        DocumentImpl oldDoc = docStorage.get(uri);
        checkIfJustPulledFromDisc(oldDoc);
        long oldDocTime = oldDoc.getLastUsedTime();
        long newDocPutTime = newDoc.getLastUsedTime();
        int oldHashcode = oldDoc.getDocumentHashCode();
        int newHashcode = newDoc.getDocumentHashCode();
        removeDocFromHeap(oldDoc);
        Function<URI, Boolean> putUndo = (u) -> {
            DocumentImpl oldDocument = (DocumentImpl) undoIO.deserialize(uri, oldHashcode);
            DocumentImpl newDocument = (DocumentImpl) undoIO.deserialize(uri, newHashcode);
            deleteWordsFromTrie(newDocument);
            removeDocFromHeap(newDocument);
            boolean b = this.docStorage.put(u, oldDocument) != null;
            addWordsToTrie(oldDocument);
            addDocToHeap(oldDocument, oldDocTime);
            return b;
        };
        Function<URI, Boolean> putRedo = (u) -> {
            DocumentImpl oldDocument = (DocumentImpl) undoIO.deserialize(uri, oldHashcode);
            DocumentImpl newDocument = (DocumentImpl) undoIO.deserialize(uri, newHashcode);
            deleteWordsFromTrie(oldDocument);
            removeDocFromHeap(oldDocument);
            boolean b = this.docStorage.put(u, newDocument) != null;
            addWordsToTrie(newDocument);
            addDocToHeap(newDocument, newDocPutTime);
            return b;
        };
        Command toPush = new Command(uri, putUndo, putRedo);
        commandStack.push(toPush);
    }

    private void addDocToHeap(DocumentImpl doc, long time) {
        doc.setLastUsedTime(time);
        addDocToHeap(doc);
    }

    private void addDocToHeap(DocumentImpl doc) {
        URIWrapper uri = new URIWrapper(doc.getKey());
        int index = leastUsedDocHeap.getArrayIndex(uri);
        if (index == -1) {
            this.leastUsedDocHeap.insert(uri);
            currentTotalDocBytes += doc.getDocument().length;
        }
        leastUsedDocHeap.reHeapify(uri);
    }

    private void removeDocFromHeap(DocumentImpl doc) {
        URIWrapper uri = new URIWrapper(doc.getKey());
        int index = leastUsedDocHeap.getArrayIndex(uri);
        if (index != -1) {
            doc.setLastUsedTime(Long.MIN_VALUE);
            this.leastUsedDocHeap.reHeapify(uri);
            this.leastUsedDocHeap.removeMin();
            currentTotalDocBytes -= doc.getDocument().length;
        }
    }

    private void pushDelCommand(URI uri, DocumentImpl doc) {
        long docTime = doc.getLastUsedTime();
        int hashcode = doc.getDocumentHashCode();
        Function<URI, Boolean> delUndo = (u) -> {
            DocumentImpl document = (DocumentImpl) undoIO.deserialize(uri, hashcode);
            boolean b = docStorage.put(u, document) == null;
            addWordsToTrie(document);
            addDocToHeap(document, docTime);
            return b;
        };
        Function<URI, Boolean> delRedo = (u) -> {
            DocumentImpl document = (DocumentImpl) undoIO.deserialize(uri, hashcode);
            deleteWordsFromTrie(document);
            removeDocFromHeap(document);
            return this.docStorage.put(u, null) != null;
        };
        Command toPush = new Command(uri, delUndo, delRedo);
        commandStack.push(toPush);
    }

    private void refillCommandStack(Stack<Command> temp) {
        while (temp.size() > 0) {
            Command toRedo = temp.pop();
            toRedo.redo();
            commandStack.push(toRedo);
        }
    }


    /**
     * @param format
     * @return true if valid CompressionFormat, false if null or invalid
     */
    private boolean checkFormat(CompressionFormat format) {
        if (format == null) {
            return false;
        }
        for (CompressionFormat c : CompressionFormat.values()) {
            if (format.equals(c)) {
                return true;
            }
        }
        return false;
    }

    private byte[] compress(byte[] bytes, CompressionFormat format) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        switch (format) {
            case ZIP:
                if (compressZIP(in, byteOutput)) return null;
                break;
            case JAR:
                if (compressJAR(in, byteOutput)) return null;
                break;
            case GZIP:
                GzipCompressorOutputStream gZip = new GzipCompressorOutputStream(byteOutput);
                IOUtils.copy(in, gZip);
                gZip.close();
                break;
            case BZIP2:
                BZip2CompressorOutputStream bZip = new BZip2CompressorOutputStream(byteOutput);
                IOUtils.copy(in, bZip);
                bZip.close();
                break;
            case SEVENZ:
                in.close();
                byte[] compressed = compress7z(bytes);
                return compressed;
        }
        in.close();
        return byteOutput.toByteArray();
    }

    private byte[] compress7z(byte[] bytes) throws IOException {
        File inFile = new File("inFile");
        FileUtils.writeByteArrayToFile(inFile, bytes);
        SeekableInMemoryByteChannel compressedOutput = new SeekableInMemoryByteChannel(new byte[1024]);
        SevenZOutputFile out = new SevenZOutputFile(compressedOutput);
        SevenZArchiveEntry entry = out.createArchiveEntry(inFile, "shrug");
        out.putArchiveEntry(entry);
        FileInputStream inFileStream = new FileInputStream(inFile);
        byte[] b = new byte[bytes.length];
        int count = 0;
        while ((count = inFileStream.read(b)) > 0) {
            out.write(b, 0, count);
        }
        inFileStream.close();
        out.closeArchiveEntry();
        out.close();
        inFile.delete();
        return compressedOutput.array();
    }

    private boolean compressJAR(ByteArrayInputStream in, ByteArrayOutputStream byteOutput) throws IOException {
        try {
            ArchiveOutputStream jar = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.JAR, byteOutput);
            jar.putArchiveEntry(new JarArchiveEntry("Cookie_Jar"));
            IOUtils.copy(in, jar);
            jar.closeArchiveEntry();
            jar.close();
        } catch (ArchiveException e) {
            return true;
        }
        return false;
    }

    private boolean compressZIP(ByteArrayInputStream in, ByteArrayOutputStream byteOutput) throws IOException {
        try {
            ArchiveOutputStream zip = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, byteOutput);
            zip.putArchiveEntry(new ZipArchiveEntry("Please_purchase_WinRar"));
            IOUtils.copy(in, zip);
            zip.closeArchiveEntry();
            zip.close();
        } catch (ArchiveException e) {
            return true;
        }
        return false;
    }

    private String decompress(byte[] compressed, CompressionFormat format) {
        try {
            ByteArrayInputStream compressedInput = new ByteArrayInputStream(compressed);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            switch (format) {
                case ZIP:
                    if (decompressZIP(compressedInput, output)) return null;
                    break;
                case JAR:
                    if (decompressJAR(compressedInput, output)) return null;
                    break;
                case GZIP:
                    GzipCompressorInputStream gZip = new GzipCompressorInputStream(compressedInput);
                    IOUtils.copy(gZip, output);
                    gZip.close();
                    break;
                case BZIP2:
                    BZip2CompressorInputStream bZip = new BZip2CompressorInputStream(compressedInput);
                    IOUtils.copy(bZip, output);
                    bZip.close();
                    break;
                case SEVENZ:
                    compressedInput.close();
                    return decompress7Z(compressed);
            }
            compressedInput.close();
            return output.toString();
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
            return null;
        }
    }

    private String decompress7Z(byte[] compressed) {
        try {
            SeekableInMemoryByteChannel inMemoryByteChannel = new SeekableInMemoryByteChannel(compressed);
            SevenZFile sevenZFile = new SevenZFile(inMemoryByteChannel);
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            byte[] result = new byte[(int) entry.getSize()]; //What size should I put here?
            sevenZFile.read(result, 0, result.length);
            sevenZFile.close();
            return new String(result);
        } catch (IOException e) {
            System.out.println(e.toString());
            return null;
        }
    }

    private boolean decompressJAR(ByteArrayInputStream compressedInput, ByteArrayOutputStream output) throws IOException {
        try {
            ArchiveInputStream jar = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.JAR, compressedInput);
            JarArchiveEntry entry = (JarArchiveEntry) jar.getNextEntry();
            IOUtils.copy(jar, output);
            jar.close();
        } catch (ArchiveException e) {
            return true;
        }
        return false;
    }

    private boolean decompressZIP(ByteArrayInputStream compressedInput, ByteArrayOutputStream output) throws IOException {
        try {
            ArchiveInputStream zip = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, compressedInput);
            ZipArchiveEntry entry = (ZipArchiveEntry) zip.getNextEntry();
            IOUtils.copy(zip, output);
            zip.close();
        } catch (ArchiveException e) {
            return true;
        }
        return false;
    }


    protected String commandStackToString() {
        return commandStack.toString();
    }
}
