package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.DocumentStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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

    public DocumentStoreImpl()
    {
        this.defCompForm = CompressionFormat.ZIP;
        this.docHashTable = new HashTableImpl<URI, DocumentImpl>();
        this.commandStack = new StackImpl<Command>();
        this.docComparator = new DocumentComparator();
        this.wordSearchTrie = new TrieImpl<DocumentImpl>(this.docComparator);
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

        for (DocumentImpl doc: sortedDocs)
        {
            sortedDocStrings.add(this.getDocument(doc.getKey()));
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

        for (DocumentImpl doc: sortedDocs)
        {
            sortedCompressedBytes.add(this.getCompressedDocument(doc.getKey()));
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

            if (!(this.docHashTable.contains(uri, document)))
            {
                this.processDocContents(document, new String(decompressedContents));
                document.contents = document.compress();
                DocumentImpl previousDoc = this.docHashTable.get(uri);
                String previousContentString = this.getDocument(uri);

                DocumentImpl finalDocument = document;;

                if (previousDoc != null)
                {
                    Command newCommand = new Command
                            (uri,
                            (thisURI -> {this.processDocContents(previousDoc, previousContentString);
                                         return this.docHashTable.put(uri, previousDoc) == finalDocument; }),
                            (thisURI -> {
                                this.processDocContents(finalDocument, new String(decompressedContents));
                                return this.docHashTable.put(uri, finalDocument) == previousDoc; }));

                    this.commandStack.push(newCommand);
                }
                else
                {
                    Command newCommand = new Command
                            (uri,
                            (thisURI -> {this.searchTrieDelete(uri);
                                         return this.docHashTable.put(uri, null) == finalDocument; }),
                            (thisURI -> {
                                this.processDocContents(finalDocument, new String(decompressedContents));
                                return this.docHashTable.put(uri, finalDocument) == null; }));

                    this.commandStack.push(newCommand);
                }
                this.docHashTable.put(uri, document);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return document.hashCode();
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
                return this.docHashTable.get(uri).decompress();
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
            return this.docHashTable.get(uri).getDocument();
        }

        return null;
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
                    (thisURI -> {this.processDocContents(previousDoc, previousContentString);
                                 return this.docHashTable.put(uri, previousDoc) == null; }),
                    (thisURI -> {this.searchTrieDelete(uri);
                                 return this.docHashTable.put(uri, null) == previousDoc; }));

            this.commandStack.push(newCommand);

            this.searchTrieDelete(uri);
            this.docHashTable.put(uri, null);
            return true;
        }

        else
        {
            return false;
        }
    }

    private void searchTrieDelete(URI uri)
    {
        String string = this.getDocument(uri);

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
}
