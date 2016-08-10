package linkedhashtree;
// synthesis of LinkedHashMap and BinarySearchTree

import java.util.Random;
//* Author - Joseph Jaspers Jan 2016
//  The LinkedHashTree is a tree that sorts by key. 
//  Nodes contain a multiple of pointers.
// Left Right are aspects of the binary tree
// next/prev function as a double liked list. 
// the Hash map enables the tree to have O(1) contains. 
// If implemented correctly this structure should enable O(nlog(n)) add times O(1) contains.
// The structure is sorted upon adding all elements and maintains insertion order. 
// This can be checked with the print Order method and printInsertionOrder method.
// Currently probing is not supported - this structure was originally implemented for V to be a number in which case collisions were not possible
public class LinkedHashTree<K extends Comparable, V extends Comparable> {

    private Object[] bucket;
    private int size;
    private Node root;
    private Node first;
    private Node last;
    private float loadFactor = (float) .75;
    boolean modifiable = true;

    private class Node {

        Node next;
        Node prev;
        Node collisionNext;

        Node left;
        Node right;

        K key;
        V value;

        Node(K key, V value, Node n, Node p, Node l, Node r, Node c) {
            this.key = key;
            this.value = value;
            next = n;
            prev = p;
            collisionNext = c;
            left = l;
            right = r;
        }
    }

    public LinkedHashTree(int size) {
        bucket = new Object[size];
        size = 0;
        root = null;
        first = null;
    }

    public LinkedHashTree(int size, float loadFactor, boolean modifiable) {
        bucket = new Object[size];
        this.loadFactor = loadFactor;
        size = 0;
        root = null;
        first = null;
        this.modifiable = modifiable;
    }

    public LinkedHashTree() {
        bucket = new Object[16];
        size = 0;
        root = null;
        first = null;
    }

    public int size() {
        return size;
    }

    public int getHashCode(Object obj) {
        return Math.abs(obj.hashCode() % bucket.length);
    }

    public void clear() {
        bucket = new Object[16];
        root = null;
        first = null;
    }

    public Object clone() {
        throw new UnsupportedOperationException("Not written");
    }

    public boolean containsKey(Object obj) {
        K key = (K) obj;
        int index = getHashCode(key);

        Node ref = (Node) bucket[index];

        while (ref != null) {
            if (ref.key.equals(key)) {
                return true;
            }
            ref = ref.collisionNext;
        }
        return false;
    }

    public boolean containsValue(Object obj) {
        V val = (V) obj;

        Node ref = first;
        while (ref != null) {
            if (ref.value.equals(val)) {
                return true;
            }
            ref = ref.next;
        }
        return false;
    }

    public boolean add(K key, V value) { //functions like put 
//        System.out.println("adding " + key);
        if (containsKey(key)) {
            if (get(key).equals(value)) {
                return false;
            } else {
                remove(key);
            }
        }
        if (first == null) {
//            System.out.println("adding first");
            first = new Node(key, value, null, null, null, null, null);
            root = first;
            last = first;
            bucket[getHashCode(key)] = first;
            size++;
        } else {
            add(root, key, value);
//            System.out.println("adding not first");
        }
        size++;
        reHash();
        return true;
    }

    private Node add(Node n, K key, V value) {
        if (n == null) {
            int index = getHashCode(key);
            n = new Node(key, value, null, last, null, null, (Node) bucket[index]);
            last.next = n;
            last = last.next;
            bucket[index] = n;
            return n;
        }
        int comp = key.compareTo(n.key);
        if (comp < 0) {
            n.left = add(n.left, key, value);
            return n;
        } else {
            n.right = add(n.right, key, value);
            return n;
        }
    }

    private void reHash() {
        if (size > bucket.length * loadFactor) {
            bucket = new Object[bucket.length * 2];

            Node ref = first;
            int index;
            while (ref != null) {
                index = getHashCode(ref.key);
                ref.collisionNext = (Node) bucket[index];
                bucket[index] = ref;
                ref = ref.next;
            }
        }
    }

