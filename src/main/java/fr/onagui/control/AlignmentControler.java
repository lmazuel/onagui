package fr.onagui.control;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.onagui.alignment.AbstractAlignmentMethod;
import fr.onagui.alignment.Alignment;
import fr.onagui.alignment.AlignmentFactory;
import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.Mapping.MAPPING_TYPE;
import fr.onagui.alignment.Mapping.VALIDITY;
import fr.onagui.alignment.NoMappingPossible;
import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.OntoTools;
import fr.onagui.alignment.io.CSVImpl;
import fr.onagui.alignment.io.EuzenatRDFImpl;
import fr.onagui.alignment.io.IOAlignment;
import fr.onagui.alignment.io.SkosImpl;
import fr.onagui.alignment.method.ExactAlignmentMethod;
import fr.onagui.alignment.method.ISubAlignmentMethod;
import fr.onagui.alignment.method.LevenshteinAlignmentMethod;
import fr.onagui.gui.Messages;


public class AlignmentControler<ONTORES1, ONTORES2> {

	private Alignment<ONTORES1, ONTORES2> alignment = null;
	private OntoContainer<ONTORES1> container1 = null;
	private OntoContainer<ONTORES2> container2 = null;

	private MyTreeModel<ONTORES1> treeModel1 = null;
	private MyTreeModel<ONTORES2> treeModel2 = null;

	private Set<AbstractAlignmentMethod<ONTORES1, ONTORES2>> methods = null;

	private class IOEventManagerJDialog implements IOAlignment.IOEventManager {

		private int nbWarning = 0;
		
		@Override
		public void inputEvent(String msg) {
			System.err.println(msg);
			++nbWarning;
		}

		@Override
		public void outputEvent(String msg) {
			System.err.println(msg);
			++nbWarning;
		}
		
		public void reset() {
			nbWarning = 0;
		}
		
