package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T>{
    private StackNode head;
    private int size;

    private class StackNode {
        private T value;
        protected StackNode next;

        public StackNode(T val) {
            this.value = val;
            this.next = null;
        }

        public T getValue() {return this.value;}

        @Override
        public String toString() {
            return "StackNode{" +
                    "value=" + value +
                    '}';
        }
    }

    public StackImpl() {
        this.head = null;
        this.size = 0;
    }
    /**
     * @param element object to add to the Stack
     */
    @Override
    public void push(T element) {
        StackNode toAdd = new StackNode(element);
        toAdd.next = this.head;
        this.head = toAdd;
        size++;
    }

    /**
     * removes and returns element at the top of the stack
     *
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop() {
        if (this.head == null) { return null;}
        else {
            StackNode toPop = this.head;
            this.head = this.head.next;
            toPop.next = null;
            size--;
            return toPop.getValue();
        }
    }

    /**
     * @return the element at the top of the stack without removing it
     */
    @Override
    public T peek() {
        if (head == null) {
            return null;
        }
        return head.getValue();
    }

    /**
     * @return how many elements are currently in the stack
     */
    @Override
    public int size() {
        return this.size;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[head: ");
        StackNode current = head;
        while (current != null) {
            result.append(current.toString() + ", ");
            current = current.next;
        }
        result.append("]");
        return result.toString();
    }
}
