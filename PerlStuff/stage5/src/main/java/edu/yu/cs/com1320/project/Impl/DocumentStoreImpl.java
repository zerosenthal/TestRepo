package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.DocumentStore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


/**
 * Project: stage5
 * DocumentStoreImpl.java - class representing an instance of Document Store
 * Created 3/8/2019
 * @see DocumentImpl
 *
 * @author Elimelekh Perl
 */
public class DocumentStoreImpl implements DocumentStore
{
    protected CompressionFormat defCompForm;
    protected BTreeImpl docBTree;
    private StackImpl<Command> commandStack;
    private TrieImpl<URI> wordSearchTrie;
    private DocumentComparator docComparator;
    protected MinHeapImpl<URIWrapper> usageQueue;
    private int maxDocumentCount;
    private int maxDocumentBytes;
    private int currentDocumentCount;
    private int currentDocumentBytes;
    private StackImpl<URI> sentToDisk;
    private Map<URI, Integer> versionCounter;
    private GraveyardIOImpl graveyardDocIO;



    public DocumentStoreImpl()
    {
        this.defCompForm = CompressionFormat.ZIP;
        this.docBTree = new BTreeImpl();
        this.commandStack = new StackImpl<Command>();
        this.docComparator = new DocumentComparator(this);
        this.wordSearchTrie = new TrieImpl<URI>(this.docComparator);
        this.usageQueue = new MinHeapImpl<URIWrapper>();
        this.maxDocumentCount = 0;
        this.maxDocumentBytes = 0;
        this.currentDocumentCount = 0;
        this.currentDocumentBytes = 0;
        this.sentToDisk = new StackImpl<URI>();
        this.versionCounter = new HashMap<>();
        this.graveyardDocIO = new GraveyardIOImpl(this.versionCounter);
    }

    public DocumentStoreImpl(File baseDir)
    {
        this.defCompForm = CompressionFormat.ZIP;
        this.docBTree = new BTreeImpl(baseDir);
        this.commandStack = new StackImpl<Command>();
        this.docComparator = new DocumentComparator(this);
        this.wordSearchTrie = new TrieImpl<URI>(this.docComparator);
        this.usageQueue = new MinHeapImpl<URIWrapper>();
        this.maxDocumentCount = 0;
        this.maxDocumentBytes = 0;
        this.currentDocumentCount = 0;
        this.currentDocumentBytes = 0;
        this.sentToDisk = new StackImpl<URI>();
        this.versionCounter = new HashMap<>();
        this.graveyardDocIO = new GraveyardIOImpl(this.versionCounter, baseDir);
    }


