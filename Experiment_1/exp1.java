class MyStack {
    int[] data;
    int maxSize;
    int topIndex;

    public MyStack(int n) {
        maxSize = n;
        data = new int[maxSize];
        topIndex = -1;
    }

    public void push(int val) {
        if (isFull()) {
            System.out.println("Cannot push, stack is full!");
            return;
        }
        data[++topIndex] = val;
    }

    public void pop() {
        if (isEmpty()) {
            System.out.println("Cannot pop, stack is empty!");
            return;
        }
        topIndex--;
    }

    public int top() {
        if (isEmpty()) {
            System.out.println("No elements in stack.");
            return -1; // return sentinel since stack is empty
        }
        return data[topIndex];
    }

    public boolean isEmpty() {
        return topIndex < 0;
    }

    public boolean isFull() {
        return topIndex == maxSize - 1;
    }

    public static void main(String[] args) {
        MyStack st = new MyStack(5);
        st.push(10);
        st.push(20);
        st.push(30);

        System.out.println("Current top: " + st.top());
        st.pop();
        System.out.println("Top after one pop: " + st.top());

        st.push(40);
        st.push(50);
        st.push(60);
        st.push(70);

        while (!st.isEmpty()) {
            System.out.println("Removing: " + st.top());
            st.pop();
        }

        st.pop();
    }
}
