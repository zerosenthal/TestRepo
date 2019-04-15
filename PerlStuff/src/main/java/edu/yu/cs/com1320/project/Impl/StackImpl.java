package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Stack;

/**
 * Project: stage2
 * StackImpl.java - class representing a generic stack implementation
 * Created 3/18/2019
 *
 * @author Elimelekh Perl
 */
public class StackImpl<T> implements Stack<T>
{
    protected StackNode head;
    protected int size;

    public StackImpl()
    {
        this.head = null;
        this.size = 0;
    }

    protected class StackNode
    {
        protected T data;
        protected StackNode next;

        public StackNode(T newData)
        {
            this.data = newData;
            this.next = null;
        }
    }

    /**
     * @param element object to add to the Stack
     */
    @Override
    public void push(T element)
    {
        StackNode newNode = new StackNode(element);

        newNode.next = this.head;
        this.head = newNode;

        this.size++;
    }

    /**
     * removes and returns element at the top of the stack
     *
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop()
    {
        if (this.head == null)
        {
            return null;
        }
        else
        {
            StackNode topNode = this.head;
            this.head = this.head.next;
            this.size--;

            return topNode.data;
        }
    }

    /**
     * @return the element at the top of the stack without removing it
     */
    @Override
    public T peek()
    {
        if (this.head != null)
        {
            return this.head.data;
        }
        return null;
    }

    /**
     * @return how many elements are currently in the stack
     */
    @Override
    public int size()
    {
        return this.size;
    }
}
