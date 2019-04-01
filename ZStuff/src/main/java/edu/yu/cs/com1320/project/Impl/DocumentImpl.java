package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;

import java.net.URI;
import java.util.Arrays;


/**
 * @author Zechariah-Rosenthal
 */
public class DocumentImpl implements Document {
    private byte[] compressedFile;
    private int hashCode;
    private URI uri;
    private DocumentStore.CompressionFormat format;

    public DocumentImpl(byte[] compressedFile, int hashCode, URI uri, DocumentStore.CompressionFormat format) {
        this.compressedFile = compressedFile;
        this.hashCode = hashCode;
        this.uri = uri;
        this.format = format;
    }

    @Override
    public String toString() {
        return "DocumentImpl{" +
                "compressedFile=" + Arrays.toString(compressedFile) +
                ", hashCode=" + hashCode +
                ", uri=" + uri +
                ", format=" + format +
                '}';
    }

    @Override
    public byte[] getDocument() {
        return this.compressedFile;
    }

    @Override
    public int getDocumentHashCode() {
        return this.hashCode;
    }

    @Override
    public URI getKey() {
        return this.uri;
    }

    @Override
    public DocumentStore.CompressionFormat getCompressionFormat() {
        return this.format;
    }
}
