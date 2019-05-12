package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.DocumentStore;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DocumentStoreImplTestStage3 {

    @Test
    public void putManyDocsSearchprint() throws IOException, URISyntaxException {
        String str1 = "First the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        String str2 = "Second the the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        BufferedInputStream input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));
        URI uri2 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=smoke&key2=mirrors#fragid1");
        String str3 = "Third Document coming right up for the test!";
        BufferedInputStream input3 = IOUtils.buffer(IOUtils.toInputStream(str3, "UTF-8"));
        URI uri3 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=silly&key2=sally#fragid1");
        String str4 = "Fourth I am unique!@$@#$@%";
        BufferedInputStream input4 = IOUtils.buffer(IOUtils.toInputStream(str4, "UTF-8"));
        URI uri4 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=sally&key2=ly#fragid1");

        DocumentStoreImpl store = new DocumentStoreImpl();

        store.putDocument(input4,uri4);
        store.putDocument(input1, uri1);
        store.putDocument(input3, uri3);
        store.putDocument(input2, uri2);

        System.out.println(store.search("the"));
        System.out.println(store.search("unique"));
    }

    @Test
    public void putManyDocsSearch1() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        String str2 = "I'm the the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        BufferedInputStream input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));
        URI uri2 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=smoke&key2=mirrors#fragid1");
        String str3 = "Third Document coming right up for the test!";
        BufferedInputStream input3 = IOUtils.buffer(IOUtils.toInputStream(str3, "UTF-8"));
        URI uri3 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=silly&key2=sally#fragid1");

        DocumentStoreImpl store = new DocumentStoreImpl();

        store.putDocument(input1, uri1);
        store.putDocument(input3, uri3);
        store.putDocument(input2, uri2);
        ArrayList<String> result = new ArrayList<>();
        result.add(str1);
        result.add(str2);
        result.add(str3);
        assertEquals(result,store.search("the"));
    }

    @Test
    public void putManyDocsSearch2() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        String str2 = "I'm the the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        BufferedInputStream input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));
        URI uri2 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=smoke&key2=mirrors#fragid1");
        String str3 = "Third Document coming right up for the test!";
        BufferedInputStream input3 = IOUtils.buffer(IOUtils.toInputStream(str3, "UTF-8"));
        URI uri3 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=silly&key2=sally#fragid1");

        DocumentStoreImpl store = new DocumentStoreImpl();

        store.putDocument(input3, uri3);
        store.putDocument(input2, uri2);
        store.putDocument(input1, uri1);
        ArrayList<String> result = new ArrayList<>();
        result.add(str1);
        assertEquals(result,store.search("thethe"));
    }

    @Test
    public void putManyDocsSearch3() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        String str2 = "I'm the the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        BufferedInputStream input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));
        URI uri2 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=smoke&key2=mirrors#fragid1");
        String str3 = "Third Document coming right up for the test!";
        BufferedInputStream input3 = IOUtils.buffer(IOUtils.toInputStream(str3, "UTF-8"));
        URI uri3 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=silly&key2=sally#fragid1");

        DocumentStoreImpl store = new DocumentStoreImpl();

        store.putDocument(input1, uri1);
        store.putDocument(input3, uri3);
        store.putDocument(input2, uri2);
        store.undo(uri1);
        assertTrue(store.search("thethe").isEmpty());
    }

    @Test
    public void putManyDocsSearch4() throws IOException, URISyntaxException {
        String str1 = "Testing the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        BufferedInputStream input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
        URI uri1 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=value&key2=value2#fragid1");
        String str2 = "I'm the the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        BufferedInputStream input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));
        URI uri2 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=smoke&key2=mirrors#fragid1");
        String str3 = "Third Document coming right up for the test!";
        BufferedInputStream input3 = IOUtils.buffer(IOUtils.toInputStream(str3, "UTF-8"));
        URI uri3 = new URI("abc://admin:admin@geeksforgeeks.org:1234/path/data?key=silly&key2=sally#fragid1");

        DocumentStoreImpl store = new DocumentStoreImpl();

        store.putDocument(input1, uri1);
        store.putDocument(input3, uri3);
        store.putDocument(input2, uri2);
        store.undo(uri1);
        store.undo(uri2);
        ArrayList<String> result = new ArrayList<>();
        result.add(str3);
        assertEquals(result,store.search("the"));
    }

}