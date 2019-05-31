package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.DocumentStore;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * Project: stage5
 * BTreeImplTest.java
 * Created 5/31/2019
 *
 * @author Elimelekh Perl
 */
public class BTreeImplTest
{
    private BTreeImpl bTree;
    URI uri1;
    URI uri2;
    DocumentImpl doc1;
    DocumentImpl doc2;

    @Before
    public void setUp() throws Exception
    {
        this.bTree = new BTreeImpl();

        this.uri1 = new URI("https://bTreeTests/doc1");
        this.doc1 = new DocumentImpl(new ByteArrayInputStream("str1".getBytes()), uri1, DocumentStore.CompressionFormat.BZIP2);

        this.uri2 = new URI("https://bTreeTests/doc2");
        this.doc2 = new DocumentImpl(new ByteArrayInputStream("str2".getBytes()), uri1, DocumentStore.CompressionFormat.BZIP2);

        this.bTree.put(uri1, this.doc1);
        this.bTree.put(uri2, this.doc2);
    }

    @Test
    public void get() throws URISyntaxException
    {
       assertEquals("testing get1", this.doc1, this.bTree.get(this.uri1));
       assertEquals("testing get2", this.doc2, this.bTree.get(this.uri2));
    }

    @Test
    public void moveToDisk() throws Exception
    {
        this.bTree.moveToDisk(this.uri1);

        //borrowed from DocIO serialize test
        String expectedPath = System.getProperty("user.dir") + System.getProperty("file.separator") + this.uri1.getHost() + this.uri1.getPath() + ".json";
        assertTrue((new File(expectedPath)).exists());

        DocumentImpl docFromDisk = this.bTree.get(this.uri1);
        assertEquals("testing clean return to memory", this.doc1, docFromDisk);

        //borrowed from DocIO deserialize test
        assertFalse(new File(expectedPath).exists());
    }
}