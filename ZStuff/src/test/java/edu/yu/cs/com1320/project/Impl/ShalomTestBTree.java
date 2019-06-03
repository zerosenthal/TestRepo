package edu.yu.cs.com1320.project.Impl;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;

/**
 * @author Shalom Gottesman
 */

@SuppressWarnings("unused")
public class ShalomTestBTree {
    final static boolean Trace = false;

    @Test public void initialize() {
        if (Trace == true) {System.out.println("\nBTree stage 5 Test: Test 1 initialize");}
        BTreeImpl bTree = new BTreeImpl();
    }
    @Test public void add() throws URISyntaxException {
        if (Trace == true) {System.out.println("\nBTree stage 5 Test: Test 2 add");}
        BTreeImpl bTree = new BTreeImpl();
        URI uri = new URI("one.doca");
        String str = "hi there";
        HashMap<String, Integer> hMap = new DocumentStoreImpl().getHashMapOfWords(str);
        Document doc1 = new DocumentImpl(new byte[10], str.hashCode(), uri, DocumentStore.CompressionFormat.BZIP2, hMap);
        bTree.put(uri, doc1);
    }
    @Test public void addMany() throws URISyntaxException {
        if (Trace == true) {System.out.println("\nBTree stage 5 Test: Test 3 addMany");}
        BTreeImpl bTree = new BTreeImpl();
        addMany(bTree, 3);
    }
    @Test public void serializeSome( ) throws Exception {
        if (Trace == true) {System.out.println("\nBTree stage 5 Test: Test 4 serializeSome");}
        BTreeImpl bTree = new BTreeImpl();
        URI[] uris = addMany(bTree, 4);
        SeaializeABunch(bTree, uris);
    }
    @Test public void retreive() throws Exception {
        if (Trace == true) {System.out.println("\nBTree stage 5 Test: Test 5 retreive");}
        BTreeImpl bTree = new BTreeImpl();
        URI[] uris = addMany(bTree, 5);
        URI[] serialized = SeaializeABunch(bTree, uris);
        Document[] docs = retreiveSome(bTree, serialized);
    }
    @Test public void retreiveAndVerify() throws Exception {
        if (Trace == true) {System.out.println("\nBTree stage 5 Test: Test 6 retreiveAndVerify");}
        BTreeImpl bTree = new BTreeImpl();
        URI[] uris = addMany(bTree, 6);
        URI[] serialized = SeaializeABunch(bTree, uris);
        Document[] docs = retreiveSome(bTree, serialized);
        for (int x = 0; x < docs.length; x++) {
            for (int y = 0; y< uris.length; y++) {
                if (docs[x].getKey().equals(uris[y])) {
                    int p = y;
                    byte[] origArray = new byte[] {(byte) y, (byte) (2* y), (byte) (3* y), (byte) (4* y), (byte) (5* y)};
                    assertTrue(Arrays.equals(docs[x].getDocument(), (origArray)));
                }
            }
        }
    }
    private Document[] retreiveSome(BTreeImpl bTree, URI[] serializedURIs) {
        List<Document> retreivedDocs = new ArrayList<Document>();
        for(int x = 0; x < serializedURIs.length; x += 2) {
            retreivedDocs.add((Document) bTree.get(serializedURIs[x]));
        }
        Document[] docArray = retreivedDocs.toArray(new Document[serializedURIs.length / 2]);
        return docArray;
    }
    private URI[] SeaializeABunch(BTreeImpl bTree, URI[] uris) throws Exception {//assumes array size is greater than 30
        List<URI> uriList = new ArrayList<URI>();
        for (int x = 0; x < 30; x += 3) {
            bTree.moveToDisk(uris[x]);
            uriList.add(uris[x]);
        }
        URI[] serializedUris = uriList.toArray(new URI[10]);
        return serializedUris;
    }
    private URI[] addMany(BTreeImpl bTree, int testNumber) throws URISyntaxException {
        File newDir = new File(System.getProperty("user.dir") + File.separatorChar + "jsonFiles" + testNumber);
        bTree.setBaseDir(newDir);
        String path = "https://www.yu..edu.//this/is/going/to/be/fun";
        String extention = "coa";
        URI[] uris = new URI[30];
        for (int x = 0; x < 30; x++) {
            URI uri = new URI(path + x +extention);
            byte[] bytes = new byte[] {(byte) x, (byte) (2* x), (byte) (3* x), (byte) (4* x), (byte) (5* x)};
            String str = "hello there my name is shalom" + 5*x+10;
            HashMap<String, Integer> hMap = new DocumentStoreImpl().getHashMapOfWords(str);
            Document doc = new DocumentImpl(bytes, str.hashCode(), uri, DocumentStore.CompressionFormat.BZIP2, hMap);
            bTree.put(uri, doc);
            uris[x] = uri;
        }
        return uris;
    }

}
