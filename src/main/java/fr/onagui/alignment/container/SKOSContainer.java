/**
 * 
 */
package fr.onagui.alignment.container;

import info.aduna.iteration.Iterations;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.sail.memory.MemoryStore;

import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.OntoVisitor;

/**
 * @author Laurent Mazuel
 */
public class SKOSContainer implements OntoContainer<Resource> {

	private Repository triplestore = null;
	private ValueFactory factory = null;
	private URI onto_uri = null;
	
	private static Map<org.openrdf.model.URI, boolean[]> propertyForConcepts = null;
	static {
		/* First value is "subject is concept", second "object is concept" */
		propertyForConcepts = new HashMap<org.openrdf.model.URI, boolean[]>();
		propertyForConcepts.put(SKOS.TOP_CONCEPT_OF, new boolean[] {true, false});
		propertyForConcepts.put(SKOS.HAS_TOP_CONCEPT, new boolean[] {false, true});
		propertyForConcepts.put(SKOS.BROADER, new boolean[] {true, true});
		propertyForConcepts.put(SKOS.BROADER_TRANSITIVE, new boolean[] {true, true});
		propertyForConcepts.put(SKOS.NARROWER, new boolean[] {true, true});
		propertyForConcepts.put(SKOS.NARROWER_TRANSITIVE, new boolean[] {true, true});
	}

	private Set<String> languages = null; // Lazy initialization
	private Set<Resource> conceptSchemes = null; // Lazy initialization
	private Map<Resource, Resource> topConceptOfCache = null; // Cache

	public SKOSContainer(File physicalPath) throws RepositoryException,
			RDFParseException, IOException {
		
		triplestore = new SailRepository(new MemoryStore());
		triplestore.initialize();
		factory = triplestore.getValueFactory();

		RepositoryConnection connect = triplestore.getConnection();
		// Try RDF/XML, fallback to N3 and fail if it's not enough
		try {
			connect.add(physicalPath, null, RDFFormat.RDFXML);
		} catch (RDFParseException e) {
			connect.add(physicalPath, null, RDFFormat.N3);
		}
		connect.close();

		onto_uri = physicalPath.toURI();
		
		// Preload
		getAllLanguageInLabels();
		topConceptOfCache = new HashMap<Resource, Resource>();
		for(Resource scheme: getConceptSchemes()) {
			for(Resource topConcept: getTopConcepts(scheme)) {
				topConceptOfCache.put(topConcept, scheme);
			}
		}
	}
	
	private class SkosConceptHandler extends RDFHandlerBase {
		private OntoVisitor<Resource> myvisitor;
		
		public SkosConceptHandler(OntoVisitor<Resource> visitor) {
			myvisitor = visitor;
		}
		
		@Override
		public void handleStatement(Statement stmt) throws RDFHandlerException {
			org.openrdf.model.URI predicate = stmt.getPredicate();
			if(predicate.equals(RDF.TYPE) || propertyForConcepts.get(predicate)[0]) { // Subject is concept
				myvisitor.visit(stmt.getSubject());
			} 
			else if (propertyForConcepts.get(predicate)[1]) { // Object is concept
				myvisitor.visit((Resource) stmt.getObject());
			}
		}
	}
	
	@Override
	public void accept(OntoVisitor<Resource> visitor) {
		SkosConceptHandler myhandler = new SkosConceptHandler(visitor);
		RepositoryConnection connect = null;
		try {
			connect = triplestore.getConnection();
			connect.exportStatements(null, RDF.TYPE, SKOS.CONCEPT, true, myhandler);
			for(org.openrdf.model.URI property: propertyForConcepts.keySet()) {
				connect.exportStatements(null, property, null, true, myhandler);
			}
		} catch (RepositoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RDFHandlerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if(connect != null) {
				try {
					connect.close();
				} catch (RepositoryException e) {}
			}
		}
	}


	@Override
	public String getFormalism() {
		return "skos";
	}
	
