package fr.onagui.alignment.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.OntoVisitor;

/**
 * An abstract implementation of an OntoContainer that encapsulates an RDF4J Model
 * and that provides basic method implementations.
 */
public abstract class AbstractModelContainer implements OntoContainer<Resource> {
	
	private String formalism;
	
	// The Model holding our data
	protected Model model;
	
	// URI of the File from where the model was loaded
	protected URI uri;

	public AbstractModelContainer(String formalism, File loadedFile) throws RDFParseException, UnsupportedRDFormatException, FileNotFoundException, IOException {
		super();
		this.formalism = formalism;
		this.uri = loadedFile.toURI();
		
		// load the RDF data
		this.model = Rio.parse(
				new FileInputStream(loadedFile),
				loadedFile.getName(),
				Rio.getParserFormatForFileName(loadedFile.getName()).orElse(RDFFormat.RDFXML)
		);
	}
	
	public AbstractModelContainer(String formalism) throws RDFParseException, UnsupportedRDFormatException, FileNotFoundException, IOException {
		super();
		this.formalism = formalism;
	}
	
	protected void init(Model model, URI uri) {
		this.model = model;
		this.uri = uri;
	}
	
	
	
	/**
	 * Returns the formalism String passed as a parameter in the constructor
	 */
	@Override
	public String getFormalism() {
		return formalism;
	}
	
	/**
	 * Returns the IRI of the given concept in the Model, or a fake IRI starting with "bnode:" + the BNode ID for blank nodes
	 * @param cpt
	 * @return
	 */
	@Override
	public URI getURI(Resource cpt) {
		if(cpt instanceof IRI) {
			return URI.create(((IRI)cpt).stringValue());
		} else {
			return URI.create("bnode:"+((BNode)cpt).getID());
		}
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
	 * Returns a Resource with the given URI, or the blank node with the given BNode ID if the URI starts with "bnode:"
	 */
	@Override
	public Resource getConceptFromURI(URI uri) {
		if(uri.getScheme().equals("bnode")) {
			return SimpleValueFactory.getInstance().createBNode(uri.toString().substring("bnode:".length()));
		} else {
			return SimpleValueFactory.getInstance().createIRI(uri.toString());
		}		
	}
	
	/**
	 * Returns a URI from the loaded file path
	 */
	@Override
	public URI getURI() {
		return this.uri;
	}
	
	/**
	 * Returns rdfs:resource
	 */
	@Override
	public Resource getRoot() {
		// Create a fake root with rdfs:resource URI
		return SimpleValueFactory.getInstance().createIRI("http://www.w3.org/2000/01/rdf-schema#Resource");
	}	
	
	/**
	 * Returns the first value of a DCTERMS.MODIFIED on the given Resource
	 */
	@Override
	public Optional<Date> getModifiedDate(Resource cpt) {
		Set<Value> dctermsValues = model.filter(cpt, DCTERMS.MODIFIED, null).objects();
		for (Value aValue : dctermsValues) {
			if(aValue instanceof Literal) {
				try {
					return Optional.of(((Literal)aValue).calendarValue().toGregorianCalendar().getTime());
				} catch (Exception e) {
					System.err.println("Cannot get value as date : "+aValue.stringValue());
				}
			}
		}
		return Optional.empty();
	}
	
	private class AllConceptCollector implements OntoVisitor<Resource> {
		private Set<Resource> result = new HashSet<Resource>();
		
		@Override
		public void visit(Resource concept) {
			result.add(concept);
		}
	}
	
	
}
