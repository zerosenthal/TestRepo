package edu.yu.cs.com1320.project.Impl;

import org.junit.Test;

import static org.junit.Assert.*;

public class StackImplTest {

    @Test
    public void push() {
        StackImpl<Integer> stack = new StackImpl<>();
        stack.push(1);
        stack.push(null);
    }

    @Test
    public void pop() {
        StackImpl<Integer> stack = new StackImpl<>();
        for (int i = 1; i < 11; i++) {
            stack.push(613);
        }
        while(stack.size() != 0) {
            assertEquals(Integer.valueOf(613), stack.pop());
        }

    }

    @Test
    public void peek() {
        StackImpl<Integer> stack = new StackImpl<>();
        stack.push(1);
        assertEquals(Integer.valueOf(1),stack.peek());
    }

    @Test
    public void size() {
        StackImpl<Integer> stack = new StackImpl<>();
        for (int i = 0; i < 10000; i++) {
            stack.push(Integer.valueOf(i));
        }
//        System.out.println(stack);
        assertTrue(stack.size() == 10000);
    }

    @Test
    public void popEmpty() {
        StackImpl<Integer> stack = new StackImpl<>();
        assertNull(stack.pop());
    }

    @Test
    public void peekEmpty() {
        StackImpl<Integer> stack = new StackImpl<>();
        assertNull(stack.peek());
    }
    @Test
    public void sizeEmpty() {
        StackImpl<Integer> stack = new StackImpl<>();
        assertTrue(stack.size() == 0);
    }
}