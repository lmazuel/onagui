package fr.onagui.control;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import fr.onagui.alignment.OntoContainer;


/** Use as an "UserObject" of a TreeNode.
 * @author Laurent Mazuel
 *
 * @param <ONTORES>
 */
public class TreeNodeOntologyObject<ONTORES> {

	private ONTORES concept = null;
	private OntoContainer<ONTORES> container = null;
	private MyTreeModel<ONTORES> treeModel = null;
	private boolean isExpanded = false;

	/**
	 * @param concept
	 */
	public TreeNodeOntologyObject(
			MyTreeModel<ONTORES> treeModel,
			OntoContainer<ONTORES> container,
			ONTORES concept,
			boolean isExpanded) {
		this.treeModel = treeModel;
		this.concept = concept;
		this.container = container;
		this.isExpanded = isExpanded;
	}
	
	public ONTORES getConcept() {
		return concept;
	}
	
	protected OntoContainer<ONTORES> getContainer() {
		return container;
	}
	
	public boolean isExpanded() {
		return isExpanded;
	}
	
	public void expandNode(DefaultMutableTreeNode myself) {
		if(isExpanded()) return;
		isExpanded = true;

		if(myself.isLeaf()) return;
		
		myself.removeAllChildren(); // Remove the fake node
		Set<ONTORES> children = container.getChildren(concept);
		for(ONTORES child : children) {
			treeModel.createTreeNodes(myself, child);
		}
		treeModel.reload();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	// Method used to show label tree
	@Override
	public String toString() {
		final String currentShowingLanguage = treeModel.getCurrentShowingLanguage();
		
		// Si j'ai une langue préférée, la chercher
		Set<String> prefLabels = getContainer().getPrefLabels(concept, currentShowingLanguage);
		// Si pas de langue préférée ou si ça donne rien, cas général
		if(prefLabels.isEmpty()) {
			prefLabels = getContainer().getPrefLabels(concept);
		}
		String prefix = (container.isIndividual(concept))?"I: ":"";
		if(!prefLabels.isEmpty())
			return prefix + prefLabels.iterator().next();
		URI uri = getContainer().getURI(concept);
		String fragment = uri.getFragment();
		
		if(fragment != null) {
			return prefix+fragment;
		}
		
		System.out.println("Null fragment for: "+uri.toString());
		return prefix + uri.toString();
		
	}		
}
