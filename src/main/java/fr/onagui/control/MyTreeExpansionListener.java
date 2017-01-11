/**
 * 
 */
package fr.onagui.control;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import fr.onagui.gui.AlignmentGUI;

/**
 * @author mazman
 *
 */
public class MyTreeExpansionListener implements TreeExpansionListener {
	
	private AlignmentGUI view = null;
	private JTree tree = null;
	
	public MyTreeExpansionListener(AlignmentGUI view, JTree tree) {
		this.view = view;
		this.tree = tree;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
	 */
	@Override
	public void treeCollapsed(TreeExpansionEvent arg0) {
		// Nothing now
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event.TreeExpansionEvent)
	 */
	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
		Object userObject = node.getUserObject();
		if(userObject instanceof TreeNodeOntologyObject) {
			TreeNodeOntologyObject ontologyNodeObject = (TreeNodeOntologyObject)userObject;
			if(ontologyNodeObject.isExpanded()) return;

			// Save expanded state
			TreePath rootTreePath = new TreePath(((DefaultMutableTreeNode)tree.getModel().getRoot()).getPath());
			Enumeration<TreePath> expandedNodes = tree.getExpandedDescendants(rootTreePath);

			// Expand new node if needed
			ontologyNodeObject.expandNode(node);
			
			// Restore expanded state
			if (expandedNodes != null) {
	            while (expandedNodes.hasMoreElements()) {
	                tree.expandPath(expandedNodes.nextElement());
	            }
	        }
			
			// Expand new node
			DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) node.getChildAt(0);
			tree.scrollPathToVisible(new TreePath(firstChild.getPath()));
			
			// Refresh!
			view.refreshGUIFromModel();
		}
	}
}
