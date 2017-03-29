import java.net.URI;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.OntoVisitor;

public class MyOntoContainerTestAdapter<ONTORES> implements OntoContainer<ONTORES> {

	@Override
	public String getFormalism() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getURI(ONTORES cpt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ONTORES> getAllConcepts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ONTORES> getAllProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ONTORES getConceptFromURI(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ONTORES getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isIndividual(ONTORES cpt) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<ONTORES> getChildren(ONTORES cpt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ONTORES> getParents(ONTORES cpt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(OntoVisitor<ONTORES> visitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SortedSet<String> getAllLanguageInLabels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getPrefLabels(ONTORES cpt, String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getPrefLabels(ONTORES cpt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getAltLabels(ONTORES cpt, String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getAltLabels(ONTORES cpt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getAnnotations(ONTORES cpt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getLabels(ONTORES cpt, String prop) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getModifiedDate(ONTORES cpt) {
		// TODO Auto-generated method stub
		return null;
	}

}
