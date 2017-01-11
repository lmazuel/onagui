package fr.onagui.alignment;

import java.net.URI;
import java.util.Collections;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/** Represente un alignement en deux ontologies.
 * @author Laurent Mazuel
 *
 * @param <ONTORES1> Le type primitif d'un concept dans l'ontologie 1.
 * @param <ONTORES2> Le type primitif d'un concept dans l'ontologie 2.
 */
public class Alignment<ONTORES1, ONTORES2> {

	// Les deux ontologies mise en jeu dans ce mapping
	private OntoContainer<ONTORES1> onto1 = null;
	private OntoContainer<ONTORES2> onto2 = null;

	// La liste ordonnée de tous les mappings
	private SortedSet<Mapping<ONTORES1, ONTORES2>> mapping = null;

	// Les index vers les sous-ensembles
	private SortedMap<URI, SortedSet<Mapping<ONTORES1, ONTORES2>>> index1 = null;
	private SortedMap<URI, SortedSet<Mapping<ONTORES1, ONTORES2>>> index2 = null;

	// Les index vers les concepts considérés comme mapping impossible
	private SortedMap<URI, NoMappingPossible<ONTORES1>> impossibleMapping1 = null;
	private SortedMap<URI, NoMappingPossible<ONTORES2>> impossibleMapping2 = null;

	/**
	 * @param onto1
	 * @param onto2
	 */
	public Alignment(OntoContainer<ONTORES1> onto1, OntoContainer<ONTORES2> onto2) {
		this.onto1 = onto1;
		this.onto2 = onto2;
		mapping = new TreeSet<Mapping<ONTORES1,ONTORES2>>();
		// Building the indexes
		// For onto1
		index1 = new TreeMap<URI, SortedSet<Mapping<ONTORES1,ONTORES2>>>();
		impossibleMapping1 = new TreeMap<URI, NoMappingPossible<ONTORES1>>();			
		// For onto2
		index2 = new TreeMap<URI, SortedSet<Mapping<ONTORES1,ONTORES2>>>();
		impossibleMapping2 = new TreeMap<URI, NoMappingPossible<ONTORES2>>();
	}

	public OntoContainer<ONTORES1> getOnto1() {
		return onto1;
	}

	public OntoContainer<ONTORES2> getOnto2() {
		return onto2;
	}

	/** Return an unmodifiable view of the index for ontology 1.
	 * @return
	 * @see Collections#unmodifiableSortedMap(SortedMap)
	 */
	public SortedMap<URI, SortedSet<Mapping<ONTORES1, ONTORES2>>> getIndex1() {
		return Collections.unmodifiableSortedMap(index1);
	}

	/** Return an unmodifiable view of the index for ontology 2.
	 * @return
	 * @see Collections#unmodifiableSortedMap(SortedMap)
	 */
	public SortedMap<URI, SortedSet<Mapping<ONTORES1, ONTORES2>>> getIndex2() {
		return Collections.unmodifiableSortedMap(index2);
	}

	/** Return an unmodifiable view of all mapping.
	 * @return
	 */
	public SortedSet<Mapping<ONTORES1, ONTORES2>> getMapping() {
		return Collections.unmodifiableSortedSet(mapping);
	}

	/** Return an unmodifiable view of not possible mapping from ontology 1.
	 * @return
	 */
	public SortedMap<URI, NoMappingPossible<ONTORES1>> getImpossibleMapping1() {
		return Collections.unmodifiableSortedMap(impossibleMapping1);
	}

	/** Return an unmodifiable view of not possible mapping from ontology 2.
	 * @return
	 */
	public SortedMap<URI, NoMappingPossible<ONTORES2>> getImpossibleMapping2() {
		return Collections.unmodifiableSortedMap(impossibleMapping2);
	}

	public synchronized void addMap(Mapping<ONTORES1,ONTORES2> map) {
		// Adding to first index
		if(index1.containsKey(onto1.getURI(map.getFirstConcept()))) {
			index1.get(onto1.getURI(map.getFirstConcept())).add(map);
		}
		else {
			TreeSet<Mapping<ONTORES1,ONTORES2>> index1map = new TreeSet<Mapping<ONTORES1,ONTORES2>>();
			index1map.add(map);
			index1.put(onto1.getURI(map.getFirstConcept()), index1map);
		}
		// Adding to second index
		if(index2.containsKey(onto2.getURI(map.getSecondConcept()))) {
			index2.get(onto2.getURI(map.getSecondConcept())).add(map);
		}
		else {
			TreeSet<Mapping<ONTORES1,ONTORES2>> index2map = new TreeSet<Mapping<ONTORES1,ONTORES2>>();
			index2map.add(map);
			index2.put(onto2.getURI(map.getSecondConcept()), index2map);
		}
		// Adding to all mapping list
		mapping.add(map);
	}

