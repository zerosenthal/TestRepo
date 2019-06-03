package edu.yu.cs.com1320.project.Impl;

import java.net.URI;
import java.util.Comparator;

public class URIToDocComparator implements Comparator<URI> {
    private String keyword;
    private BTreeImpl<URI, DocumentImpl> docStorage;

    public URIToDocComparator(BTreeImpl<URI, DocumentImpl> bTree) {
        this.keyword = null;
        this.docStorage = bTree;
    }
    public void setKeyword(String key) {
        this.keyword = key;
    }

    @Override
    public int compare(URI o1, URI o2) {
        if (o1 == null || o2 == null || keyword == null) {
            throw new NullPointerException("Document's and keyword can't be null");
        }
        DocumentImpl doc1 = docStorage.get(o1);
        DocumentImpl doc2 = docStorage.get(o2);
        int num1 = doc1.wordCount(this.keyword);
        int num2 = doc2.wordCount(this.keyword);
        if (num1 > num2) {
            return -1;
        }
        if (num1 < num2) {
            return 1;
        }
        return 0;
    }
}


