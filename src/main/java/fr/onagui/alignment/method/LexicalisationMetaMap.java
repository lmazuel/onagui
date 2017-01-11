package fr.onagui.alignment.method;

import java.util.TreeMap;

/** Common meta-map for lexicalisation alignment method.
 * @author Laurent Mazuel
 */
public class LexicalisationMetaMap extends TreeMap<String, String> {

	/** To make Java happy ..... */
	private static final long serialVersionUID = 1525782312731095110L;

	public void addUsedLabel1(String value) {
		this.put("usedLabel1", value);
	}
	public void addUsedLabel2(String value) {
		this.put("usedLabel2", value);
	}
	public void addPrefLabel1(String value) {
		this.put("prefLabel1", value);
	}
	public void addPrefLabel2(String value) {
		this.put("prefLabel2", value);
	}
	public void addTypeOfUsedLabel1(String value) {
		this.put("typeOfUsedLabel1", value);
	}
	public void addTypeOfUsedLabel2(String value) {
		this.put("typeOfUsedLabel2", value);
	}


	public static LexicalisationMetaMap createMetaMap(
			String usedLabel1,
			String usedLabel2) {
		LexicalisationMetaMap meta = new LexicalisationMetaMap();
		meta.addUsedLabel1(usedLabel1);
		meta.addUsedLabel2(usedLabel2);
		return meta;
	}

	public static LexicalisationMetaMap createMetaMap(
			String usedLabel1,
			String usedLabel2,
			String prefLabel1,
			String prefLabel2) {
		LexicalisationMetaMap meta = LexicalisationMetaMap.createMetaMap(usedLabel1, usedLabel2);
		meta.addPrefLabel1(prefLabel1);
		meta.addPrefLabel2(prefLabel2);
		return meta;
	}
	
	public static LexicalisationMetaMap createMetaMap(
			String usedLabel1,
			String usedLabel2,
			String prefLabel1,
			String prefLabel2,
			String typeOfUsedLabel1,
			String typeOfUsedLabel2) {
		LexicalisationMetaMap meta = LexicalisationMetaMap.createMetaMap(usedLabel1, usedLabel2, prefLabel1, prefLabel2);
		meta.addTypeOfUsedLabel1(typeOfUsedLabel1);
		meta.addTypeOfUsedLabel2(typeOfUsedLabel2);
		return meta;
	}

}