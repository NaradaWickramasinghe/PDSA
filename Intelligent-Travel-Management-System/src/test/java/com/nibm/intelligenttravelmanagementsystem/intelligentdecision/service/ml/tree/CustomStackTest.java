package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ml.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomStack (LIFO Data Structure) Unit Tests")
class CustomStackTest {

    private CustomStack<String> stack;

    @BeforeEach
    void setUp() {
        stack = new CustomStack<>();
    }

    @Test
    @DisplayName("New stack should be empty with size 0")
    void testNewStackIsEmpty() {
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @Test
    @DisplayName("Pushing elements should increase size and update top")
    void testPush() {
        stack.push("Rule 1: Budget <= 800");
        assertFalse(stack.isEmpty());
        assertEquals(1, stack.size());
        assertEquals("Rule 1: Budget <= 800", stack.peek());

        stack.push("Rule 2: Beach > 3.5");
        assertEquals(2, stack.size());
        assertEquals("Rule 2: Beach > 3.5", stack.peek());
    }

    @Test
    @DisplayName("Pop should remove and return elements in LIFO (Last In First Out) order")
    void testPopLIFO() {
        stack.push("First");
        stack.push("Second");
        stack.push("Third");

        assertEquals("Third", stack.pop());
        assertEquals(2, stack.size());
        assertEquals("Second", stack.peek());

        assertEquals("Second", stack.pop());
        assertEquals(1, stack.size());
        assertEquals("First", stack.peek());

        assertEquals("First", stack.pop());
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @Test
    @DisplayName("Peek should return top element without removing it")
    void testPeek() {
        stack.push("Node A");
        assertEquals("Node A", stack.peek());
        assertEquals(1, stack.size());
        assertEquals("Node A", stack.peek()); // repeated peek
    }

    @Test
    @DisplayName("Pop on empty stack should throw EmptyStackException")
    void testPopEmptyThrowsException() {
        assertThrows(EmptyStackException.class, () -> stack.pop());
    }

    @Test
    @DisplayName("Peek on empty stack should throw EmptyStackException")
    void testPeekEmptyThrowsException() {
        assertThrows(EmptyStackException.class, () -> stack.peek());
    }

    @Test
    @DisplayName("Clear should reset the stack to empty state")
    void testClear() {
        stack.push("A");
        stack.push("B");
        stack.push("C");
        assertEquals(3, stack.size());

        stack.clear();
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @Test
    @DisplayName("toChronologicalList should preserve root-to-leaf / push order")
    void testToChronologicalList() {
        stack.push("Step 1 (Root)");
        stack.push("Step 2 (Branch)");
        stack.push("Step 3 (Leaf)");

        List<String> list = stack.toChronologicalList();
        assertEquals(3, list.size());
        assertEquals("Step 1 (Root)", list.get(0));
        assertEquals("Step 2 (Branch)", list.get(1));
        assertEquals("Step 3 (Leaf)", list.get(2));
    }

    @Test
    @DisplayName("toTopDownList should return items in top-to-bottom LIFO order")
    void testToTopDownList() {
        stack.push("Step 1 (Root)");
        stack.push("Step 2 (Branch)");
        stack.push("Step 3 (Leaf)");

        List<String> list = stack.toTopDownList();
        assertEquals(3, list.size());
        assertEquals("Step 3 (Leaf)", list.get(0));
        assertEquals("Step 2 (Branch)", list.get(1));
        assertEquals("Step 1 (Root)", list.get(2));
    }

    @Test
    @DisplayName("Stack iterator should traverse in LIFO order and handle exhaustion")
    void testIterator() {
        stack.push("A");
        stack.push("B");
        stack.push("C");

        Iterator<String> it = stack.iterator();
        assertTrue(it.hasNext());
        assertEquals("C", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    @DisplayName("Stack should support DecisionStep objects for decision tree auditing")
    void testDecisionStepStack() {
        CustomStack<DecisionStep> stepStack = new CustomStack<>();

        DecisionStep step1 = DecisionStep.builder()
                .featureName("BeachPref")
                .featureValue(4.5)
                .threshold(3.0)
                .branchLeft(false)
                .ruleDescription("BeachPref (4.50) > 3.00")
                .build();

        DecisionStep step2 = DecisionStep.builder()
                .featureName("Budget")
                .featureValue(500.0)
                .threshold(800.0)
                .branchLeft(true)
                .ruleDescription("Budget (500.00) <= 800.00")
                .build();

        stepStack.push(step1);
        stepStack.push(step2);

        assertEquals(2, stepStack.size());
        assertEquals("Budget (500.00) <= 800.00", stepStack.peek().getFormattedRule());

        List<DecisionStep> forwardPath = stepStack.toChronologicalList();
        assertEquals("BeachPref (4.50) > 3.00", forwardPath.get(0).getFormattedRule());
        assertEquals("Budget (500.00) <= 800.00", forwardPath.get(1).getFormattedRule());
    }
}
