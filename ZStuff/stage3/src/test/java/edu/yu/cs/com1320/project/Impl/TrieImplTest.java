package edu.yu.cs.com1320.project.Impl;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.Assert.*;

public class TrieImplTest {

    private Comparator<Integer> intComp = (Integer a, Integer b) -> {
        if(a>b) return 1;
        if(a<b) return -1;
        return 0;
    };

    @Test
    public void fixKeyTrim() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        assertEquals(testTrie.fixKey("   test   "),"test");
    }
    @Test
    public void fixKeytoLowerCase() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        assertEquals(testTrie.fixKey("TEST"),"test");
    }
    @Test
    public void fixKeySymbol() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        assertEquals(testTrie.fixKey("@t..e,s`~&-t"),"test");
    }
    @Test
    public void fixKeyNumbers() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        assertEquals(testTrie.fixKey("123tes432352t94589"),"test");
    }
    @Test(expected = IllegalArgumentException.class)
    public void fixKeyNull() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        testTrie.fixKey(null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void fixKeyTwoWords() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        testTrie.fixKey("Test test");
    }
    @Test(expected = IllegalArgumentException.class)
    public void fixKeyEmpty() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        testTrie.fixKey("");
    }
    @Test(expected = IllegalArgumentException.class)
    public void fixKeyAllSymbols() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        testTrie.fixKey(")(*&^%$#@!");
    }

    @Test
    public void getAllSorted() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        Integer a = (10);
        Integer b = (20);
        Integer c = (5);
        String key = "secret";
        testTrie.put(key, a);
        testTrie.put(key, b);
        testTrie.put(key, c);

        System.out.println(testTrie.getAllSorted(key));
    }

    @Test
    public void put() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        Integer a = (10);
        Integer b = (20);
        String key = "secret";
        testTrie.put(key, a);
        testTrie.put(key, b);
        ArrayList<Integer> testList = new ArrayList<>();
        testList.add(b);
        testList.add(a);
        testList.sort(intComp);
        System.out.println(testList);
        assertEquals(testList, testTrie.getAllSorted(key));
    }

    @Test
    public void putWeirdKey() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        Integer a = (10);
        Integer b = (20);
        String key = "ZZAAMM";
        testTrie.put(key, a);
        testTrie.put(key, b);
        ArrayList<Integer> testList = new ArrayList<>();
        testList.add(b);
        testList.add(a);
        testList.sort(intComp);
        System.out.println(testList);
        assertEquals(testList, testTrie.getAllSorted(key));
    }

    @Test
    public void deleteAll1() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        Integer a = (10);
        Integer b = (20);
        Integer c = (5);
        String key = "secret";
        testTrie.put(key, a);
        testTrie.put("secretss", b);
        testTrie.put(key, c);
        testTrie.deleteAll(key);
        assertNull(testTrie.getAllSorted(key));
    }

    @Test
    public void deleteAll2() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        Integer a = (10);
        Integer b = (20);
        Integer c = (5);
        String key = "secret";
        String key2 = "secretss";
        testTrie.put(key, a);
        testTrie.put(key2, b);
        testTrie.put(key, c);
        testTrie.deleteAll(key);
        assertNotNull(testTrie.getAllSorted(key2));
    }

    @Test
    public void delete() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        Integer a = (10);
        Integer b = (20);
        String key = "secret";
        testTrie.put(key, a);
        testTrie.put(key, b);

        testTrie.delete(key,b);
        assertEquals(a, testTrie.getAllSorted(key).get(0));
    }

    @Test
    public void delete2() {
        TrieImpl<Integer> testTrie = new TrieImpl<>(intComp);
        Integer a = (10);
        Integer b = (20);
        String key = "secret";
        testTrie.put(key, a);
        testTrie.put(key, b);

        testTrie.delete(key,b);
        testTrie.delete(key,a);
        assertNull(key, testTrie.getAllSorted(key));
    }

}