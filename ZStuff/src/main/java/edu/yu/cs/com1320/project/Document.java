package edu.yu.cs.com1320.project;

import java.net.URI;

public interface Document
{
    byte[] getDocument();
    int getDocumentHashCode();
    URI getKey();
    DocumentStore.CompressionFormat getCompressionFormat();
}
