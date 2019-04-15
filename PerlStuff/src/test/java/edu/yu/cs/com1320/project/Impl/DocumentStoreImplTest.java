package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.DocumentStore;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Project: stage2
 * DocumentStoreImplTest.java
 * Created 3/11/2019
 *
 * @author Elimelekh Perl
 */
public class DocumentStoreImplTest
{
    private DocumentStoreImpl docStore;
    private DocumentStoreImpl docStore2;
    private InputStream testInput;
    private URI testURI;

    @Before
    public void setUp() throws URISyntaxException
    {
        docStore = new DocumentStoreImpl();
        assertEquals("testing default compression format", DocumentStore.CompressionFormat.ZIP, docStore.defCompForm);

        String str = "Hello this is a string";
        testInput = new ByteArrayInputStream(str.getBytes());
        testURI = URI.create("testing_this_input");

        String str1 = "String1";
        String str2 = "String2";
        String str3 = "String3";
        String str4 = "String4";

        docStore.putDocument(new ByteArrayInputStream(str1.getBytes()), new URI("1"));
        docStore.putDocument(new ByteArrayInputStream(str2.getBytes()), new URI("2"));
        docStore.putDocument(new ByteArrayInputStream(str4.getBytes()), new URI("1"));
        docStore.putDocument(new ByteArrayInputStream(str3.getBytes()), new URI("3"));
        docStore.deleteDocument(new URI("2"));

        docStore2 = new DocumentStoreImpl();
    }

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

        assertTrue("testing delete method", docStore.docHashTable.get(testURI) == null);
    }

    @Test
    public void testGenericUndo() throws URISyntaxException
    {
        assertTrue(docStore.undo());

        assertEquals("testing undo delete", "String2", docStore.getDocument(new URI("2")));

        assertTrue(docStore.undo());

        assertEquals("testing undo put new doc", null, docStore.docHashTable.get(new URI("3")));

        assertTrue(docStore.undo());

        assertEquals("testing undo put overwrite doc", "String1", docStore.getDocument(new URI("1")));
    }

    @Test
    public void testURIUndo() throws URISyntaxException
    {
       docStore.undo(new URI("1"));

        assertEquals("testing undo uri", "String1", docStore.getDocument(new URI("1")));
        assertEquals("testing undo uri, stack reset", "String3", docStore.getDocument(new URI("3")));
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyCommandStack()
    {
        docStore2.undo();
    }

    @Test(expected = IllegalStateException.class)
    public void testUndoNoURI() throws URISyntaxException
    {
        docStore.undo(new URI("42"));
    }
}