package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Custom Generic Stack (LIFO - Last In First Out) Data Structure.
 *
 * Designed and implemented for the PDSA (Programming, Data Structures & Algorithms) Module.
 * Uses a dynamic singly-linked node chain ensuring O(1) time complexity for push, pop, and peek operations.
 *
 * @param <T> The element type stored in this stack.
 */
public class CustomStack<T> implements Iterable<T>, Serializable {

    private static class Node<E> implements Serializable {
        private final E data;
        private final Node<E> next;

        Node(E data, Node<E> next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node<T> top;
    private int size;

    public CustomStack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Pushes an item onto the top of this stack.
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     *
     * @param item the item to be pushed onto this stack.
     */
    public void push(T item) {
        this.top = new Node<>(item, this.top);
        this.size++;
    }

    /**
     * Removes the object at the top of this stack and returns that object as the value of this function.
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     *
     * @return The object at the top of this stack.
     * @throws EmptyStackException if this stack is empty.
     */
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T data = this.top.data;
        this.top = this.top.next;
        this.size--;
        return data;
    }

    /**
     * Looks at the object at the top of this stack without removing it from the stack.
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     *
     * @return the object at the top of this stack.
     * @throws EmptyStackException if this stack is empty.
     */
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return this.top.data;
    }

    /**
     * Tests if this stack is empty.
     * Time Complexity: O(1)
     *
     * @return {@code true} if and only if this stack contains no items; {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this.top == null;
    }

    /**
     * Returns the number of elements in this stack.
     * Time Complexity: O(1)
     *
     * @return the number of elements in this stack.
     */
    public int size() {
        return this.size;
    }

    /**
     * Removes all elements from this stack.
     * Time Complexity: O(1)
     */
    public void clear() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Converts stack items into a List in Chronological (Bottom-to-Top / Root-to-Leaf) order.
     * Useful for constructing forward decision audit trails.
     * Time Complexity: O(N)
     *
     * @return List of elements in original push sequence.
     */
    public List<T> toChronologicalList() {
        List<T> list = new ArrayList<>(this.size);
        collectChronological(this.top, list);
        return list;
    }

    private void collectChronological(Node<T> current, List<T> list) {
        if (current == null) return;
        // Recurse to the bottom of the stack first (post-order / reverse LIFO)
        collectChronological(current.next, list);
        list.add(current.data);
    }

    /**
     * Converts stack items into a List in LIFO order (Top-to-Bottom).
     * Time Complexity: O(N)
     *
     * @return List of elements starting with the most recently pushed item.
     */
    public List<T> toTopDownList() {
        List<T> list = new ArrayList<>(this.size);
        Node<T> current = this.top;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> current = top;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more elements in stack iterator");
                }
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CustomStack[");
        Node<T> current = top;
        boolean first = true;
        while (current != null) {
            if (!first) sb.append(", ");
            sb.append(current.data);
            first = false;
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
