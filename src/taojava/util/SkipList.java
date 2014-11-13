package taojava.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A randomized implementation of sorted lists.
 * 
 * @author Samuel A. Rebelsky
 * @author Hattie Zucker
 */
/*
 * Citations: Credit to Zhi Chen and Leah Greenberg for (a lot of help) idea of
 * making generic Nodes, for demonstrating that Node needs a level field, for
 * the null test in the add procedure Credit to Sam for helping me with
 * Iterators and with the remove proc
 */

public class SkipList<T extends Comparable<T>>
    implements SortedList<T>
{
  // +--------+----------------------------------------------------------
  // | Fields |
  // +--------+
  /**
   * The front of the list
   */
  Node<T> header;
  /**
   * The maximum size of a level you can use
   **/
  int maxLevel;

  /**
   * The value of p to use
   */
  double p;
  /**
   * The number of modifications to the list. Used to determine whether an
   * iterator is valid.
   */
  long mods;

  /**
   * An integer representation of the level of the node.
   */
  int level;
  /**
   * An iterator to traverse the list
   */
  Iterator<T> it;

  // +------------------+------------------------------------------------
  // | Internal Classes |
  // +------------------+
  /**
   * Nodes for skip lists.
   */
  @SuppressWarnings("hiding")
  public class Node<T>
  {
    // +--------+--------------------------------------------------------
    // | Fields |
    // +--------+

    /**
     * The value stored in the node.
     */
    T val;
    /**
     * Array of pointers.
     */
    Node<T>[] forward;
    /**
     * The level the node holds.
     */
    int size;

    // +--------------+---------------------------------------------------
    // | Constructors |
    // +--------------+
    @SuppressWarnings("unchecked")
    public Node(int size, T val)
    {
      this.forward = new Node[maxLevel];
      this.val = val;
      this.size = size;
    }// Node(int, T)

    @SuppressWarnings("unchecked")
    public Node(int size)
    {
      this.forward = new Node[maxLevel];
      this.val = null;
    }// Node(int)

  } // class Node

  // +--------------+----------------------------------------------------
  // | Constructors |
  // +--------------+
  public SkipList(double probability, int maxLevel)
  {
    this.p = probability;
    this.maxLevel = maxLevel;
    this.header = new Node<T>(maxLevel);
    this.mods = 0;
    this.level = this.maxLevel;
  }// SkipList(double, int)

  // +-------------------------+-----------------------------------------
  // | Internal Helper Methods |
  // +-------------------------+
  public int randomLevel()
  {
    // set a new level
    int newLevel = 0;
    // randomly generate numbers to set a new level
    while (Math.random() < this.p)
      newLevel++;
    // return a newLevel unless it is bigger than maxLevel, then
    // return maxLevel
    return Math.min(newLevel, this.maxLevel);
  }// randomLevel()

  // +-----------------------+-------------------------------------------
  // | Methods from Iterable |
  // +-----------------------+

  /**
   * Return a read-only iterator (one that does not implement the remove
   * method) that iterates the values of the list from smallest to largest.
   */
  public Iterator<T> iterator()
  {
    return new Iterator<T>()
      {
        // +--------+------------------------------------------------------
        // | Fields |
        // +--------+

        /**
         * The Node<T> that immediately precedes the value to be returned by
         * next.
         */
        Node<T> cursor = SkipList.this.header;

        /**
         * The number of modifications at the time this iterator was created
         * or last updated.
         */
        long mods;

        // +---------+-----------------------------------------------------
        // | Helpers |
        // +---------+

        /**
         * Determine if the list has been updated since this iterator was
         * created or modified.
         */
        void failFast()
        {
          if (this.mods != SkipList.this.mods)
            throw new ConcurrentModificationException();
        } // failFast

        // +---------+-----------------------------------------------------
        // | Methods |
        // +---------+
        public boolean hasNext()
        {
          failFast();
          return this.cursor.forward[0] != null;
        } // hasNext()

        public T next()
          throws NoSuchElementException
        {
          failFast();
          if (!this.hasNext())
            throw new NoSuchElementException();
          // Advance to the next node.
          this.cursor = this.cursor.forward[0];
          // The next value is in the current node.
          return this.cursor.val;
        } // next()

        public void remove()
          throws NoSuchElementException
        {
          failFast();
          // make temporary thing to remove and then remove it
          T val = this.cursor.val;
          SkipList.this.remove(val);
          this.cursor = this.cursor.forward[0];
          // update mods
          SkipList.this.mods++;
        } // remove()
      };
  } // iterator()

  // +------------------------+------------------------------------------
  // | Methods from SimpleSet |
  // +------------------------+

  /**
   * Add a value to the set.
   *
   * @post contains(val)
   * @post For all lav != val, if contains(lav) held before the call to add,
   *       contains(lav) continues to hold.
   */
  public void add(T val)
  {
    // set current to be the dummy at the start of the list
    Node<T> current = this.header;
    @SuppressWarnings("unchecked")
    // make update, a vector that holds nodes
    Node<T>[] update = new Node[maxLevel];
    // for each level
    for (int i = this.level - 1; i >= 0; i--)
      {
        // while the next item is not null and not less than val
        while ((current.forward[i] != null)
               && current.forward[i].val.compareTo(val) < 1)
          {
            // set current to be what it points to
            current = current.forward[i];
          }// while
           // fill update with the pointers you need to update
        update[i] = current;
      }// for
       // current should now be whatever it points to
    current = current.forward[0];
    // if current is null or not equal to val
    if ((current == null) || (!current.val.equals(val)))
      {
        // make a new level
        int newLevel = randomLevel();
        // if the newLevel is greater than the maximum level
        if (newLevel > maxLevel)
          {
            // for each level between maxLevel and newLevel
            for (int i = maxLevel + 1; i <= newLevel; i++)
              {
                // update header
                update[i] = this.header;
              }// for
               // resize maxLevel
            maxLevel = newLevel;
          }// if
           // current is the new node you are creating
        current = new Node<T>(this.level, val);
        // for all levels
        for (int i = 0; i < newLevel; i++)
          {
            // set the pointers
            current.forward[i] = update[i].forward[i];
            // use update to get pointers to current
            update[i].forward[i] = current;
          }// for
      }// if
  } // add(T val)

  /**
   * Determine if the set contains a particular value.
   */
  public boolean contains(T val)
  {
    // start at the header
    Node<T> current = this.header;
    // for each level
    for (int i = this.level - 1; i >= 0; i--)
      {
        // while the current element isn't less than val
        while (current.forward[i] != null
               && current.forward[i].val.compareTo(val) < 0)
          {
            // set current
            current = current.forward[i];
          }// while
      }// for
       // move current forward
    current = current.forward[0];
    // if current's val is the key
    if (current != null && current.val.compareTo(val) == 0)
      return true;
    // else
    return false;
  } // contains(T)

  /**
   * Remove an element from the set.
   *
   * @post !contains(val)
   * @post For all lav != val, if contains(lav) held before the call to
   *       remove, contains(lav) continues to hold.
   */
  public void remove(T val)
  {
    // set current to be the dummy at the start of the list
    Node<T> current = this.header;
    @SuppressWarnings("unchecked")
    // make update, a vector that holds nodes
    Node<T>[] update = new Node[maxLevel];
    // for each level
    for (int i = this.level - 1; i >= 0; i--)
      {
        // while next element is not null and not less than val
        while (current.forward[i] != null
               && current.forward[i].val.compareTo(val) < 1)
          {
            // move current forward
            current = current.forward[i];
          }
        // update holds current for each level
        update[i] = current;
      }
    // move current forward
    current = current.forward[0];
    if (current == null)
      {
        // if current is null, you have to return
        return;
      }// if
    else if (current.val.compareTo(val) == 0)
      {
        for (int i = 0; i <= this.level; i++)
          {
            if (update[i].forward[i] != current)
              {
                break;
              }// if
            update[i].forward[i] = current.forward[i];
          }// for
        while (this.level > 0 && this.header.forward[this.level] == null)
          {
            this.level--;
          }// while
           // increment mods
        this.mods++;
      }// else if
  }// remove(T)

  // +--------------------------+----------------------------------------
  // | Methods from SemiIndexed |
  // +--------------------------+

  /**
   * Get the element at index i.
   *
   * @throws IndexOutOfBoundsException
   *             if the index is out of range (index < 0 || index >= length)
   */
  public T get(int i)
    throws IndexOutOfBoundsException
  {
    // STUB
    return null;
  } // get(int)

  /**
   * Determine the number of elements in the collection.
   */
  public int length()
  {
    // STUB
    return 0;
  } // length()
} // class SkipList<T>
/*
 * Note: My programs fail for certain tests in the suite but I ran out of time
 * 
 * Part 4: 
 * Skip lists are useful because they traverse a list quickly. They depend
 * on the value of p you select, which can effect whether you're taking more
 * time to search or using more storage space. SortedArrayLists are useful
 * but they take a lot more time to search. A skip list is good if a fast search is
 * needed, a sorted array list is good if that doesn't matter or if you don't
 * need to consider time vs. space trade-off.
 * 
 * Part 5: As part of this assignment, you likely read a number of the code
 * files that are part of the project. Pick three interesting things you learned
 * in reading those files, summarize them in a way that one of your classmates
 * could understand them, and provide examples (at least one per thing) of other
 * ways in which you might use that approach.
 * 1. I didn't think it would be possible to combine all of the parts of 
 * SkipList into one class (i.e. I thought that you would need a new Iterator 
 * class, and I also thought that Node and SkipList might be separate. After 
 * reading the class I understood that you can make the Iterator class an 
 * inner class. If I were implementing a different sort of collection I would 
 * try to keep things in one class for clarity.
 * 2.In the expt package I thought it was interesting to have one experiment and
 * then have a bunch of different classes that call the one main experiment. I
 * would probably have initially written one huge class but this made it easier 
 * not only to test the different lists but also to understand how each structure
 * was functioning. By separating the classes, you are keeping the actual calls to 
 * the utils classes separate. I would use this for any experiments I would have to do
 * in the future that involve comparing things. 
 * 3. I had to read over the line that reads:
 * public class SkipList<T extends Comparable<T>> implements SortedList<T>
 * many times because I didn't understand the explanation of it in the assignment.
 * After completing more work on the assignment, I understood that it means that
 * you are actually making the object T a comparable object. I thought this was
 * interesting because it made it possible to apply comparable methods the the
 * object of type T. I would use something like this again to make things generic.
 * It was a helpful way to make a class applicable more generally.
 * 
 */
