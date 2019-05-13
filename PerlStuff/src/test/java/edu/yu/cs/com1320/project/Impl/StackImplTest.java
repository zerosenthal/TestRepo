package edu.yu.cs.com1320.project.Impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Project: com1320_search
 * StackImplTest.java - class testing StackImpl
 * Created 3/18/2019
 * @see StackImpl
 *
 * @author Elimelekh Perl
 */
public class StackImplTest
{
    protected StackImpl<String> stack;

    @Before
    public void setUp() throws Exception
    {
        this.stack = new StackImpl<String>();

        this.stack.push("Elimelekh");
        this.stack.push("is");
        this.stack.push("name");
        this.stack.push("my");
        this.stack.push("Hello");
    }

    @Test
    public void testPop()
    {
        String word = stack.pop();

        assertEquals("testing pop value", "Hello", word);
        assertEquals("testing new head", "my", stack.head.data);
    }

    @Test
    public void testPeek()
    {
        String word = stack.peek();
        assertEquals("testing peek value", "Hello", word);
    }

    @Test
    public void testSize()
    {
        assertEquals("testing original size", 5, stack.size());

        stack.pop();

        assertEquals("testing post-pop size", 4, stack.size());

        stack.push("earthlings");
        stack.push("Greetings");

        assertEquals("testing post-push size", 6, stack.size());
    }
}