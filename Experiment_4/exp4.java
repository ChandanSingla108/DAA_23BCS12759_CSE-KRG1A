// DOUBLY LINKED LIST
class DNode {
    int value;
    DNode prev, next;

    DNode(int v) {
        value = v;
        prev = null;
        next = null;
    }
}

class DLinkedList {
    private DNode start, end;

    public DLinkedList() {
        start = end = null;
    }

    public void addFront(int v) {
        DNode node = new DNode(v);
        if (start == null) {
            start = end = node;
        } else {
            node.next = start;
            start.prev = node;
            start = node;
        }
    }

    public void addBack(int v) {
        DNode node = new DNode(v);
        if (end == null) {
            start = end = node;
        } else {
            end.next = node;
            node.prev = end;
            end = node;
        }
    }

    public void removeFront() {
        if (start == null) return;
        if (start == end) {
            start = end = null;
        } else {
            start = start.next;
            start.prev = null;
        }
    }

    public void removeBack() {
        if (end == null) return;
        if (start == end) {
            start = end = null;
        } else {
            end = end.prev;
            end.next = null;
        }
    }

    public void printList() {
        DNode cur = start;
        while (cur != null) {
            System.out.print(cur.value + " ");
            cur = cur.next;
        }
        System.out.println();
    }
}

// CIRCULAR LINKED LIST
class CNode {
    int val;
    CNode next;

    CNode(int v) {
        val = v;
        next = null;
    }
}

class CLinkedList {
    private CNode rear;

    public CLinkedList() {
        rear = null;
    }

    public void addFront(int v) {
        CNode node = new CNode(v);
        if (rear == null) {
            rear = node;
            rear.next = rear;
        } else {
            node.next = rear.next;
            rear.next = node;
        }
    }

    public void addBack(int v) {
        CNode node = new CNode(v);
        if (rear == null) {
            rear = node;
            rear.next = rear;
        } else {
            node.next = rear.next;
            rear.next = node;
            rear = node;
        }
    }

    public void removeFront() {
        if (rear == null) return;
        CNode head = rear.next;
        if (rear == head) {
            rear = null;
        } else {
            rear.next = head.next;
        }
    }

    public void removeBack() {
        if (rear == null) return;
        CNode cur = rear.next;
        if (cur == rear) {
            rear = null;
        } else {
            while (cur.next != rear) {
                cur = cur.next;
            }
            cur.next = rear.next;
            rear = cur;
        }
    }

    public void show() {
        if (rear == null) {
            System.out.println("List is empty");
            return;
        }
        CNode head = rear.next;
        CNode cur = head;
        do {
            System.out.print(cur.val + " ");
            cur = cur.next;
        } while (cur != head);
        System.out.println();
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Doubly Linked List Demo ===");
        DLinkedList dl = new DLinkedList();
        dl.addFront(1);
        dl.addBack(2);
        dl.addFront(3);
        dl.printList();
        dl.removeFront();
        dl.removeBack();
        dl.printList();

        System.out.println("\n=== Circular Linked List Demo ===");
        CLinkedList cl = new CLinkedList();
        cl.addFront(1);
        cl.addBack(2);
        cl.addFront(3);
        cl.show();
        cl.removeFront();
        cl.removeBack();
        cl.show();
    }
}
