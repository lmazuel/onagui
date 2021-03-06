/* Copyright (C) 2008-2009 by Laurent Mazuel
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 
 */
package fr.onagui.alignment;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * This class represents a mapping between two entities:
 * <ol>
 * <li>A first concept with type T</li>
 * <li>A second concept with type V</li>
 * <li>A score which represents this mapping</li>
 * <li>The type of this mapping</li>
 * </ol>
 * 
 * @author Laurent Mazuel
 * @see MappingComparator
 * 
 * @param <T>
 *            The class for a concept in the first ontology
 * @param <V>
 *            The class for a concept in the second ontology
 */
public class Mapping<T, V> implements Comparable<Mapping<?, ?>> {

	/** The string noted as "method" if no method was given. */
	public static String UNKNOW_METHOD = "unknow";

	private T firstConcept = null;
	private V secondConcept = null;
	private double score = 0.0;
	private MAPPING_TYPE type = MAPPING_TYPE.EQUIV;
	private String method = UNKNOW_METHOD;
	private VALIDITY validity = VALIDITY.TO_CONFIRM;
	private String comment = "";
	private Map<String, String> meta = new TreeMap<String, String>();
	private DateTime creationDate = new DateTime();

	/**
	 * Mapping type. The last is the most important, to allow to compare two
	 * types using their indexes.
	 * Note that the values in the enum are returned _in the order they are declared_ so any changes in the
	 * order declaration of values will have a direct impcat on GUI.
	 * 
	 * @author Laurent Mazuel
	 */
	public enum MAPPING_TYPE {
		EQUIV("exactMatch", SKOS.EXACT_MATCH, "equivalence", "=", "eq"), // Equivalence
		OVERLAP("closeMatch", SKOS.CLOSE_MATCH), // Overlap
		RELATED("relatedMatch", SKOS.RELATED_MATCH),
		SUBSUMEDBY("broadMatch", SKOS.BROAD_MATCH, "<"), // Subsomption by
		SUBSUMES("narrowMatch", SKOS.NARROW_MATCH, ">"), // subsumes
		// this is the generic SKOS mapping relation
		UNDEFINED("undefined", SKOS.MAPPING_RELATION),
		DISJOINT("disjoint", null); // Disjoint

		private String label = null;
		private String[] equivalentForms = null;
		private IRI skosPropertyIri;

		private MAPPING_TYPE(String label, IRI skosPropertyIri, String... equivalentForms) {
			this.label = label;
			this.equivalentForms = equivalentForms;
			this.skosPropertyIri = skosPropertyIri;
		}

		/** Return the label, for UI usage.
		 * @return
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * Returns the corresponding SKOS property
		 * @return
		 */
		public IRI getSkosPropertyIri() {
			return skosPropertyIri;
		}

		/**
		 * Test if the string in parameter can be considered as equivalent of
		 * this type. Some relation type have different forms, as "equivalence",
		 * "=", "eq", "equiv", etc.
		 * 
		 * @param s
		 *            A string which may reprents this type.
		 * @return <code>true</code> if the string represents this type,
		 *         <code>false</code> otherwise.
		 */
		public boolean isEquivalentToString(String s) {
			// Test using representing string
			if (this.name().equals(s.toUpperCase()))
				return true;
			// Test using equivalent forms of this type
			for (String type : equivalentForms) {
				if (s.toLowerCase().equals(type.toLowerCase()))
					return true;
			}
			// No, sorry i didn't found it...
			return false;
		}

		/**
		 * Return the type which is represented by this string, or
		 * <code>null</code> if none was found.
		 * 
		 * @param s
		 *            A string which may be a type.
		 * @return A type if possible, <code>null</code> if not.
		 */
		public static MAPPING_TYPE getTypeFromString(String s) {
			for (MAPPING_TYPE type : MAPPING_TYPE.values()) {
				if (type.isEquivalentToString(s))
					return type;
			}
			return null;
		}

		public static MAPPING_TYPE getTypeFromSkosPredicate(IRI skosPredicate) {
			for (MAPPING_TYPE type : MAPPING_TYPE.values()) {
				if (type.getSkosPropertyIri() != null && type.getSkosPropertyIri().stringValue().equals(skosPredicate.stringValue())) {
					return type;
				}
			}
			return null;
		}
		
		
	}

	/**
	 * Validitiy of this alignment.
	 * 
	 * @author Laurent Mazuel
	 */
	public enum VALIDITY {
		VALID, TO_CONFIRM, INVALID;
	}

	/**
	 * Construct a new mapping.
	 * 
	 * @param firstConcept
	 * @param secondConcept
	 * @param score
	 * @param type
	 */
	public Mapping(T firstConcept, V secondConcept, double score,
			MAPPING_TYPE type) {
		this(firstConcept, secondConcept, score, type, UNKNOW_METHOD,
				VALIDITY.TO_CONFIRM);
	}

	/**
	 * @param firstConcept
	 * @param secondConcept
	 * @param score
	 * @param type
	 * @param method
	 */
	public Mapping(T firstConcept, V secondConcept, double score,
			MAPPING_TYPE type, String method) {
		this(firstConcept, secondConcept, score, type, method, VALIDITY.TO_CONFIRM);
	}

	/**
	 * @param firstConcept
	 * @param secondConcept
	 * @param score
	 * @param type
	 * @param method
	 * @param validity
	 */
	public Mapping(T firstConcept, V secondConcept, double score,
			MAPPING_TYPE type, String method, VALIDITY validity) {
		this(firstConcept, secondConcept, score, type, method, validity,
				new DateTime());
	}

