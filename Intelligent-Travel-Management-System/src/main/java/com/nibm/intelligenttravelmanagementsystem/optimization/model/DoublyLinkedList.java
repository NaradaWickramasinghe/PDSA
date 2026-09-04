package com.nibm.intelligenttravelmanagementsystem.optimization.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Generic Doubly Linked List implementation.
 * Used in the Optimization module for managing the Pareto-optimal frontier archive,
 * allowing O(1) removal of dominated candidates.
 *
 * Direct syllabus mapping: "Doubly linked list remove", "Linked lists stacks"
 *
 * @param <T> Element type
 */
public class DoublyLinkedList<T> {

    public static class Node<T> {
        public T data;
        public Node<T> prev;
        public Node<T> next;

        public Node(T data) {
            this.data = data;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public DoublyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Appends a new item to the end of the doubly linked list.
     * Time Complexity: O(1)
     */
    public Node<T> add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
        return newNode;
    }

    /**
     * Removes a specific node from the doubly linked list in O(1) time.
     * Syllabus topic: "Doubly linked list remove"
     */
    public boolean remove(Node<T> node) {
        if (node == null) return false;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

        node.prev = null;
        node.next = null;
        size--;
        return true;
    }

    /**
     * Searches and removes the first matching element.
     * Time Complexity: O(N)
     */
    public boolean remove(T data) {
        Node<T> current = head;
        while (current != null) {
            if ((data == null && current.data == null) || (data != null && data.equals(current.data))) {
                return remove(current);
            }
            current = current.next;
        }
        return false;
    }

    public Node<T> getHead() {
        return head;
    }

    public Node<T> getTail() {
        return tail;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Converts elements to a standard Java List.
     */
    public List<T> toList() {
        List<T> list = new ArrayList<>(size);
        Node<T> current = head;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }
}