    public V get(K key) {
        int index = getHashCode(key);
        Node ref = (Node) bucket[index];

        while (ref != null) {
            if (ref.key.equals(key)) {
                return ref.value;
            }
            ref = ref.collisionNext;
        }
        return null;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public boolean remove(K key) {
        if (!containsKey(key)) {
            return false;
        }
        int index = getHashCode(key);
        Node ref = (Node) bucket[index];
        Node store = null;
        if (ref.key.equals(key)) { //remove from Bucket 
            store = ref;
            bucket[index] = ref.collisionNext;
        } else {
            while (ref != null) {
                if (ref.collisionNext.key.equals(key)) {
                    store = ref.collisionNext;
                    ref.collisionNext = ref.collisionNext.collisionNext;
                    break;
                }
                ref = ref.collisionNext;
            }
        }
        if (ref.equals(first)) { //remove from LinkedList 
            first = ref.next;
        } else if (ref.equals(last)) {
            last.prev.next = null;
            last = last.prev;
        } else {
            ref.prev.next = ref.next;
        }
        remove(root, store.key);
        size--;
        return true;
    }
    private final Random flipACoin = new Random();

    private Node remove(Node n, K key) {
        if (n == null) {
            return null;
        }
        int comp = key.compareTo(n.key);
        if (comp < 0) {
            n.left = remove(n.left, key);
            return n;
        }
        if (comp > 0) {
            n.right = remove(n.right, key);
            return n;
        }
        if (n.left == null) {
            return n.right;
        }
        if (n.right == null) {
            return n.left;
        }
        boolean choose_min = (flipACoin.nextInt(2) == 1);
        if (choose_min) {
            Node store = findMin(n.right);
            n.key = store.key;
            n.value = store.value;
            n.right = removeMin(n.right);
        } else {
            Node store = findMax(n.left);
            n.key = store.key;
            n.value = store.value;
            n.left = removeMax(n.left);
        }
        return n;
    }

    public K getMinKey() {
        return (K) (Node) findMin(root).key;
    }

    public K getMaxKey() {
        return (K) (Node) findMax(root).key;
    }

    private Node findMin(Node n) {
        if (n.left == null) {
            return n;
        } else {
            return findMin(n.left);
        }
    }

    private Node findMax(Node n) {
        if (n.right == null) {
            return n;
        } else {
            return findMax(n.right);
        }
    }

    private boolean removeMin() {
        if (isEmpty()) {
            return false;
        }
        root = removeMin(root);
        return true;
    }

    private Node removeMin(Node n) { //private for removeFunction
        if (n.left == null) {
            return n.right;
        } else {
            n.left = removeMin(n.left);
            return n;
        }
    }

    private boolean removeMax() {
        if (isEmpty()) {
            return false;
        }
        root = removeMax(root);
        return true;
    }

    private Node removeMax(Node n) {
        if (n.right == null) {
            return n.left;
        } else {
            n.right = removeMax(n.right);
            return n;
        }
    }

    public void printInsertionOrder() {
        Node ref = first;
        int i = 0;
        while (ref != null) {
            System.out.println(ref.key + " " + ref.value);
            ref = ref.next;
            i++;
        }
    }

    public void printOrder() {
        printOrder(root);
    }

    private void printOrder(Node n) {
        if (n == null) {
            return;
        }
        printOrder(n.left);
        System.out.println(n.key + " " + n.value);
        printOrder(n.right);
    }

    public static void main(String[] args) {
        Random rand = new Random();

        LinkedHashTree<Integer, Integer> lht = new LinkedHashTree();

        long timeStart = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            int a = rand.nextInt(20000000);
            lht.add(a, i);
        }
        long timeEnd = System.currentTimeMillis();
        System.out.println("adding 1234");
        lht.add(1234, 0);
        System.out.println("size is " + lht.size());
        System.out.println("removing 1234 " + lht.containsKey(1234));
        lht.remove(1234);
        System.out.println("still contains 1234 " + lht.containsKey(1234));
        System.out.println("size is " + lht.size());
        System.out.println("----------------printing Insertion Order-------------------");
        //    lht.printInsertionOrder();
        System.out.println("----------------printing Order-------------------");
        //    lht.printOrder();
        System.out.println("The add time is " + String.format("%09f", ((double) (timeEnd - timeStart) / (double) (1000))));

    }

}
