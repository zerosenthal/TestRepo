package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;


/**
 * @author Zechariah-Rosenthal
 */
public class DocumentImpl implements Document {
    private byte[] compressedFile;
    private int hashCode;
    private URI uri;
    private DocumentStore.CompressionFormat format;
    private HashMap<String,Integer> words;

    public DocumentImpl(byte[] compressedFile, int hashCode, URI uri, DocumentStore.CompressionFormat format, HashMap<String,Integer> hashMap) {
        this.compressedFile = compressedFile;
        this.hashCode = hashCode;
        this.uri = uri;
        this.format = format;
        this.words = hashMap;
    }

    @Override
    public String toString() {
        return "DocumentImpl{" +
                "compressedFile=" + Arrays.toString(compressedFile) +
                ", hashCode=" + hashCode +
                ", uri=" + uri +
                ", format=" + format +
                '}';
    }
    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return
     */
    @Override
    public int wordCount(String word) {
        word = fixKey(word);
        Integer result = this.words.get(word);
        if (result == null) {
            return 0;
        }
        return result;
    }

    protected String fixKey(String key) {
        if(key == null || key.equals("")) {
            throw new IllegalArgumentException("Key cannot be empty.");
        }
        key = key.trim();
        if(key.contains(" ")) {
            throw new IllegalArgumentException("Key must be a single word.");
        }
        key = key.replaceAll("[\\p{P}\\p{S}0-9]","");
        if(key.equals("")) {
            throw new IllegalArgumentException("Key must be a word.");
        }
        key = key.toLowerCase();
        return key;
    }

    public HashMap<String,Integer> getWords() {
        return this.words;
    }

    @Override
    public byte[] getDocument() {
        return this.compressedFile;
    }

    @Override
    public int getDocumentHashCode() {
        return this.hashCode;
    }

    @Override
    public URI getKey() {
        return this.uri;
    }

    @Override
    public DocumentStore.CompressionFormat getCompressionFormat() {
        return this.format;
    }
}
