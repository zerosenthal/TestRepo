package edu.yu.cs.com1320.project;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.bzip2.*;
import org.apache.commons.compress.compressors.gzip.*;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Contract;

import java.io.*;
import java.net.URI;

public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI, DocumentImpl> hTable;
    private CompressionFormat defaultFormat;

    public DocumentStoreImpl() {
        this.hTable = new HashTableImpl<>();
        this.defaultFormat = CompressionFormat.ZIP;
    }

    public DocumentStoreImpl(int m) {
        this.hTable = new HashTableImpl<>(m);
        this.defaultFormat = CompressionFormat.ZIP;
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
     * @param uri    unique identifier for the document
     * @param format compression format to use for compressing this document
     * @return the hashcode of the document
     */
    public int putDocument(InputStream input, URI uri, CompressionFormat format) {
        if (input == null || uri == null) {
            return -1; //If there was an error reading the file, what should I do here?
        }
        try {
            byte[] uncompressedBytes = IOUtils.toByteArray(input);
            input.close();
            String uncompressedFile = new String(uncompressedBytes);
            int fileHashCode = uncompressedFile.hashCode();
            if (checkDuplicateDoc(uri, fileHashCode)) {
                return 0; //If there was a duplicate, what should I return?
            }
            if (!checkFormat(format)) {
                format = this.defaultFormat;
            }
            byte[] compressedBytes = compress(uncompressedBytes, format);
            if (compressedBytes == null) return -1;
            DocumentImpl doc = new DocumentImpl(compressedBytes, fileHashCode, uri, format);
            this.hTable.put(uri, doc);
            return fileHashCode;
        } catch (IOException ioe) {
            System.out.println(ioe.toString());
            return -1;
        }
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the <B>uncompressed</B> document as a String, or null if no such document exists
     */
    public String getDocument(URI uri) {
        DocumentImpl doc = this.hTable.get(uri);
        if (doc == null) {
            return null;
        } else {
            return decompress(doc.getDocument(), doc.getCompressionFormat());
        }
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the <B>compressed</B> version of the document
     */
    public byte[] getCompressedDocument(URI uri) {
        DocumentImpl doc = this.hTable.get(uri);
        if (doc == null) {
            return null;
        } else {
            return doc.getDocument();
        }
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    public boolean deleteDocument(URI uri) {
        if (hTable.put(uri, null) == null) {
            return false;
        }
        return true;
    }

    private boolean checkDuplicateDoc(URI uri, int hashCode) {
        DocumentImpl duplicate = this.hTable.get(uri);
        if (duplicate == null) {
            return false;
        } else {
            return hashCode == duplicate.getDocumentHashCode();
        }
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
        byte[] b = new byte[1024];
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
            sevenZFile.read(result,0,result.length);
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

    /**
     * DO NOT IMPLEMENT IN STAGE 1 OF THE PROJECT. THIS IS FOR STAGE 2.
     * <p>
     * undo the last put or delete command
     *
     * @return true if successfully undid command, false if not successful
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    public boolean undo() throws IllegalStateException {
        return false;
    }

    /**
     * DO NOT IMPLEMENT IN STAGE 1 OF THE PROJECT. THIS IS FOR STAGE 2.
     * <p>
     * undo the last put or delete that was done with the given URI as its key
     *
     * @param uri
     * @return
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    public boolean undo(URI uri) throws IllegalStateException {
        return false;
    }

    @Override
    public String toString() {
        return "DocumentStoreImpl{" +
                "hTable=" + hTable +
                '}';
    }
}
