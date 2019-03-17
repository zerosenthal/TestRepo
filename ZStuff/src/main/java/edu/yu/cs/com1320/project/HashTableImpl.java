package edu.yu.cs.com1320.project;

import java.util.Arrays;
import java.util.Objects;

/**
 * Instances of HashTableImpl should be constructed with two type parameters, one for the type of the keys in the table and one for the type of the values
 *
 * @param <Key>
 * @param <Value>
 * @author Zechariah-Rosenthal
 */
public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {

    //hashtable with separate chaining. Will only contain Node refs, but I'm bad at generics right now.
    private Object[] hTable;
    //length of hTable, should be prime for magic reasons
    private int m;
    private int n; //Number of elements in the HashTable
    private static final int DEFAULT_SIZE = 101;

    private class Node {
        private Key key;
        private Value value;
        Node next;

        Node(Key k, Value v) {
            this.key = k;
            this.value = v;
            this.next = null;
        }

        Key getKey() {
            return this.key;
        }

        Value getValue() {
            return this.value;
        }

        void setValue(Value v) {
            this.value = v;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            try {
                Node node = (Node) o;
                return getKey().equals(node.getKey()) && Objects.equals(getValue(), node.getValue());
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(getKey(), getValue());
        }

        @Override
        public String toString() {
            return "{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }
    }

    /**
     * @param size int that generates the size of the hashTable. Should be prime for magic reasons
     */
    public HashTableImpl(int size) {
        if (size < 1) {
            size = DEFAULT_SIZE;
        }
        this.hTable = new Object[size];
        this.m = size;
        this.n = 0;
    }

    public HashTableImpl() {
        this(DEFAULT_SIZE);
    }

    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     * @throws IllegalArgumentException if passed a null key
     */
    @SuppressWarnings("unchecked")
    public Value get(Key k) {
        if (k == null) {
            throw new IllegalArgumentException("Key to get cannot be null");
        }
        int index = this.hash(k);
        Node current = (Node) this.hTable[index];
        while (current != null) {
            if (current.getKey().equals(k)) {
                return current.getValue();
            }
            current = current.next;
        }
        return null;
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     * @throws IllegalArgumentException if passed a null key
     */
    @SuppressWarnings("unchecked")
    public Value put(Key k, Value v) {
        if (k == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (v == null) {
            return delete(k);
        }
        if (this.n > this.m * 5) {
            this.reHash();
        }

        int index = this.hash(k);
        Node current = (Node) this.hTable[index];
        if (current == null) {
            this.hTable[index] = new Node(k, v);
            n++;
            return null;
        }
        Node previous = current;
        while (current != null) {
            if (current.getKey().equals(k)) {
                Value oldVal = current.getValue();
                current.setValue(v);
                return oldVal;
            }
            previous = current;
            current = current.next;
        }
        previous.next = new Node(k, v);
        n++;
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        StringBuilder result = new StringBuilder("[");
        for (Object i : this.hTable) {
            Node n = (Node) i;
            if (n == null) {
                result.append("null, ");
            } else if (n.next == null) {
                result.append(n.toString() + ", ");
            } else {
                result.append("[");
                Node current = n;
                while (current.next != null) {
                    result.append(current.toString() + ", ");
                    current = current.next;
                }
                result.append(current.toString() + "], ");
            }
        }
        result.append("]");
        return result.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashTableImpl)) return false;
        HashTableImpl<Key, Value> that = (HashTableImpl<Key, Value>) o;
        return Arrays.equals(hTable, that.hTable);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hTable);
    }

    /**
     * @param k Key
     * @return int that's a valid index in the hash table
     */
    private int hash(Key k) {
        return Math.abs(k.hashCode() % this.m);
    }

    private void reHash() {
        HashTableImpl<Key, Value> temp = new HashTableImpl<>(this.m * 2);
        for (int i = 0; i < this.hTable.length; i++) {
            if (this.hTable[i] != null) {
                Node current = (Node) this.hTable[i];
                while (current != null) {
                    temp.put(current.getKey(), current.getValue());
                    current = current.next;
                }
            }
        }
        this.hTable = temp.hTable;
        this.m *= 2;
    }

    private Value delete(Key k) {
        int index = this.hash(k);
        Node current = (Node) this.hTable[index];
        Node previous = null;
        while (current != null) {
            if (current.getKey().equals(k)) {
                Value oldVal = current.getValue();
                if (previous == null) {
                    this.hTable[index] = current.next;
                } else {
                    previous.next = current.next;
                }
                current.next = null;
                return oldVal;
            }
            previous = current;
            current = current.next;
        }
        return null;
    }

}
