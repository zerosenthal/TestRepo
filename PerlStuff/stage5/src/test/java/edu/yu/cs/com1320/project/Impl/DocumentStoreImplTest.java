package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.DocumentStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

/**
 * Project: stage5
 * DocumentStoreImplTest.java
 * Created 3/11/2019
 *
 * @author Elimelekh Perl
 */
public class DocumentStoreImplTest
{
    private DocumentStoreImpl docStore;
    private DocumentStoreImpl docStore2;
    private DocumentStoreImpl docStore3;
    private DocumentStoreImpl docStore4;
    private InputStream testInput;
    private URI testURI;

    @Before
    public void setUp() throws URISyntaxException
    {
        docStore = new DocumentStoreImpl();
        assertEquals("testing default compression format", DocumentStore.CompressionFormat.ZIP, docStore.defCompForm);

        String str = "Hello this is a string";
        testInput = new ByteArrayInputStream(str.getBytes());
        testURI = URI.create("https://docStoreTests/store1/testURI");

        String str1 = "String1";
        String str2 = "String2";
        String str3 = "String3";
        String str4 = "String4";
        String str5 = "String5";

        docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), new URI("https://docStoreTests/store1/1"));
        docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), new URI("https://docStoreTests/store1/2"));
        docStore.putDocument(new ByteArrayInputStream(str4.getBytes()), new URI("https://docStoreTests/store1/1"));
        docStore.putDocument(new ByteArrayInputStream(str3.getBytes()), new URI("https://docStoreTests/store1/3"));
        docStore.deleteDocument(new URI("https://docStoreTests/store1/2"));

        docStore2 = new DocumentStoreImpl();

        docStore3 = new DocumentStoreImpl();

        String text1 = "This is a string, you say, I say";
        String text2 = "A string, this is. This, A STRING, I say";
        String text3 = "I say, I say, I say";

        docStore3.putDocument(new ByteArrayInputStream(text1.getBytes()), new URI("https://docStoreTests/store3/1"));
        docStore3.putDocument(new ByteArrayInputStream(text2.getBytes()), new URI("https://docStoreTests/store3/2"));
        docStore3.putDocument(new ByteArrayInputStream(text3.getBytes()), new URI("https://docStoreTests/store3/3"));

        docStore4 = new DocumentStoreImpl();

        String message1 = "This document goes in first";
        String message2 = "This document goes in second";
        String message3 = "This document goes in third";
        String message4 = "This document replaces the second document";

        docStore4.putDocument(new ByteArrayInputStream(message1.getBytes()), new URI("https://docStoreTests/store4/1"));
        docStore4.putDocument(new ByteArrayInputStream(message2.getBytes()), new URI("https://docStoreTests/store4/2"));
        docStore4.putDocument(new ByteArrayInputStream(message3.getBytes()), new URI("https://docStoreTests/store4/3"));
        docStore4.putDocument(new ByteArrayInputStream(message4.getBytes()), new URI("https://docStoreTests/store4/2"));
    }

    /*work in progress
    @After
    public void tearDown()
    {
        File testDir = (new File(System.getProperty("user.dir") + "docStoreTests")).getParentFile();
        File graveyardDir = (new File(System.getProperty("user.dir") + "docStoreTests")).getParentFile();

        for (File file: testDir.listFiles()) {
            file.delete();
        }
        testDir.delete();

        for (File file: graveyardDir.listFiles()) {
            file.delete();
        }
        graveyardDir.delete();
    }*/

    @Test
    public void testSetDefaultCompressionFormat()
    {

        docStore.setDefaultCompressionFormat(DocumentStore.CompressionFormat.JAR);
        assertEquals("testing set comp form method", DocumentStore.CompressionFormat.JAR, docStore.defCompForm);
    }

    @Test
    public void testZIP() throws IOException
    {
        docStore.putDocument(testInput, testURI);

        String outputString = docStore.getDocument(testURI);

        assertEquals("testing zip compressor, decompressor", "Hello this is a string", outputString);
    }

    @Test
    public void testJAR() throws IOException
    {
        docStore.putDocument(testInput, testURI, DocumentStore.CompressionFormat.JAR);

        String outputString = docStore.getDocument(testURI);

        assertEquals("testing compressor", "Hello this is a string", outputString);
    }

    @Test
    public void testGZIP() throws IOException
    {
        docStore.putDocument(testInput, testURI, DocumentStore.CompressionFormat.GZIP);

        String outputString = docStore.getDocument(testURI);

        assertEquals("testing compressor", "Hello this is a string", outputString);
    }

    @Test
    public void testBZIP2() throws IOException
    {
        docStore.putDocument(testInput, testURI, DocumentStore.CompressionFormat.BZIP2);

        String outputString = docStore.getDocument(testURI);

        assertEquals("testing compressor", "Hello this is a string", outputString);
    }

    @Test
    public void testSevenZ()
    {
        docStore.putDocument(testInput, testURI, DocumentStore.CompressionFormat.SEVENZ);

        String outputString = docStore.getDocument(testURI);

        assertEquals("testing compressor", "Hello this is a string", outputString);
    }


    @Test
    public void testGetCompressedDocument() throws IOException
    {
        testInput.mark(10000);

        docStore.putDocument(testInput, testURI);

        testInput.reset();

        byte[] storedArray = (new DocumentImpl(testInput,testURI, DocumentStore.CompressionFormat.ZIP)).compress();
        byte[] outputArray = docStore.getCompressedDocument(testURI);

        assertArrayEquals("testing get compressed byte[]", storedArray, outputArray);
    }

    @Test
    public void testDeleteDocument()
    {
        docStore.putDocument(testInput, testURI);
        docStore.deleteDocument(testURI);

        assertTrue("testing delete method", docStore.docBTree.get(testURI) == null);
    }

    @Test
    public void testGenericUndo() throws URISyntaxException
    {
        assertTrue(docStore.undo());

        assertEquals("testing undo delete", "String2", docStore.getDocument(new URI("https://docStoreTests/store1/2")));

        assertTrue(docStore.undo());

        assertEquals("testing undo put new doc", null, docStore.docBTree.get(new URI("https://docStoreTests/store1/3")));

        assertTrue(docStore.undo());

        assertEquals("testing undo put overwrite doc", "String1", docStore.getDocument(new URI("https://docStoreTests/store1/1")));
    }

    @Test
    public void testURIUndo() throws URISyntaxException
    {
       assertTrue(docStore.undo(new URI("https://docStoreTests/store1/1")));

        assertEquals("testing undo uri", "String1", docStore.getDocument(new URI("https://docStoreTests/store1/1")));
        assertEquals("testing undo uri, stack reset", "String3", docStore.getDocument(new URI("https://docStoreTests/store1/3")));
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyCommandStack()
    {
        docStore2.undo();
    }

    @Test(expected = IllegalStateException.class)
    public void testUndoNoURI() throws URISyntaxException
    {
        docStore.undo(new URI("https://docStoreTests/store1/42"));
    }

    @Test
    public void testSearch() throws URISyntaxException
    {
        List<String> stringList = new ArrayList<String>(Arrays.asList(this.docStore3.getDocument(new URI("https://docStoreTests/store3/2")), this.docStore3.getDocument(new URI("https://docStoreTests/store3/1"))));
        List<String> sayList = new ArrayList<String>(Arrays.asList(this.docStore3.getDocument(new URI("https://docStoreTests/store3/3")), this.docStore3.getDocument(new URI("https://docStoreTests/store3/1")), this.docStore3.getDocument(new URI("https://docStoreTests/store3/2"))));

        assertEquals("testing search uncompressed", stringList, this.docStore3.search("string"));
        assertEquals("testing search uncompressed capital keyword", stringList, this.docStore3.search("String"));
        assertEquals("testing search uncompressed", sayList, this.docStore3.search("say"));
    }

    @Test
    public void testSearchCompressed() throws URISyntaxException
    {
        List<byte[]> stringList = new ArrayList<byte[]>(Arrays.asList(this.docStore3.getCompressedDocument(new URI("https://docStoreTests/store3/2")), this.docStore3.getCompressedDocument(new URI("https://docStoreTests/store3/1"))));
        List<byte[]> sayList = new ArrayList<byte[]>(Arrays.asList(this.docStore3.getCompressedDocument(new URI("https://docStoreTests/store3/3")), this.docStore3.getCompressedDocument(new URI("https://docStoreTests/store3/1")), this.docStore3.getCompressedDocument(new URI("https://docStoreTests/store3/2"))));

        assertEquals("testing search compressed", stringList, this.docStore3.searchCompressed("string"));
        assertEquals("testing search compressed", stringList, this.docStore3.searchCompressed("String"));
        assertEquals("testing search compressed", sayList, this.docStore3.searchCompressed("say"));
    }

    @Test
    public void testUndoTrie() throws URISyntaxException
    {
        String text4 = "Say say say say";
        List<String> sayList = new ArrayList<String>(Arrays.asList(this.docStore3.getDocument(new URI("https://docStoreTests/store3/3")), this.docStore3.getDocument(new URI("https://docStoreTests/store3/1")), this.docStore3.getDocument(new URI("https://docStoreTests/store3/2"))));

        this.docStore3.putDocument(new ByteArrayInputStream(text4.getBytes()), new URI("https://docStoreTests/store3/4"));

        docStore3.undo();

        assertEquals("testing undo put trie update", sayList, this.docStore3.search("say"));

        docStore3.deleteDocument(new URI("https://docStoreTests/store3/3"));

        docStore3.undo();

        assertEquals("testing undo delete trie update", sayList, this.docStore3.search("say"));
    }

    @Test
    public void testByteMemManage() throws URISyntaxException
    {
        String newMessage = "This document exceeds the byte storage limits";

        this.docStore4.setMaxDocumentBytes(670);

        this.docStore4.putDocument(new ByteArrayInputStream(newMessage.getBytes()), new URI("https://docStoreTests/store4/5"));

        File diskFile = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "docStoreTests/store4/1" + ".json");

        assertTrue("testing memory management for byte limit", diskFile.exists());
    }

    @Test
    public void testCountMemManage() throws URISyntaxException
    {
        String newMessage = "This document exceeds the doc count limits";

        this.docStore4.setMaxDocumentCount(3);

        this.docStore4.putDocument(new ByteArrayInputStream(newMessage.getBytes()), new URI("https://docStoreTests/store4/5"));

        File diskFile = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "docStoreTests/store4/1" + ".json");

        assertTrue("testing memory management for byte limit", diskFile.exists());
    }

    @Test
    public void testStage5() throws URISyntaxException
    {
        DocumentStoreImpl docStore = new DocumentStoreImpl();

        URI uri1 = new URI("https://docStoreTests/doc1");
        String doc1_1 = "doc1.1";
        String doc1_2 = "doc1.2";
        String doc1_3 = "doc1.3";

        URI uri2 = new URI("https://docStoreTests/doc2");
        String doc2_1 = "doc2.1";

        URI uri3 = new URI("https://docStoreTests/doc3");
        String doc3_1 = "doc3.1";

        docStore.setMaxDocumentCount(2);

        docStore.putDocument(new ByteArrayInputStream(doc1_1.getBytes()), uri1, DocumentStore.CompressionFormat.GZIP);
        docStore.putDocument(new ByteArrayInputStream(doc1_2.getBytes()), uri1, DocumentStore.CompressionFormat.GZIP);
        docStore.putDocument(new ByteArrayInputStream(doc1_3.getBytes()), uri1, DocumentStore.CompressionFormat.GZIP); //doc1 should be overridden twice, v1 and v2 should be on disk

        File docFile1_1 = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "graveyard" + System.getProperty("file.separator") + uri1.getHost() + uri1.getPath() + System.getProperty("file.separator") + "v1" + ".json");
        File docFile1_2 = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "graveyard" + System.getProperty("file.separator") + uri1.getHost() + uri1.getPath() + System.getProperty("file.separator") + "v2" + ".json");
        assertTrue(docFile1_1.exists());
        assertTrue(docFile1_2.exists());

        docStore.putDocument(new ByteArrayInputStream(doc2_1.getBytes()), uri2, DocumentStore.CompressionFormat.GZIP);

        docStore.putDocument(new ByteArrayInputStream(doc3_1.getBytes()), uri3, DocumentStore.CompressionFormat.GZIP);//exceeds mem limit, should send doc1.3 to disk in bTree

        File docFile1_3 = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + uri1.getHost() + uri1.getPath() + ".json");
        assertTrue(docFile1_3.exists());

        docStore.search("doc13"); //search should bring doc1.3 back to memory, kick doc2 out

        docStore.undo(); //should undo put of doc3, allow doc2 back into memory

        File docFile2_1 = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + uri2.getHost() + uri2.getPath() + ".json");
        assertFalse(docFile2_1.exists());
        assertEquals("doc2.1", docStore.getDocument(uri2));

        docStore.undo(uri1); //should bypass doc2 on stack, roll back doc1 to v2

        assertEquals("doc1.2", docStore.getDocument(uri1));
        assertFalse(docFile1_2.exists());

        docStore.undo(uri1); //should bypass doc2 on stack, roll back doc1 to v1
        assertEquals("doc1.1", docStore.getDocument(uri1));
        assertFalse(docFile1_1.exists());
    }
}