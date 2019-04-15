package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.DocumentStore;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class DocumentStoreImplTestStage2 {

    @Test
    public void undoCheckByPrintln() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        String str2 = "I'm the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        BufferedInputStream input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));
        URI uri2 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=smoke&key2=mirrors#fragid1");
        String str3 = "Third Document coming right up for the test!";
        BufferedInputStream input3 = IOUtils.buffer(IOUtils.toInputStream(str3, "UTF-8"));
        URI uri3 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=silly&key2=sally#fragid1");
        DocumentStoreImpl store = new DocumentStoreImpl(3);
        store.putDocument(input1, uri1);
        store.deleteDocument(uri1);
        System.out.println("After put and delete:\n" + store.commandStackToString());
        System.out.println(store);
        store.undo();
        System.out.println("After 1st undo:\n" + store.commandStackToString());
        System.out.println(store);
        store.undo();
        System.out.println("After 2nd undo:\n" + store.commandStackToString());
        System.out.println(store);
    }

    @Test(expected = IllegalStateException.class)
    public void undoEmptyStack() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.undo();
    }

    @Test(expected = IllegalStateException.class)
    public void undoWrongURI() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        URI uri2 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=smoke&key2=mirrors#fragid1");
        store.putDocument(input1,uri1);
        store.undo(uri2);
    }

    @Test
    public void undoChosenURI() throws IOException, URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        store.putDocument(input1,uri1);
        store.deleteDocument(uri1);
        String str2 = "I'm the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        BufferedInputStream input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));
        URI uri2 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=smoke&key2=mirrors#fragid1");
        store.putDocument(input2,uri2);
        store.undo(uri1);
        assertEquals(str1, store.getDocument(uri1));

    }

}