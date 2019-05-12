package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.MinHeap;
import org.junit.Test;

import static org.junit.Assert.*;

public class MinHeapImplTest {

    @Test
    public void isEmpty() {
        MinHeapImpl<Integer> intHeap = new MinHeapImpl<>();
        assert(intHeap.isEmpty());
    }


    @Test
    public void getArrayIndexByPrintLn() {
        MinHeapImpl<Integer> intHeap = new MinHeapImpl<>();
        intHeap.insert(5);
        System.out.println("First Insert:");
        System.out.println("{5:"+intHeap.getArrayIndex(5)+"}");
        intHeap.insert(4);
        System.out.println("Second Insert:");
        System.out.println("{4:"+intHeap.getArrayIndex(4)+"}");
        System.out.println("{5:"+intHeap.getArrayIndex(5)+"}");
        intHeap.insert(3);
        System.out.println("Third Insert:");
        System.out.println("{3:"+intHeap.getArrayIndex(3)+"}");
        System.out.println("{4:"+intHeap.getArrayIndex(4)+"}");
        System.out.println("{5:"+intHeap.getArrayIndex(5)+"}");
        intHeap.insert(1);
        System.out.println("Fourth Insert:");
        System.out.println("{1:"+intHeap.getArrayIndex(1)+"}");
        System.out.println("{3:"+intHeap.getArrayIndex(3)+"}");
        System.out.println("{4:"+intHeap.getArrayIndex(4)+"}");
        System.out.println("{5:"+intHeap.getArrayIndex(5)+"}");
    }

    @Test
    public void insert() {
        MinHeapImpl<Integer> intHeap = new MinHeapImpl<>();
        intHeap.insert(5);
        intHeap.insert(4);
        intHeap.insert(3);
        intHeap.insert(10);
        intHeap.insert(1);
        assert(intHeap.isMinHeap());
    }

    @Test
    public void removeMin1() {
        MinHeapImpl<Integer> intHeap = new MinHeapImpl<>();
        intHeap.insert(5);
        intHeap.insert(4);
        intHeap.insert(3);
        intHeap.insert(10);
        intHeap.insert(1);
        assertEquals(Integer.valueOf(1),intHeap.removeMin());
    }

    @Test
    public void removeMin2() {
        MinHeapImpl<Integer> intHeap = new MinHeapImpl<>();
        intHeap.insert(5);
        intHeap.insert(4);
        intHeap.insert(3);
        intHeap.insert(10);
        intHeap.insert(1);
        intHeap.removeMin();
        assertEquals(Integer.valueOf(3),intHeap.removeMin());
    }
}