package edu.yu.cs.com1320.project;

import java.net.URI;

public interface Document extends Comparable<Document>
{
    byte[] getDocument();
    int getDocumentHashCode();
    URI getKey();
    DocumentStore.CompressionFormat getCompressionFormat();

    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return
     */
    int wordCount(String word);
    long getLastUsedTime();
    void setLastUsedTime(long timeInMilliseconds);
}