	/**
	 * @param firstConcept
	 * @param secondConcept
	 * @param score
	 * @param type
	 * @param method
	 * @param validity
	 */
	public Mapping(T firstConcept, V secondConcept, double score,
			MAPPING_TYPE type, String method, VALIDITY validity,
			DateTime creationDate) {
		this.firstConcept = firstConcept;
		this.secondConcept = secondConcept;
		this.score = score;
		this.type = type;
		this.method = method;
		this.validity = validity;
		this.creationDate = creationDate;
	}

	/**
	 * Construct a new mapping, assuming that type is {@link MAPPING_TYPE#EQUIV}
	 * .
	 * 
	 * @param firstConcept
	 * @param secondConcept
	 * @param score
	 */
	public Mapping(T firstConcept, V secondConcept, double score) {
		this(firstConcept, secondConcept, score, MAPPING_TYPE.EQUIV);
	}

	/**
	 * Construct a new mapping, assuming that type is {@link MAPPING_TYPE#EQUIV}
	 * and score is 1.0.
	 * 
	 * @param firstConcept
	 * @param secondConcept
	 */
	public Mapping(T firstConcept, V secondConcept) {
		this(firstConcept, secondConcept, 1.0, MAPPING_TYPE.EQUIV);
	}
	
	/**
	 * Constructs a new Mapping based on another one (copy-constructor), except for the type
	 * @param otherMapping
	 */
	public Mapping(Mapping<T, V> otherMapping, MAPPING_TYPE newType) {
		this.firstConcept = otherMapping.firstConcept;
		this.secondConcept = otherMapping.secondConcept;
		this.score = otherMapping.score;
		// copy everything except the type
		this.type = newType;
		this.method = otherMapping.method;
		this.validity = otherMapping.validity;
		this.creationDate = otherMapping.creationDate;
		this.comment = otherMapping.comment;
		this.meta = otherMapping.meta;
	}
	
	/**
	 * Constructs a new Mapping based on another one (copy-constructor), except for the validity
	 * @param otherMapping
	 */
	public Mapping(Mapping<T, V> otherMapping, VALIDITY newValidity) {
		this.firstConcept = otherMapping.firstConcept;
		this.secondConcept = otherMapping.secondConcept;
		this.score = otherMapping.score;
		this.type = otherMapping.type;
		this.method = otherMapping.method;
		// copy everything except the validity
		this.validity = newValidity;
		this.creationDate = otherMapping.creationDate;
		this.comment = otherMapping.comment;
		this.meta = otherMapping.meta;
	}

	/**
	 * @return the firstConcept
	 */
	public T getFirstConcept() {
		return firstConcept;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @return the secondConcept
	 */
	public V getSecondConcept() {
		return secondConcept;
	}

	/**
	 * @return the type
	 */
	public MAPPING_TYPE getType() {
		return type;
	}
	
//	public void setType(MAPPING_TYPE type) {
//		this.type = type;
//	}

	public Map<String, String> getMeta() {
		return meta;
	}

	public void setMeta(Map<String, String> meta) {
		this.meta = meta;
	}

	void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return the method used to compute this mapping
	 */
	public String getMethod() {
		return method;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result
				+ ((firstConcept == null) ? 0 : firstConcept.hashCode());
		result = prime * result + ((meta == null) ? 0 : meta.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((secondConcept == null) ? 0 : secondConcept.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result
				+ ((validity == null) ? 0 : validity.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		Mapping other = (Mapping) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (firstConcept == null) {
			if (other.firstConcept != null)
				return false;
		} else if (!firstConcept.equals(other.firstConcept))
			return false;
		if (meta == null) {
			if (other.meta != null)
				return false;
		} else if (!meta.equals(other.meta))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (Double.doubleToLongBits(score) != Double
				.doubleToLongBits(other.score))
			return false;
		if (secondConcept == null) {
			if (other.secondConcept != null)
				return false;
		} else if (!secondConcept.equals(other.secondConcept))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (validity == null) {
			if (other.validity != null)
				return false;
		} else if (!validity.equals(other.validity))
			return false;
		return true;
	}

	/**
	 * La méthode compareTo n'utilise que des caractéristiques non éditables dans OnaGUI
	 * Pour ne pas que la ligne bouge si on édite le type ou la validité.
	 */
	@Override
	public int compareTo(Mapping<?, ?> o) {
		// Consistance avec l'égalité
		if (this.equals(o))
			return 0;

		// Comparaison sur le premier concept
		int compare = firstConcept.toString().compareTo(o.firstConcept.toString());
		if(compare != 0) {
			return compare;
		}
		// comparaison sur le deuxieme concept
		compare = secondConcept.toString().compareTo(o.secondConcept.toString());
		if(compare != 0) {
			return compare;
		}		

		// Ici les concepts sont identiques, on regarde le score
		compare = Double.valueOf(this.score).compareTo(Double.valueOf(o.score));
		if (compare != 0)
			return compare;

		// concept 1, concept 2 et score identique
		// on se rabat sur un toString
		return this.toString().compareTo(o.toString());		
	}
	
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	/**
	 * @return the validity
	 */
	public VALIDITY getValidity() {
		return validity;
	}

	/**
	 * @param validity
	 *            the validity to set
	 */
//	public void setValidity(VALIDITY validity) {
//		this.validity = validity;
//	}

	/**
	 * @return The creation ddate of this instance.
	 */
	public DateTime getCreationDate() {
		return creationDate;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("[");
		buf.append(firstConcept);
		buf.append('-');
		buf.append(type);
		buf.append('-');
		buf.append(secondConcept);
		buf.append(", SCR=");
		buf.append(score);
		buf.append(", method=");
		buf.append(method);
		buf.append(", valid=");
		buf.append(validity);
		buf.append(", creation date=");
		ISODateTimeFormat.basicDateTimeNoMillis().printTo(buf, creationDate);
		buf.append("]");
		return buf.toString();
	}

}