	private Set<Resource> getAllFromType(org.openrdf.model.URI type) {
		Set<Resource> result = new HashSet<Resource>();
		try {
			RepositoryConnection connect = triplestore.getConnection();
			RepositoryResult<Statement> stmts = connect.getStatements(null,
					RDF.TYPE, type, true);
			List<Statement> stmts_list = Iterations.asList(stmts);
			for (Statement s : stmts_list) {
				result.add(s.getSubject());
			}
			connect.close();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private class SkosConceptCollector implements OntoVisitor<Resource>{
		private Set<Resource> result = new HashSet<Resource>();
		public Set<Resource> getResult() {
			return result;
		}
		@Override
		public void visit(Resource concept) {
			result.add(concept);
		}
	}
	
	@Override
	public Set<Resource> getAllConcepts() {
		SkosConceptCollector collector = new SkosConceptCollector();
		accept(collector);
		return collector.getResult();
	}
	
	@Override
	public boolean isIndividual(Resource cpt) {
		return false;
	}
	
	@Override
	public Set<Resource> getChildren(Resource cpt) {
		if(cpt.equals(getRoot())) {
			return getConceptSchemes();
		}
		if(conceptSchemes.contains(cpt)) {
			return getTopConcepts(cpt);
		}
		/* "usual" concept */
		Set<Resource> result = new HashSet<Resource>();
		try {
			RepositoryConnection connect = triplestore.getConnection();
			for(Statement res : getStatementWhereSubject(connect, cpt)) {
				if(res.getPredicate().equals(SKOS.NARROWER) ||
				   res.getPredicate().equals(SKOS.NARROWER_TRANSITIVE)) {
					result.add((Resource) res.getObject());
				}
			}
			for(Statement res : getStatementWhereObject(connect, cpt)) {
				if(res.getPredicate().equals(SKOS.BROADER) ||
				   res.getPredicate().equals(SKOS.BROADER_TRANSITIVE)) {
					result.add(res.getSubject());
				}
			}
			connect.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public Set<Resource> getParents(Resource cpt) {
		if(cpt.equals(getRoot())) {
			return new HashSet<Resource>();
		}
		if(conceptSchemes.contains(cpt)) {
			return Collections.singleton(getRoot());
		}
		if(topConceptOfCache.containsKey(cpt)) {
			return Collections.singleton(topConceptOfCache.get(cpt));
		}
		/* "usual" concept */
		Set<Resource> result = new HashSet<Resource>();
		try {
			RepositoryConnection connect = triplestore.getConnection();
			for(Statement res : getStatementWhereSubject(connect, cpt)) {
				if(res.getPredicate().equals(SKOS.BROADER) ||
				   res.getPredicate().equals(SKOS.BROADER_TRANSITIVE)) {
					result.add((Resource) res.getObject());
				}
			}
			for(Statement res : getStatementWhereObject(connect, cpt)) {
				if(res.getPredicate().equals(SKOS.NARROWER) ||
				   res.getPredicate().equals(SKOS.NARROWER_TRANSITIVE)) {
					result.add(res.getSubject());
				}
			}
			connect.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public Set<Resource> getTopConcepts(Resource scheme) {
		Set<Resource> result = new HashSet<Resource>();
		RepositoryResult<Statement> stmts = null;
		RepositoryConnection connect = null;
		try {
			connect = triplestore.getConnection();
			// Has top concept
			stmts = connect.getStatements(scheme, SKOS.HAS_TOP_CONCEPT, null, true);
			for (Statement s : Iterations.asList(stmts)) {
				result.add((Resource) s.getObject());
			}
			// top concept of
			stmts = connect.getStatements(null, SKOS.TOP_CONCEPT_OF, scheme, true);
			for (Statement s : Iterations.asList(stmts)) {
				result.add(s.getSubject());
			}
			connect.close();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;		
	}
	
	private List<Statement> getStatementWhereSubject(RepositoryConnection connect, Resource res) {
		try {
			RepositoryResult<Statement> stmts = connect.getStatements(res, null, null, true);
			return Iterations.asList(stmts);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return new Vector<Statement>();
	}

	private List<Statement> getStatementWhereObject(RepositoryConnection connect, Resource res) {
		try {
			RepositoryResult<Statement> stmts = connect.getStatements(null, null, res, true);
			return Iterations.asList(stmts);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return new Vector<Statement>();
	}	
	
	private Set<Resource> getSubjectsWhereProp(RepositoryConnection connect, org.openrdf.model.URI prop) {
		Set<Resource> result = new HashSet<Resource>();
		// Has top concept
		try {
			RepositoryResult<Statement> stmts = connect.getStatements(null, prop, null, true);
			List<Statement> stmts_list = Iterations.asList(stmts);
			for (Statement s : stmts_list) {
				result.add(s.getSubject());
			}
			return result;
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return result;
	}

	private Set<Resource> getResourceObjectWhereProp(RepositoryConnection connect, org.openrdf.model.URI prop) {
		Set<Resource> result = new HashSet<Resource>();
		// Has top concept
		try {
			RepositoryResult<Statement> stmts = connect.getStatements(null, prop, null, true);
			List<Statement> stmts_list = Iterations.asList(stmts);
			for (Statement s : stmts_list) {
				try {
					result.add((Resource) s.getObject());
				} catch (ClassCastException e) {
					// Nothing, is a literal
				}
			}
			return result;
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public Set<Resource> getAllProperties() {
		return Collections.emptySet();
	}

	@Override
	public Set<Resource> getAllAnnotationsProperty() {
		Set<Resource> result = new HashSet<Resource>();
		result.add(SKOS.PREF_LABEL);
		result.add(SKOS.ALT_LABEL);
		return result;
	}

	@Override
	public URI getURI(Resource cpt) {
		try {
			return new URI(((org.openrdf.model.URI) cpt).toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Strange resource...");
		}
	}

	private Set<String> getLabels(Resource cpt, org.openrdf.model.URI prop) {
		if (cpt == null)
			throw new IllegalArgumentException("cpt cannot be null");
		Set<String> result = new HashSet<String>();
		// "prefLabel"
		try {
			RepositoryConnection connect = triplestore.getConnection();
			RepositoryResult<Statement> stmts = connect.getStatements(cpt,
					prop, null, true);
			List<Statement> stmts_list = Iterations.asList(stmts);
			for (Statement s : stmts_list) {
				Literal literal = (Literal) s.getObject();
				result.add(literal.getLabel());
			}
			connect.close();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return result;
	}

	private Set<String> getLabels(Resource cpt, String lang,
			org.openrdf.model.URI prop) {
		if (cpt == null || lang == null)
			throw new IllegalArgumentException(
					"cpt or lang cannot be null: cpt=" + cpt + " lang=" + lang);
		Set<String> result = new HashSet<String>();
		// "prefLabel"
		try {
			RepositoryConnection connect = triplestore.getConnection();
			RepositoryResult<Statement> stmts = connect.getStatements(cpt,
					prop, null, true);
			List<Statement> stmts_list = Iterations.asList(stmts);
			for (Statement s : stmts_list) {
				Literal literal = (Literal) s.getObject();
				if ((lang.equals("") && literal.getLanguage() == null)
						|| (lang.equals(literal.getLanguage())))
					result.add(literal.getLabel());
			}
			connect.close();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public Set<String> getPrefLabels(Resource cpt) {
		return getLabels(cpt, SKOS.PREF_LABEL);
	}

	@Override
	public Set<String> getPrefLabels(Resource cpt, String lang) {
		return getLabels(cpt, lang, SKOS.PREF_LABEL);
	}

	@Override
	public Set<String> getAltLabels(Resource cpt) {
		return getLabels(cpt, SKOS.ALT_LABEL);
	}

	@Override
	public Set<String> getAltLabels(Resource cpt, String lang) {
		return getLabels(cpt, lang, SKOS.ALT_LABEL);
	}

	@Override
	public Set<String> getAllLanguageInLabels() {
		if (this.languages == null) {
			this.languages = new TreeSet<String>();

			Collection<org.openrdf.model.URI> props = Arrays.asList(
					SKOS.PREF_LABEL, SKOS.ALT_LABEL);

			for (org.openrdf.model.URI prop : props) {
				try {
					RepositoryConnection connect = triplestore.getConnection();
					RepositoryResult<Statement> stmts = connect.getStatements(null,
							prop, null, true);
					List<Statement> stmts_list = Iterations.asList(stmts);

					for (Statement s : stmts_list) {
						Literal literal = (Literal) s.getObject();
						if (literal.getLanguage() != null)
							languages.add(literal.getLanguage());
					}
					connect.close();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
		}
		return languages;
	}

	@Override
	public Resource getConceptFromURI(URI uri) {
		return factory.createURI(uri.toString());
	}

	@Override
	public URI getURI() {
		return onto_uri;
	}

	@Override
	public Resource getRoot() {
		// Create a fake root with OWL Thing uri. Better than nothing... (joke).
		return factory.createURI("http://www.w3.org/2002/07/owl#Thing");
	}

	public Set<Resource> getConceptSchemes() {
		if(this.conceptSchemes == null) {
			try {
				RepositoryConnection connect = triplestore.getConnection();
				this.conceptSchemes = getAllFromType(SKOS.CONCEPT_SCHEME);
				this.conceptSchemes.addAll(getSubjectsWhereProp(connect, SKOS.HAS_TOP_CONCEPT));
				this.conceptSchemes.addAll(getResourceObjectWhereProp(connect, SKOS.TOP_CONCEPT_OF));
				connect.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return this.conceptSchemes;
	}
}