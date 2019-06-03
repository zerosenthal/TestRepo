package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests Stage 5
 * Tests part 3 in the requirements doc
 *
 * @author Zechariah Rosenthal
 */
public class DocumentIOImplTest {
    /** 3.1 and 3.2 tests
     * Serializes the appropriate fields
     * Uses GSON with custom (de)serializers
     *  I (Zechariah Rosenthal) used a utility class GSONDocUtility to convert Document -> JSON String and back
     */

    /**
     * Tests whether serializing then deserializing produces a document that's equal in the respective serialized fields
     */
    @Test
    public void thereAndBackAgain() {
        //Make a doc to serialize
        URI uri = URI.create("https://jsonTests/doc");
        String str = "string string string";
        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("string", 3);
        //Note fake uncompressed byteArray. Doesn't matter for this test.
        DocumentImpl oldDoc = new DocumentImpl(str.getBytes(), str.hashCode(), uri, DocumentStore.CompressionFormat.BZIP2, wordMap);

        String json = GSONDocUtility.serialize(oldDoc);
        Document newDoc = GSONDocUtility.deserialize(json);

        assertTrue(Arrays.equals(oldDoc.getDocument(),newDoc.getDocument()));
        assertTrue(oldDoc.getKey().equals(newDoc.getKey()));
        assertEquals(oldDoc.getCompressionFormat(), newDoc.getCompressionFormat());
        assertTrue(oldDoc.getWordMap().equals(newDoc.getWordMap()));
        assertEquals(oldDoc.getDocumentHashCode(), newDoc.getDocumentHashCode());
    }


    /**
     * Visually ensure that the JSON serialization "looks right" with all the fields serialized that should be
     */
    @Test
    public void serializeAndPrint() {
        //Make a doc to serialize
        URI uri = URI.create("https://jsonTests/doc");
        String str = "string string string";
        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("string", 3);
        //Note fake uncompressed byteArray. Doesn't matter for this test.
        DocumentImpl doc = new DocumentImpl(str.getBytes(), str.hashCode(), uri, DocumentStore.CompressionFormat.BZIP2, wordMap);

        System.out.println("<serializeAndPrint>");
        System.out.println(GSONDocUtility.serialize(doc));
        System.out.println("</serializeAndPrint>\n");
    }

    /**
     * Visually ensure that the JSON deserialization "looks right" with all the fields deserialized that should be
     */
    @Test
    public void deserializeAndPrint() {
        //Make a doc to serialize
        URI uri = URI.create("https://jsonTests/doc");
        String str = "string string string";
        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("string", 3);
        //Note fake uncompressed byteArray. Doesn't matter for this test.
        DocumentImpl doc = new DocumentImpl(str.getBytes(), str.hashCode(), uri, DocumentStore.CompressionFormat.BZIP2, wordMap);
        String json = GSONDocUtility.serialize(doc);
        DocumentImpl newDoc = (DocumentImpl)GSONDocUtility.deserialize(json);
        System.out.println("<deserializeAndPrint>\nOldDoc: \n"+doc.toString()+"\nNewDoc: \n"+newDoc.toString()+"\n</deserializeAndPrint>");
    }

    /**
     * 3.3 and 3.4 tests
     * DocumentIOImpl should manage reading and writing files and cleaning up after itself
     * URI's should be processed into appropiate paths
     */

    /**
     * Test that URI is turned into the correct path both with a default dir and a chosen dir
     */
    @Test
    public void URItoPathDefault() {
        DocumentIOImpl docIO = new DocumentIOImpl();

        URI uri = URI.create("https://jsonTests/doc");
        String str = "string string string";
        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("string", 3);
        //Note fake uncompressed byteArray. Doesn't matter for this test.
        DocumentImpl doc = new DocumentImpl(str.getBytes(), str.hashCode(), uri, DocumentStore.CompressionFormat.BZIP2, wordMap);

        Path expectedPath = Paths.get(System.getProperty("user.dir") + System.getProperty("file.separator") + uri.getHost() + uri.getPath() + ".json");
        Path processedPath = docIO.getPathFromUri(uri);
        assertTrue(expectedPath.equals(processedPath));
    }

    @Test
    public void URItoPathGivenDir() {
        File baseDir = Paths.get("C:\\").toFile();
        DocumentIOImpl docIO = new DocumentIOImpl(baseDir);

        URI uri = URI.create("https://jsonTests/doc");
        String str = "string string string";
        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("string", 3);
        //Note fake uncompressed byteArray. Doesn't matter for this test.
        DocumentImpl doc = new DocumentImpl(str.getBytes(), str.hashCode(), uri, DocumentStore.CompressionFormat.BZIP2, wordMap);

        Path expectedPath = Paths.get(baseDir.toString()+ System.getProperty("file.separator") + uri.getHost() + uri.getPath() + ".json");
        Path processedPath = docIO.getPathFromUri(uri);
        assertTrue(expectedPath.equals(processedPath));
    }

    /**
     * Assert that DocumentIOImpl makes the File and its in the right path
     * Assert that DocumentIOImpl returns the original doc and deletes the file
     */

    @Test
    public void serialize() throws URISyntaxException
    {
        URI uri = URI.create("https://jsonTests/doc");
        String str = "string string string";
        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("string", 3);
        //Note fake uncompressed byteArray. Doesn't matter for this test.
        DocumentImpl doc = new DocumentImpl(str.getBytes(), str.hashCode(), uri, DocumentStore.CompressionFormat.BZIP2, wordMap);

        DocumentIOImpl docIO = new DocumentIOImpl();
        Path expectedPath = Paths.get(System.getProperty("user.dir") + System.getProperty("file.separator") + uri.getHost() + uri.getPath() + ".json");
        assertEquals("testing file path", expectedPath, docIO.serialize(doc).toPath());

        assertTrue(expectedPath.toFile().exists());
        docIO.deserialize(uri);
    }

    @Test
    public void deserialize() throws URISyntaxException
    {
        URI uri = URI.create("https://jsonTests/doc");
        String str = "string string string";
        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("string", 3);
        //Note fake uncompressed byteArray. Doesn't matter for this test.
        DocumentImpl oldDoc = new DocumentImpl(str.getBytes(), str.hashCode(), uri, DocumentStore.CompressionFormat.BZIP2, wordMap);

        DocumentIOImpl docIO = new DocumentIOImpl();
        docIO.serialize(oldDoc);
        Document newDoc = docIO.deserialize(uri);

        assertTrue(Arrays.equals(oldDoc.getDocument(),newDoc.getDocument()));
        assertTrue(oldDoc.getKey().equals(newDoc.getKey()));
        assertEquals(oldDoc.getCompressionFormat(), newDoc.getCompressionFormat());
        assertTrue(oldDoc.getWordMap().equals(newDoc.getWordMap()));
        assertEquals(oldDoc.getDocumentHashCode(), newDoc.getDocumentHashCode());

        String expectedPath = System.getProperty("user.dir") + System.getProperty("file.separator") + uri.getHost() + uri.getPath() + ".json";
        assertFalse(new File(expectedPath).exists());
    }
}
