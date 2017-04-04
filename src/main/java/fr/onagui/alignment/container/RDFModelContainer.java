package fr.onagui.alignment.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
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
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.OntoVisitor;

public class RDFModelContainer implements OntoContainer<Resource> {

	private final String formalism = "RDF";
	
	// The Model holding our data
	private Model model;
	
	// File from where the model was loaded
	private File loadedFile;
	
	// The list of types used in the data, as a cached value
	private Set<Resource> types;
	
	// The list of languages in the data
	private SortedSet<String> allLanguageInLabels = null;
	
	public RDFModelContainer(File loadedFile) throws RDFParseException, UnsupportedRDFormatException, FileNotFoundException, IOException {
		super();
		this.loadedFile = loadedFile;
		
		// load the RDF data
		this.model = Rio.parse(
				new FileInputStream(loadedFile),
				loadedFile.getName(),
				Rio.getParserFormatForFileName(loadedFile.getName()).orElse(RDFFormat.RDFXML)
		);		
		
		// load the types
		this.types = readAllTypes();
		
		// load the languages
		this.allLanguageInLabels = getLanguagesOfProperty(RDFS.LABEL);
	}

	@Override
	public String getFormalism() {
		return formalism;
	}

	@Override
	public URI getURI(Resource cpt) {
		return (cpt instanceof IRI)?URI.create(((IRI)cpt).stringValue()):null;
	}

	/**
	 * Calls accept() with a Collector to retrieve all concepts
	 */
	@Override
	public Set<Resource> getAllConcepts() {
		AllConceptCollector collector = new AllConceptCollector();
		this.accept(collector);
		return collector.result;
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

	@Override
	public Resource getConceptFromURI(URI uri) {
		return SimpleValueFactory.getInstance().createIRI(uri.toString());
	}

	@Override
	public URI getURI() {
		// return a URI from the loaded file path
		return this.loadedFile.toURI();
	}

	@Override
	public Resource getRoot() {
		// Create a fake root with OWL Thing uri. Better than nothing... (joke).
		return SimpleValueFactory.getInstance().createIRI("http://www.w3.org/2002/07/owl#Thing");
	}

	@Override
	public boolean isIndividual(Resource cpt) {
		// we consider everything like an individual
		return true;
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

	/**
	 * Returns the first value of a DCTERMS.MODIFIED on the given Concept
	 */
//	@Override
//	public Optional<Date> getModifiedDate(Resource cpt) {
//		Set<Value> dctermsValues = model.filter(cpt, DCTERMS.MODIFIED, null).objects();
//		for (Value aValue : dctermsValues) {
//			if(aValue instanceof Literal) {
//				try {
//					return Optional.of(((Literal)aValue).calendarValue().toGregorianCalendar().getTime());
//				} catch (Exception e) {
//					System.err.println("Cannot get value as date : "+aValue.stringValue());
//				}
//			}
//		}
//		return Optional.empty();
//	}

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
	 * Returns the RDFS.LABEL of the concept in the given lang
	 */
	@Override
	public Set<String> getPrefLabels(Resource cpt, String lang) {
		return getPropertyValuesAsString(cpt, RDFS.LABEL, lang);
	}

	/**
	 * Returns the RDFS.LABEL of the concept
	 */
	@Override
	public Set<String> getPrefLabels(Resource cpt) {
		return getPropertyValuesAsString(cpt, RDFS.LABEL);
	}

	/**
	 * Returns an empty set. Alt labels are not handled in this generic RDF container.
	 */
	@Override
	public Set<String> getAltLabels(Resource cpt, String lang) {
		return Collections.emptySet();
	}

	/**
	 * Returns an empty set. Alt labels are not handled in this generic RDF container.
	 */
	@Override
	public Set<String> getAltLabels(Resource cpt) {
		return Collections.emptySet();
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
	
	private SortedSet<String> getLanguagesOfProperty(IRI prop) {
		SortedSet<String> result = new TreeSet<String>();
		model.filter(null, prop, null)
				.objects().stream()
				.filter(v -> v instanceof Literal)
				.map(v -> ((Literal)v).getLanguage() )
				.forEach(v -> {
					if(v.isPresent()) { result.add(v.get()); }
				});
		
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
	
	private class AllConceptCollector implements OntoVisitor<Resource> {
		private Set<Resource> result = new HashSet<Resource>();
		
		@Override
		public void visit(Resource concept) {
			result.add(concept);
		}
	}
	
}
