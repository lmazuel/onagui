/**
 * 
 */
package fr.onagui.alignment;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;

import fr.onagui.alignment.method.LevenshteinAlignmentMethod;


/**
 * @author Laurent Mazuel
 */
public class AlignmentFactory<ONTORES1, ONTORES2> {
		
	private PropertyChangeSupport propertyManagement = null;
	
	private boolean DEBUG_MODE = false;

	/** The event name used in property change listener */
	public static final String PROGRESS_EVENT_NAME = "progressRatio";
	
	private AbstractAlignmentMethod<ONTORES1, ONTORES2> alignmentMethod = null;
	
	public AlignmentFactory(AbstractAlignmentMethod<ONTORES1, ONTORES2> method) {
		propertyManagement = new PropertyChangeSupport(this);
		alignmentMethod = method;
	}
	
	public AlignmentFactory() {
		this(new LevenshteinAlignmentMethod<ONTORES1, ONTORES2>());
	}
	
	/** A change listener if you want to be aware of progression of alignement.
	 * Only launch {@link #PROGRESS_EVENT_NAME} event, with a ratio between 0 and 1 corresponding 
	 * to the task achievement.
	 * @param listener An instance of PropertyChangeListener.
	 * @see #PROGRESS_EVENT_NAME
	 * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyManagement.addPropertyChangeListener(listener);
	}
	
	/** remove a change listener if don't want to follow progress.
	 * @param listener A change listener.
	 * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
	 */
	public void removePropertyChangeListerner(PropertyChangeListener listener) {
		propertyManagement.removePropertyChangeListener(listener);
	}
	
	/** Compute a mapping between a set of nodes from Model1 to a set of nodes of model 2.
	 * @param model1 An ontology 1, used to get some information about concepts.
	 * @param model1Set A set of nodes from model 1.
	 * @param model2 An ontology 2.
	 * @param model2Set A set of nodes from model 2.
	 * @return A map where key are concept from model 1 and values are {@link Mapping}.
	 */
	public Alignment<ONTORES1, ONTORES2> computeMapping(
			OntoContainer<ONTORES1> model1,
			Set<ONTORES1> model1Set,
			OntoContainer<ONTORES2> model2,
			Set<ONTORES2> model2Set) {
		
		Alignment<ONTORES1, ONTORES2> alignment = new Alignment<ONTORES1, ONTORES2>(model1, model2);

		alignmentMethod.init(); // Init each time an alignment is made
		
		int numberOfConceptIn1 = model1Set.size();
		int madeConcepts = 0;
		double oldRatio = 0.0;

		if(DEBUG_MODE) {
			System.out.println("Methode d'alignement utilisé: "+alignmentMethod.getClass().getName());
		}
		
		long global_before = System.currentTimeMillis();
		long big_before = global_before;
		
		for(ONTORES1 cpt1Inst : model1Set) {
			if(DEBUG_MODE) {
				System.out.println("Concept Commencé: "+model1.getURI(cpt1Inst));
				System.out.println("C'est le concept "+(madeConcepts+1) + " sur "+numberOfConceptIn1);
				big_before = System.currentTimeMillis();
			}
			
			for(ONTORES2 cpt2Inst : model2Set) {
								
				Mapping<ONTORES1, ONTORES2> mapping = alignmentMethod.computeMapping(model1, cpt1Inst, model2, cpt2Inst);
				// Force the "method" filed to do not allow alignment method to write it
				if(mapping != null) {
					mapping.setMethod(alignmentMethod.toString());
					alignment.addMap(mapping);
				}
			}
			
			// Give property change to listener
			madeConcepts++;
			double progressRatio = ((double)madeConcepts)/((double)numberOfConceptIn1);
			propertyManagement.firePropertyChange(PROGRESS_EVENT_NAME, oldRatio, progressRatio);
			oldRatio = progressRatio;
			
			long big_after = System.currentTimeMillis();
			if(DEBUG_MODE) {
				System.out.println("Concept fini: "+model1.getURI(cpt1Inst));
				System.out.println("Nombre de concept aligné pour l'instant dans l'ontologie 1: "+alignment.getIndex1().size());
				System.out.println("Nombre de concept aligné pour l'instant dans l'ontologie 2: "+alignment.getIndex2().size());
				printTime(big_before, big_after);
			}

		}
		if(DEBUG_MODE) {
			System.out.println("Fin de l'alignement, nombre de concepts alignés de l'ontologie 1: "+alignment.getIndex1().size());
			System.out.println("Fin de l'alignement, nombre de concepts alignés de l'ontologie 2: "+alignment.getIndex2().size());
		}
		
		System.out.println("Alignment has taken:");
		printTime(global_before, System.currentTimeMillis());
		
		return alignment;
	}

	/** Compute a mapping between the entire set of concepts from Model1 to the entire set of concepts of model 2.
	 * Only use classes, use {@link #computeMapping(OntoContainer, Set, OntoContainer, Set)} to use properties.
	 * @param <ONTORES1> The container 1 type, at least the first instance type of AlignmentFactory.
	 * @param <ONTORES2> The container 2 type, at least the second instance type of AlignmentFactory.
	 * @param model1 An ontology 1.
	 * @param model2 An ontology 2.
	 * @return A map where key are concept from model 1 and values are {@link Mapping}.
	 */
	public Alignment<ONTORES1, ONTORES2> computeMapping(
			OntoContainer<ONTORES1> model1,
			OntoContainer<ONTORES2> model2) {
		
		Set<ONTORES1> model1Set = model1.getAllConcepts();
		Set<ONTORES2> model2Set = model2.getAllConcepts();
		return computeMapping(model1, model1Set, model2, model2Set);
	}
	
	/** Computes a set given a set of set. Typically used when parameter is a "values()" call from a Map where
	 * "value" are Set of something.
	 * @param <T> the type inside the set.
	 * @param mapping A mapping 
	 * @return A set.
	 * @see #computeMapping(OntoContainer, OntoContainer)
	 */
	public static <T> Set<T> flattenSet(Set<Set<T>> mapping) {
		Set<T> resultSet = new HashSet<T>();
		for(Set<T> set : mapping) {
			resultSet.addAll(set);
		}
		return resultSet;
	}

	/** Pretty print for a time (in secondes).
	 * @param before Time before action.
	 * @param after Time after action.
	 * @see System#currentTimeMillis()
	 */
	public static void printTime(long before, long after) {
		System.out.println("\t->Time : "+(((after - before))/1000.0)+"s");
	}
}
