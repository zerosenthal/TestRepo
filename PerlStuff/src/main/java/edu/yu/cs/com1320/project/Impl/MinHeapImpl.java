package edu.yu.cs.com1320.project.Impl;


import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Project: stage4
 * MinHeapImpl.java - class representing full implementation of MinHeap
 * Created 4/19/2019
 *
 * @author Elimelekh Perl
 */
public class MinHeapImpl<E extends Comparable> extends MinHeap
{
    public MinHeapImpl()
    {
        super.elements = (E[]) new Comparable[3];
        super.count = 0;
        super.elementsToArrayIndex = new HashMap<E, Integer>();
    }

    @Override
    public void reHeapify(Comparable element)
    {
        int elementIndex = getArrayIndex(element);

        //if element is > its left child, downheap
        if ((elementIndex * 2 < elements.length) && (elements[elementIndex * 2] != null) && (isGreater(elementIndex, elementIndex * 2)))
        {
            downHeap(elementIndex);
        }

        //if parent is > element, upheap
        else if ((elementIndex != 1) && isGreater((int)Math.floor((elementIndex / 2.0)), elementIndex))
        {
            upHeap(elementIndex);
        }

        else
        {
            checkChildOrder(elementIndex);
        }
    }

    @Override
    protected int getArrayIndex(Comparable element)
    {
        return (int) super.elementsToArrayIndex.get(element);
    }

    @Override
    protected void doubleArraySize()
    {
        E[] tempArray = (E[]) super.elements;

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
            checkChildOrder(k);
            k = k / 2;
        }
        checkChildOrder(k);
    }

    /**
     * move an element down the heap until it is less than
     * both its children or is at the bottom of the heap
     */
    @Override
    protected  void downHeap(int k)
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
                checkChildOrder(k);
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            checkChildOrder(k);
            k = j;
        }
    }

    @Override
    public void insert(Comparable x)
    {
        // double size of array if necessary
        if (super.count >= super.elements.length - 1)
        {
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        super.elements[++super.count] = (E) x;

        super.elementsToArrayIndex.put((E) x, super.count);

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

    private void checkChildOrder(int elementIndex)
    {
        //if sibling is null, do nothing
        if (elementIndex + 1 < super.elements.length && super.elements[elementIndex + 1] == null)
        {
        }

        //if element isn't the root, is right child and is less than left child, swap
        else if ((elementIndex != 1) && (elementIndex % 2 != 0) && (isGreater(elementIndex - 1, elementIndex)))
        {
            swap(elementIndex - 1, elementIndex);
        }

        //if element is left child, there is a right child, and is greater than right child, swap
        else if ((elementIndex % 2 == 0) && (hasNext(elementIndex)) && (isGreater(elementIndex, elementIndex + 1)))
        {
            swap(elementIndex, elementIndex + 1);
        }
    }

    private boolean hasNext(int elementIndex)
    {
        return ((elementIndex + 1 < elements.length) && (elements[elementIndex + 1] != null));
    }

}