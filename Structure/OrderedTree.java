package Structure;

import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Instances of this class represent nodes of ordered trees where the data in
 * each node is of a class specified by the user.
 * 
 * Parse trees, syntax trees, and other ordered trees may be represented by
 * their roots.
 * 
 * There is no upper bound on the number of children that an ordered tree may
 * have.
 * 
 * Instances of this class are mutable only through the iterator.
 * 
 * @author Jeff Smith
 * @version for CS 152, Spring 2011, SJSU
 */

public class OrderedTree<E>
{

    // the data in the node
    private E data;

    // a list of the node's children
    private LinkedList<OrderedTree<E>> children;

    /**
     * Builds an ordered tree with null data and an empty list of children.
     */

    public OrderedTree()
    {
        this(null, null);
    }

    /**
     * Builds an ordered tree from given data and children. A shallow copy is
     * made of the list of children.
     * 
     * @param data
     *            the data for the node. Null values are permitted
     * @param children
     *            the list of children of the node. Null values are converted to
     *            empty lists.
     */

    public OrderedTree(E data, List<OrderedTree<E>> children)
    {
        this.data = data;
        if (children == null)
            this.children = new LinkedList<OrderedTree<E>>();
        else
            this.children = new LinkedList<OrderedTree<E>>(children);
    }

    /**
     * Finds the value of the data in the root
     * 
     * @return the value of the data in the root (which may be null)
     */

    public E getRootData()
    {
        return data;
    }
    
    public void setRootData(E data)
    {
    	this.data = data;
    }

    /**
     * Constructs a bidirectional iterator over the list of children
     * 
     * @return the iterator
     */

    public ListIterator<OrderedTree<E>> listIterator()
    {
        return children.listIterator();
    }

    /**
     * Determines whether the ordered tree equals another ordered tree. The
     * comparison is a deep comparison that uses <code>equals</code> to compare
     * each node. In particular, two trees without children are equal iff their
     * roots are.
     * 
     * @param o
     *            the other ordered tree
     * @return <code>true</code> iff the two trees are equal
     */

    public boolean equals(Object o)
    {
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        OrderedTree<?> otherTree = (OrderedTree<?>) o;
        if (!data.equals(otherTree.data))
            return false;
        for (int k = 0; k < children.size(); k++)
            if (!children.get(k).equals(otherTree.children.get(k)))
                return false;
        return true;
    }

    /**
     * Traverses the tree rooted at the node, in preorder. A null data value is
     * treated as an empty string, and a null list of children is treated as an
     * empty list.
     * 
     * @return a string representing the result of preorder traversal, with the
     *         contents of every node appearing on a separate line, indented an
     *         amount proportional to its distance from the root
     */

    public String toString()
    {
        return toString("", System.getProperty("line.separator"));
    }

    /**
     * A function that behaves as toString, but takes the amount of indentation
     * of the first line as a parameter.
     * 
     * @param indentation
     *            a string of spaces representing the indentation of each child
     *            from its parent
     * @param newline
     *            the string to be used to separate lines of output
     * @return a printed representation of the traversal, suitable as a return
     *         value for toString()
     */

    private String toString(String indentation, String newline)
    {
        StringBuffer resultSoFar = new StringBuffer(indentation); // won't be
                                                                  // null

        // print the data, appropriately indented

        if (data == null)
        {
            resultSoFar.append("null");
        } else
        {
            resultSoFar.append(data.toString());
        }
        resultSoFar.append(newline);

        // recursively print the children,
        // indented an additional two spaces

        if (children == null)
        {
            return new String(resultSoFar);
        }
        ListIterator<OrderedTree<E>> lit = listIterator();
        while (lit.hasNext())
        {
            String childString = lit.next().toString(indentation + "  ",
                    newline);
            resultSoFar.append(childString);
        }
        return new String(resultSoFar);
    }

    /**
     * Traverses a tree to the index specified by the index parameter. This
     * index parameter is a list of children, delimited by periods. For
     * example, the string "0.0.0" should point to the first child of the
     * first child of the first child of the root.
     * @param index
     * @return
     */
    public OrderedTree<E> seek(String index)
    {
        int i, n;
        n = index.length();
        i = Integer.parseInt(index.substring(0,1));
        if(children == null || i > children.size()-1)
        {
        	return null;
        }
        else if(n == 1)
        {
            return children.get(i);
        }
        else
        {
            index = index.substring(2);
            return children.get(i).seek(index);
        }
    }
    
    public E get(String index)
    {
    	OrderedTree<E> node = seek(index);
    	if(node == null) return null;
    	else
    	{
    		return node.getRootData();
    	}
    }
    
    public boolean set(String index, E data)
    {
    	if(get(index) == null) return false;
    	else
    	{
    		seek(index).setRootData(data);
    		return true;
    	}
    }
}