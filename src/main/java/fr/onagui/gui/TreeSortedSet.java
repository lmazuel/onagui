package fr.onagui.gui;

import java.util.TreeSet;

public class TreeSortedSet <T extends Updateable> extends TreeSet<T>{
	
	public boolean update(T e, Object value) {
	       if (remove(e)) {
	           e.update(value);
	           return add(e);
	       } else { 
	           return false;
	       }	
		}
}