    @Override
    public CompressionFormat getDefaultCompressionFormat()
    {
        return this.defCompForm;
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
     * @param uri the unique identifier of the document to get
     * @return the <B>uncompressed</B> document as a String, or null if no such document exists
     */
    @Override
    public String getDocument(URI uri)
    {
        DocumentImpl doc = this.get(uri);
        if (doc != null)
        {
            try
            {
                doc.setLastUseTime(System.currentTimeMillis());
                this.usageQueue.reHeapify(new URIWrapper(uri));
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
        DocumentImpl doc = this.get(uri);

        if (doc != null)
        {
            doc.setLastUseTime(System.currentTimeMillis());
            this.usageQueue.reHeapify(new URIWrapper(uri));
            return doc.getDocument();
        }

        return null;
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
            document.setLastUseTime(System.currentTimeMillis());
            DocumentImpl previousDoc = this.get(uri);
            Integer currentVersion = this.versionCounter.get(uri);
            if (currentVersion == null)
            {
                this.versionCounter.put(uri, 0);
            }

            if (previousDoc != document)
            {
                document.contents = document.compress();

                DocumentImpl finalDocument = document;

                if (previousDoc != null)
                {
                    this.searchTrieDelete(uri);
                    long previousTimeStamp = previousDoc.getLastUseTime();
                    long finalTimeStamp = System.currentTimeMillis();
                    Command newCommand = new Command
                            (uri,
                            (thisURI -> undoLoadedPut(uri, previousTimeStamp)),
                            (thisURI -> redoLoadedPut(uri, finalTimeStamp)));

                    this.graveyardDocIO.serialize(previousDoc, 0);//send replaced doc to disk as current version#
                    this.versionCounter.put(uri, (this.versionCounter.get(uri)) + 1); //increment current version#
                    this.docBTree.put(uri, document);
                    this.commandStack.push(newCommand);
                    document.setLastUseTime(finalTimeStamp);
                    this.usageQueue.reHeapify(new URIWrapper(uri));
                    this.enforceMemoryLimits(false, (previousDoc.getDocument().length), (finalDocument.getDocument().length));
                }
                else
                {
                    long finalTimeStamp = System.currentTimeMillis();
                    Command newCommand = new Command
                            (uri,
                            (thisURI -> undoNewPut(uri)),
                            (thisURI -> redoNewPut(uri, finalTimeStamp)));

                    this.versionCounter.put(uri, (this.versionCounter.get(uri)) + 1); //increment current version#
                    this.docBTree.put(uri, document);
                    this.commandStack.push(newCommand);
                    document.setLastUseTime(finalTimeStamp);
                    this.usageQueue.insert(new URIWrapper(uri));
                    this.enforceMemoryLimits(true, 0, (finalDocument.getDocument().length));
                }
                this.processDocContents(document, new String(decompressedContents));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return document.getDocumentHashCode();
    }

    protected void processDocContents(DocumentImpl doc, String string)
    {
        Map<String, Integer> wordCounter = new HashMap<String, Integer>();

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

            this.wordSearchTrie.put(word, doc.getKey());
        }

        doc.setWordMap(wordCounter);
    }

    private boolean undoLoadedPut(URI uri, long previousTimeStamp)
    {
        DocumentImpl previousDoc = (DocumentImpl) this.graveyardDocIO.deserialize(uri, -1);//bring previous version back into memory, roll current version# back by 1
        DocumentImpl finalDocument = this.get(uri);
        this.allowReturnToMemory(false, finalDocument.getDocument().length, previousDoc.getDocument().length);
        try
        {
            this.processDocContents(previousDoc, previousDoc.decompress());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        this.graveyardDocIO.serialize(finalDocument, 1);//send override doc to disk as current version# + 1
        Boolean undoSuccessful = this.docBTree.put(uri, previousDoc) == finalDocument;
        previousDoc.setLastUseTime(previousTimeStamp);
        this.usageQueue.reHeapify(new URIWrapper(uri));
        return undoSuccessful;
    }

    private boolean redoLoadedPut(URI uri, long finalTimeStamp)
    {
        DocumentImpl previousDoc = this.get(uri);
        DocumentImpl finalDocument = (DocumentImpl) this.graveyardDocIO.deserialize(uri, 1);//bring next version, ie override doc, back into memory, increment version#
        this.enforceMemoryLimits(false, (previousDoc.getDocument().length), (finalDocument.getDocument().length));
        try
        {
            this.processDocContents(finalDocument, finalDocument.decompress());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        this.graveyardDocIO.serialize(previousDoc, -1);//send previous version to disk
        Boolean redoSuccessful = (this.docBTree.put(uri, finalDocument)) == previousDoc;
        finalDocument.setLastUseTime(finalTimeStamp);
        this.usageQueue.reHeapify(new URIWrapper(uri));
        return redoSuccessful;
    }

    private boolean undoNewPut(URI uri)
    {
        DocumentImpl finalDocument = this.get(uri);
        allowReturnToMemory(true, finalDocument.getDocument().length, 0);
        this.graveyardDocIO.serialize(finalDocument, 0); //send new doc to disk as next version#
        this.versionCounter.put(uri, (this.versionCounter.get(uri))-1); //roll back current version#
        this.searchTrieDelete(uri);
        this.usageQueueRemove(uri);
        Boolean undoSuccessful = this.docBTree.put(uri, null) == finalDocument;
        return undoSuccessful;
    }

    private boolean redoNewPut(URI uri, long finalTimeStamp)
    {
        DocumentImpl finalDocument = (DocumentImpl) this.graveyardDocIO.deserialize(uri, 1); //bring next version, ie new doc, back into memory, increment version#
        try
        {
            this.processDocContents(finalDocument, finalDocument.decompress());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        this.enforceMemoryLimits(true, 0, finalDocument.getDocument().length);
        Boolean redoSuccessful = this.docBTree.put(uri, finalDocument) == null;
        finalDocument.setLastUseTime(finalTimeStamp);
        this.usageQueue.insert(new URIWrapper(uri));
        return redoSuccessful;
    }

    private int putNull(URI uri)
    {
        DocumentImpl previousDoc = (DocumentImpl) this.get(uri);

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

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean deleteDocument(URI uri)
    {
        DocumentImpl previousDoc = this.get(uri);
        long previousTimeStamp = previousDoc.getLastUseTime();

        if (previousDoc != null)
        {
            Command newCommand = new Command
                    (uri,
                            (thisURI -> undoDelete(uri, previousTimeStamp)),
                            (thisURI -> redoDelete(uri)));

            this.commandStack.push(newCommand);

            this.searchTrieDelete(uri);
            this.currentDocumentCount--;
            this.currentDocumentBytes = this.currentDocumentBytes - previousDoc.getDocument().length;
            this.graveyardDocIO.serialize(previousDoc, 0);//send deleted doc to disk as current version#
            this.usageQueueRemove(uri);
            this.docBTree.put(uri, null);
            return true;
        }

        else
        {
            return false;
        }
    }

    private boolean undoDelete(URI uri, long previousTimeStamp)
    {
        DocumentImpl previousDoc = (DocumentImpl) this.graveyardDocIO.deserialize(uri, 0); //bring deleted doc (current version#) back into memory
        try
        {
            this.processDocContents(previousDoc, previousDoc.decompress());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        this.enforceMemoryLimits(true, 0, previousDoc.getDocument().length);
        boolean undoSuccessful = this.docBTree.put(uri, previousDoc) == null;
        previousDoc.setLastUseTime(previousTimeStamp);
        this.usageQueue.insert(new URIWrapper(uri));
        return undoSuccessful;
    }

    private boolean redoDelete(URI uri)
    {
        DocumentImpl previousDoc = this.get(uri);
        this.searchTrieDelete(uri);
        this.currentDocumentCount--;
        this.currentDocumentBytes = this.currentDocumentBytes - previousDoc.getDocument().length;
        this.graveyardDocIO.serialize(previousDoc, 0);//send deleted doc to disk as current version#
        this.usageQueueRemove(uri);
        boolean redoSuccessful = this.docBTree.put(uri, null) == previousDoc;
        return redoSuccessful;
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
            else
            {
                return false;
            }
        }
    }

    private class DocumentComparator implements Comparator<URI>
    {
        private DocumentStoreImpl thisDocStore;
        private String keyword;

        public DocumentComparator(DocumentStoreImpl dsi)
        {
            this.thisDocStore = dsi;
        }

        @Override
        public int compare(URI uri1, URI uri2)
        {
            DocumentImpl doc1 = (DocumentImpl) thisDocStore.docBTree.get(uri1);
            DocumentImpl doc2 = (DocumentImpl) thisDocStore.docBTree.get(uri2);

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

        List<URI> sortedURIs = this.wordSearchTrie.getAllSorted(keyword);

        List<String> sortedDocStrings = new ArrayList<String>();

        long currentTime = System.currentTimeMillis();

        for (URI uri: sortedURIs)
        {
            DocumentImpl thisDoc = (DocumentImpl) this.get(uri);
            thisDoc.setLastUseTime(currentTime);
            this.usageQueue.reHeapify(new URIWrapper(uri));
            try
            {
                sortedDocStrings.add(thisDoc.decompress());
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

        List<URI> sortedURIs = this.wordSearchTrie.getAllSorted(keyword);

        List<byte[]> sortedCompressedBytes = new ArrayList<byte[]>();

        long currentTime = System.currentTimeMillis();

        for (URI uri: sortedURIs)
        {
            DocumentImpl thisDoc = (DocumentImpl) this.get(uri);
            thisDoc.setLastUseTime(currentTime);
            this.usageQueue.reHeapify(new URIWrapper(uri));
            sortedCompressedBytes.add(thisDoc.contents);
        }

        return sortedCompressedBytes;
    }

    private void searchTrieDelete(URI uri)
    {
        String string = null;
        try
        {
            string = (this.get(uri)).decompress();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        String simpleString = string.replaceAll("\\p{Punct}", "").toLowerCase();

        String[] words = simpleString.split(" ");

        for (String word: words)
        {
            this.wordSearchTrie.delete(word, uri);
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
            this.enforceMemoryLimits(false, 0, 0);
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
            this.enforceMemoryLimits(false, 0, 0);
        }
    }

    private void enforceMemoryLimits(boolean newDoc, int oldDocByteSize, int newDocByteSize)
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
                try
                {
                    URI uri = (this.usageQueue.removeMin()).getUri();
                    DocumentImpl doc = this.docBTree.get(uri);
                    this.currentDocumentCount--;
                    this.currentDocumentBytes -= doc.contents.length;
                    this.docBTree.moveToDisk(uri);
                    this.sentToDisk.push(uri);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        //if maxDocBytes has been set, delete least recently used until under mem limit
        if ((maxDocumentBytes != 0))
        {
            while ((currentDocumentBytes > maxDocumentBytes))
            {
                try
                {
                    URI uri = (this.usageQueue.removeMin()).getUri();
                    DocumentImpl doc = this.docBTree.get(uri);
                    this.currentDocumentCount--;
                    this.currentDocumentBytes -= doc.contents.length;
                    this.docBTree.moveToDisk(uri);
                    this.sentToDisk.push(uri);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void allowReturnToMemory(boolean newDoc, int undoneDocByteSize, int originalDocByteSize)
    {
        //if undo doesn't restore former doc, decrement count
        if (newDoc)
        {
            this.currentDocumentCount--;
        }
        //replace previous doc byte size (zero if new doc) with new doc byte size
        this.currentDocumentBytes = this.currentDocumentBytes - undoneDocByteSize + originalDocByteSize;

        //if both maxes have been set, add most recently serialized until at mem limit
        if ((maxDocumentBytes != 0) && (maxDocumentCount != 0))
        {
            this.refillToBothLimits();
        }

        //if maxDocCount has been set, add most recently serialized until at mem limit
        if ((maxDocumentCount != 0))
        {
           this.refillToCountLimit();
        }

        //if maxDocBytes has been set, add most recently serialized until at mem limit
        if ((maxDocumentBytes != 0))
        {
            this.refillToByteLimit();
        }
    }

    private void refillToBothLimits()
    {
        while (this.sentToDisk.size() != 0 && (currentDocumentBytes < maxDocumentBytes) && (currentDocumentCount < maxDocumentCount))
        {
            URI uri = this.sentToDisk.pop();

            DocumentImpl doc = this.docBTree.get(uri);

            //if next doc goes over the limits, send back to disk and break loop
            if (doc.contents.length + currentDocumentBytes > maxDocumentBytes)
            {
                try
                {
                    this.docBTree.moveToDisk(uri);
                    this.sentToDisk.push(uri);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            } else
            {
                doc.setLastUseTime(java.lang.System.currentTimeMillis());
                this.usageQueue.insert(new URIWrapper(uri));
                this.currentDocumentCount++;
                this.currentDocumentBytes += doc.contents.length;
            }
        }
    }

    private void refillToCountLimit()
    {
        while (this.sentToDisk.size() != 0 && (currentDocumentCount < maxDocumentCount))
        {
            URI uri = this.sentToDisk.pop();
            DocumentImpl doc = this.docBTree.get(uri);
            doc.setLastUseTime(java.lang.System.currentTimeMillis());
            this.usageQueue.insert(new URIWrapper(uri));
            this.currentDocumentCount++;
            this.currentDocumentBytes += doc.contents.length;
        }
    }

    private void refillToByteLimit()
    {
        while (this.sentToDisk.size() != 0 && (currentDocumentBytes < maxDocumentBytes))
        {
            URI uri = this.sentToDisk.pop();

            DocumentImpl doc = this.docBTree.get(uri);

            //if next doc goes over the limits, send back to disk and break
            if (doc.contents.length + currentDocumentBytes > maxDocumentBytes)
            {
                try
                {
                    this.docBTree.moveToDisk(uri);
                    this.sentToDisk.push(uri);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            }
            else
            {
                doc.setLastUseTime(java.lang.System.currentTimeMillis());
                this.usageQueue.insert(new URIWrapper(uri));
                this.currentDocumentCount++;
                this.currentDocumentBytes += doc.contents.length;
            }
        }
    }


    private void usageQueueRemove(URI uri)
    {
        DocumentImpl doc = this.get(uri);
        long previousTime = doc.getLastUseTime();
        doc.setLastUseTime(0);
        this.usageQueue.reHeapify(new URIWrapper(uri));
        this.usageQueue.removeMin();
        doc.setLastUseTime(previousTime);
    }

    private boolean sentToDiskRemove(URI uri)
    {
        URI topURI = this.sentToDisk.peek();

        if (topURI == null)
        {
            return false;
        }
        else if (!topURI.equals(uri))
        {
            URI tempURI = this.sentToDisk.pop();
            Boolean uriFound = sentToDiskRemove(uri);
            this.sentToDisk.push(tempURI);
            return uriFound;
        }

        else
        {
            this.sentToDisk.pop();
            return true;
        }
    }

    private class URIWrapper implements Comparable<URIWrapper>
    {
        private URI uri;
        private  final BTreeImpl bTree;

        public URIWrapper(URI uri)
        {
            this.uri = uri;
            this.bTree = docBTree;
        }

        public URI getUri()
        {
            return this.uri;
        }

        public BTreeImpl getBTree()
        {
            return this.bTree;
        }

        @Override
        public boolean equals(Object that)
        {
            if (this == that)
            {
                return true;
            }

            if (that == null || getClass() != that.getClass())
            {
                return false;
            }

            URIWrapper uriWrap = (URIWrapper) that;

            return (this.hashCode() == uriWrap.hashCode());
        }

        @Override
        public int hashCode()
        {
            return this.uri.hashCode();
        }


        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         *
         * <p>The implementor must ensure
         * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
         * for all {@code x} and {@code y}.  (This
         * implies that {@code x.compareTo(y)} must throw an exception iff
         * {@code y.compareTo(x)} throws an exception.)
         *
         * <p>The implementor must also ensure that the relation is transitive:
         * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
         * {@code x.compareTo(z) > 0}.
         *
         * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
         * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
         * all {@code z}.
         *
         * <p>It is strongly recommended, but <i>not</i> strictly required that
         * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
         * class that implements the {@code Comparable} interface and violates
         * this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is
         * inconsistent with equals."
         *
         * <p>In the foregoing description, the notation
         * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
         * <i>signum</i> function, which is defined to return one of {@code -1},
         * {@code 0}, or {@code 1} according to whether the value of
         * <i>expression</i> is negative, zero, or positive, respectively.
         *
         * @param that the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(URIWrapper that)
        {
            DocumentImpl thisDocument = (DocumentImpl) this.bTree.get(this.uri);
            DocumentImpl thatDocument = (DocumentImpl) that.getBTree().get(that.getUri());

            return thisDocument.compareTo(thatDocument);
        }
    }

    private DocumentImpl get(URI uri)
    {
        DocumentImpl doc = this.docBTree.get(uri);
        if (doc == null)
        {
            return null;
        }

        else if (this.usageQueue.getArrayIndex(new URIWrapper(uri)) == -1)
        {
            this.enforceMemoryLimits(true, 0, doc.getDocument().length);
            this.sentToDiskRemove(uri);
            doc.setLastUseTime(java.lang.System.currentTimeMillis());
            this.usageQueue.insert(new URIWrapper(uri));
            return doc;
        }
        else
        {
            return this.docBTree.get(uri);
        }
    }
}
