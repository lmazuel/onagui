import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Set;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.Sets;

import fr.onagui.alignment.container.DOEOWLContainer;
import fr.onagui.alignment.container.RDFModelContainer;
import fr.onagui.alignment.container.SKOSContainer;

public class TestContainer {

	@Test
	public void testOWLXML() throws URISyntaxException, OWLOntologyCreationException {
		URI filename = TestContainer.class.getResource("TestOneConceptXml.owl").toURI();
		DOEOWLContainer container = new DOEOWLContainer(filename);
		assertEquals(container.getAllConcepts().size(), 1);
		assertEquals(container.getAllLanguageInLabels(), Sets.newHashSet());
	}

	@Test
	public void testOWLTurtle() throws URISyntaxException, OWLOntologyCreationException {
		URI filename = TestContainer.class.getResource("TestOneConceptTurtle.ttl").toURI();
		DOEOWLContainer container = new DOEOWLContainer(filename);
		assertEquals(container.getAllConcepts().size(), 1);
		assertEquals(container.getAllLanguageInLabels(), Sets.newHashSet());
	}
	
	@Test
	public void testSkosXML() throws URISyntaxException, RepositoryException, RDFParseException, IOException {
		URI filename = TestContainer.class.getResource("SkosTestXml.skos.owl").toURI();
		SKOSContainer container = new SKOSContainer(Paths.get(filename).toFile());
		assertEquals(container.getAllConcepts().size(), 1);
		assertEquals(container.getAllLanguageInLabels(), Sets.newHashSet("en","fr"));
	}

	@Test
	public void testSkosNT() throws URISyntaxException, RepositoryException, RDFParseException, IOException {
		URI filename = TestContainer.class.getResource("SkosTestNT.ttl").toURI();
		SKOSContainer container = new SKOSContainer(Paths.get(filename).toFile());
		Set<Resource> allConcepts = container.getAllConcepts();
		assertEquals(allConcepts.size(), 1);
		assertEquals(container.getAllLanguageInLabels(), Sets.newHashSet("en","fr"));

		Resource cpt = allConcepts.iterator().next();
		assertEquals(Sets.newHashSet("mon premier concept"), container.getPrefLabels(cpt, "fr"));
		assertEquals(Sets.newHashSet("my first concept no lang tag"), container.getPrefLabels(cpt, ""));
		assertEquals(Sets.newHashSet("my first concept no lang tag", "mon premier concept", "my first concept"),
					 container.getPrefLabels(cpt));
	}
	
	@Test
	public void testRDFAllVocabularies() throws URISyntaxException, RepositoryException, RDFParseException, IOException {
		URI filename = TestContainer.class.getResource("RDFModelContainerTest.ttl").toURI();
		RDFModelContainer container = new RDFModelContainer(Paths.get(filename).toFile());
		Set<Resource> allConcepts = container.getAllConcepts();
		assertEquals(4, allConcepts.size());
		assertEquals(container.getAllLanguageInLabels(), Sets.newHashSet("en","fr"));

		for (Resource resource : allConcepts) {
			// label equal to URI
			assertEquals(Sets.newHashSet(resource.stringValue()), container.getPrefLabels(resource));
			// no children
			assertEquals(0, container.getChildren(resource).size());
			// one parent
			assertEquals(1, container.getParents(resource).size());
		}
		
		
	}
}
