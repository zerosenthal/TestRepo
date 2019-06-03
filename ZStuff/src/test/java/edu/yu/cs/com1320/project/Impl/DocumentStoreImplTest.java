package edu.yu.cs.com1320.project.Impl;


import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.*;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Tests Undo functionality part 4 of requirements doc
 *
 * @author Zechariah Rosenthal
 */
public class DocumentStoreImplTest {
    String str1, str2, str3;
    URI uri1, uri2, uri3;

    private DocumentStoreImpl setup() {
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        str1 = "First the compressor the the the the the thethe the thethe the thethe the thethe the thethe the thethe the the";
        uri1 = URI.create("http://ZROTESTS/URI1");
        str2 = "Second the the second document!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
        str3 = "Third Document coming right up for the test!";
        uri2 = URI.create("https://ZROTESTS/URI2");
        uri3 = URI.create("zrotests/uri3");
        BufferedInputStream input1, input2, input3;
        input1 = input2 = input3 = null;
        try {
            input1 = IOUtils.buffer(IOUtils.toInputStream(str1, "UTF-8"));
            input2 = IOUtils.buffer(IOUtils.toInputStream(str2, "UTF-8"));
            input3 = IOUtils.buffer(IOUtils.toInputStream(str3, "UTF-8"));
        } catch (IOException e) {

        }
        dsi.putDocument(input1, uri1);
        dsi.putDocument(input2, uri2);
        dsi.putDocument(input3, uri3);
        return dsi;
    }

    /**
     * Shows that documents managed to memory are still available for getting
     */
    @Test
    public void noRoomInMemoryStillGet() {
        DocumentStoreImpl dsi = setup();
        dsi.setMaxDocumentCount(1);
        assertEquals(str1, dsi.getDocument(uri1));
        assertEquals(str2, dsi.getDocument(uri2));
        assertEquals(str3, dsi.getDocument(uri3));
    }

    /**
     * Shows that undo un-puts the correct document and leaves the rest unscathed
     */
    @Test
    public void undo() {
        DocumentStoreImpl dsi = setup();
        dsi.setMaxDocumentCount(1);
        assertTrue(dsi.undo());
        assertNull(dsi.getDocument(uri3));
        assertEquals(str1, dsi.getDocument(uri1));
    }

    /**
     * Shows that undoing the bottom of the commandStack un-puts the correct document and leaves the rest unscathed
     */
    @Test
    public void undoURI1() {
        DocumentStoreImpl dsi = setup();
        dsi.setMaxDocumentCount(1);
        assertTrue(dsi.undo(uri1));
        assertNull(dsi.getDocument(uri1));
        assertEquals(str2, dsi.getDocument(uri2));
        assertEquals(str3, dsi.getDocument(uri3));
    }

    /**
     * Tests that undoing an empty stack throws the correct exception
     */
    @Test(expected = IllegalStateException.class)
    public void undoEmptyStack() {
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        dsi.undo();
    }
}
