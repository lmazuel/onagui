package fr.onagui.alignment.method;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.google.common.base.MoreObjects;

public class LabelInformation {
	private String label = null;
	private String prefLabel = null;
	private Set<String> origins = null;
	
	public LabelInformation(String label, String prefLabel, String... origins) {
		this.origins = new HashSet<String>();
		this.origins.addAll(Arrays.asList(origins));
		this.label = label;
		this.prefLabel = prefLabel;
	}
	
	public void addOrigin(String origin) {
		this.origins.add(origin);
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getPrefLabel() {
		return prefLabel;
	}
	
	public Set<String> getOrigins() {
		return origins;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("origins", getOrigins())
			.add("label", getLabel())
			.add("prefLabel", getPrefLabel())
			.toString();
	}
	
	public static Collection<LabelInformation> createFromMultipleLabels(Collection<String> labels, String prefLabel, String origin) {
		Collection<LabelInformation> result = new Vector<LabelInformation>();
		for(String label : labels) {
			LabelInformation localInfo = new LabelInformation(label, prefLabel, origin);
			result.add(localInfo);
		}
		return result;
	}
}