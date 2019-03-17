package edu.yu.cs.com1320.project;

import org.junit.Test;

import static org.junit.Assert.*;

public class HashTableImplTest {


    @Test
    public void getFirstNode() {
        HashTableImpl<Integer, Integer> testHash = new HashTableImpl<>(3);
        testHash.put(9, 999);
        testHash.put(3, 3);
        assertEquals(new Integer(999), testHash.get(9));
    }

    @Test
    public void getSecondNode() {
        HashTableImpl<Integer, Integer> testHash = new HashTableImpl<>(3);
        testHash.put(9, 999);
        testHash.put(3, 333);
        assertEquals(new Integer(333), testHash.get(3));
    }

    @Test
    public void getNull() {
        HashTableImpl<Integer, Integer> testHash = new HashTableImpl<>(3);
        testHash.put(9, 999);
        testHash.put(3, 3);
        assertNull(testHash.get(0));
    }

    @Test
    public void putNew() {
        HashTableImpl<Integer, Integer> testHash = new HashTableImpl<>(3);
        assertNull(testHash.put(9, 999));
    }

    @Test
    public void putOverwrite() {
        HashTableImpl<Integer, Integer> testHash = new HashTableImpl<>(3);
        testHash.put(9, 999);
        assertEquals(new Integer(999), testHash.put(9, 333));

    }

    @Test
    public void getBigSearch() {
        HashTableImpl<Integer, Integer> testHash = new HashTableImpl<>(3);
        for (int i = 0; i < 1000; i++) {
            testHash.put(i, (int) Math.pow(i, 2));
        }
        System.out.println("BigSearch: " + testHash);
        assertEquals(new Integer((int) Math.pow(100, 2)), testHash.get(100));
    }

    @Test
    public void deleteAll() {
        HashTableImpl<Integer, Integer> testHash = new HashTableImpl<>();
        for (int i = 0; i < 400; i++) {
            testHash.put(i, i);
        }
        for (int i = 399; i >= 0; i--) {
            assertEquals(new Integer(i), testHash.put(i, null));
        }
        System.out.println("DeleteAll: " + testHash);
        assertNull(testHash.get((int) (Math.random() * 399)));
    }


}