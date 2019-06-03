package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * @author Zechariah-Rosenthal
 */
public class DocumentImpl implements Document {
    private byte[] compressedFile;
    private int hashCode;
    private URI uri;
    private DocumentStore.CompressionFormat format;
    private HashMap<String,Integer> words;
    private long lastUsedTime;

    public DocumentImpl(byte[] compressedFile, int hashCode, URI uri, DocumentStore.CompressionFormat format, HashMap<String,Integer> hashMap) {
        this.compressedFile = compressedFile;
        this.hashCode = Math.abs(hashCode);
        this.uri = uri;
        this.format = format;
        this.words = hashMap;
        this.lastUsedTime = System.currentTimeMillis();
    }

    @Override
    public Map<String, Integer> getWordMap() {
        return this.words;
    }

    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        if(wordMap != null) this.words = (HashMap<String, Integer>) wordMap;
    }

    @Override
    public long getLastUsedTime() {
        return lastUsedTime;
    }

    @Override
    public void setLastUsedTime(long timeInMilliseconds) {
        this.lastUsedTime = timeInMilliseconds;
    }


    @Override
    public int compareTo(Document o) {
        if (o == null) throw new NullPointerException();
        if (this.getLastUsedTime() < o.getLastUsedTime()) {
            return -1;
        } else {
            return 1;
        }
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

    protected HashMap<String,Integer> getWords() {
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
    public String toString() {
        return "DocumentImpl{" +
                "compressedFile=" + Arrays.toString(compressedFile) +
                ", hashCode=" + hashCode +
                ", uri=" + uri +
                ", format=" + format +
                ", words=" + words +
                ", lastUsedTime=" + lastUsedTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentImpl)) return false;
        DocumentImpl document = (DocumentImpl) o;
        return hashCode == document.hashCode &&
                Arrays.equals(compressedFile, document.compressedFile) &&
                Objects.equals(uri, document.uri) &&
                format == document.format &&
                Objects.equals(getWords(), document.getWords());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(hashCode, uri, format, getWords());
        result = 31 * result + Arrays.hashCode(compressedFile);
        return result;
    }

    @Override
    public DocumentStore.CompressionFormat getCompressionFormat() {
        return this.format;
    }
}
