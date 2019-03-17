package edu.yu.cs.com1320.project;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Project: stage1
 * DocumentStoreImplTest.java
 * Created 3/11/2019
 *
 * @author Elimelekh Perl
 */
public class DocumentStoreImplTest
{
    private DocumentStoreImpl docStore;
    private InputStream testInput;
    private URI testURI;

    @Before
    public void setUp()
    {
        docStore = new DocumentStoreImpl();
        assertEquals("testing default compression format", DocumentStore.CompressionFormat.ZIP, docStore.defCompForm);

        String str = "Hello this is a string";
        testInput = new ByteArrayInputStream(str.getBytes());
        testURI = URI.create("testing_this_input");
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
}