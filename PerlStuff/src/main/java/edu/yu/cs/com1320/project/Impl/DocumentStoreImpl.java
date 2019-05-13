package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.DocumentStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Project: stage2
 * DocumentStoreImpl.java - class representing an instance of Document Store
 * Created 3/8/2019
 * @see DocumentImpl
 *
 * @author Elimelekh Perl
 */
public class DocumentStoreImpl implements DocumentStore
{
    protected CompressionFormat defCompForm;
    protected HashTableImpl<URI, DocumentImpl> docHashTable;
    private StackImpl<Command> commandStack;
    private TrieImpl<DocumentImpl> wordSearchTrie;
    private DocumentComparator docComparator;
    protected MinHeapImpl<DocumentImpl> usageQueue;
    private int maxDocumentCount;
    private int maxDocumentBytes;
    private int currentDocumentCount;
    private int currentDocumentBytes;


    public DocumentStoreImpl()
    {
        this.defCompForm = CompressionFormat.ZIP;
        this.docHashTable = new HashTableImpl<URI, DocumentImpl>();
        this.commandStack = new StackImpl<Command>();
        this.docComparator = new DocumentComparator();
        this.wordSearchTrie = new TrieImpl<DocumentImpl>(this.docComparator);
        this.usageQueue = new MinHeapImpl<DocumentImpl>();
        this.maxDocumentCount = 0;
        this.maxDocumentBytes = 0;
        this.currentDocumentCount = 0;
        this.currentDocumentBytes = 0;
    }

    private class DocumentComparator implements Comparator<DocumentImpl>
    {
        private String keyword;

        @Override
        public int compare(DocumentImpl doc1, DocumentImpl doc2)
        {
            if (doc1.wordCount(this.keyword) == doc2.wordCount(this.keyword))
            {
                return 0;
            }
            else if (doc1.wordCount(this.keyword) < doc2.wordCount(this.keyword))
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }

        private void setKeyword(String keyword)
        {
            this.keyword = keyword;
        }
    }

    @Override
    public CompressionFormat getDefaultCompressionFormat()
    {
        return this.defCompForm;
    }

