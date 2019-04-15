package edu.yu.cs.com1320.project.Impl;


import edu.yu.cs.com1320.project.DocumentStore;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class DocumentStoreImplTestStage1 {

    @Test
    public void setDefaultCompressionFormat() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.GZIP);
        assertEquals(DocumentStore.CompressionFormat.GZIP, store.getDefaultCompressionFormat());
    }

    @Test
    public void putReturnsHashCodeOfUncompressedInput() throws IOException, URISyntaxException {
        String str = "Testing the compressor the the the";
        BufferedInputStream input = IOUtils.buffer(IOUtils.toInputStream(str, "UTF-8"));
        URI uri = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        DocumentStoreImpl store = new DocumentStoreImpl();
        assertEquals(str.hashCode(), store.putDocument(input, uri, DocumentStore.CompressionFormat.GZIP));
    }

    @Test
    public void putDocumentAndGetUncompressedString() throws IOException, URISyntaxException {
        String str = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input = IOUtils.buffer(IOUtils.toInputStream(str, "UTF-8"));
        URI uri = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.putDocument(input, uri, DocumentStore.CompressionFormat.GZIP);
        assertEquals(str, store.getDocument(uri));
    }

    @Test
    public void putDocumentDefaultCompression() throws IOException, URISyntaxException {
        String str = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input = IOUtils.buffer(IOUtils.toInputStream(str, "UTF-8"));
        URI uri = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.GZIP);
        store.putDocument(input, uri);
        assertEquals(str, store.getDocument(uri));
    }

    @Test
    public void putGetZip() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.ZIP);
        store.putDocument(input1, uri1);
        assertEquals(str1, store.getDocument(uri1));
    }

    @Test
    public void putGetGZIP() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.GZIP);
        store.putDocument(input1, uri1);
        assertEquals(str1, store.getDocument(uri1));
    }

    @Test
    public void putGetBZIP() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.BZIP2);
        store.putDocument(input1, uri1);
        assertEquals(str1, store.getDocument(uri1));
    }

    @Test
    public void putManyDocsGZIP() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        String str2 = "I'm the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        BufferedInputStream input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));
        URI uri2 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=smoke&key2=mirrors#fragid1");
        String str3 = "Third Document coming right up for the test!";
        BufferedInputStream input3 = IOUtils.buffer(IOUtils.toInputStream(str3, "UTF-8"));
        URI uri3 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=silly&key2=sally#fragid1");

        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.GZIP);
        assertEquals(str1.hashCode(), store.putDocument(input1, uri1));
        assertEquals(str2.hashCode(), store.putDocument(input2, uri2));
        assertEquals(str3.hashCode(), store.putDocument(input3, uri3));
    }

    @Test
    public void putManyDocsDifferentCompressionFormats() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        String str2 = "I'm the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        BufferedInputStream input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));
        URI uri2 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=smoke&key2=mirrors#fragid1");
        String str3 = "Third Document coming right up for the test!";
        BufferedInputStream input3 = IOUtils.buffer(IOUtils.toInputStream(str3, "UTF-8"));
        URI uri3 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=silly&key2=sally#fragid1");

        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.GZIP);

        store.putDocument(input1, uri1);
        store.putDocument(input2, uri2, DocumentStore.CompressionFormat.BZIP2);
        store.putDocument(input3, uri3, DocumentStore.CompressionFormat.ZIP);
        assertEquals(str1, store.getDocument(uri1));
        assertEquals(str2, store.getDocument(uri2));
        assertEquals(str3, store.getDocument(uri3));
    }

    @Test
    public void putGetJar() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.JAR);
        store.putDocument(input1, uri1);
        assertEquals(str1, store.getDocument(uri1));
    }

    @Test
    public void putGet7Z() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");

        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.SEVENZ);

        store.putDocument(input1, uri1);
        assertEquals(str1, store.getDocument(uri1));
        assertEquals(str1, store.getDocument(uri1));
        assertEquals(str1, store.getDocument(uri1));
    }

    @Test
    public void putGetBig7Z() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the theTesting the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");

        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.SEVENZ);

        store.putDocument(input1, uri1);
        assertEquals(str1, store.getDocument(uri1));
        assertEquals(str1, store.getDocument(uri1));
        assertEquals(str1, store.getDocument(uri1));
    }

    @Test
    public void putAndOverwriteADoc() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        String str2 = "I'm the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        BufferedInputStream input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));

        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.GZIP);

        store.putDocument(input1, uri1);
        store.putDocument(input2, uri1, DocumentStore.CompressionFormat.BZIP2);

        assertEquals(str2, store.getDocument(uri1));
    }

    @Test
    public void actuallyCompressed7z() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the thethe the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");

        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.SEVENZ);

        store.putDocument(input1, uri1);
        assertNotEquals(store.getCompressedDocument(uri1),store.getDocument(uri1));
    }

    @Test
    public void putManyDoc7z() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        String str2 = "I'm the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        BufferedInputStream input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));
        URI uri2 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=smoke&key2=mirrors#fragid1");
        String str3 = "Third Document coming right up for the test!";
        BufferedInputStream input3 = IOUtils.buffer(IOUtils.toInputStream(str3, "UTF-8"));
        URI uri3 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=silly&key2=sally#fragid1");

        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setDefaultCompressionFormat(DocumentStore.CompressionFormat.SEVENZ);

        assertEquals(str1.hashCode(), store.putDocument(input1, uri1));
        assertEquals(str2.hashCode(), store.putDocument(input2, uri2));
        assertEquals(str3.hashCode(), store.putDocument(input3, uri3));
    }


}
