package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Project: stage5
 * DocumentIOImplTest.java
 * Created 5/30/2019
 *
 * @author Elimelekh Perl
 */
public class DocumentIOImplTest
{

    /*IMPORTANT: These tests check to ensure that serialize and deserialize
        code runs error free, and checks for basic file creation and deletion.
        Ensuring proper serialization and deserialization behavior must be
        done via the eye test through inspecting objects during debugging.
     */

    private DocumentIOImpl docIO;

    @Before
    public void setUp() throws Exception
    {
        this.docIO = new DocumentIOImpl();
    }

    @Test
    public void serialize() throws URISyntaxException, IOException
    {
        URI uri = new URI("https://jsonTests/doc");
        DocumentImpl doc = new DocumentImpl(new ByteArrayInputStream(("string").getBytes()), uri, DocumentStore.CompressionFormat.ZIP);
        doc.contents = doc.compress();

        String expectedPath = System.getProperty("user.dir") + System.getProperty("file.separator") + uri.getHost() + uri.getPath() + ".json";
        assertEquals("testing file path", new File(expectedPath), this.docIO.serialize(doc));

        assertTrue((new File(expectedPath)).exists());
    }

    @Test
    public void deserialize() throws URISyntaxException, IOException
    {
        URI uri = new URI("https://jsonTests/doc");
        DocumentImpl doc = new DocumentImpl(new ByteArrayInputStream(("string").getBytes()), uri, DocumentStore.CompressionFormat.ZIP);
        doc.contents = doc.compress();

        this.docIO.serialize(doc);
        Document deserializedDoc = this.docIO.deserialize(uri);

        assertEquals("testing deserialize", doc, (DocumentImpl) deserializedDoc);
        assertEquals("testing strings", doc.decompress(), ((DocumentImpl) deserializedDoc).decompress());

        String expectedPath = System.getProperty("user.dir") + System.getProperty("file.separator") + uri.getHost() + uri.getPath() + ".json";
        assertFalse(new File(expectedPath).exists());
    }
}