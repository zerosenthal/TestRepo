package edu.yu.cs.com1320.project;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public interface DocumentStore
{
    static enum CompressionFormat{
      ZIP,JAR,SEVENZ,GZIP,BZIP2
    };

    /**
     * specify which compression format should be used if none is specified on a putDocument,
     * either because the two-argument putDocument method is used or the CompressionFormat is
     * null in the three-argument version of putDocument
     * @param format
     * @see DocumentStore#putDocument(InputStream, URI)
     * @see DocumentStore#putDocument(InputStream, URI, CompressionFormat)
     */
    void setDefaultCompressionFormat(CompressionFormat format);

    default CompressionFormat getDefaultCompressionFormat(){
        return CompressionFormat.ZIP;
    }
    /**
     * since the user does not specify a compression format, use the default compression format
     * @param input the document being put
     * @param uri unique identifier for the document
     * @return the hashcode of the document
     */
    int putDocument(InputStream input, URI uri) throws IOException;

    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format compression format to use for compressing this document
     * @return the hashcode of the document
     */
    int putDocument(InputStream input, URI uri, CompressionFormat format) throws IOException;

    /**
     * @param uri the unique identifier of the document to get
     * @return the <B>uncompressed</B> document as a String, or null if no such document exists
     */
    String getDocument(URI uri) throws IOException;

    /**
     * @param uri the unique identifier of the document to get
     * @return the <B>compressed</B> version of the document
     */
    byte[] getCompressedDocument(URI uri);

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    boolean deleteDocument(URI uri);

    /**
     *
     * DO NOT IMPLEMENT IN STAGE 1 OF THE PROJECT. THIS IS FOR STAGE 2.
     *
     * undo the last put or delete command
     * @return true if successfully undid command, false if not successful
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    boolean undo() throws IllegalStateException;

    /**
     *
     * DO NOT IMPLEMENT IN STAGE 1 OF THE PROJECT. THIS IS FOR STAGE 2.
     *
     * undo the last put or delete that was done with the given URI as its key
     * @param uri
     * @return
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    boolean undo(URI uri) throws IllegalStateException;
}