package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.DocumentStore;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Project: stage5
 * BTreeImplTest.java
 * Created 5/31/2019
 *
 * @author Elimelekh Perl and Zechariah Rosenthal
 */
public class BTreeImplTest
{
    private BTreeImpl<URI,DocumentImpl> bTree;
    URI uri1;
    URI uri2;
    DocumentImpl doc1;
    DocumentImpl doc2;

    @Before
    public void setUp() throws Exception
    {
        this.bTree = new BTreeImpl<>();
        HashMap<String, Integer> hMap1 = new HashMap<>();
        HashMap<String, Integer> hMap2 = new HashMap<>();
        hMap1.put("str1",1);
        hMap2.put("str2",1);

        this.uri1 = new URI("https://bTreeTests/doc1");
        this.doc1 = new DocumentImpl("str1".getBytes(),"str1".hashCode(), uri1, DocumentStore.CompressionFormat.BZIP2, hMap1);

        this.uri2 = new URI("https://bTreeTests/doc2");
        this.doc2 = new DocumentImpl("str2".getBytes(),"str2".hashCode(), uri2, DocumentStore.CompressionFormat.BZIP2, hMap2);

        this.bTree.put(uri1, this.doc1);
        this.bTree.put(uri2, this.doc2);
    }

    /**
     * Tests part 1 of the requirement that there is a working BTree with get and put functionality
     *
     */
    @Test
    public void get()
    {
       assertEquals("testing get1", this.doc1, this.bTree.get(this.uri1));
       assertEquals("testing get2", this.doc2, this.bTree.get(this.uri2));
    }


    /**
     * Tests moveToDisk and get from disk functionality in the BTree
     *
     */
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