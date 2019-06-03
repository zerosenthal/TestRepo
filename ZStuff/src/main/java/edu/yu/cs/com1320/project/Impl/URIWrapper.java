package edu.yu.cs.com1320.project.Impl;

import java.net.URI;
import java.util.Objects;

public class URIWrapper implements Comparable<URIWrapper> {
    private URI uri;
    private static BTreeImpl<URI, DocumentImpl> docStorage;

    public URIWrapper(URI uri) {
        this.uri = uri;
    }

    public static void setBTree(BTreeImpl<URI, DocumentImpl> bTree) {
        docStorage = bTree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof URIWrapper)) return false;
        URIWrapper that = (URIWrapper) o;
        return Objects.equals(uri, that.uri);
    }
    public URI getUri() {
        return this.uri;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    @Override
    public int compareTo(URIWrapper o) {
        if(docStorage == null) throw new IllegalStateException("Must set a BTree for the URIs. BTree cannot be null.");
        if (o == null) throw new NullPointerException();
        DocumentImpl thisDoc = docStorage.get(this.uri);
        DocumentImpl thatDoc = docStorage.get(o.uri);
        if (thisDoc == null || thatDoc == null) {
            throw new NullPointerException();
        }
        if (thisDoc.getLastUsedTime() < thatDoc.getLastUsedTime()) {
            return -1;
        } else {
            return 1;
        }
    }
}