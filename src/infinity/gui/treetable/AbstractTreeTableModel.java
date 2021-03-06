
package infinity.gui.treetable;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

public abstract class AbstractTreeTableModel implements TreeTableModel
{
  private final List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
  private final Object root;

  protected AbstractTreeTableModel(Object root)
  {
    this.root = root;
  }

// --------------------- Begin Interface TreeModel ---------------------

  //
  // Default implmentations for methods in the TreeModel interface.
  //

  @Override
  public Object getRoot()
  {
    return root;
  }

  @Override
  public boolean isLeaf(Object node)
  {
    return getChildCount(node) == 0;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {}

  // This is not called in the JTree's default mode: use a naive implementation.
  @Override
  public int getIndexOfChild(Object parent, Object child)
  {
    for (int i = 0; i < getChildCount(parent); i++) {
      if (getChild(parent, i).equals(child))
        return i;
    }
    return -1;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l)
  {
    listeners.add(l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l)
  {
    listeners.remove(l);
  }

// --------------------- End Interface TreeModel ---------------------


// --------------------- Begin Interface TreeTableModel ---------------------

  //
  // Default impelmentations for methods in the TreeTableModel interface.
  //

  @Override
  public Class<? extends Object> getColumnClass(int column) { return Object.class; }

  /**
   * By default, make the column with the Tree in it the only editable one.
   * Making this column editable causes the JTable to forward mouse
   * and keyboard events in the Tree column to the underlying JTree.
   */
  @Override
  public boolean isCellEditable(Object node, int column)
  {
    return getColumnClass(column) == TreeTableModel.class;
  }

  @Override
  public void setValueAt(Object aValue, Object node, int column) {}

// --------------------- End Interface TreeTableModel ---------------------

  /*
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * @see EventListenerList
   */
  protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children)
  {
    TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
    for (int i = 0; i < listeners.size(); i++)
      listeners.get(i).treeNodesChanged(event);
  }

  /*
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * @see EventListenerList
   */
  protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children)
  {
    TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
    for (int i = 0; i < listeners.size(); i++)
      listeners.get(i).treeNodesInserted(event);
  }

  /*
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * @see EventListenerList
   */
  protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children)
  {
    TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
    for (int i = 0; i < listeners.size(); i++)
      listeners.get(i).treeNodesRemoved(event);
  }

  /*
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * @see EventListenerList
   */
  protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children)
  {
    TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
    for (int i = 0; i < listeners.size(); i++)
      listeners.get(i).treeStructureChanged(event);
  }
}
