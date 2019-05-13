package edu.yu.cs.com1320.project.Impl;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

/**
 * Project: stage4
 * MinHeapImplTest.java
 * Created 4/19/2019
 * @see MinHeapImpl
 *
 * @author Elimelekh Perl
 */
public class MinHeapImplTest
{
    private MinHeapImpl<IntObject> minHeap;
    IntObject int1;
    IntObject int2;
    IntObject int3;
    IntObject int4;
    IntObject int5;
    IntObject int6;
    IntObject int7;

    private class IntObject implements Comparable
    {
        private Integer integer;

        public IntObject(int newInt)
        {
            this.integer = newInt;
        }

        public Integer getInt()
        {
            return this.integer;
        }
        public void setInt(int newInt)
        {
            this.integer = newInt;
        }

        @Override
        public int compareTo(@NotNull Object o)
        {
            if (o.getClass() == IntObject.class)
            {
                IntObject oInt = (IntObject) o;
                return this.integer.compareTo(oInt.getInt());
            }
            else
            {
                return 0;
            }
        }
    }

    @Before
    public void setUp()
    {
        this.minHeap = new MinHeapImpl<IntObject>();

        this.int1 = new IntObject(2);
        this.int2 = new IntObject(16);
        this.int3 = new IntObject(5);
        this.int4 = new IntObject(20);
        this.int5 = new IntObject(13);
        this.int6 = new IntObject(1);
        this.int7 = new IntObject(7);

        this.minHeap.insert(int1);
        this.minHeap.insert(int2);
        this.minHeap.insert(int3);
        this.minHeap.insert(int4);
        this.minHeap.insert(int5);
        this.minHeap.insert(int6);
        this.minHeap.insert(int7);
    }

    @Test
    public void testReHeapify()
    {
        this.int7.setInt(3);

        this.minHeap.reHeapify(this.int7);
    }


    @Test
    public void testRemoveMin()
    {
        assertEquals("testing removeMin", int6, this.minHeap.removeMin());
    }
}