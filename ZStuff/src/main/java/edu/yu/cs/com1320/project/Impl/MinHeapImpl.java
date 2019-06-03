package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable> extends MinHeap<E>{


    @SuppressWarnings("unchecked")
    public MinHeapImpl() {
        this.elements = (E[]) new Comparable[10];
        this.count = 0;
        this.elementsToArrayIndex = new HashMap<>();
    }

    @Override
    public void reHeapify(E element) {
        int k = getArrayIndex(element);
        if(k==-1) return;
        downHeap(k);
        upHeap(k);
    }

    /**
     *
     * @param element
     * @return if element is in the Heap, return the element's ArrayIndex. If it's not in the heap, return -1
     */
    @Override
    protected int getArrayIndex(E element) {
        Integer index = this.elementsToArrayIndex.get(element);
        if(index == null) index = -1;
        return index;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doubleArraySize() {
        E[] temp = (E[]) new Comparable[this.elements.length*2];
        for(int i = 1; i <= this.elements.length;i++) {
            temp[i] = elements[i];
        }
        this.elements = temp;
    }

    @Override
    protected  boolean isEmpty()
    {
        return this.count == 0;
    }
    //Override the following methods to handle the elementsToArrayIndex Map logic
    /**
     * swap the values stored at elements[i] and elements[j]
     */
    @Override
    @SuppressWarnings("unchecked")
    protected  void swap(int i, int j)
    {
        E temp = this.elements[i];
        this.elements[i] = this.elements[j];
        this.elements[j] = temp;
        elementsToArrayIndex.put(elements[i],i);
        elementsToArrayIndex.put(elements[j],j);
    }

    @Override
    public void insert(E x)
    {
        // double size of array if necessary
        if (this.count >= this.elements.length - 1)
        {
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        this.elements[++this.count] = x;
        elementsToArrayIndex.put(x, this.count);
        //percolate it up to maintain heap order property
        this.upHeap(this.count);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E removeMin()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];
        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elements[this.count + 1] = null; //null it to prepare for GC
        this.elementsToArrayIndex.remove(min);
        return min;
    }

    /**
     * @return true iff this MinHeapImpl is a valid Min Heap
     */
    protected boolean isMinHeap() {
        return this.isMinHeap(1);
    }

    /**
     * @param i root of subtree
     * @return  true if the subtree of MinHeapImpl rooted at k is a min heap
     */
    private boolean isMinHeap(int i) {
        if(i > this.count) return true;
        int leftSubTree = 2*i;
        int rightSubTree = 2*i + 1;
        if(leftSubTree <= this.count && isGreater(i, leftSubTree)) return false;
        if(rightSubTree <= this.count && isGreater(i, rightSubTree)) return false;
        return isMinHeap(leftSubTree) && isMinHeap(rightSubTree);
    }
}
