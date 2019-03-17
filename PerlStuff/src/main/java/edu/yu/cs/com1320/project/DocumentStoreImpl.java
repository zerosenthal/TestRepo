package edu.yu.cs.com1320.project;

import java.io.*;
import java.net.URI;


/**
 * Project: stage1
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

    public DocumentStoreImpl()
    {
        this.defCompForm = getDefaultCompressionFormat();
        this.docHashTable = new HashTableImpl<URI, DocumentImpl>();
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
    public int putDocument(InputStream input, URI uri, CompressionFormat format)
    {
        DocumentImpl document = null;
        try
        {
            document = new DocumentImpl(input, uri, format);

            if (!(this.docHashTable.contains(uri, document)))
            {
                document.contents = document.compress();
                this.docHashTable.put(uri, document);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return document.hashCode();
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
            this.docHashTable.put(uri, null);
            return true;
        }

        else
        {
            return false;
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
    @Override
    public boolean undo(URI uri) throws IllegalStateException
    {
        return false;
    }
}
