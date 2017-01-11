package fr.onagui.control;

public class StatCounter {
	
	public StatCounter() {
		// TODO Auto-generated constructor stub
	}
	
	private int notFound = 0;
	private int found=0;
	private int notSearch = 0;
	
	public int getNotFound() {
		return notFound;
	}
	
	public int getFound() {
		return found;
	}
	
	public int getNotSearch() {
		return notSearch;
	}
	
	public void countNotFound() {
		this.notFound++;
	}
	
	public void countFound() {
		this.found++;
	}
	
	public void countNotSearch() {
		this.notSearch++;
	}
	
	public void incorparte(StatCounter cnt) {
		this.notFound += cnt.notFound;
		this.found += cnt.found;
		this.notSearch += cnt.notSearch;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Fait: "+getFound());
		buf.append(" - Impossible: "+getNotFound());
		buf.append(" - Non fait: "+getNotSearch());
		buf.append(" - Nb concepts: "+(getFound() + getNotFound() + getNotSearch()));
		return buf.toString();
	}

}