	public void removeMap(Mapping<ONTORES1,ONTORES2> map) {
		// Remove from first index
		if(index1.containsKey(onto1.getURI(map.getFirstConcept()))) {
			index1.get(onto1.getURI(map.getFirstConcept())).remove(map);
		}
		// Remove from second index
		if(index2.containsKey(onto2.getURI(map.getSecondConcept()))) {
			index2.get(onto2.getURI(map.getSecondConcept())).remove(map);
		}
		// Remove from all concepts
		mapping.remove(map);
	}

	public void removeMapFromConcept1(ONTORES1 concept1) {
		URI concept1URI = onto1.getURI(concept1);
		if(index1.containsKey(concept1URI)) {
			for(Mapping<ONTORES1,ONTORES2> map : index1.get(concept1URI)) {
				URI concept2URI = onto2.getURI(map.getSecondConcept());
				if(index2.containsKey(concept2URI)) {
					index2.get(concept2URI).remove(map);
					mapping.remove(map);
				}				
			}
			index1.remove(concept1URI);
		}
	}

	public void removeMapFromConcept2(ONTORES2 concept2) {
		URI concept2URI = onto2.getURI(concept2);
		if(index2.containsKey(concept2URI)) {
			for(Mapping<ONTORES1,ONTORES2> map : index2.get(concept2URI)) {
				URI concept1URI = onto1.getURI(map.getFirstConcept());
				if(index1.containsKey(concept1URI)) {
					index1.get(concept1URI).remove(map);
					mapping.remove(map);
				}				
			}
			index2.remove(concept2URI);
		}
	}

	public SortedSet<Mapping<ONTORES1, ONTORES2>> getAllMappingFor1(ONTORES1 ontores) {
		if(index1.containsKey(onto1.getURI(ontores))) {
			return Collections.unmodifiableSortedSet(index1.get(onto1.getURI(ontores)));
		}
		else {
			return new TreeSet<Mapping<ONTORES1,ONTORES2>>();
		}
	}

	public SortedSet<Mapping<ONTORES1, ONTORES2>> getAllMappingFor2(ONTORES2 ontores) {
		if(index2.containsKey(onto2.getURI(ontores))) {
			return Collections.unmodifiableSortedSet(index2.get(onto2.getURI(ontores)));
		}
		else {
			return new TreeSet<Mapping<ONTORES1,ONTORES2>>();
		}
	}

	public void addImpossibleMappingFrom1(NoMappingPossible<ONTORES1> notMap) {
		impossibleMapping1.put(onto1.getURI(notMap.getConcept()), notMap);
	}

	public void addImpossibleMappingFrom2(NoMappingPossible<ONTORES2> notMap) {
		impossibleMapping2.put(onto2.getURI(notMap.getConcept()), notMap);
	}

	public void removeImpossibleMappingFrom1(ONTORES1 concept) {
		impossibleMapping1.remove(onto1.getURI(concept));
	}

	public void removeImpossibleMappingFrom2(ONTORES2 concept) {
		impossibleMapping2.remove(onto2.getURI(concept));
	}

	/** Add mappings from alignment in parameters.
	 * DO NOT MAKE A COPY OF NEW MAPPINGS, DIRECTLY USE THE SAME REFERENCE.
	 * @param alignment Another alignment.
	 */
	public void addAll(Alignment<ONTORES1, ONTORES2> alignment) {
		if(!alignment.getOnto1().equals(onto1) || !alignment.getOnto2().equals(onto2)) {
			throw new IllegalArgumentException("addAll exception: ontologies must be the same, not only the same type.");
		}

		for(Mapping<ONTORES1, ONTORES2> newMappings : alignment.getMapping()) {
			addMap(newMappings);
		}
		for(NoMappingPossible<ONTORES1> nomap : alignment.getImpossibleMapping1().values()) {
			addImpossibleMappingFrom1(nomap);
		}
		for(NoMappingPossible<ONTORES2> nomap : alignment.getImpossibleMapping2().values()) {
			addImpossibleMappingFrom2(nomap);
		}
	}

	/** Return the mapping between the specified concepts.
	 * @param cpt1 
	 * @param cpt2
	 * @return
	 */
	public SortedSet<Mapping<ONTORES1, ONTORES2>> subSet(ONTORES1 cpt1, ONTORES2 cpt2) {
		SortedSet<Mapping<ONTORES1, ONTORES2>> result = new TreeSet<Mapping<ONTORES1,ONTORES2>>();
		URI cpt1URI = onto1.getURI(cpt1);
		if(index1.containsKey(cpt1URI)) {
			for(Mapping<ONTORES1, ONTORES2> map : index1.get(cpt1URI)) {
				if(map.getSecondConcept().equals(cpt2))
					result.add(map);
			}
		}
		return result;
	}

	public boolean isImpossibleToAlign1(ONTORES1 concept) {
		return impossibleMapping1.containsKey(onto1.getURI(concept));
	}

	public boolean isImpossibleToAlign2(ONTORES2 concept) {
		return impossibleMapping2.containsKey(onto2.getURI(concept));
	}

	public boolean alignExist1(ONTORES1 concept) {
		return index1.containsKey(onto1.getURI(concept));
	}

	public boolean alignExist2(ONTORES2 concept) {
		return index2.containsKey(onto2.getURI(concept));
	}
}
