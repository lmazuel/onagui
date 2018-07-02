import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedMap;
import java.util.SortedSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import fr.onagui.alignment.Alignment;
import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.Mapping.MAPPING_TYPE;
import fr.onagui.alignment.Mapping.VALIDITY;
import fr.onagui.alignment.NoMappingPossible;
import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.io.CSVImpl;
import fr.onagui.alignment.io.EuzenatRDFImpl;
import fr.onagui.alignment.io.IOAlignment;
import fr.onagui.alignment.io.SkosImpl;

public class TestIO {

	public static Alignment<String, String> alignment;
	@SuppressWarnings("unchecked")
	public static OntoContainer<String> mockContainer = mock(OntoContainer.class);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		alignment = new Alignment<>(mockContainer, mockContainer);
		when(mockContainer.getURI(anyString())).then(new Answer<URI>() {
			@Override
			public URI answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return new URI("urn:"+args[0]);
			}
		});
		when(mockContainer.getConceptFromURI(any(URI.class))).then(new Answer<String> () {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				URI uri = (URI) args[0];
				return uri.toString().substring(4);
			}
		});
		when(mockContainer.getURI()).thenReturn(new URI("urn:onto"));
		when(mockContainer.getFormalism()).thenReturn("skos");
		alignment.addMap(new Mapping<String, String>("Cpt1", "Cpt2", 1.42, MAPPING_TYPE.OVERLAP, "Tests", VALIDITY.TO_CONFIRM));
		alignment.addImpossibleMappingFrom1(new NoMappingPossible<String>("Impossible1"));
		alignment.addImpossibleMappingFrom2(new NoMappingPossible<String>("Impossible2"));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		alignment = null;
	}

	@Test
	public void testCSV() {
		try {
			makeFullAssertions(new CSVImpl());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to perform the test");
		}
	}
	
	@Test
	public void testCSVNoTitles() throws URISyntaxException {
		// note : this does not work if path contains special characters
		makeLoadAssertions(new CSVImpl(), new File(TestIO.class.getResource("csvnotitles.csv").toURI()));
	}

	@Test
	public void testRDF() {
		try {
			makeFullAssertions(new EuzenatRDFImpl());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to perform the test");
		}
	}

	@Test
	public void testRDFLoadISOLatin1() throws URISyntaxException {
		IOAlignment ioplugin = new EuzenatRDFImpl();
		Alignment<String, String> newAlignment = null;
		try {
			newAlignment = ioplugin.loadAlignment(mockContainer, mockContainer, new File(TestIO.class.getResource("isolatin.rdf").toURI()));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Something wrong while reading test");
		}
		SortedSet<Mapping<String, String>> mappings = newAlignment.getMapping();
		assertEquals(1, mappings.size());
		Mapping<String, String> firstMapping = mappings.first();
		assertEquals("Cpt1éééé", firstMapping.getFirstConcept());
		assertEquals("Cpt2éééé", firstMapping.getSecondConcept());
	}
	
	@Test
	public void testSKOS() {
		Path tmpPath;
		try {
			tmpPath = Files.createTempFile(null, null);
			String filename = tmpPath.toString();
			makeSaveAssertions(new SkosImpl(), filename);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to create a temp file for the test");
		} 
	}
	
	private void makeFullAssertions(IOAlignment ioplugin) throws IOException {
		Path tmpPath = Files.createTempFile(null, null); 
				
		makeSaveAssertions(ioplugin, tmpPath.toString());
		makeLoadAssertions(ioplugin, tmpPath.toFile());		
	}

	private void makeSaveAssertions(IOAlignment ioplugin, String filename) {
		try {
			ioplugin.saveAlignment(alignment, filename, null);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Something wrong while writer alignment");
		}
	}
	
	private void makeLoadAssertions(IOAlignment ioplugin, File file) {
		Alignment<String, String> newAlignment = null;
		try {
			newAlignment = ioplugin.loadAlignment(mockContainer, mockContainer, file);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Something wrong while reading test");
		}
		SortedSet<Mapping<String, String>> mappings = newAlignment.getMapping();
		assertEquals(1, mappings.size());
		Mapping<String, String> firstMapping = mappings.first();
		assertEquals("Cpt1", firstMapping.getFirstConcept());
		assertEquals("Cpt2", firstMapping.getSecondConcept());
		assertEquals(1.42, firstMapping.getScore(), 0);
		assertEquals(MAPPING_TYPE.OVERLAP, firstMapping.getType());
		assertEquals("Tests", firstMapping.getMethod());
		assertEquals(VALIDITY.TO_CONFIRM, firstMapping.getValidity());
		
		SortedMap<URI, NoMappingPossible<String>> impossibleMapping1 = newAlignment.getImpossibleMapping1();
		assertEquals(1, impossibleMapping1.size());
		assertTrue(impossibleMapping1.containsKey(URI.create("urn:Impossible1")));

		SortedMap<URI, NoMappingPossible<String>> impossibleMapping2 = newAlignment.getImpossibleMapping2();
		assertEquals(1, impossibleMapping2.size());
		assertTrue(impossibleMapping2.containsKey(URI.create("urn:Impossible2")));
	}
}
