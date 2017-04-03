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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
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

public class TestSkosImpl {

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
	public void testSKOS() {
//		Path tmpPath;
//		try {
//			tmpPath = Files.createTempFile(null, null);
//			String filename = tmpPath.toString();
//			makeSaveAssertions(new SkosImpl(), filename);
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail("Unable to create a temp file for the test");
//		} 
		
		try {
			File f = new File("test-skos.rdf");
			if(!f.exists()) {
				f.createNewFile();
			}
			makeSaveAssertions(new SkosImpl(), f.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void makeSaveAssertions(IOAlignment ioplugin, String filename) {
		try {
			ioplugin.saveAlignment(alignment, filename, null);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Something wrong while writer alignment");
		}
	}
}
