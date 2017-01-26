package fr.onagui.config;

import java.awt.Color;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FixedColorManager implements ScoreColorConfiguration {

	/* Color, fixed */

	public static final Color COLOR_1 = new Color(51,255,51); // Green
	public static final Color COLOR_2 = new Color(255,255,0); // Yellow
	public static final Color COLOR_3 = new Color(255,153,0); // Orange
	public static final Color COLOR_4 = new Color(255, 0, 0); // Red

	/* Threshold, can be modified by constructor */

	private double threshold_1 = 0.75;
	private double threshold_2 = 0.5;
	private double threshold_3 = 0.25;

	public FixedColorManager() {
		// Use default threshold
	}

	public FixedColorManager(Node scoreNode) {
		// Loading threshold
		try {
			NodeList child = scoreNode.getChildNodes();
			for(int i=0; i<child.getLength(); i++) {
				Node curtNode = child.item(i);
				if(curtNode.getNodeType() != Node.ELEMENT_NODE)
					continue;
				NamedNodeMap attrs = curtNode.getAttributes();
				Attr num = (Attr)attrs.getNamedItem("num");
				Attr value = (Attr)attrs.getNamedItem("value");
				int num_int = Integer.valueOf(num.getValue()); 
				double real_value = Double.valueOf(value.getValue());
				switch(num_int) {
				case 1: threshold_1 = real_value; break;
				case 2: threshold_2 = real_value; break;
				case 3: threshold_3 = real_value; break;
				default:
					throw new IllegalArgumentException("Cannot assign threshold num "+num_int);
				}
			}
			System.out.println("Loading FixedColorMAnager successfull: "+this.toString());
		}
		catch (Exception e) {
			System.err.println("Unable to load FixedColorManager, config file is false...");
			System.err.println("Error was: "+e.getMessage());
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see fr.onagui.config.ScoreColorConfiguration#getColorFromScore(double)
	 */
	@Override
	public Color getColorFromScore(double score) {
		if(score >= threshold_1)
			return COLOR_1;
		else if (score >= threshold_2)
			return COLOR_2;
		else if (score >= threshold_3)
			return COLOR_3;
		else
			return COLOR_4;
	}

	/* (non-Javadoc)
	 * @see fr.onagui.config.ScoreColorConfiguration#getColorImpossibleToAlign()
	 */
	@Override
	public Color getColorImpossibleToAlign() {
		return Color.black;
	}

	/* (non-Javadoc)
	 * @see fr.onagui.config.ScoreColorConfiguration#getColorNeutral()
	 */
	@Override
	public Color getColorNeutral() {
		return Color.gray;
	}

	@Override
	public String toString() {
		return "("+threshold_1+" - "+threshold_2+" - "+threshold_3+")";
	}
	
}
