package fr.onagui.alignment.container;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.OntoVisitor;

/**
 * A 4-wheels-driving implementation of an OntoContainer capable of reading any RDF files
 * by breaking the instances into classes, and that reads the pref and alt labels of each entries
 * from a list of common labelling properties (rdfs:label, schema:name, skos:prefLabel, foaf:name, dcterms:title, dc:title)
 * 
 * @author Thomas Francart
 *
 */
public class RDFModelContainer extends AbstractModelContainer implements OntoContainer<Resource> {

	private static final String FORMALISM = "RDF";
	
	// The list of types used in the data, as a cached value
	private Set<Resource> types;
	
	// The list of languages in the data
	private SortedSet<String> allLanguageInLabels = null;
	
	// The properties considered as pref labels
	private Set<IRI> prefLabelsIri = new HashSet<IRI>(Arrays.asList(new IRI[] { 
			RDFS.LABEL,
			SKOS.PREF_LABEL,
			FOAF.NAME,
			SimpleValueFactory.getInstance().createIRI("http://schema.org/name"),
			DCTERMS.TITLE,
			DC.TITLE
	}));
	
	private Set<IRI> altLabelsIri = new HashSet<IRI>(Arrays.asList(new IRI[] { 
			SKOS.ALT_LABEL,
			FOAF.NICK,
			SimpleValueFactory.getInstance().createIRI("http://schema.org/alternateName"),
			DCTERMS.ALTERNATIVE
	}));
	
	public RDFModelContainer(File loadedFile) throws RDFParseException, UnsupportedRDFormatException, FileNotFoundException, IOException {
		super(FORMALISM, loadedFile);
		// load the types
		this.types = readAllTypes();
		
		// load the languages of all 		
		this.allLanguageInLabels = getLanguagesOfProperties(this.prefLabelsIri);
	}

	/**
	 * Returns all the predicates used in this Model
	 */
	@Override
	public Set<Resource> getAllProperties() {
		Set<Resource> result = new HashSet<Resource>();
		model.predicates().forEach(iri -> {
			result.add(iri);
		});
		return result;
	}

	/**
	 * Return all the predicates having a literal value on this concept
	 */
	@Override
	public Set<String> getAnnotations(Resource cpt) {
		return model.filter(cpt, null, null).stream()
				.filter(s -> s.getObject() instanceof Literal)
				.map(s -> s.getPredicate().toString() )
				.collect(Collectors.toSet());
	}

	@Override
	public Set<String> getLabels(Resource cpt, String prop) {
		return getPropertyValuesAsString(cpt, SimpleValueFactory.getInstance().createIRI(prop));
	}

	/**
	 * We consider nothing like an individual
	 */
	@Override
	public boolean isIndividual(Resource cpt) {
		return false;
	}

	@Override
	public Set<Resource> getChildren(Resource cpt) {
		if(cpt.equals(getRoot())) {
			// if on the root, list all the types
			return this.types;
		}
		if(this.types.contains(cpt)) {
			// if on a type, then list all of its instances
			return listInstances(cpt);
		} else {
			// return an empty set
			return Collections.emptySet();
		}
	}

	@Override
	public Set<Resource> getParents(Resource cpt) {
		if(cpt.equals(getRoot())) {
			return Collections.emptySet();
		}
		if(this.types.contains(cpt)) {
			return Collections.singleton(getRoot());
		} else {
			// return all the types of the Resource
			return listTypes(cpt);
		}
	}

	/**
	 * Visits all the URI in the file that are subjects of an RDF.TYPE
	 */
	@Override
	public void accept(OntoVisitor<Resource> visitor) {
		model.filter(null, RDF.TYPE, null).subjects().forEach(r -> {
			visitor.visit(r);
		});
	}

	/**
	 * Returns the possible languages of RDFS.LABEL
	 */
	@Override
	public SortedSet<String> getAllLanguageInLabels() {
		return allLanguageInLabels;
	}

