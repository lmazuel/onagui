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

import java.util.Comparator;

/** Implements a mapping comparator, based on natural order of score.
 * You can use this comparator to order a SortedSet of mapping.
 * <b>Note: this comparator imposes orderings that are inconsistent with equals.</b>
 * Its the reason why the abstract mapping does not directly implement a natural order. 
 * 
 * @author Laurent Mazuel
 */
public final class MappingComparator<T, V> implements Comparator<Mapping<T, V>> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Mapping<T, V> o1, Mapping<T, V> o2) {
		return Double.compare(o1.getScore(), o2.getScore());
	}
}