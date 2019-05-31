package edu.yu.cs.com1320.project.Impl;


import edu.yu.cs.com1320.project.MinHeap;

import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Project: stage4
 * MinHeapImpl.java - class representing full implementation of MinHeap
 * Created 4/19/2019
 *
 * @author Elimelekh Perl
 */
public class MinHeapImpl<E extends Comparable> extends MinHeap<E>
{
    public MinHeapImpl()
    {
        super.elements = (E[]) new Comparable[3];
        super.count = 0;
        super.elementsToArrayIndex = new HashMap<E, Integer>();
    }

    @Override
    public void reHeapify(E element)
    {
        int elementIndex = getArrayIndex(element);
        downHeap(elementIndex);
        upHeap(elementIndex);
    }

    protected int getArrayIndex(E element)
    {
        if (super.elementsToArrayIndex.containsKey(element))
        {
            return (int) super.elementsToArrayIndex.get(element);
        }

        else
        {
            return -1;
        }
    }

    @Override
    protected void doubleArraySize()
    {
        E[] tempArray = super.elements;

        super.elements = (E[]) new Comparable[tempArray.length * 2];

        int i = 0;
        for (E element: tempArray)
        {
            super.elements[i] = element;
            i++;
        }
    }

    /**
     * is elements[i] > elements[j]?
     */
    @Override
    protected  boolean isGreater(int i, int j)
    {
        if (super.elements[j] == null)
        {
            return false;
        }

        return super.elements[i].compareTo(super.elements[j]) > 0;
    }

    /**
     * swap the values stored at elements[i] and elements[j]
     */
    @Override
    protected  void swap(int i, int j)
    {
        E temp = (E) super.elements[i];
        super.elements[i] = super.elements[j];
        super.elementsToArrayIndex.put(super.elements[i], i);
        super.elements[j] = temp;
        super.elementsToArrayIndex.put(super.elements[j], j);
    }

    /**
     *while the key at index k is less than its
     *parent's key, swap its contents with its parent’s
     */
    @Override
    protected  void upHeap(int k)
    {
        while (k > 1 && this.isGreater(k / 2, k))
        {
            this.swap(k, k / 2);
            k = k / 2;
        }
    }

    /**
     * move an element down the heap until it is less than
     * both its children or is at the bottom of the heap
     */
    @Override
    protected void downHeap(int k)
    {
        while (2 * k <= super.count)
        {
            //identify which of the 2 children are smaller
            int j = 2 * k;
            if (j < super.count && this.isGreater(j, j + 1))
            {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j))
            {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }
    }

    @Override
    public void insert(E x)
    {
        // double size of array if necessary
        if (super.count >= super.elements.length - 1)
        {
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        super.elements[++super.count] = x;

        super.elementsToArrayIndex.put(x, super.count);

        //percolate it up to maintain heap order property
        this.upHeap(super.count);
    }

    @Override
    public E removeMin()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = (E) super.elements[1];
        //swap root with last, decrement count
        this.swap(1, super.count--);
        //move new root down as needed
        this.downHeap(1);
        super.elements[super.count + 1] = null; //null it to prepare for GC
        super.elementsToArrayIndex.remove(min);
        return min;
    }


    private boolean hasNext(int elementIndex)
    {
        return ((elementIndex + 1 < elements.length) && (elements[elementIndex + 1] != null));
    }

}
