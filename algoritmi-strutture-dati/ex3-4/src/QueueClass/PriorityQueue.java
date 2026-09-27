package QueueClass;

import java.util.*;

/**
 * A priority queue implementation using a heap data structure and a hash map for O(1) element access.
 * Elements are ordered according to the specified comparator.
 *
 * @param <E> the type of elements in this queue
 */

public class PriorityQueue<E> implements AbstractQueue<E> {
    private ArrayList<E> heap;
    private HashMap<E, Integer> map;
    private Comparator<E> comparator;

    /**
     * Constructs a new empty priority queue with the specified comparator.
     *
     * @param comparator the comparator that will be used to order this priority queue
     */
    public PriorityQueue(Comparator<E> comparator) {
        this.heap = new ArrayList<>();
        this.map = new HashMap<>();
        this.comparator = comparator;
    }

    /**
     * Checks if the priority queue is empty.
     *
     * @return {@code true} if the priority queue contains no elements
     */
    @Override
    public boolean empty() {
        return heap.isEmpty();
    }

    /**
     * Inserts the specified element into this priority queue if it is not already present.
     * The element is added maintaining the heap invariant.
     *
     * @param element the element to add
     * @return {@code true} if the element was added, {@code false} if it was already present
     */
    @Override
    public boolean push(E element) {
        if (element == null) throw new NullPointerException("element null.");
        if (!contains(element)) {
            int i = heap.size();
            heap.add(element);
            map.put(element, i);
            while (i > 0) {
                int parent = (i - 1) / 2;
                if (comparator.compare(heap.get(i), heap.get(parent)) < 0) {
                    swap(i, parent);
                    i = parent;
                } else {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if this priority queue contains the specified element.
     *
     * @param element the element whose presence in this priority queue is to be tested
     * @return {@code true} if this priority queue contains the specified element
     */
    @Override
    public boolean contains(E element) {
        return map.containsKey(element);
    }

    /**
     * Retrieves, but does not remove, the element at the top of this priority queue.
     *
     * @return the element at the top of this priority queue
     * @throws NoSuchElementException if the priority queue is empty
     */
    @Override
    public E top() {
        if (heap.isEmpty()) throw new IndexOutOfBoundsException("Queue is empty");
        return heap.get(0);
    }

    /**
     * Removes the element at the top of this priority queue.
     * The heap invariant is maintained after removal.
     */
    @Override
    public void pop() {
        if (!heap.isEmpty()) {
            map.remove(heap.get(0));
            E lastElement = heap.remove(heap.size() - 1);
            if (!heap.isEmpty()) {
                heap.set(0, lastElement);
                map.put(lastElement, 0);
                heapify(0);
            }
        } else throw new IndexOutOfBoundsException("pop on empty queue");
    }


    /**
     * Removes the specified element from this priority queue if it is present.
     * The heap invariant is maintained after removal.
     *
     * @param element the element to be removed
     * @return {@code true} if the element was removed, {@code false} if it was not present
     */
    @Override
    public boolean remove(E element) {
        if (!contains(element)) {
            return false;
        }

        int index = map.get(element);
        E lastElement = heap.remove(heap.size() - 1);
        map.remove(element);

        if (index < heap.size()) {
            heap.set(index, lastElement);
            map.put(lastElement, index);
            if (!heapify(index)) {
                while (index > 0) {
                    int parent = (index - 1) / 2;
                    if (comparator.compare(heap.get(index), heap.get(parent)) < 0) {
                        swap(index, parent);
                        index = parent;
                    } else {
                        break;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Restores the heap property starting from the specified index down the heap.
     *
     * @param index the index from which to start heapifying
     * @return {@code true} if heapify happened, {@code false} otherwise
     */
    private boolean heapify(int index) {
        int left = 2 * index + 1;
        int right = 2 * index + 2;
        int priority = index;

        if (left < heap.size() && comparator.compare(heap.get(left), heap.get(priority)) < 0) {
            priority = left;
        }

        if (right < heap.size() && comparator.compare(heap.get(right), heap.get(priority)) < 0) {
            priority = right;
        }

        if (priority != index) {
            swap(index, priority);
            heapify(priority);
            return true;
        }
        return false;
    }


    /**
     * Swaps the elements at the specified positions in the heap and updates the map.
     *
     * @param i the index of one element to be swapped
     * @param j the index of the other element to be swapped
     */
    private void swap(int i, int j) {
        E temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);

        map.put(heap.get(i), i);
        map.put(heap.get(j), j);
    }

    /**
     * Prints the elements of the heap to the standard output.
     */
    public void printHeap() {
        for (E element : heap) {
            System.out.println(element);
        }
    }

    /**
     * Prints the contents of the hash map (element to index mapping) to the standard output.
     */
    public void printHashMap() {
        if (map.isEmpty()) {
            System.out.println("HashMap è vuoto.");
            return;
        }
        System.out.println("Contenuto della HashMap:");
        for (Map.Entry<E, Integer> entry : map.entrySet()) {
            System.out.println("Elemento: " + entry.getKey() + " -> Indice: " + entry.getValue());
        }
    }
}