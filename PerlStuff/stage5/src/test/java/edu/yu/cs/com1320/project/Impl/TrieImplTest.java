package edu.yu.cs.com1320.project.Impl;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Project: stage3
 * TrieImplTest.java
 * Created 4/5/2019
 * @see TrieImpl
 *
 * @author Elimelekh Perl
 */
public class TrieImplTest
{
    private TrieImpl<Integer> intTrie;

    @Before
    public void setUp() throws Exception
    {
        Comparator<Integer> integerComparator = new Comparator<Integer>()
        {
            @Override
            public int compare(Integer o1, Integer o2)
            {
                return o1.compareTo(o2);
            }
        };
        this.intTrie = new TrieImpl(integerComparator);

        this.intTrie.put("Eli", 10);
        this.intTrie.put("Eli", 10);
        this.intTrie.put("Eli", 11);
        this.intTrie.put("Eli", 9);
        this.intTrie.put("Elimelekh", 12);
        this.intTrie.put("Elimelekh", 2);
        this.intTrie.put("Russ", 3);
        this.intTrie.put("Russ", 202020);
        this.intTrie.put("Russel", 0);
        this.intTrie.put("Russia", 1992);
    }

    @Test
    public void testGetAllSorted()
    {
        List<Integer> testList = new ArrayList<Integer>(Arrays.asList(9,10,11));

        assertEquals("testing getAllSorted()", testList, this.intTrie.getAllSorted("Eli"));
    }


    @Test
    public void testDeleteAllLeaf()
    {
        this.intTrie.deleteAll("Elimelekh");

        assertEquals("testing deleteAll for leaf node", new ArrayList(), this.intTrie.getAllSorted("Elimelekh"));
        assertEquals("testing deleteAll for leaf node", new ArrayList(), this.intTrie.getAllSorted("Elim"));
    }

    @Test
    public void testDeleteAllParent()
    {
        this.intTrie.deleteAll("Eli");

        assertEquals("testing deleteAll for parent node", new ArrayList(), this.intTrie.getAllSorted("Eli"));
        assertEquals("testing deleteAll for parent node", new ArrayList<Integer>(Arrays.asList(2, 12)), this.intTrie.getAllSorted("Elimelekh"));
    }

    @Test
    public void testDelete()
    {
        this.intTrie.delete("Russ", 202020);

        assertEquals("testing specific delete", new ArrayList<Integer>(Arrays.asList(3)), this.intTrie.getAllSorted("Russ"));
        assertEquals("testing specific delete", new ArrayList<Integer>(Arrays.asList(1992)), this.intTrie.getAllSorted("Russia"));
    }

    @Test
    public void testDeleteLoneLeaf()
    {
        this.intTrie.delete("Russel", 0);

        assertEquals("testing specific delete for leaf", new ArrayList(), this.intTrie.getAllSorted("Russel"));
        assertEquals("testing specific delete for leaf", new ArrayList(), this.intTrie.getAllSorted("Russe"));
    }
}