package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Project: stage3
 * TrieImpl.java - class representing a Trie
 * Created 4/4/2019
 * @see TooSimpleTrie
 *
 * @author Elimelekh Perl
 */
public class TrieImpl<Value> implements Trie<Value>
{
    protected static final int alphabetSize = 256; // extended ASCII
    protected TrieNode root;
    protected Comparator<Value> valueComparator;

    public TrieImpl(Comparator<Value> comparator)
    {
        this.root = new TrieNode();
        this.valueComparator = comparator;
    }

    protected static class TrieNode<Value>
    {
        private List<Value> valueList;
        protected TrieNode[] links = new TrieNode[alphabetSize];

        public TrieNode()
        {
            this.valueList = null;
        }

        protected void addValue(Value val)
        {
            if (this.valueList == null)
            {
                this.valueList = new ArrayList<Value>();
            }

            if (this.valueList.contains(val))
            {
                this.valueList.set(this.valueList.indexOf(val), val);
            }
            else
            {
                this.valueList.add(val);
            }
        }

        protected void removeValue(Value val) { this.valueList.remove(val); }

        protected int listSize() { return this.valueList.size(); }
    }

    /**
     * get all matches for the given key, sorted in descending order.
     * Search is CASE INSENSITIVE.
     *
     * @param key
     * @return
     */
    @Override
    public List getAllSorted(String key)
    {
        TrieNode node = this.get(this.root, key, 0);

        if (node == null || node.valueList == null)
        {
            return new ArrayList<Value>();
        }

        node.valueList.sort(this.valueComparator);

        return node.valueList;
    }

    /**
     * @param node
     * @param key
     * @param d
     *
     * @return
     */
    private TrieNode get(TrieNode node, String key, int d)
    {
        //link was null - return null, indicating a miss
        if (node == null)
        {
            return null;
        }
        //we've reached the last node in the key,
        //return the node
        if (d == key.length())
        {
            return node;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        return this.get(node.links[c], key, d + 1);
    }

    /**
     * add the given value at the given key
     *
     * @param key
     * @param val
     */
    @Override
    public void put(String key, Value val)
    {
        //deleteAll the value from this key
        if (val == null)
        {
            this.deleteAll(key);
        }
        else
        {
            this.root = put(this.root, key, val, 0);
        }
    }

    /**
     *
     * @param currentNode
     * @param key
     * @param val
     * @param d
     * @return root
     */
    private TrieNode put(TrieNode currentNode, String key, Value val, int d)
    {
        //create a new node
        if (currentNode == null)
        {
            currentNode = new TrieNode();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length())
        {
            currentNode.addValue(val);
            return currentNode;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        currentNode.links[c] = this.put(currentNode.links[c], key, val, d + 1);

        return currentNode;
    }

    /**
     * delete ALL matches for the given key
     *
     * @param key
     */
    @Override
    public void deleteAll(String key)
    {
        this.root = deleteAll(this.root, key, 0);
    }

    /**
     *
     * @param currentNode
     * @param key
     * @param d
     * @return root
     */
    private TrieNode deleteAll(TrieNode currentNode, String key, int d)
    {
        if (currentNode == null)
        {
            return null;
        }
        //we're at the node to del - clear value collection
        if (d == key.length())
        {
            currentNode.valueList = null;
        }
        //continue down the trie to the target node
        else
        {
            char c = key.charAt(d);
            currentNode.links[c] = this.deleteAll(currentNode.links[c], key, d + 1);
        }
        //this node has a val – do nothing, return the node
        if (currentNode.valueList != null)
        {
            return currentNode;
        }
        //remove subtrie rooted at currentNode if it is completely empty
        for (int c = 0; c <alphabetSize; c++)
        {
            if (currentNode.links[c] != null)
            {
                return currentNode; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }

    /**
     * delete ONLY the given value from the given key. Leave all other values.
     *
     * @param key
     * @param val
     */
    @Override
    public void delete(String key, Value val)
    {
        this.root = delete(this.root, key, val, 0);
    }

    /**
     *
     * @param currentNode
     * @param key
     * @param d
     * @return root
     */
    private TrieNode delete(TrieNode currentNode, String key, Value val, int d)
    {
        if (currentNode == null)
        {
            return null;
        }
        //we're at the node to del - remove specified val
        if (d == key.length())
        {
            //if del makes list empty, use deleteAll logic to clear empty subtries and parents
            if (currentNode.listSize() == 1)
            {
                this.deleteAll(key);
            }
            else
            {
                currentNode.removeValue(val);
            }
        }
        //continue down the trie to the target node
        else
        {
            char c = key.charAt(d);
            currentNode.links[c] = this.delete(currentNode.links[c], key, val, d + 1);
        }

        return currentNode;
    }
}
