import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;

import com.google.common.collect.Sets;

import fr.onagui.alignment.Alignment;
import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.method.LevenshteinAlignmentMethod;
import fr.onagui.alignment.method.LevenshteinDistance;


public class TestAlignment {

	@SuppressWarnings("unchecked")
	public static OntoContainer<String> mockContainer = mock(OntoContainer.class);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
		when(mockContainer.getPrefLabels(any(String.class), eq(""))).then(new Answer<Set<String>> () {
			@Override
			public Set<String> answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				String uri = (String) args[0];
				if(uri.equals("urn:cpt1")) return Sets.newHashSet("POLLUTION DE L'EAU");
				if(uri.equals("urn:cpt2")) return Sets.newHashSet("pollution de l'eau");
				if(uri.equals("urn:cpt3")) return Sets.newHashSet("QUALITE DE L'EAU");
				if(uri.equals("urn:cpt4")) return Sets.newHashSet("qualité de l'eau");
				return Sets.newHashSet();
			}
		});
		when(mockContainer.getPrefLabels(any(String.class))).then(new Answer<Set<String>> () {
			@Override
			public Set<String> answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				String uri = (String) args[0];
				return mockContainer.getPrefLabels(uri, "");
			}
		});
		when(mockContainer.getAltLabels(any(String.class), eq(""))).then(new Answer<Set<String>> () {
			@Override
			public Set<String> answer(InvocationOnMock invocation) throws Throwable {
				return Sets.newHashSet();
			}
		});
	}
	
	@Test
	public void testLevenshtein() {
		String s1 = "POLLUTION DE L'EAU";
		String s2 = "pollution de l'eau";
		System.out.println(LevenshteinDistance.computeNormalizedLevenshteinDistance(s1.toCharArray(), s2.toCharArray()));
	}
	
	@Test
	public void testLevenshteinWrapper() {
		LevenshteinAlignmentMethod<String, String> method = new LevenshteinAlignmentMethod<>();
		method.setLangsFrom1(Sets.newTreeSet(Sets.newHashSet(method.NO_TAG)));
		method.setLangsFrom2(Sets.newTreeSet(Sets.newHashSet(method.NO_TAG)));
		method.init();
		Mapping<String, String> computeMapping = method.computeMapping(mockContainer, "urn:cpt1", mockContainer, "urn:cpt2");
		System.out.println(computeMapping.getScore());
		assertEquals(1.0, computeMapping.getScore(), 0);

		computeMapping = method.computeMapping(mockContainer, "urn:cpt3", mockContainer, "urn:cpt4");
		System.out.println(computeMapping.getScore());
		assertEquals(1.0, computeMapping.getScore(), 0);
	}

	@Test
	public void testAlignmentIndex() {
		OntoContainer<String> mockContainer1 = mock(OntoContainer.class);
		when(mockContainer1.getURI(anyString())).then(new Answer<URI>() {
			@Override
			public URI answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return new URI("urn:"+args[0]);
			}
		});

		OntoContainer<String> mockContainer2 = mock(OntoContainer.class);
		when(mockContainer2.getURI(anyString())).then(new Answer<URI>() {
			@Override
			public URI answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return new URI("urn:"+args[0]);
			}
		});

		Alignment<String, String> alignement = new Alignment<String, String>(mockContainer1, mockContainer2);
		assertFalse(alignement.alignExist1("cpt1"));
		assertFalse(alignement.alignExist2("cpt2"));
		assertTrue(alignement.getAllMappingFor1("cpt1").isEmpty());
		assertTrue(alignement.getAllMappingFor2("cpt2").isEmpty());

		Mapping<String, String> map = new Mapping<String, String>("cpt1", "cpt2");
		alignement.addMap(map);
		assertEquals("cpt1", alignement.getAllMappingFor1("cpt1").first().getFirstConcept());
		assertEquals("cpt2", alignement.getAllMappingFor1("cpt1").first().getSecondConcept());
		assertEquals("cpt1", alignement.getAllMappingFor2("cpt2").first().getFirstConcept());
		assertEquals("cpt2", alignement.getAllMappingFor2("cpt2").first().getSecondConcept());
		assertTrue(alignement.alignExist1("cpt1"));
		assertTrue(alignement.alignExist2("cpt2"));

		alignement.removeMap(map);
		assertTrue(alignement.getAllMappingFor1("cpt1").isEmpty());
		assertTrue(alignement.getAllMappingFor2("cpt2").isEmpty());
		assertFalse(alignement.alignExist1("cpt1"));
		assertFalse(alignement.alignExist2("cpt2"));

		alignement.addMap(map);
		assertEquals("cpt1", alignement.getAllMappingFor1("cpt1").first().getFirstConcept());
		assertEquals("cpt2", alignement.getAllMappingFor1("cpt1").first().getSecondConcept());
		assertEquals("cpt1", alignement.getAllMappingFor2("cpt2").first().getFirstConcept());
		assertEquals("cpt2", alignement.getAllMappingFor2("cpt2").first().getSecondConcept());
		assertTrue(alignement.alignExist1("cpt1"));
		assertTrue(alignement.alignExist2("cpt2"));

		alignement.removeMapFromConcept1("cpt1");
		assertTrue(alignement.getAllMappingFor1("cpt1").isEmpty());
		assertTrue(alignement.getAllMappingFor2("cpt2").isEmpty());
		assertFalse(alignement.alignExist1("cpt1"));
		assertFalse(alignement.alignExist2("cpt2"));

		alignement.addMap(map);
		assertEquals("cpt1", alignement.getAllMappingFor1("cpt1").first().getFirstConcept());
		assertEquals("cpt2", alignement.getAllMappingFor1("cpt1").first().getSecondConcept());
		assertEquals("cpt1", alignement.getAllMappingFor2("cpt2").first().getFirstConcept());
		assertEquals("cpt2", alignement.getAllMappingFor2("cpt2").first().getSecondConcept());
		assertTrue(alignement.alignExist1("cpt1"));
		assertTrue(alignement.alignExist2("cpt2"));

		alignement.removeMapFromConcept2("cpt2");
		assertTrue(alignement.getAllMappingFor1("cpt1").isEmpty());
		assertTrue(alignement.getAllMappingFor2("cpt2").isEmpty());
		assertFalse(alignement.alignExist1("cpt1"));
		assertFalse(alignement.alignExist2("cpt2"));
	}
}
