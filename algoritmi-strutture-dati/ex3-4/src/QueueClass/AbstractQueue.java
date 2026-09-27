package QueueClass;

public interface AbstractQueue<E> {
    public boolean empty(); // controlla se la coda è vuota -- O(1)
    public boolean push(E e); // aggiunge un elemento alla coda -- O(logN)
    public boolean contains(E e); // controlla se un elemento è in coda -- O(1)
    public E top(); // accede all'elemento in cima alla coda -- O(1)
    public void pop(); // rimuove l'elemento in cima alla coda -- O(logN)
    public boolean remove(E e); // rimuove un elemento se presente in coda -- O(logN)
}
