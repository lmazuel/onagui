/**
 * 
 */
package fr.onagui.alignment;

import java.net.URI;
import java.util.Set;
import java.util.SortedSet;

/** Un meta-modèle d'ontologie pour l'interface avec le logiciel d'alignement. 
 * @author Laurent Mazuel
 * @param <ONTORES> Le type primitif d'un concept dans le formalisme choisi.
 */
public interface OntoContainer<ONTORES> {
	
	/* **************** *
	 * Partie technique *
	 * **************** */
	
	/** Une chaine de caractere representant le type de formalisme de cette ontologie.
	 * @return Une chaine de caractere representant le type de formalisme de cette ontologie.
	 */
	public String getFormalism();
	
	/** Renvoie l'URI d'un concept.
	 * @param cpt Un concept.
	 * @return L'URI de ce concept.
	 * @see #getConceptFromURI(URI)
	 */
	public URI getURI(ONTORES cpt);
	
	/** L'ensemble des concepts (classes) de ce modèle.
	 * @return L'ensemble des concepts (classes) de ce modèle.
	 */
	public Set<ONTORES> getAllConcepts();
	
	/** L'ensemble des propriétés de ce modèle.
	 * @return L'ensemble des propriétés de ce modèle.
	 */
	public Set<ONTORES> getAllProperties();
	
	/** Get Annotation of this concept
	 * @param cpt a Concept
	 * @return A set of URIs
	 */
	public Set<String> getAnnotations(ONTORES cpt);
	
	/** Get labels for this predicate
	 * @param cpt The concept
	 * @param prop The predicate
	 * @return The set of labels
	 */
	public Set<String> getLabels(ONTORES cpt, String prop);
		
	/** Recupère un concept à partir de l'URI.
	 * @param uri L'URI d'un concept.
	 * @return Un concept.
	 * @see #getURI()
	 */
	public ONTORES getConceptFromURI(URI uri);
		
	/** Renvoie l'URI de l'ontologie.
	 * @return L'URI de l'ontologie.
	 */
	public URI getURI();
	
	/** Renvoie la racine de l'ontologie.
	 * @return La racine de l'ontologie.
	 */
	public ONTORES getRoot();
	
	/** Decrit si le concept courant est une instance ou pas.
	 * @param cpt Un concept
	 * @return <code>true</code> si instance, <code>false</code> sinon. */
	public boolean isIndividual(ONTORES cpt);
	
	/** Les fils de ce concept.
	 * @param cpt Un concept
	 * @return Les fils de ce concept.
	 */
	public Set<ONTORES> getChildren(ONTORES cpt);
	
	/** Les parents de ce concept
	 * @param cpt Un concept
	 * @return Les parents directs de ce concept
	 */
	public Set<ONTORES> getParents(ONTORES cpt);
	
	/** A visitor which accept a callback on the concept 
	 * 
	 * @param visitor A visitor implementation
	 */
	public void accept(OntoVisitor<ONTORES> visitor);
	
	/* *************** *
	 * Partie lexicale *
	 * *************** */
	
	/** Renvoie la liste des langages utilisés dans les labels.
	 * @return
	 */
	public SortedSet<String> getAllLanguageInLabels();
	
	/** Retourne l'ensemble des termes préférés de ce concept de la langue en parametre.
	 * En general, le resultat est un singleton. 
	 * @param cpt Un concept.
	 * @param lang La langue demandée, format normalisé selon l'entrée "xml:lang". 
	 * La chaine vide "" pour n'avoir que les noeuds sans précision.
	 * @return L'ensemble des termes préférés de ce concept.
	 * @throws IllegalArgumentException Si l'un des paramètres est <code>null</code>.
	 */
	public Set<String> getPrefLabels(ONTORES cpt, String lang);
	
	/** Retourne l'ensemble des termes préférés de ce concept, quelque soit la langue.
	 * @param cpt Un concept.
	 * @return L'ensemble des termes préférés de ce concept.
	 * @throws IllegalArgumentException Si l'un des paramètres est <code>null</code>.
	 */
	public Set<String> getPrefLabels(ONTORES cpt);

	/** Retourne l'ensemble des termes alternatifs de ce concept de la langue en parametre.
	 * @param cpt Un concept.
	 * @param lang La langue demandée, format normalisé selon l'entrée "xml:lang". 
	 * La chaine vide "" pour n'avoir que les noeuds sans précision.
	 * @return L'ensemble des termes alternatifs de ce concept.
	 * @throws IllegalArgumentException Si l'un des paramètres est <code>null</code>.
	 */
	public Set<String> getAltLabels(ONTORES cpt, String lang);
	
	/** Retourne l'ensemble des termes alternatifs de ce concept, quelque soit la langue.
	 * @param cpt Un concept.
	 * @return L'ensemble des termes alternatifs de ce concept.
	 * @throws IllegalArgumentException Si l'un des paramètres est <code>null</code>.
	 */
	public Set<String> getAltLabels(ONTORES cpt);
	
}
