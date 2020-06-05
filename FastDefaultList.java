package skiplist;

import java.lang.reflect.Array;
import java.lang.IllegalStateException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;


/**
 * Implements the List interface as a skiplist so that all the
 * standard operations take O(log n) time
 */
public class FastDefaultList<T> extends AbstractList<T> {
	class Node {
		T x;
		Node[] next;
		int[] length;
		@SuppressWarnings("unchecked")
		public Node(T ix, int h) {
			x = ix;
			next = (Node[])Array.newInstance(Node.class, h+1);
			length = new int[h+1];
		}
		public int height() {
			return next.length - 1;
		}
	}

	/**
	 * This node sits on the left side of the skiplist
	 */
	protected Node sentinel;

	/**
	 * The maximum height of any element
	 */
	int h;

	/**
	 * The number of elements stored in the skiplist
	 */
	//int n;   //don't need this anymore

	/**
	 * A source of random numbers
	 */
	Random rand;

	public FastDefaultList() {
		//n = 0;
		sentinel = new Node(null, 32);
		h = 0;
		rand = new Random(0);
	}


	/**
	 * Represents a node/index Pair
	 */
	protected class Pair {
		Node u;
		int i;

		Pair(Node u, int i) {
			this.u = u;
			this.i = i;
		}
	}

	/**
	 * Find the node that precedes list index i in the skiplist.
	 * @param x - the value to search for
	 * @return the predecessor of the node at index i or the final
	 * node if i exceeds size() - 1.
	 */
	protected Pair findPred(int i) {
        // It's not enough to know u, you also need the value j,
        // maybe change the return type to Pair and return the pair (u,j)
		Node u = sentinel;
		int r = h;
		int j = -1;   // index of the current node in list 0
		while (r >= 0) {
			while (u.next[r] != null && j + u.length[r] < i) {
				j += u.length[r];
				u = u.next[r];
			}
			r--;
		}
		return new Pair (u,j);
	}


	public T get(int i) {
        // this is too restrictive any non-negative i is allowed
		if (i < 0) throw new IndexOutOfBoundsException(); //removed n as don't need this anymore
		Pair p = findPred(i);
		if(p.u.next[0] != null){
			if(p.i + p.u.length[0] == i){
				return p.u.next[0].x;
			}
		}
		return null;
	}


	public T set(int i, T x) {
        // this is too restrictive any non-negative i is allowed
		if (i < 0) throw new IndexOutOfBoundsException();
		// Node u = findPred(i).next[0];
		Pair p = findPred(i);
//		T y = p.u.x;
//		p.u.x = x;
//		return y;

		if (p.u.next[0] != null) {
			if (p.i + p.u.length[0] == i) {
				Node u = p.u.next[0];
				T y = u.x;
				u.x = x;
				return y;
			}
		}

		// A new Node
		Node w = new Node(x, pickHeight());
		if (w.height() > h) { h = w.height(); } // reset high if w next() array is higher than sentinel
		setter(i, w); //add new node without pushing other Node forward

		return null;
	}

	//simply copy the add() method
	protected Node setter(int i, Node w) {
		Node u = sentinel;
		int k = w.height();
		int r = h;
		int j = -1; // index of u
		while (r >= 0) {
			while (u.next[r] != null && j+u.length[r] < i) {
				j += u.length[r];
				u = u.next[r];
			}
			//u.length[r]++; // accounts for new node in list 0 // since we just set the current index with new datae
			// we don't have to increase the length as we are not pushing node forward
			if (r <= k) {
				w.next[r] = u.next[r];
				u.next[r] = w;
				w.length[r] = u.length[r] - (i - j);
				u.length[r] = i - j;
			}
			r--;
		}
		//n++; //removed n as Hint: You don't need this anymore!
		return u;
	}

	/**
	 * Insert a new node into the skiplist
	 * @param i the index of the new node
	 * @param w the node to insert
	 * @return the node u that precedes v in the skiplist
	 */
	protected Node add(int i, Node w) {
		Node u = sentinel;
		int k = w.height();
		int r = h;
		int j = -1; // index of u
		while (r >= 0) {
			while (u.next[r] != null && j+u.length[r] < i) {
				j += u.length[r];
				u = u.next[r];
			}
			u.length[r]++; // accounts for new node in list 0
			if (r <= k) {
				w.next[r] = u.next[r];
				u.next[r] = w;
				w.length[r] = u.length[r] - (i - j);
				u.length[r] = i - j;
			}
			r--;
		}
		//n++; //removed n as Hint: You don't need this anymore!
		return u;
	}

	/**
	 * Simulate repeatedly tossing a coin until it comes up tails.
	 * Note, this code will never generate a height greater than 32
	 * @return the number of coin tosses - 1
	 */
	protected int pickHeight() {
		int z = rand.nextInt();
		int k = 0;
		int m = 1;
		while ((z & m) != 0) {
			k++;
			m <<= 1;
		}
		return k;
	}

	public void add(int i, T x) {
        // Hint: bounds checking again!
		if (i < 0) throw new IndexOutOfBoundsException();
		Node w = new Node(x, pickHeight());
		if (w.height() > h)
			h = w.height();
		add(i, w);
	}

	public T remove(int i) {
		if (i < 0) throw new IndexOutOfBoundsException();
		T x = null;
		Node u = sentinel;
		int r = h;
		int j = -1; // index of node u
		while (r >= 0) {
			while (u.next[r] != null && j+u.length[r] < i) {
				j += u.length[r];
				u = u.next[r];
			}
			u.length[r]--;  // for the node we are removing
			if (j + u.length[r] + 1 == i && u.next[r] != null) {
				x = u.next[r].x;
				u.length[r] += u.next[r].length[r];
				u.next[r] = u.next[r].next[r];
				if (u == sentinel && u.next[r] == null)
					h--;
			}
			r--;
		}
		//n--;  //removed n as Hint: You don't need this anymore!
		return x;
	}


	public int size() {
		return Integer.MAX_VALUE;
	}

	public String toString() {
        // This is just here to help you a bit with debugging
		StringBuilder sb = new StringBuilder();
			int i = -1;
			Node u = sentinel;
			while (u.next[0] != null) {
				i += u.length[0];
				u = u.next[0];
				sb.append(" " + i + " => " + u.x);
			}
			return sb.toString();
	}

	public static void main(String[] args) {
		// put your test code here if you like
		List<String> list = new FastDefaultList<String>();
		System.out.println("Call get 1000: " + list.get(1000));
		list.add(1000, "Angle");
		System.out.println("Call get 1000 after add(): " + list.get(1000));

		System.out.println("===========================");

      	list.add(25, "Stacee");
		System.out.println("Call get 1000 after add on idx 25: " + list.get(1000));
		System.out.println("Call get 25: " + list.get(25));
		System.out.println("Call get 1001: " + list.get(1001));

		System.out.println("===========================");

      	list.remove(1001);
		System.out.println("Remove index 1001 after remove(): " + list.get(1001));

		System.out.println("===========================");

		list.add(0,"Rent");
		list.add(1,"Mark");
		list.set(25,"Collins");
		list.set(50,"Mimi");
		System.out.println("Added Rent at index 0 and Mark at index 1.");
		System.out.println("Set index 25 as Collins and 50 as Mimi.");
		System.out.println("Now the list contain: " + list);

	}
}