	/**
	 * Returns the values of all the properties considered as prefLabels of this concept in the given lang
	 */
	@Override
	public Set<String> getPrefLabels(Resource cpt, String lang) {
		Set<String> result = new HashSet<>();
		for (IRI aPrefLabelProperty : this.prefLabelsIri) {
			result.addAll(getPropertyValuesAsString(cpt, aPrefLabelProperty, lang));
		}
		return result;
	}

	/**
	 * Returns the values of all the properties considered as prefLabels of the concept
	 */
	@Override
	public Set<String> getPrefLabels(Resource cpt) {
		Set<String> result = new HashSet<>();
		for (IRI aPrefLabelProperty : this.prefLabelsIri) {
			result.addAll(getPropertyValuesAsString(cpt, aPrefLabelProperty));
		}
		return result;
	}

	/**
	 * Returns the values of all the properties considered as altLabels of this concept in the given lang
	 */
	@Override
	public Set<String> getAltLabels(Resource cpt, String lang) {
		Set<String> result = new HashSet<>();
		for (IRI anAltLabelProperty : this.altLabelsIri) {
			result.addAll(getPropertyValuesAsString(cpt, anAltLabelProperty, lang));
		}
		return result;
	}

	/**
	 * Returns the values of all the properties considered as altLabels of this concept
	 */
	@Override
	public Set<String> getAltLabels(Resource cpt) {
		Set<String> result = new HashSet<>();
		for (IRI anAltLabelProperty : this.altLabelsIri) {
			result.addAll(getPropertyValuesAsString(cpt, anAltLabelProperty));
		}
		return result;
	}

	private Set<String> getPropertyValuesAsString(Resource cpt, IRI prop) {
		if (cpt == null)
			throw new IllegalArgumentException("cpt cannot be null");
		
		// a nice one-liner to list all the objects of the given property
		// and convert them as string values
		return model.filter(cpt, prop, null)
				.objects().stream()
				.map(v -> v.stringValue())
				.collect(Collectors.toSet());
	}
	
	private Set<String> getPropertyValuesAsString(Resource cpt, IRI prop, String lang) {
		if (cpt == null)
			throw new IllegalArgumentException("cpt cannot be null");
		
		// a nice one-liner to list all the objects of the given property
		// and convert them as string values
		return model.filter(cpt, prop, null).objects().stream()
				.filter(v -> { 
					// TODO : need a proper check to compare "fr" and "fr-FR"
					return ((v instanceof Literal) && ((Literal)v).getLanguage().isPresent() && ((Literal)v).getLanguage().get().equals(lang));
				})
				.map(v -> { return v.stringValue(); })
				.collect(Collectors.toSet());
	}
	
	private SortedSet<String> getLanguagesOfProperties(Set<IRI> props) {
		SortedSet<String> result = new TreeSet<String>();
		
		// il y a surement moyen de combiner plusieurs Stream entre elles
		// ... mais c'est compliqué
		// ... voir http://stackoverflow.com/questions/22740464/adding-two-java-8-streams-or-an-extra-element-to-a-stream#comment34676045_22741520
		
		for (IRI aPrefLabelProperty : this.prefLabelsIri) {
			model.filter(null, aPrefLabelProperty, null).objects().stream()
				.filter(v -> v instanceof Literal)
				.map(v -> ((Literal)v).getLanguage() )
				.forEach(v -> {
					if(v.isPresent()) { result.add(v.get()); }
				});
		}
		
		return result;
	}

	
	private Set<Resource> readAllTypes() {
		Set<Resource> result = new HashSet<Resource>();
		model.filter(null, RDF.TYPE, null).forEach(s -> {
			if(s.getObject() instanceof IRI) {
				result.add((IRI)s.getObject());
			}
		});
		return result;
	}
	
	private Set<Resource> listInstances(Resource cpt) {
		return model.filter(null, RDF.TYPE, cpt).subjects();
	}
	
	private Set<Resource> listTypes(Resource cpt) {
		Set<Resource> result = new HashSet<Resource>();
		model.filter(cpt, RDF.TYPE, null).forEach(s -> {
			if(s.getObject() instanceof IRI) {
				result.add((IRI)s.getObject());
			}
		});
		return result;
	}
	
	
	
}
