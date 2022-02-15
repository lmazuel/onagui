package fr.onagui.control;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.google.common.base.Predicate;

import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.OntoVisitor;
import fr.onagui.gui.Messages;


public class MyTreeModel<ONTORES> extends DefaultTreeModel {

	/** To make Java happy */
	private static final long serialVersionUID = 2339139857992621648L;

	private OntoContainer<ONTORES> container = null;
	
	private String currentShowingLanguage = "";

	/**
	 * @return the currentShowingLanguage
	 */
	public String getCurrentShowingLanguage() {
		return currentShowingLanguage;
	}

	/**
	 * @param currentShowingLanguage the currentShowingLanguage to set
	 */
	public void setCurrentShowingLanguage(String currentShowingLanguage) {
		this.currentShowingLanguage = currentShowingLanguage;
	}

	/** Find a node from its URI.
	 * I cannot use directly the concept, because I don't know if it redefines correctly "equals" and "hashcode"...
	 */
	private Map<URI, List<DefaultMutableTreeNode>> conceptToTreeNode = null;  

	/**
	 * @param container
	 */
	public MyTreeModel(OntoContainer<ONTORES> container) {
		super(null); // False constructor
		this.container = container;

		// By default, choose a language by alphabetical order
		SortedSet<String> allLanguageInLabels = this.container.getAllLanguageInLabels();
		// pick up fr or en if they exists
		if(allLanguageInLabels.contains("fr")) {
			this.setCurrentShowingLanguage("fr");
		} else if (allLanguageInLabels.contains("en")) {
			this.setCurrentShowingLanguage("en");
		} else {
			this.setCurrentShowingLanguage(allLanguageInLabels.stream().findFirst().orElse(""));
		}		

		DefaultMutableTreeNode top = buildTreeModeFromContainer();
		setRoot(top);
	}

	private void putToUriToTreeNodeMap(URI concept, DefaultMutableTreeNode node) {
		if(conceptToTreeNode.containsKey(concept)) {
			conceptToTreeNode.get(concept).add(node);
		}
		else {
			List<DefaultMutableTreeNode> c = new Vector<DefaultMutableTreeNode>();
			c.add(node);
			conceptToTreeNode.put(concept, c);
		}
	}

	private DefaultMutableTreeNode buildTreeModeFromContainer() {
		this.conceptToTreeNode = new TreeMap<URI, List<DefaultMutableTreeNode>>();
		try {
			// Get the root of ontology
			ONTORES thingRoot = container.getRoot();
			return createTreeNodes(null, thingRoot);
		} catch (Exception e) {
			System.err.println("Some problem within...");
			e.printStackTrace();
			return null;
		}
	}

	/** Create hierarchy for node (generics version). Handle children recursively.
	 * @param top
	 * @param child
	 * @return The built tree node
	 * @throws OWLReasonerException
	 */
	DefaultMutableTreeNode createTreeNodes(DefaultMutableTreeNode top, ONTORES child) {
		// Creation de localRoot
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(
				new TreeNodeOntologyObject<ONTORES>(
						this,
						container,
						child,
						false
				)
		);
		putToUriToTreeNodeMap(container.getURI(child), node);
		if(top != null) {
			// insertAndSort(top, node);
			top.add(node);
		}

		Set<ONTORES> children = container.getChildren(child);
		if(!children.isEmpty()) {
			// A fake node, just to have the "+" on the screen
			node.add(new DefaultMutableTreeNode());
		}
		return node;
	}

	/** Return the first node linked to this concept.
	 * Can be <code>null</code>.
	 * @param concept
	 * @return
	 */
	public DefaultMutableTreeNode getFirstNodeFromConcept(ONTORES concept) {
		List<DefaultMutableTreeNode> nodes = getAllTreeNodesFromConcept(concept);
		if(nodes != null) {
			return nodes.get(0);
		}
		else {
			System.err.println("Warning: impossible to find a node for concept "+container.getURI(concept));
			return null;
		}
	}