		public void showDialog() {
			if(nbWarning > 0) {
				JOptionPane.showMessageDialog(null,
						Messages.getString("AlignmentControler.0"), //$NON-NLS-1$
						Messages.getString("AlignmentControler.1"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	private IOAlignment ioEuzenatManager = null;
	private IOAlignment ioCsvManager = null;
	private IOAlignment ioSkosManager = null;
	private IOEventManagerJDialog ioEventManager = null;
	
	
	public AlignmentControler() {
		alignment = new Alignment<ONTORES1, ONTORES2>(null, null);
		ioEventManager = new IOEventManagerJDialog();
		ioEuzenatManager = new EuzenatRDFImpl(ioEventManager);
		ioCsvManager = new CSVImpl();
		ioSkosManager = new SkosImpl(ioEventManager);
		methods = new HashSet<AbstractAlignmentMethod<ONTORES1, ONTORES2>>();
		Set<Class<? extends AbstractAlignmentMethod>> classes = new HashSet<Class<? extends AbstractAlignmentMethod>>();

		// Pas mal générique, maintenant il faudrait que je charge ça à la volée...
		classes.add(LevenshteinAlignmentMethod.class.asSubclass(AbstractAlignmentMethod.class));
		classes.add(ISubAlignmentMethod.class.asSubclass(AbstractAlignmentMethod.class));
		classes.add(ExactAlignmentMethod.class.asSubclass(AbstractAlignmentMethod.class));
		methods = buildInstancesFromClass(classes);
	}

	public Set<AbstractAlignmentMethod<ONTORES1, ONTORES2>> buildInstancesFromClass(Set<Class<? extends AbstractAlignmentMethod>> classes) {
		Set<AbstractAlignmentMethod<ONTORES1, ONTORES2>> methods = new HashSet<AbstractAlignmentMethod<ONTORES1, ONTORES2>>();
		for(Class<? extends AbstractAlignmentMethod> c : classes) {
			try {
				// Appel du constructeur par défaut
				AbstractAlignmentMethod inst = c.newInstance();
				methods.add(inst);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}		
		return methods;
	}

	public Set<AbstractAlignmentMethod<ONTORES1, ONTORES2>> getLoadedAlignmentMethods() {
		return methods;
	}

	public void setContainer1(OntoContainer<ONTORES1> container1) {
		this.container1 = container1;
		treeModel1 = new MyTreeModel<ONTORES1>(container1);
		alignment = new Alignment<ONTORES1, ONTORES2>(container1, container2);
	}

	public void setContainer2(OntoContainer<ONTORES2> container2) {
		this.container2 = container2;
		treeModel2 = new MyTreeModel<ONTORES2>(container2);
		alignment = new Alignment<ONTORES1, ONTORES2>(container1, container2);
	}

	public void clearAllAlignments() {
		alignment = new Alignment<ONTORES1, ONTORES2>(container1, container2);
	}

	public void setReloadContainer1(OntoContainer<ONTORES1> container1) {
		this.container1 = container1;
		treeModel1 = new MyTreeModel<ONTORES1>(container1);
		// FIXME faire quelque chose à alignement ou c'est bon?
	}

	public void setReloadContainer2(OntoContainer<ONTORES2> container2) {
		this.container2 = container2;
		treeModel2 = new MyTreeModel<ONTORES2>(container2);
		// FIXME faire quelque chose à alignement ou c'est bon?
	}

	public boolean hasContainer1() {
		return container1 != null;
	}

	public boolean hasContainer2() {
		return container2 != null;
	}

	public URI getContainer1URI() {
		return hasContainer1()?container1.getURI():null;
	}

	public URI getContainer2URI() {
		return hasContainer2()?container2.getURI():null;
	}

	public void notFoundChange(boolean notFoundState, int ontoNumber, TreeNodeOntologyObject attachedConcept) {
		// Maintenant, not found est allumé
		if(notFoundState) {
			// Ajouter le fait que ce concept devient impossible
			if(ontoNumber == 1) {
				NoMappingPossible<ONTORES1> noMap = new NoMappingPossible<ONTORES1>((ONTORES1)attachedConcept.getConcept());
				alignment.addImpossibleMappingFrom1(noMap);
				// Je ne retire pas forcèment les mapping associés, au cas ou l'utilisateur fait une fausse manip
			}
			else {
				NoMappingPossible<ONTORES2> noMap = new NoMappingPossible<ONTORES2>((ONTORES2)attachedConcept.getConcept());
				alignment.addImpossibleMappingFrom2(noMap);
				// Je ne retire pas forcèment les mapping associés, au cas ou l'utilisateur fait une fausse manip				
			}
		}
		// Not found est maintenant eteint
		else {
			// Retirer le fait que ce concept est impossible
			if(ontoNumber == 1) {
				alignment.removeImpossibleMappingFrom1((ONTORES1)attachedConcept.getConcept());
			}
			else {
				alignment.removeImpossibleMappingFrom2((ONTORES2)attachedConcept.getConcept());
			}
		}
	}

	public boolean alignExist(TreeNodeOntologyObject node, int number) {
		if(number == 1) {
			ONTORES1 cpt = (ONTORES1)node.getConcept();
			return alignment.alignExist1(cpt);
		}
		else {
			ONTORES2 cpt = (ONTORES2)node.getConcept();
			return alignment.alignExist2(cpt);
		}		
	}

	public SortedSet<Mapping<ONTORES1, ONTORES2>> getAllMappingOfConcept(TreeNodeOntologyObject node, int number) {
		if(number == 1) {
			ONTORES1 cpt = (ONTORES1)node.getConcept();
			return alignment.getIndex1().get(container1.getURI(cpt));
		}
		else {
			ONTORES2 cpt = (ONTORES2)node.getConcept();
			return alignment.getIndex2().get(container2.getURI(cpt));
		}
	}

	public SortedSet<String> getLanguagesUsedInOnto(int number) {
		switch (number) {
		case 1:
			return container1.getAllLanguageInLabels();
		case 2:
			return container2.getAllLanguageInLabels();
		}	
		throw new IllegalArgumentException("Number must be 1 or 2, not: "+number); //$NON-NLS-1$
	}

	public void setCurrentLanguage(int num, String lang) {
		if(lang == null) {
			throw new IllegalArgumentException("Lang cannot be null"); //$NON-NLS-1$
		}
		switch (num) {
		case 1:
			treeModel1.setCurrentShowingLanguage(lang);
			break;
		case 2:
			treeModel2.setCurrentShowingLanguage(lang);
		}
	}

	public MyTreeModel getTreeModel1() {
		return treeModel1;
	}

	public MyTreeModel getTreeModel2() {
		return treeModel2;
	}

	public boolean isImpossibleToAlign1(TreeNodeOntologyObject<ONTORES1> concept) {
		return alignment.isImpossibleToAlign1(concept.getConcept());
	}

	public boolean isImpossibleToAlign2(TreeNodeOntologyObject<ONTORES2> concept) {
		return alignment.isImpossibleToAlign2(concept.getConcept());
	}

	public SortedSet<Mapping<ONTORES1, ONTORES2>> subSet(TreeNodeOntologyObject<ONTORES1> node1, TreeNodeOntologyObject<ONTORES2> node2) {
		return alignment.subSet(node1.getConcept(), node2.getConcept());
	}

	public SortedSet<Mapping<ONTORES1, ONTORES2>> subSet(TreeNodeOntologyObject<?> node1, int number) {
		if(number == 1) {
			return alignment.getAllMappingFor1((ONTORES1)node1.getConcept());
		}
		else {
			return alignment.getAllMappingFor2((ONTORES2)node1.getConcept());
		}
	}

	public SortedSet<Mapping<ONTORES1, ONTORES2>> getAllMapping() {
		return alignment.getMapping();
	}

	public void addMapping(TreeNodeOntologyObject<ONTORES1> node1, TreeNodeOntologyObject<ONTORES2> node2, double score, MAPPING_TYPE type, String method) {
		ONTORES1 cpt1 = node1.getConcept();
		ONTORES2 cpt2 = node2.getConcept();
		Mapping<ONTORES1, ONTORES2> map = new Mapping<ONTORES1, ONTORES2>(cpt1, cpt2, score, type, method, VALIDITY.VALID);
		alignment.addMap(map);
	}

	public void removeMapping(TreeNodeOntologyObject<ONTORES1> node1, TreeNodeOntologyObject<ONTORES2> node2, double score, MAPPING_TYPE type) {
		ONTORES1 cpt1 = node1.getConcept();
		ONTORES2 cpt2 = node2.getConcept();
		Mapping<ONTORES1, ONTORES2> map = new Mapping<ONTORES1, ONTORES2>(cpt1, cpt2, score, type);
		alignment.removeMap(map);
	}

	public void removeMapping(Mapping<ONTORES1, ONTORES2> map) {
		alignment.removeMap(map);
	}

	public void computeAndAddMapping(AbstractAlignmentMethod<ONTORES1, ONTORES2> method, PropertyChangeListener listener) { 
		AlignmentFactory<ONTORES1, ONTORES2> factory = new AlignmentFactory<ONTORES1, ONTORES2>(method);
		factory.addPropertyChangeListener(listener);
		Alignment<ONTORES1, ONTORES2> newAlignment = factory.computeMapping(container1, container2);
		alignment.addAll(newAlignment);
	}

	public void computeAndAddMapping(
			AbstractAlignmentMethod<ONTORES1, ONTORES2> method,
			PropertyChangeListener listener,
			DefaultMutableTreeNode rootFrom1,
			DefaultMutableTreeNode rootFrom2) {
		// D'abord, calculer l'ensemble des concepts en Jeu

		// Pour le container 1
		TreeNodeOntologyObject<ONTORES1> userObject1 = (TreeNodeOntologyObject<ONTORES1>) rootFrom1.getUserObject();
		Set<ONTORES1> concepts1 = OntoTools.getAllDescendants(container1, userObject1.getConcept());
		
		// Pour le container 2
		TreeNodeOntologyObject<ONTORES2> userObject2 = (TreeNodeOntologyObject<ONTORES2>) rootFrom2.getUserObject();
		Set<ONTORES2> concepts2 = OntoTools.getAllDescendants(container2, userObject2.getConcept());

		// Maintenant on peux faire l'alignement
		AlignmentFactory<ONTORES1, ONTORES2> factory = new AlignmentFactory<ONTORES1, ONTORES2>(method);
		factory.addPropertyChangeListener(listener);
		Alignment<ONTORES1, ONTORES2> newAlignment = factory.computeMapping(
				container1,
				concepts1,
				container2,
				concepts2);
		alignment.addAll(newAlignment);
	}

	public StatCounter computeStatFor(DefaultMutableTreeNode treeNode, int number) {
		Enumeration<DefaultMutableTreeNode> allSubNodes = treeNode.breadthFirstEnumeration();
		StatCounter cnt = new StatCounter();
		while(allSubNodes.hasMoreElements()) {
			DefaultMutableTreeNode node = allSubNodes.nextElement();
			if(node.getUserObject() == null) continue;

			if(number == 1) {
				TreeNodeOntologyObject<ONTORES1> userObject = (TreeNodeOntologyObject<ONTORES1>)node.getUserObject();
				if(isImpossibleToAlign1(userObject)) cnt.countNotFound();
				else if (alignment.alignExist1(userObject.getConcept())) cnt.countFound();
				else cnt.countNotSearch();
			}
			else {
				TreeNodeOntologyObject<ONTORES2> userObject = (TreeNodeOntologyObject<ONTORES2>)node.getUserObject();
				if(isImpossibleToAlign2(userObject)) cnt.countNotFound();
				else if (alignment.alignExist2(userObject.getConcept())) cnt.countFound();
				else cnt.countNotSearch();
			}
		}
		return cnt;
	}

	/** Determine si le concept de l'interface doit être écrasé.
	 * Seul le 3em parametre a le droit d'être <code>null</code>, dans ce cas l'entrée {@link ERASE_TYPE#FOUND_BUT_NOT_BY_RED} est équivalente
	 * à l'entrée {@link ERASE_TYPE#ERASE_ALL}.
	 * @param eraseType Le type d'ecrasement de données.
	 * @param cptInfoFromGUI Le concept de l'interface.
	 * @param newConcept Le concept qui vas peut-etre écraser (utile uniquement pour interpreter {@link ERASE_TYPE#FOUND_BUT_NOT_BY_RED}, peutêtre <code>null</code>)
	 * @return <code>true</code> si il faut ecraser le concept, <code>false</code> sinon.
	 */
	//	public static boolean conceptAMetterAJour(ERASE_TYPE eraseType, ConceptInfo<?> cptInfoFromGUI, ConceptInfo<?> newConcept) {
	//	// Dans l'interface il existe quelque chose et je ne veux rien ecraser
	//	if(eraseType == ERASE_TYPE.NO_ERASE && (cptInfoFromGUI.isNotFound() || cptInfoFromGUI.alignExist())) return false;
	//	// Si j'accepte d'écraser les not found uniquement, je n'ecrase pas les alignements existants
	//	if(eraseType == ERASE_TYPE.NOTFOUND_ONLY && cptInfoFromGUI.alignExist()) return false;
	//	// Si j'accepte d'écraser les found uniquement, je n'ecrase pas les not-found existant
	//	if(eraseType == ERASE_TYPE.FOUND_ONLY && cptInfoFromGUI.isNotFound()) return false;
	//	// Ecrase tout, mais n'ecrase pas un vert par un rouge
	//	if(eraseType == ERASE_TYPE.FOUND_BUT_NOT_BY_RED && cptInfoFromGUI.alignExist() && newConcept != null && newConcept.isNotFound()) return false;
	//	// Sinon, j'écrase tous...
	//	return true;
	//	}

	public URI getURIOfConcept(TreeNodeOntologyObject treeNode, int number) {
		if(number == 1) {
			ONTORES1 cpt = (ONTORES1)treeNode.getConcept();
			return container1.getURI(cpt);
		}
		else {
			ONTORES2 cpt = (ONTORES2)treeNode.getConcept();
			return container2.getURI(cpt);			
		}
	}

	public Set<String> getPrefLabelsOfConcept(TreeNodeOntologyObject treeNode, int number) {
		if(number == 1) {
			ONTORES1 cpt = (ONTORES1)treeNode.getConcept();
			return container1.getPrefLabels(cpt);
		}
		else {
			ONTORES2 cpt = (ONTORES2)treeNode.getConcept();
			return container2.getPrefLabels(cpt);			
		}		
	}

	public Set<String> getAltLabelsOfConcept(TreeNodeOntologyObject treeNode, int number) {
		if(number == 1) {
			ONTORES1 cpt = (ONTORES1)treeNode.getConcept();
			return container1.getAltLabels(cpt);
		}
		else {
			ONTORES2 cpt = (ONTORES2)treeNode.getConcept();
			return container2.getAltLabels(cpt);			
		}		
	}

	public Set<String> getAnnotationsUri(TreeNodeOntologyObject treeNode, int number) {
		if(number == 1) {
			ONTORES1 cpt = (ONTORES1)treeNode.getConcept();
			return container1.getAnnotations(cpt);
		}
		else {
			ONTORES2 cpt = (ONTORES2)treeNode.getConcept();
			return container2.getAnnotations(cpt);
		}		
	}	

	public Set<String> getLabels(TreeNodeOntologyObject treeNode, String predicateUri, int number) {
		if(number == 1) {
			ONTORES1 cpt = (ONTORES1)treeNode.getConcept();
			return container1.getLabels(cpt, predicateUri);
		}
		else {
			ONTORES2 cpt = (ONTORES2)treeNode.getConcept();
			return container2.getLabels(cpt, predicateUri);
		}		
	}		
	
	public boolean openRdfAlign(File file) {
		try {
			ioEventManager.reset();
			Alignment<ONTORES1, ONTORES2> alignment = ioEuzenatManager.loadAlignment(container1, container2, file);
			this.alignment.addAll(alignment);
			ioEventManager.showDialog();
			return true;
		}
		catch (Exception e) {
			System.err.println(Messages.getString("AlignmentControler.2")); //$NON-NLS-1$
			System.err.println(e.getMessage());
			JOptionPane.showMessageDialog(null, Messages.getString("AlignmentControler.5")+ //$NON-NLS-1$
					Messages.getString("AlignmentControler.6"), //$NON-NLS-1$
					Messages.getString("AlignmentControler.7"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	public boolean openSkosAlign(File file) {
		try {
			ioEventManager.reset();
			Alignment<ONTORES1, ONTORES2> alignment = ioSkosManager.loadAlignment(container1, container2, file);
			this.alignment.addAll(alignment);
			ioEventManager.showDialog();
			return true;
		}
		catch (Exception e) {
			System.err.println(Messages.getString("AlignmentControler.2")); //$NON-NLS-1$
			System.err.println(e.getMessage());
			JOptionPane.showMessageDialog(null, Messages.getString("AlignmentControler.5")+ //$NON-NLS-1$
					Messages.getString("AlignmentControler.6"), //$NON-NLS-1$
					Messages.getString("AlignmentControler.7"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public boolean openCsvAlign(File file) {
		try {
			ioEventManager.reset();
			Alignment<ONTORES1, ONTORES2> alignment = ioCsvManager.loadAlignment(container1, container2, file);
			this.alignment.addAll(alignment);
			ioEventManager.showDialog();
			return true;
		} 
		catch (Exception e) {
			System.err.println(Messages.getString("AlignmentControler.2")); //$NON-NLS-1$
			System.err.println(e.getMessage());
			JOptionPane.showMessageDialog(null, Messages.getString("AlignmentControler.5")+ //$NON-NLS-1$
					Messages.getString("AlignmentControler.6"), //$NON-NLS-1$
					Messages.getString("AlignmentControler.7"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public void saveRdfAlign(String path, VALIDITY validityWanted) throws IOException {
		ioEventManager.reset();
		ioEuzenatManager.saveAlignment(alignment, path, validityWanted);
		ioEventManager.showDialog();
	}

	public void saveCsvAlign(String path) throws IOException {
		ioEventManager.reset();
		ioCsvManager.saveAlignment(alignment, path, null); // CSV export all
		ioEventManager.showDialog();
	}

	public void saveSkosAlign(String path) throws IOException {
		ioEventManager.reset();
		ioSkosManager.saveAlignment(alignment, path, null); // SKOS export all
		ioEventManager.showDialog();
	}
	
	/** Calcule quelque stat sur sysout
	 * La complexité est atroce, mais c'est pas grave pour notre situation.
	 * @param root La racine a considérer
	 */
	public String computeStatis(DefaultMutableTreeNode root, int number) {
		int horsAlignement = 0;
		int aligne = 0;
		int topAlign = 0;
		int botAlign = 0;

		StringBuilder buf = new StringBuilder();
		Set<? extends Object> allConcepts;
		if(number == 1) allConcepts = container1.getAllConcepts();
		else allConcepts = container2.getAllConcepts();
		int totalAlignment = 0;
		for(Object cpt : allConcepts) {
			DefaultMutableTreeNode currentNode;
			if(number == 1) currentNode = treeModel1.getFirstNodeFromConcept((ONTORES1) cpt);
			else currentNode = treeModel2.getFirstNodeFromConcept((ONTORES2) cpt);
			// Compte le nombre d'alignement
			if(currentNode.getUserObject() instanceof TreeNodeOntologyObject) {
				if(number == 1) {
					TreeNodeOntologyObject<ONTORES1> userObject = (TreeNodeOntologyObject<ONTORES1>)currentNode.getUserObject();
					if (alignment.alignExist1(userObject.getConcept())) totalAlignment++;
				}
				else {
					TreeNodeOntologyObject<ONTORES2> userObject = (TreeNodeOntologyObject<ONTORES2>)currentNode.getUserObject();
					if (alignment.alignExist2(userObject.getConcept())) totalAlignment++;
				}
			}
			// Quelques flags
			boolean topAligne = hasParentAligned(currentNode, number);
			boolean botAligne = hasChildAligned(currentNode, number);

			if(topAligne && botAligne) aligne++;
			else if(topAligne && !botAligne) botAlign++;
			else if(!topAligne && botAligne) topAlign++;
			else if(!topAligne && !botAligne) horsAlignement++;
			else {
				System.err.println("Ne devrait pas être là... Concept en cause: "+currentNode.getUserObject()); //$NON-NLS-1$
			} // Impossible, j'ai ma table booléenne en entier
		}

		int total = horsAlignement + aligne + topAlign + botAlign;
		buf.append("\n"+Messages.getString("AlignmentControler.10")+total); //$NON-NLS-1$
		buf.append("\n"+Messages.getString("AlignmentControler.11")+totalAlignment+" ("+(totalAlignment*100/total)+"%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n"+Messages.getString("AlignmentControler.14")); //$NON-NLS-1$
		buf.append("\n"+Messages.getString("AlignmentControler.15")+horsAlignement+ " ("+(horsAlignement*100/total)+"%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n"+Messages.getString("AlignmentControler.18")+topAlign+ " ("+(topAlign*100/total)+"%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n"+Messages.getString("AlignmentControler.21")+botAlign+ " ("+(botAlign*100/total)+"%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n"+Messages.getString("AlignmentControler.24")+aligne+ " ("+(aligne*100/total)+"%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return buf.toString();
	}	

	/** Test if in path to ancestor where is (or not) a node aligned.
	 * Recursive fonction.
	 * @param node
	 * @return
	 */
	private boolean hasParentAligned(DefaultMutableTreeNode node, int number) {
		if(node == null) return false;
		Object userObject = node.getUserObject();
		if(userObject instanceof TreeNodeOntologyObject) {
			TreeNodeOntologyObject nodeObject = (TreeNodeOntologyObject)userObject;
			if(number == 1 && alignment.alignExist1((ONTORES1)nodeObject.getConcept())) {
				return true;
			}
			if(number == 2 && alignment.alignExist2((ONTORES2)nodeObject.getConcept())) {
				return true;
			}
		}
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
		return hasParentAligned(parent, number);
	}

	/** Test is there is at least one child which is aligned.
	 * Non recursive fonction.
	 * @param node
	 * @return
	 */
	private boolean hasChildAligned(DefaultMutableTreeNode node, int number) {
		Enumeration<DefaultMutableTreeNode> allSubNodes = node.breadthFirstEnumeration();
		while(allSubNodes.hasMoreElements()) {
			// Le noeud "node" en parametre est inclu dans la boucle, donc je m'auto-test dans la foulée
			DefaultMutableTreeNode subNode = allSubNodes.nextElement();
			Object userObject = subNode.getUserObject();
			if(userObject instanceof TreeNodeOntologyObject) {
				TreeNodeOntologyObject nodeObject = (TreeNodeOntologyObject)userObject;
				if(number == 1 && alignment.alignExist1((ONTORES1)nodeObject.getConcept())) {
					return true;
				}
				if(number == 2 && alignment.alignExist2((ONTORES2)nodeObject.getConcept())) {
					return true;
				}
			}
		}
		return false;
	}

	/** Assigne le fait que tous les descendants du noeud courants doivent être marqué comme "non trouvé"
	 * @param node
	 */
	private void notFoundHierarchical(DefaultMutableTreeNode node) {
		Enumeration<DefaultMutableTreeNode> allSubNodes = node.breadthFirstEnumeration();
		while(allSubNodes.hasMoreElements()) {
			DefaultMutableTreeNode subNode = allSubNodes.nextElement();
			Object userObject = subNode.getUserObject();
			//			if(userObject instanceof ConceptInfo) {
			//			ConceptInfo nodeInfo = (ConceptInfo)userObject;
			//			nodeInfo.setNotFound(true);
			//			}
		}
	}

	//	public static void compareTreeModel(String model1Name, DefaultMutableTreeNode model1, String model2Name, DefaultMutableTreeNode model2) {
	//	Enumeration<DefaultMutableTreeNode> nodesFrom1 = model1.breadthFirstEnumeration();

	//	int count = 0;
	//	int nbCount = 0;

	//	System.out.println("Comparaison entre "+model1Name+ " et "+model2Name);
	//	while(nodesFrom1.hasMoreElements()) {
	//	DefaultMutableTreeNode nodeFrom1 = nodesFrom1.nextElement();
	//	Object userObject1 = nodeFrom1.getUserObject();
	//	if(userObject1 instanceof ConceptInfo) {
	//	ConceptInfo nodeInfo1 = (ConceptInfo)userObject1;
	//	if(!nodeInfo1.alignExist()) continue;
	//	String sno1 = nodeInfo1.getSnoLabel().trim();

	//	Enumeration<DefaultMutableTreeNode> nodesFrom2 = model2.breadthFirstEnumeration();
	//	while(nodesFrom2.hasMoreElements()) {
	//	DefaultMutableTreeNode nodeFrom2 = nodesFrom2.nextElement();
	//	Object userObject2 = nodeFrom2.getUserObject();
	//	if(userObject2 instanceof ConceptInfo) {
	//	ConceptInfo nodeInfo2 = (ConceptInfo)userObject2;
	//	if(!nodeInfo2.alignExist()) continue;		
	//	String sno2 = nodeInfo2.getSnoLabel().trim();

	//	nbCount++;
	//	if(sno1.equals(sno2)) {
	//	System.out.printf("%s: %s et %s: %s <=> Snomed: %s, %s\n", 
	//	model1Name,
	//	nodeInfo1.toString(),
	//	model2Name,
	//	nodeInfo2.toString(),
	//	sno1,
	//	nodeInfo1.getSnoComment());
	//	count++;
	//	}
	//	}
	//	}
	//	}
	//	}
	//	System.out.println("Nombre trouvé: "+count);
	//	System.out.println("Nombre analysé: "+nbCount);
	//	}

	//	public static void compareTreeModelV3(
	//	String model1Name,
	//	DefaultMutableTreeNode model1,
	//	String model2Name,
	//	DefaultMutableTreeNode model2,
	//	String model3Name,
	//	DefaultMutableTreeNode model3) {

	//	Enumeration<DefaultMutableTreeNode> nodesFrom1 = model1.breadthFirstEnumeration();

	//	int count = 0;
	//	int nbCount = 0;

	//	while(nodesFrom1.hasMoreElements()) {
	//	DefaultMutableTreeNode nodeFrom1 = nodesFrom1.nextElement();
	//	Object userObject1 = nodeFrom1.getUserObject();
	//	if(userObject1 instanceof ConceptInfo) {
	//	ConceptInfo nodeInfo1 = (ConceptInfo)userObject1;
	//	if(!nodeInfo1.alignExist()) continue;
	//	String sno1 = nodeInfo1.getSnoLabel().trim();

	//	Enumeration<DefaultMutableTreeNode> nodesFrom2 = model2.breadthFirstEnumeration();
	//	while(nodesFrom2.hasMoreElements()) {
	//	DefaultMutableTreeNode nodeFrom2 = nodesFrom2.nextElement();
	//	Object userObject2 = nodeFrom2.getUserObject();
	//	if(userObject2 instanceof ConceptInfo) {
	//	ConceptInfo nodeInfo2 = (ConceptInfo)userObject2;
	//	if(!nodeInfo2.alignExist()) continue;		
	//	String sno2 = nodeInfo2.getSnoLabel().trim();

	//	Enumeration<DefaultMutableTreeNode> nodesFrom3 = model3.breadthFirstEnumeration();
	//	while(nodesFrom3.hasMoreElements()) {
	//	DefaultMutableTreeNode nodeFrom3 = nodesFrom3.nextElement();
	//	Object userObject3 = nodeFrom3.getUserObject();
	//	if(userObject3 instanceof ConceptInfo) {
	//	ConceptInfo nodeInfo3 = (ConceptInfo)userObject3;
	//	if(!nodeInfo3.alignExist()) continue;		
	//	String sno3 = nodeInfo3.getSnoLabel().trim();

	//	nbCount++;
	//	if(sno1.equals(sno2) && sno2.equals(sno3)) {
	//	System.out.printf("%s: %s et %s: %s et %s: %s<=> Snomed: %s, %s\n", 
	//	model1Name,
	//	nodeInfo1.toString(),
	//	model2Name,
	//	nodeInfo2.toString(),
	//	model3Name,
	//	nodeInfo3.toString(),
	//	sno1,
	//	nodeInfo1.getSnoComment());
	//	count++;
	//	}
	//	}
	//	}
	//	}
	//	}
	//	}
	//	}
	//	System.out.println("Nombre trouvé: "+count);
	//	System.out.println("Nombre analysé: "+nbCount);
	//	}

}
