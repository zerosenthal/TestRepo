package edu.yu.cs.com1320.project.Impl;

/**
 * Project: stage5
 * QueueImpl.java - class representing an implementation of Queue
 * Created 5/29/2019
 *
 * @author Elimelekh Perl
 */
public class QueueImpl<T>
{
    private QueueNode front;
    private QueueNode back;
    protected int count;

    private class QueueNode
    {
        protected T value;
        protected QueueNode next;

        public QueueNode(T val)
        {
            this.value = val;
            this.next = null;
        }
    }
    public QueueImpl()
    {
        this.front = null;
        this.back = null;
        this.count = 0;
    }

    public void enqueue(T val)
    {
        QueueNode newNode = new QueueNode(val);

        if (this.count == 0)
        {
            this.front = this.back = newNode;
        }

        else
        {
            this.back.next = newNode;
            this.back = newNode;
        }

        this.count++;
    }

    public T dequeue()
    {
        if (this.count == 0)
        {return null;}

        else
        {
            T temp = this.front.value;
            this.front = this.front.next;
            return temp;
        }
    }

    public T peek()
    {
        if (this.count == 0)
        {return null;}

        else
        {
            return this.front.value;
        }
    }
}
