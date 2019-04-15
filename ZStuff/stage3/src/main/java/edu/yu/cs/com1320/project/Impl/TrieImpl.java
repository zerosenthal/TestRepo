package edu.yu.cs.com1320.project.Impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TrieImpl<Value> implements edu.yu.cs.com1320.project.Trie<Value> {

    private static final int ALPHABET_SIZE = 26;
    private TrieNode<Value> root;
    private Comparator<Value> comparator;

    @SuppressWarnings("unchecked")
    private class TrieNode<V> {
        TrieNode<V>[] children;
        ArrayList<V> value;
        public TrieNode() {
            this.children = new TrieNode[TrieImpl.ALPHABET_SIZE];
            this.value = null;
        }
    }

    public TrieImpl(Comparator<Value> comp) {
        this.root = new TrieNode<>();
        this.comparator = comp;
    }

    /**
     * get all matches for the given key, sorted in descending order.
     * Search is CASE INSENSITIVE.
     *
     * @param key
     * @return List<Value> if found, null if not found
     * @throws IllegalArgumentException if null or empty or multi-worded key.
     */
    @Override
    public List<Value> getAllSorted(String key) {
        key = fixKey(key);
        TrieNode<Value> found = this.get(this.root, key, 0);
        if (found == null || found.value == null) {
            return null; //if not found
        }
        ArrayList<Value> result = found.value;
        result.sort(this.comparator);
        return result;
    }

    private TrieNode<Value> get(TrieNode<Value> current, String key, int depth) {
        if (current == null) {
            return null;
        }
        if (depth == key.length()) {
            return current;
        }
        char c = key.charAt(depth);
        int childSlot = Character.getNumericValue(c) - 10;
        return this.get(current.children[childSlot], key, depth + 1);
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

    /**
     * add the given value at the given key
     *
     * @param key
     * @param val
     */
    @Override
    public void put(String key, Value val) {
        key = this.fixKey(key);
        if(val == null) {
            this.deleteAll(key);
        } else {
            this.root = put(this.root, key, val, 0);
        }
    }

    private TrieNode<Value> put(TrieNode<Value> current, String key, Value val, int depth) {
        if(current == null) {
            current = new TrieNode<>();
        }
        if(depth == key.length()) {
            if(current.value == null) {
                current.value = new ArrayList<>();
            }
            if(!current.value.contains(val)){
                current.value.add(val);
            }
            return current;
        }
        char c = key.charAt(depth);
        int childSlot = Character.getNumericValue(c) - 10;
        current.children[childSlot] = this.put(current.children[childSlot], key, val,depth + 1);
        return current;
    }

    /**
     * delete ALL matches for the given key
     *
     * @param key
     */
    @Override
    public void deleteAll(String key) {
        key = this.fixKey(key);
        this.deleteAll(this.root, key, 0);
    }

    private TrieNode<Value> deleteAll(TrieNode<Value> current, String key, int depth) {
        if(current == null) {
            return null;
        }
        if(depth == key.length()) {
            current.value = null;
        } else {
            char c = key.charAt(depth);
            int childSlot = Character.getNumericValue(c) - 10;
            current.children[childSlot] = this.deleteAll(current.children[childSlot], key, depth+1);
        }

        if(current.value != null) {
            return current;
        }
        for(int c =0; c< TrieImpl.ALPHABET_SIZE;c++) {
            if(current.children[c] != null) {
                return current;
            }
        }
        return null;
    }

    /**
     * delete ONLY the given value from the given key. Leave all other values.
     *
     * @param key
     * @param val
     */
    @Override
    public void delete(String key, Value val) {
        key = this.fixKey(key);
        TrieNode<Value> found = this.get(this.root, key, 0);
        if(found != null) {
            found.value.remove(val);
            if(found.value.isEmpty()) {
                found.value = null; //why u do dis
                this.deleteAll(key);
            }
        }
    }
}
