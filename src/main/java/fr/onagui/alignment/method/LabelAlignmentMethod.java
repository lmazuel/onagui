package fr.onagui.alignment.method;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.Vector;

import fr.onagui.alignment.AbstractAlignmentMethod;
import fr.onagui.alignment.NLPTools;
import fr.onagui.alignment.OntoContainer;


/** Classe générique pour l'implementation d'un method d'alignement par label.
 * Les paramètres jugés génériques:<ol>
 * <li>L'utilisation d'un seuil, normalisés entre 0 et 1.</li>
 * <li>L'utilisation de la langue pour reduire la combinatoire des labels</li>
 * </ol>
 * 
 * @author Laurent Mazuel
 *
 * @param <ONTORES1>
 * @param <ONTORES2>
 */
public abstract class LabelAlignmentMethod<ONTORES1, ONTORES2> extends
AbstractAlignmentMethod<ONTORES1, ONTORES2> {
	
	/** Faux tag pouvant être utilisé pour désigner les annotations sans tag de langues.
	 * A NE PAS UTILISER AVEC DANS LES METHODES ONTO_CONTAINER.
	 * @see #getLabelsForAlignement(OntoContainer, Object, Set)
	 */
	public static final String NO_TAG = "NO_TAG";

	/** Faux tag pouvant être utilisé pour désigner le fragment d'URI.
	 * A NE PAS UTILISER AVEC DANS LES METHODES ONTO_CONTAINER.
	 * @see #getLabelsForAlignement(OntoContainer, Object, Set)
	 */
	public static final String FRAG_URI = "FRAG_URI";
	/** Le seuil actuel */
	private double current_threshlod = 1.0;

	public double getThreshold() {
		return current_threshlod;
	}

	public void setThreshold(double threshold) {
		if(threshold < 0.0 || threshold > 1.0)
			throw new IllegalArgumentException("Threshold must be in [0.0,1.0]");
		this.current_threshlod = threshold;
	}

	private SortedSet<String> langsFrom1 = null;
	private SortedSet<String> langsFrom2 = null;

	public SortedSet<String> getLangsFrom1() {
		return langsFrom1;
	}

	public SortedSet<String> getLangsFrom2() {
		return langsFrom2;
	}

	public void setLangsFrom1(SortedSet<String> langsFrom1) {
		this.langsFrom1 = langsFrom1;
	}

	public void setLangsFrom2(SortedSet<String> langsFrom2) {
		this.langsFrom2 = langsFrom2;
	}

	public class CacheKey implements Comparable<CacheKey> {

		private URI cpt;
		private SortedSet<String> langs;
		/**
		 * @param cpt
		 * @param langs
		 */
		public CacheKey(URI cpt, SortedSet<String> langs) {
			this.cpt = cpt;
			this.langs = langs;
		}
		
		@Override
		public int compareTo(CacheKey o) {
			int cpUri = cpt.compareTo(o.cpt);
			if(cpUri != 0)
				return cpUri;
			return (join(langs, "-")).compareTo(join(o.langs, "-"));
		};
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((cpt == null) ? 0 : cpt.hashCode());
			result = prime * result + ((langs == null) ? 0 : langs.hashCode());
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (cpt == null) {
				if (other.cpt != null)
					return false;
			} else if (!cpt.equals(other.cpt))
				return false;
			if (langs == null) {
				if (other.langs != null)
					return false;
			} else if (!langs.equals(other.langs))
				return false;
			return true;
		}
		private LabelAlignmentMethod getOuterType() {
			return LabelAlignmentMethod.this;
		}
	}
	
	private SortedMap<CacheKey, Collection<LabelInformation>> lexicalCaches = null;
	
	@Override
	public boolean init() {
		if(lexicalCaches != null) {
			lexicalCaches.clear();
		}
		else {
			lexicalCaches = Collections.synchronizedSortedMap(new TreeMap<CacheKey, Collection<LabelInformation>>());
		}
		return true;
	}
	/** Renvoie les labels en fonction des langues choisies.
	 * Pour obtenir les labels sans tags de langue, ajouter la chaine vide "" dans la liste.
	 * @param <ONTORES>
	 * @param container Le container de l'ontologie.
	 * @param cpt Le concept dont on cherche les labels.
	 * @param langs La liste des langues.
	 * @return La liste des labels.
	 */
	public <ONTORES> Collection<LabelInformation> getLabelsForAlignement(
			OntoContainer<ONTORES> container,
			ONTORES cpt,
			SortedSet<String> langs) {
		
		// Searching in the cache
		Collection<LabelInformation> result = null;
		URI cptURI = container.getURI(cpt);
		CacheKey ck = new CacheKey(cptURI, langs);
		if((result = lexicalCaches.get(ck)) != null) {
			return result;
		}

		result = new Vector<LabelInformation>();
		Set<String> prefLabels = container.getPrefLabels(cpt);
		String choosePrefLabel = "-no preflabel-";
		if(!prefLabels.isEmpty())
			choosePrefLabel = prefLabels.iterator().next();
		
		if(langs == null) {
			throw new IllegalArgumentException("Lang cannot be null");
		}
		else {
			for(String lang : langs) {
				if(lang.equals(NO_TAG)) {
					result.addAll(LabelInformation.createFromMultipleLabels(
							container.getPrefLabels(cpt, ""), choosePrefLabel, "pref"));
					result.addAll(LabelInformation.createFromMultipleLabels(
							container.getAltLabels(cpt, ""), choosePrefLabel, "alt"));
				}
				else if(lang.equals(FRAG_URI)) {
					final String fragment = container.getURI(cpt).getFragment();
					if(fragment == null) continue;
					result.add(new LabelInformation(fragment, choosePrefLabel, "URI"));
				}
				else {
					result.addAll(LabelInformation.createFromMultipleLabels(
							container.getPrefLabels(cpt, lang), choosePrefLabel, "pref"));
					result.addAll(LabelInformation.createFromMultipleLabels(
							container.getAltLabels(cpt, lang), choosePrefLabel, "alt"));
				}
			}
		}
		
		// Stock in the cache
		lexicalCaches.put(ck, result);

		return result;
	}
	
	public static void applyNLPFilterToLabels(Collection<LabelInformation> labels) {
		for(LabelInformation label : labels) {
			String rawLabel = label.getLabel();
			// FIXME Etape typiquement SNOMED, inutile dans le cas général
			/* Lancer une reflexion sur l'utilisation de contraintes lexicales personnelles,
			 * lié à une ontologie en particulier (comme la Snomed) */
			String preTraitement = NLPTools.convertFromSnomed(rawLabel);
			String traitement = NLPTools.convertLabelForAlignment(preTraitement);
			label.setLabel(traitement);
		}
	}
	
	public static String join( Iterable< ? extends Object > pColl, String separator )
    {
        Iterator< ? extends Object > oIter;
        if ( pColl == null || ( !( oIter = pColl.iterator() ).hasNext() ) )
            return "";
        StringBuilder oBuilder = new StringBuilder( String.valueOf( oIter.next() ) );
        while ( oIter.hasNext() )
            oBuilder.append( separator ).append( oIter.next() );
        return oBuilder.toString();
    }
}