    /**
     * Retrieve all documents that contain the given key word.
     * Documents are returned in sorted, in descending order, by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     *
     * @param keyword
     * @return
     */
    @Override
    public List<String> search(String keyword)
    {
        keyword = keyword.toLowerCase();

        this.docComparator.setKeyword(keyword);

        List<DocumentImpl> sortedDocs = this.wordSearchTrie.getAllSorted(keyword);

        List<String> sortedDocStrings = new ArrayList<String>();

        long currentTime = java.lang.System.currentTimeMillis();

        for (DocumentImpl doc: sortedDocs)
        {
            doc.setLastUseTime(currentTime);
            this.usageQueue.reHeapify(doc);
            try
            {
                sortedDocStrings.add(doc.decompress());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return sortedDocStrings;
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
    public List<byte[]> searchCompressed(String keyword)
    {
        keyword = keyword.toLowerCase();

        this.docComparator.setKeyword(keyword);

        List<DocumentImpl> sortedDocs = this.wordSearchTrie.getAllSorted(keyword);

        List<byte[]> sortedCompressedBytes = new ArrayList<byte[]>();

        long currentTime = java.lang.System.currentTimeMillis();

        for (DocumentImpl doc: sortedDocs)
        {
            doc.setLastUseTime(currentTime);
            this.usageQueue.reHeapify(doc);
            sortedCompressedBytes.add(doc.contents);
        }

        return sortedCompressedBytes;
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

    public void setDefaultCompressionFormat(CompressionFormat format)
    {
        this.defCompForm = format;
    }


    /**
     * since the user does not specify a compression format, use the default compression format
     *
     * @param input the document being put
     * @param uri   unique identifier for the document
     * @return the hashcode of the document
     */
    @Override
    public int putDocument(InputStream input, URI uri)
    {
        return putDocument(input, uri, this.defCompForm);
    }

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format compression format to use for compressing this document
     * @return the hashcode of the document
     */
    @Override
    public int putDocument(InputStream input,  URI uri, CompressionFormat format)
    {
        if (input == null)
        {
            return putNull(uri);
        }

        DocumentImpl document = null;
        try
        {
            document = new DocumentImpl(input, uri, format);
            byte[] decompressedContents = document.contents;
            document.setLastUseTime(java.lang.System.currentTimeMillis());

            if (!this.docHashTable.contains(uri,document))
            {
                this.processDocContents(document, new String(decompressedContents));
                document.contents = document.compress();
                DocumentImpl previousDoc = this.docHashTable.get(uri);


                DocumentImpl finalDocument = document;;

                if (previousDoc != null)
                {
                    String previousContentString = this.getDocument(uri);
                    Command newCommand = new Command
                            (uri,
                            (thisURI -> undoLoadedPut(uri, previousDoc, previousContentString, finalDocument)),
                            (thisURI -> redoLoadedPut(uri, previousDoc, decompressedContents, finalDocument)));

                    this.docHashTable.put(uri, document);
                    this.commandStack.push(newCommand);
                    this.usageQueueRemove(previousDoc);
                    document.setLastUseTime(java.lang.System.currentTimeMillis());
                    this.usageQueue.insert(document);
                    this.checkMemoryLimits(false, (previousDoc.getDocument().length), (finalDocument.getDocument().length));
                }
                else
                {
                    Command newCommand = new Command
                            (uri,
                            (thisURI -> undoNewPut(uri, finalDocument)),
                            (thisURI -> redoNewPut(uri, decompressedContents, finalDocument)));

                    this.docHashTable.put(uri, document);
                    this.commandStack.push(newCommand);
                    document.setLastUseTime(java.lang.System.currentTimeMillis());
                    this.usageQueue.insert(document);
                    this.checkMemoryLimits(true, 0, (finalDocument.getDocument().length));
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return document.getDocumentHashCode();
    }

    private boolean undoLoadedPut(URI uri, DocumentImpl previousDoc, String previousContentString, DocumentImpl finalDocument)
    {
        this.checkMemoryLimits(false, (finalDocument.getDocument().length), (previousDoc.getDocument().length));
        this.processDocContents(previousDoc, previousContentString);
        this.usageQueue.insert(previousDoc);
        return this.docHashTable.put(uri, previousDoc) == finalDocument;
    }

    private boolean redoLoadedPut(URI uri, DocumentImpl previousDoc, byte[] decompressedContents, DocumentImpl finalDocument)
    {
        this.checkMemoryLimits(false, (previousDoc.getDocument().length), (finalDocument.getDocument().length));
        this.processDocContents(finalDocument, new String(decompressedContents));
        this.usageQueue.reHeapify(finalDocument);
        return this.docHashTable.put(uri, finalDocument) == previousDoc;
    }

    private boolean undoNewPut(URI uri, DocumentImpl finalDocument)
    {
        this.searchTrieDelete(uri);
        this.currentDocumentCount--;
        this.currentDocumentBytes = this.currentDocumentBytes - finalDocument.getDocument().length;
        this.usageQueueRemove(finalDocument);
        return this.docHashTable.put(uri, null) == finalDocument;
    }

    private boolean redoNewPut(URI uri, byte[] decompressedContents, DocumentImpl finalDocument)
    {
        this.processDocContents(finalDocument, new String(decompressedContents));
        this.checkMemoryLimits(true, 0, finalDocument.getDocument().length);
        this.usageQueue.insert(finalDocument);
        return this.docHashTable.put(uri, finalDocument) == null;
    }

    private void checkMemoryLimits(boolean newDoc, int oldDocByteSize, int newDocByteSize)
    {
        //if newDoc is being added, increment count
        if (newDoc)
        {
            this.currentDocumentCount++;
        }
        //replace previous doc byte size (zero if new doc) with new doc byte size
        this.currentDocumentBytes = this.currentDocumentBytes - oldDocByteSize + newDocByteSize;


        //if maxDocCount has been set, delete least recently used until under mem limit
        if ((maxDocumentCount != 0))
        {
            while ((currentDocumentCount > maxDocumentCount))
            {
                this.autoDeleteDocument(this.usageQueue.removeMin());
            }
        }

        //if maxDocBytes has been set, delete least recently used until under mem limit
        if ((maxDocumentBytes != 0))
        {
            while ((currentDocumentBytes > maxDocumentBytes))
            {
                this.autoDeleteDocument(this.usageQueue.removeMin());
            }
        }
    }

    private void usageQueueRemove(DocumentImpl doc)
    {
        long previousTime = doc.lastUseTime;
        doc.setLastUseTime(0);
        this.usageQueue.reHeapify(doc);
        this.usageQueue.removeMin();
        doc.setLastUseTime(previousTime);
    }


    private int putNull(URI uri)
    {
        DocumentImpl previousDoc = this.docHashTable.get(uri);

        if (previousDoc == null)
        {//no previous doc to delete
            return Integer.MIN_VALUE;
        }
        else
        {//returns hashCode of deleted doc
            this.deleteDocument(uri);
            return previousDoc.getDocumentHashCode();
        }
    }

    protected void processDocContents(DocumentImpl doc, String string)
    {
        HashTableImpl<String, Integer> wordCounter = new HashTableImpl<String, Integer>();

        String simpleString = string.replaceAll("\\p{Punct}", "").toLowerCase();

        String[] words = simpleString.split(" ");

        for (String word: words)
        {
            Integer currentFreq = wordCounter.get(word);

            if (currentFreq == null)
            {
                wordCounter.put(word, 1);
            }
            else
            {
                int newFreq = ++currentFreq;
                wordCounter.put(word, newFreq);
            }

            this.wordSearchTrie.put(word, doc);
        }

        doc.wordCounter = wordCounter;
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the <B>uncompressed</B> document as a String, or null if no such document exists
     */
    @Override
    public String getDocument(URI uri)
    {
        if (this.docHashTable.get(uri) != null)
        {
            try
            {
                DocumentImpl doc = this.docHashTable.get(uri);
                doc.setLastUseTime(java.lang.System.currentTimeMillis());
                this.usageQueue.reHeapify(doc);
                return doc.decompress();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the <B>compressed</B> version of the document
     */
    @Override
    public byte[] getCompressedDocument(URI uri)
    {
        if (this.docHashTable.get(uri) != null)
        {
            DocumentImpl doc = this.docHashTable.get(uri);
            doc.setLastUseTime(java.lang.System.currentTimeMillis());
            this.usageQueue.reHeapify(doc);
            return doc.getDocument();
        }

        return null;
    }

    private void autoDeleteDocument(DocumentImpl doc)
    {
        Boolean moreCommands = true;
        while(moreCommands)
        {
            moreCommands = this.commandStackDelete(doc.getKey());
        }
        this.searchTrieDelete(doc.getKey());
        this.currentDocumentCount--;
        this.currentDocumentBytes = this.currentDocumentBytes - doc.getDocument().length;
        this.docHashTable.put(doc.getKey(), null);
    }


    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean deleteDocument(URI uri)
    {
        if (this.docHashTable.get(uri) != null)
        {
            DocumentImpl previousDoc = this.docHashTable.get(uri);
            String previousContentString = this.getDocument(uri);
            Command newCommand = new Command
                    (uri,
                    (thisURI -> undoDelete(uri, previousDoc,previousContentString)),
                    (thisURI -> redoDelete(uri, previousDoc)));

            this.commandStack.push(newCommand);

            this.searchTrieDelete(uri);
            this.currentDocumentCount--;
            this.currentDocumentBytes = this.currentDocumentBytes - previousDoc.getDocument().length;
            this.usageQueueRemove(previousDoc);
            this.docHashTable.put(uri, null);
            return true;
        }

        else
        {
            return false;
        }
    }

    private boolean undoDelete(URI uri, DocumentImpl previousDoc, String previousContentString)
    {
        this.processDocContents(previousDoc, previousContentString);
        this.checkMemoryLimits(true, 0, previousDoc.getDocument().length);
        this.usageQueue.insert(previousDoc);
        return this.docHashTable.put(uri, previousDoc) == null;
    }

    private boolean redoDelete(URI uri, DocumentImpl previousDoc)
    {
        this.searchTrieDelete(uri);
        this.currentDocumentCount--;
        this.currentDocumentBytes = this.currentDocumentBytes - previousDoc.getDocument().length;
        this.usageQueueRemove(previousDoc);
        return this.docHashTable.put(uri, null) == previousDoc;
    }

    private void searchTrieDelete(URI uri)
    {
        String string = null;
        try
        {
            string = this.docHashTable.get(uri).decompress();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        String simpleString = string.replaceAll("\\p{Punct}", "").toLowerCase();

        String[] words = simpleString.split(" ");

        for (String word: words)
        {
            this.wordSearchTrie.delete(word, this.docHashTable.get(uri));
        }
    }

    /**
     * DO NOT IMPLEMENT IN STAGE 1 OF THE PROJECT. THIS IS FOR STAGE 2.
     * <p>
     * undo the last put or delete command
     *
     * @return true if successfully undid command, false if not successful
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public boolean undo() throws IllegalStateException
    {
        try
        {
            if (this.commandStack.peek() == null)
            {
                throw new IllegalStateException("ERROR: no commands to undo");
            }

            Command thisCommand = this.commandStack.pop();

            return thisCommand.undo();
        }
        catch(Exception e)
        {
            if (e instanceof IllegalStateException)
            {
                throw new IllegalStateException(e.getMessage());
            }
            return false;
        }
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
    @Override
    public boolean undo(URI uri) throws IllegalStateException
    {
        try
        {
            Command topCommand = this.commandStack.peek();

            if (topCommand == null)
            {
                throw new IllegalStateException("ERROR: URI not found");
            }
            else if (!topCommand.getUri().equals(uri))
            {
                Command tempCommand = this.commandStack.pop();
                tempCommand.undo();
                Boolean undone = undo(uri);
                tempCommand.redo();
                this.commandStack.push(tempCommand);

                return undone;
            }

            else
            {
                this.commandStack.pop();
                return topCommand.undo();
            }
        }
        catch(Exception e)
        {
            if (e instanceof IllegalStateException)
            {
                throw new IllegalStateException(e.getMessage());
            }
            return false;
        }
    }


    private boolean commandStackDelete(URI uri)
    {
        Command topCommand = this.commandStack.peek();

        if (topCommand == null)
        {
            return false;
        }
        else if (!topCommand.getUri().equals(uri))
        {
            Command tempCommand = this.commandStack.pop();
            Boolean uriFound = commandStackDelete(uri);
            this.commandStack.push(tempCommand);
            return uriFound;
        }

        else
        {
            this.commandStack.pop();
            return true;
        }
    }

    /**
     * set maximum number of documents that may be stored
     *
     * @param limit
     */
    @Override
    public void setMaxDocumentCount(int limit)
    {
        if (limit < 0)
        {
            throw new IllegalArgumentException("ERROR: negative limit found");
        }

        else
        {
            this.maxDocumentCount = limit;
            this.checkMemoryLimits(false, 0, 0);
        }
    }

    /**
     * set maximum number of bytes of memory that may be used by all the compressed
     * documents in memory combined
     *
     * @param limit
     */
    @Override
    public void setMaxDocumentBytes(int limit)
    {
        if (limit < 0)
        {
            throw new IllegalArgumentException("ERROR: negative limit found");
        }

        else
        {
            this.maxDocumentBytes = limit;
            this.checkMemoryLimits(false, 0, 0);
        }
    }
}
