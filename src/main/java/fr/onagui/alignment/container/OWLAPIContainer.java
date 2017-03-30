/**
 * 
 */
package fr.onagui.alignment.container;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.OntoVisitor;


/**
 * @author Laurent Mazuel
 */
public class OWLAPIContainer implements OntoContainer<OWLEntity> {

	protected URI filename = null;
	private Repository triplestore = null;
	protected OWLOntology ontology = null;
	protected OWLOntologyManager manager = null;
	protected OWLDataFactory df;
	protected OWLReasoner reasoner = null;

	private SortedSet<String> languages = null;

	public OWLAPIContainer(URI filename) throws OWLOntologyCreationException {
		try {
			
			
			this.filename = filename;
			manager = OWLManager.createOWLOntologyManager();
			df = manager.getOWLDataFactory();
			ontology = manager.loadOntologyFromOntologyDocument(
					new FileDocumentSource(new File(filename.getPath())));
			System.out.println("Loaded");
			StructuralReasonerFactory reasonerFact = new StructuralReasonerFactory();	
			reasoner = reasonerFact.createReasoner(ontology);
			System.out.println("Reasoned");
			// Pour eviter un long calcul
			getAllLanguageInLabels();
			System.out.println("Found");
			triplestore = new SailRepository(new MemoryStore());
			triplestore.initialize();
			/*RepositoryConnection connect = triplestore.getConnection();
			File file=new File(filename.getPath());
			// Try RDF/XML, fallback to N3 and fail if it's not enough
			try {
				try {
					connect.add(file, null, RDFFormat.RDFXML);
				} catch (RDFParseException e) {
					connect.add(file, null, RDFFormat.N3);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			connect.close();*/
			
//			try {
//				System.out.println("Let's try Sesame");
//				OwlTripleStore ts = Utilities.getOwlTripleStore(ontology, true);
//				Repository sesame_repo = ts.getRepository();
//				RepositoryConnection sesame_connect = sesame_repo.getConnection();
//				System.out.println("I have: "+sesame_connect.size()+" statements");
//			} catch (RepositoryException e) {
//				System.err.println("Sesame Error!!!!");
//				e.printStackTrace();
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private class MyOwlVisitor extends OWLOntologyWalkerVisitor<OWLEntity> {
		private OntoVisitor<OWLEntity> visitor;
		public MyOwlVisitor(OWLOntologyWalker walker, OntoVisitor<OWLEntity> myvisitor) {
			super(walker);
			visitor = myvisitor;
		}

		@Override
		public OWLEntity visit(OWLClass desc) {
			if(!desc.isOWLNothing()) {
				visitor.visit(desc);
			}
			return super.visit(desc);
		}
		@Override
		public OWLEntity visit(OWLNamedIndividual individual) {
			visitor.visit(individual);
			return super.visit(individual);
		}
	}
	
	@Override
	public void accept(OntoVisitor<OWLEntity> visitor) {
		OWLOntologyWalker ontoWalker = new OWLOntologyWalker(Collections.singleton(ontology));
		OWLOntologyWalkerVisitor<OWLEntity> ontolvisitor = new MyOwlVisitor(ontoWalker, visitor);			
		ontoWalker.walkStructure(ontolvisitor);		
	}
	
	@Override
	public String getFormalism() {
		return "owl";
	}

	@Override
	public Set<OWLEntity> getAllConcepts() {
		// Only class and properties
		Set<OWLEntity> result = new HashSet<OWLEntity>();
		result.addAll(ontology.getClassesInSignature());
		result.addAll(ontology.getIndividualsInSignature());
		return result;
	}
	
	@Override
	public boolean isIndividual(OWLEntity cpt) {
		return cpt.isOWLNamedIndividual();
	}

	@Override
	public Set<OWLEntity> getAllProperties() {
		// Only class and properties
		Set<OWLEntity> result = new HashSet<OWLEntity>();
		result.addAll(ontology.getObjectPropertiesInSignature());
		return result;
	}

	@Override
	public Set<String> getAnnotations(OWLEntity cpt) {
		// FIXME Support for OWL?
		return new HashSet<String>();
	}
	
	/* (non-Javadoc)
	 * @see agui.alignment.OntoContainer#getPrefLabels(java.lang.Object)
	 */
	@Override
	public Set<String> getPrefLabels(OWLEntity cpt) {
		if(cpt == null)
			throw new IllegalArgumentException("cpt cannot be null");
		// No pref label in pure OWL...
		return Collections.emptySet();
	}

	@Override
	public Set<String> getPrefLabels(OWLEntity cpt, String lang) {
		if(cpt == null)
			throw new IllegalArgumentException("cpt cannot be null");
		// No pref label in pure OWL...
		return Collections.emptySet();
	}

	@Override
	public Set<String> getAltLabels(OWLEntity cpt) {
		if(cpt == null)
			throw new IllegalArgumentException("cpt cannot be null");

		// The rdfs:label, if it exists
		Set<String> finalLabels = new HashSet<String>();
		Set<OWLAnnotation> annotations = cpt.getAnnotations(
				ontology,
				df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
		for(OWLAnnotation annot : annotations) {
			if(annot.getValue() instanceof OWLLiteral) {
				finalLabels.add(((OWLLiteral)annot.getValue()).getLiteral());
			}
		}
		return finalLabels;
	}

	@Override
	public Set<String> getAltLabels(OWLEntity cpt, String lang) {
		if(cpt == null)
			throw new IllegalArgumentException("cpt cannot be null");

		// The rdfs:label, if it exists
		Set<String> finalLabels = new HashSet<String>();
		Set<OWLAnnotation> annotations = cpt.getAnnotations(
				ontology,
				df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
		for(OWLAnnotation annot : annotations) {
			if(annot.getValue() instanceof OWLLiteral) {
				OWLLiteral literal = (OWLLiteral)annot.getValue();
				// Si il y a une langue et que c'est celle en parametre
				if((literal.hasLang() && literal.getLang().toLowerCase().equals(lang.toLowerCase()))
						// ou si il n'y en a pas et que c'est voulu
						|| (!literal.hasLang() && lang.equals(""))) { 

					String label = literal.getLiteral();
					finalLabels.add(label);
				}
			}
		}
		return finalLabels;
	}

	@Override
	public OWLEntity getConceptFromURI(URI uri) {
		OWLClass res = df.getOWLClass(IRI.create(uri));
		if(ontology.isDeclared(res))
			return res;
		return null;
	}
	
	@Override
	public Set<OWLEntity> getChildren(OWLEntity cpt) {
		if(!cpt.isOWLClass()) return new HashSet<OWLEntity>();
		
		Set<OWLEntity> entities = new HashSet<OWLEntity>();
		Set<OWLEntity> result = new HashSet<OWLEntity>();
		OWLClass localRootClass = cpt.asOWLClass();
		entities.addAll(reasoner.getSubClasses(localRootClass, true).getFlattened());
		entities.addAll(reasoner.getInstances(localRootClass, true).getFlattened());		
		for(OWLEntity child : entities) {
			// Only Class or Named individual
			if(!child.isOWLClass() && !child.isOWLNamedIndividual())
			{
				continue;
			}
			// Not Nothing
			if(child.isOWLClass() && child.asOWLClass().isOWLNothing())
			{
				continue;
			}
			result.add(child);
		}
		return result;
	}
	
	@Override
	public Set<OWLEntity> getParents(OWLEntity cpt) {
		if(!cpt.isOWLClass()) return new HashSet<OWLEntity>();
		
		Set<OWLEntity> entities = new HashSet<OWLEntity>();
		Set<OWLEntity> result = new HashSet<OWLEntity>();
		OWLClass localRootClass = cpt.asOWLClass();
		entities.addAll(reasoner.getSuperClasses(localRootClass, true).getFlattened());
		for(OWLEntity parentClass : entities) {
			// Only Class
			if(!parentClass.isOWLClass())
			{
				continue;
			}
			// Not Nothing
			if(parentClass.isOWLClass() && parentClass.asOWLClass().isOWLNothing())
			{
				continue;
			}
			result.add(parentClass);
		}
		return result;
	}

	@Override
	public URI getURI(OWLEntity cpt) {
		return cpt.getIRI().toURI();
	}

	@Override
	public URI getURI() {
		try {
			return ontology.getOntologyID().getOntologyIRI().toURI();
		} catch (Exception e) {
			return filename;
		}
	}

	@Override
	public SortedSet<String> getAllLanguageInLabels() {
		if(languages == null) {
			languages = new TreeSet<String>();
			OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ontology));
			OWLOntologyWalkerVisitor<Object> visitor = new OWLOntologyWalkerVisitor<Object>(walker) { 
				@Override
				public Object visit(OWLLiteral literal) {
					if(literal.hasLang()) {
						languages.add(literal.getLang());
					}
					return super.visit(literal);
				}
			};
			walker.walkStructure(visitor);
		}
		return languages;
	}

	@Override
	public OWLEntity getRoot() {
		return df.getOWLClass(OWLRDFVocabulary.OWL_THING.getIRI());
	}

	public OWLReasoner getReasoner() {
		return reasoner;
	}

	@Override
	public Set<String> getLabels(OWLEntity cpt, String prop) {
		throw new UnsupportedOperationException("Not supported in OWL for now");
	}

	@Override
	public Date getModifiedDate(OWLEntity cpt) {
		// TODO Auto-generated method 
		return null;
	}
}
