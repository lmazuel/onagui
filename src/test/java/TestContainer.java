import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import fr.onagui.alignment.container.DOEOWLContainer;
import fr.onagui.alignment.container.SKOSContainer;

public class TestContainer {

	@Test
	public void testOWLXML() throws URISyntaxException, OWLOntologyCreationException {
		URI filename = TestContainer.class.getResource("TestOneConceptXml.owl").toURI();
		DOEOWLContainer container = new DOEOWLContainer(filename);
		assertEquals(container.getAllConcepts().size(), 1);
	}

	@Test
	public void testOWLTurtle() throws URISyntaxException, OWLOntologyCreationException {
		URI filename = TestContainer.class.getResource("TestOneConceptTurtle.owl").toURI();
		DOEOWLContainer container = new DOEOWLContainer(filename);
		assertEquals(container.getAllConcepts().size(), 1);
	}
	
	@Test
	public void testSkosXML() throws URISyntaxException, RepositoryException, RDFParseException, IOException {
		URI filename = TestContainer.class.getResource("SkosTestXml.skos.owl").toURI();
		SKOSContainer container = new SKOSContainer(Paths.get(filename).toFile());
		assertEquals(container.getAllConcepts().size(), 1);
	}

	@Test
	public void testSkosNT() throws URISyntaxException, RepositoryException, RDFParseException, IOException {
		URI filename = TestContainer.class.getResource("SkosTestNT.skos.owl").toURI();
		SKOSContainer container = new SKOSContainer(Paths.get(filename).toFile());
		assertEquals(container.getAllConcepts().size(), 1);
	}	
}
