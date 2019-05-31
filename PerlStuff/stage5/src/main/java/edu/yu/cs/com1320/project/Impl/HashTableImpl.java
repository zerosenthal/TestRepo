package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.HashTable;

/**
 * Project: stage1
 * HashTableImpl.java - class representing HashTable implementation
 * Created 3/4/2019
 * @author Elimelekh Perl
 */

public class HashTableImpl<Key, Value> implements HashTable<Key, Value>
{
   private static final int DEFAULT_SIZE = 3;

   protected Object[] array;
   protected int size;
   protected int numNodes;
   protected int loadFac;

    /**
     * default no-arg constructor
     * instantiates instance of HashTable to DEFAULT_SIZE
     */
   public HashTableImpl()
   {
       this.array = new Object[DEFAULT_SIZE];
       this.size = DEFAULT_SIZE;
   }


   /**
     *class representing node placed in HashTable
     */
   private class HashNode
   {
       private Key key;
       private Value value;
       private HashNode next;

       /**
        * constructor
        * @param newKey of type Key
        * @param newValue of type Value
        */
       private HashNode(Key newKey, Value newValue)
       {
           this.key = newKey;
           this.value = newValue;
           this.next = null;
       }

       @Override
       public boolean equals(Object that)
       {
           if (this == that)
           {
               return true;
           }

           if (that == null || getClass() != that.getClass())
           {
               return false;
           }

           HashNode hashNode = (HashNode)that;

           return (this.key.equals(hashNode.key) && this.value.equals(hashNode.value));
       }

       @Override
       public int hashCode()
       {
           return this.key.hashCode();
       }
   }

    /**
     * hash function for independent key
     * @param k of type Key
     * @return int hashed index
     */
   private int hash(Key k)
    {
        if (k.getClass().getName().equals("Integer"))
        {
            return Math.abs((Integer)k) % this.size;
        }

        else
        {
            return Math.abs(k.hashCode()) % this.size;
        }
    }

    /**
     * hash function for HashNode instance
     * @param node of type HashNode
     * @return int hashed index
     */
    private int hash(HashNode node)
    {
        return hash(node.key);
    }


    private void doubleArray()
    {
        Object[] tempArray = this.array;

        this.size = (tempArray.length * 2) + 1;
        this.array = new Object[this.size];

        for (Object node: tempArray)
        {
            HashNode current = (HashNode)node;

            while(current != null)
            {
                HashNode temp = current.next;
                this.insert(current);
                current.next = null;
                current = temp;
            }
        }
    }

    /**
     * @param k the key whose value should be returned
     * @throws IllegalArgumentException for null key input
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    public Value get(Key k)
    {
        if (k == null)
        {
            throw new IllegalArgumentException("ERROR: null key");
        }

        HashNode current = (HashNode)this.array[hash(k)];

        while (current != null)
        {
            if (k.equals(current.key))
            {
                return current.value;
            }

            current = current.next;
        }

        return null;
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store
     * @throws IllegalArgumentException for null key input
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    public Value put(Key k, Value v)
    {
        if (k == null)
        {
            throw new IllegalArgumentException("ERROR: null key");
        }

        HashNode newNode = new HashNode(k, v);

        if (v == null)
        {
            return this.delete(newNode);
        }

        if (this.get(k) == null)
        {
            this.numNodes++;
            this.loadFac = this.numNodes / this.size;

            if (loadFac >= 4)
            {
                this.doubleArray();
            }
        }

        return this.insert(newNode);
    }

    private Value insert (HashNode newNode)
    {
        HashNode current = (HashNode)this.array[hash(newNode.key)];

        if (null == current)
        {
            this.array[hash(newNode.key)] = newNode;

            return null;
        }

        else if (newNode.key.equals(current.key))
        {
            newNode.next = current.next;
            this.array[hash(newNode.key)] = newNode;

            return current.value;
        }

        else
        {
            while (current.next != null)
            {
                if (newNode.key.equals(current.next.key))
                {
                    Value temp = current.next.value;
                    current.next.value = newNode.value;

                    return temp;
                }

                current = current.next;
            }

            current.next = newNode;

            return null;
        }
    }

    private Value delete(HashNode node)
    {
        HashNode current = (HashNode)this.array[hash(node.key)];

        if (null == current)
        {
            return null;
        }

        else if (node.key.equals(current.key))
        {
            this.array[hash(node.key)] = current.next;

            return current.value;
        }

        else
        {
            while (current.next != null)
            {
                if (node.key.equals(current.next.key))
                {
                    Value temp = current.next.value;
                    current.next = current.next.next;
                    this.numNodes--;

                    return temp;
                }

                current = current.next;
            }
            return null;
        }
    }

    /**
     * @param k of type Key
     * @param v of type Key
     * @return boolean denoting if the key-value pair is present in the HashTable
     */
    public boolean contains(Key k, Value v)
    {
        if (k == null)
        {
            throw new IllegalArgumentException("ERROR: null key");
        }

        HashNode newNode = new HashNode(k,v);

        HashNode current = (HashNode)this.array[hash(k)];

        while (current != null)
        {
            if (newNode.equals(current))
            {
                return true;
            }

            current = current.next;
        }

        return false;
    }
}
