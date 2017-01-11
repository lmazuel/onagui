package fr.onagui.alignment;

public class NoMappingPossible<T> {
	
	private T concept = null;
	private String comment = null;
	
	/**
	 * @param concept
	 */
	public NoMappingPossible(T concept) {
		this.concept = concept;
	}
	
	public T getConcept() {
		return concept;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((concept == null) ? 0 : concept.hashCode());
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
		final NoMappingPossible other = (NoMappingPossible) obj;
		if (concept == null) {
			if (other.concept != null)
				return false;
		} else if (!concept.equals(other.concept))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("No mapping possible with concept: ");
		buf.append(concept);
		return buf.toString();
	}
	
}
