package edu.yu.cs.com1320.project.Impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Project: stage2
 * HashTableImplTest.java
 * Created 3/7/2019
 * @see HashTableImpl
 *
 * @author Elimelekh Perl
 */
public class HashTableImplTest
{
    Integer value1;
    Boolean value2;

    HashTableImpl<String, Integer> stringHashTable;
    HashTableImpl<Integer, Boolean> intHashTable;

    @Before
    public void setUp()
    {
        stringHashTable = new HashTableImpl<String, Integer>();
        value1 = stringHashTable.put("Bob", 20);
        value1 = stringHashTable.put("Josh", 13);
        value1 = stringHashTable.put("Maxwell", 103);
        value1 = stringHashTable.put("Mary", 16);
        value1 = stringHashTable.put("John", 42);
        value1 = stringHashTable.put("Jerry",5);

        intHashTable = new HashTableImpl<Integer, Boolean>();
        value2 = intHashTable.put(3, true);
        value2 = intHashTable.put(6, true);
        value2 = intHashTable.put(25676, false);
        value2 = intHashTable.put(10000, false);
    }

    @Test
    public void testDefaultConstructor()
    {
        HashTableImpl<String, Integer> defaultHashTable = new HashTableImpl<String, Integer>();

        assertTrue("testing default HashTable size", 3 == defaultHashTable.size);
    }

    @Test
    public void testGet()
    {
        value1 = stringHashTable.get("Maxwell");
        assertEquals("testing get String key unchained", (Integer)103, value1);

        value1 = stringHashTable.get("John");
        assertEquals("testing get String key chained", (Integer)42, value1);

        value1 = stringHashTable.get("Elimelekh");
        assertEquals("testing get String key nonexistent", null, value1);

        value2 = intHashTable.get(3);
        assertEquals("testing get Int key unchained", (Boolean)true, value2);

        value2 = intHashTable.get(25676);
        assertEquals("testing get Int key chained", (Boolean)false, value2);

        value2 = intHashTable.get(4);
        assertEquals("testing get Int key nonexistent", null, value2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullKeyGet()
    {
        value1 = stringHashTable.get(null);
    }

    @Test
    public void testPut()
    {
        value1 = stringHashTable.put("Odell", 13);
        assertEquals("testing String put empty bucket", null, value1);

        value1 = stringHashTable.put("Jhos", 25);
        assertEquals("testing String put full bucket", null, value1);

        value1 = stringHashTable.put("Bob", 11);
        assertEquals("testing String put replacement", (Integer)20, value1);

        value2 = intHashTable.put(4, true);
        assertEquals("testing Int put empty bucket", null, value2);

        value2 = intHashTable.put(10, false);
        assertEquals("testing Int put full bucket", null, value2);

        value2 = intHashTable.put(6, false);
        assertEquals("testing Int put replacement", true, value2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullKeyPut()
    {
        value1 = stringHashTable.put(null, 30);
    }

    @Test
    public void testArrayDouble()
    {
        for (int i = 0; i <= 200; i++)
        {
            value2 = intHashTable.put(i, true);
        }

        assertEquals("testing array doubling", 63, intHashTable.size);
    }
}