	public List<DefaultMutableTreeNode> getAllTreeNodesFromConcept(ONTORES concept) {
		URI conceptURI = container.getURI(concept);
		if(conceptToTreeNode.containsKey(conceptURI)) {
			return conceptToTreeNode.get(conceptURI);
		}
		// Not in the tree right now
		Set<ONTORES> parents = container.getParents(concept);
		for(ONTORES parent : parents) {
			List<DefaultMutableTreeNode> parentsTreeNodes = getAllTreeNodesFromConcept(parent);
			
			if(parentsTreeNodes == null) {
				System.err.println("Warning: impossible to find the parent node "+container.getURI(parent)+" of concept "+container.getURI(concept));
				return null;
			}
			
			for(DefaultMutableTreeNode parentTreeNode : parentsTreeNodes) {
				TreeNodeOntologyObject<ONTORES> userParentNode = (TreeNodeOntologyObject<ONTORES>) parentTreeNode.getUserObject();
				userParentNode.expandNode(parentTreeNode);
			}
		}
		
		return conceptToTreeNode.get(conceptURI);
	}

	public static <ONTORES> void insertAndSort(DefaultMutableTreeNode parent, DefaultMutableTreeNode newChild) {
		TreeNodeOntologyObject<ONTORES> newContent = (TreeNodeOntologyObject<ONTORES>)newChild.getUserObject();		
		
		for(int childIndex=0; childIndex<parent.getChildCount(); childIndex++) {
			DefaultMutableTreeNode oneChild = (DefaultMutableTreeNode)parent.getChildAt(childIndex);
			TreeNodeOntologyObject<ONTORES> localContent = (TreeNodeOntologyObject<ONTORES>)oneChild.getUserObject();
			
			/* Use the "toString" method to order node.
			 * If greater than 0, "newChild" must be replace local Child
			 */
			if(newContent.toString().compareToIgnoreCase(localContent.toString()) < 0) {
				parent.insert(newChild, childIndex);
				return;
			}
		}
		// If I am here, newChild is greater than all nodes
		try {
			parent.add(newChild);
		} catch (IllegalArgumentException e) {
			System.err.println("Cycle detected. It will be broken on the Java tree interface.");
		}
		
	}
	
	public static void sortChildren(DefaultMutableTreeNode parent) {
		int n = parent.getChildCount();
		List<DefaultMutableTreeNode> children = new ArrayList<DefaultMutableTreeNode>(n);
		for (int i = 0; i < n; i++) {
			children.add((DefaultMutableTreeNode) parent.getChildAt(i));
		}
		Collections.sort(children, new Comparator< DefaultMutableTreeNode>() {
			@Override
			public int compare(DefaultMutableTreeNode a, DefaultMutableTreeNode b) {
				String sa = a.getUserObject().toString();
				String sb = b.getUserObject().toString();
				return sa.compareToIgnoreCase(sb);
			}
		}); //iterative merge sort
		parent.removeAllChildren();
		for (DefaultMutableTreeNode node: children) {
			parent.add(node);
		}
	}
	

	/** Return at least one DefaultMutableTreeNode by concept in the ontology.
	 *  If one concept can have several ancestor, only one (arbitrary) is choose.
	 * @return
	 */
	public Collection<DefaultMutableTreeNode> getAllFirstNodes() {
		Collection<DefaultMutableTreeNode> result = new Vector<DefaultMutableTreeNode>();
		for(List<DefaultMutableTreeNode> nodes : conceptToTreeNode.values()) {
			result.add(nodes.get(0));
		}
		return result;
	}
	
    private Comparator<DefaultMutableTreeNode> nodeComparator = new Comparator<DefaultMutableTreeNode>() {
    	@Override
		public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
    		return o1.toString().compareTo(o2.toString());
    	}
    };
    	
	public Collection<DefaultMutableTreeNode> accept(final Predicate<DefaultMutableTreeNode> predicate, final int limit) {
		final Collection<DefaultMutableTreeNode> result = new TreeSet<DefaultMutableTreeNode>(nodeComparator);
		try {
			container.accept(new OntoVisitor<ONTORES>() {
				@Override
				public void visit(ONTORES concept) {
					DefaultMutableTreeNode node = new DefaultMutableTreeNode(
							new TreeNodeOntologyObject<ONTORES>(
									MyTreeModel.this,
									container,
									concept,
									false
							)
					);
					if(predicate.apply(node)) {
						result.add(node);
						if(result.size() >= limit) {
							// Cheat but shut...
							StringBuilder msg = new StringBuilder();
							msg.append(Messages.getString("FirstLimitPartNodes"));
							msg.append(" ");
							msg.append(limit);
							msg.append(" ");
							msg.append(Messages.getString("LastLimitPartNodes"));
							result.add(new DefaultMutableTreeNode(msg.toString()));
							throw new ArrayIndexOutOfBoundsException();
						}
					}
				}
			});
		} catch (ArrayIndexOutOfBoundsException e) {}
		return result;
	}
}
