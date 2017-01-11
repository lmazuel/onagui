/**
 * 
 */
package fr.onagui.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.SortedSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import fr.onagui.alignment.Mapping;
import fr.onagui.config.ScoreColorConfiguration;
import fr.onagui.control.AlignmentControler;
import fr.onagui.control.TreeNodeOntologyObject;

public class TreeNodeRenderer extends DefaultTreeCellRenderer {

	/** To make Java happy */
	private static final long serialVersionUID = -3129243471376574226L;

	//	private final ImageIcon cercleJaune = new ImageIcon(TreeNodeRenderer.class.getResource("/fr/onagui/images/cerclejaune.gif"));
	//	private final ImageIcon cercleRouge = new ImageIcon(TreeNodeRenderer.class.getResource("/fr/onagui/images/cerclerouge.gif"));
	//	private final ImageIcon cercleVert = new ImageIcon(TreeNodeRenderer.class.getResource("/fr/onagui/images/cerclevert.gif"));

	private int ontologyNumber = 0;
	private AlignmentControler alignmentControler;
	private ScoreColorConfiguration scc;

	private Icon impossibleIcon = null;
	private Icon notknowIcon = null;

	public TreeNodeRenderer(int number,
			ScoreColorConfiguration scc,
			AlignmentControler alignmentControler) {
		this.ontologyNumber = number;
		this.alignmentControler = alignmentControler;
		this.scc = scc;

		impossibleIcon = new ImageIcon(TreeNodeRenderer.getCircleOfColor(scc.getColorImpossibleToAlign()));
		notknowIcon = new ImageIcon(TreeNodeRenderer.getCircleOfColor(scc.getColorNeutral()));
	}

	public static Image getCircleOfColor(Color color) {
		return getCircleOfColor(color, 0);
	}
	
	public static Image getCircleOfColor(Color color, int numberInside) {
		BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graph = image.createGraphics();
		graph.setColor(color);
		graph.fillOval(0, 0, 20, 20);
		if(numberInside > 1) {
			String numberStr = String.valueOf(numberInside);
			graph.setColor(Color.black);
			graph.drawString(numberStr, 6, 15);
		}
		return image;
	}

	@Override
	public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(
				tree, value, sel,
				expanded, leaf, row,
				hasFocus);

		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
		Object userObject = treeNode.getUserObject();
		if(userObject instanceof TreeNodeOntologyObject) {
			TreeNodeOntologyObject ontologyNodeObject = (TreeNodeOntologyObject)userObject;
			// Impossible ?
			boolean impossible = false;
			if(ontologyNumber == 1) {
				impossible = alignmentControler.isImpossibleToAlign1(ontologyNodeObject);
			}
			else {
				impossible = alignmentControler.isImpossibleToAlign2(ontologyNodeObject);
			}

			// Impossible
			if(impossible) {
				setIcon(impossibleIcon);
			}
			// Not impossible, something exist?
			else if(!alignmentControler.alignExist(ontologyNodeObject, ontologyNumber)) {
				setIcon(notknowIcon);
			}
			// Exist, what color ?
			else {
				SortedSet<Mapping> maps = alignmentControler.getAllMappingOfConcept(ontologyNodeObject, ontologyNumber);
				int numberOfMaps = maps.size();
				double maxScore = 0;
				for(Mapping map : maps) {
					maxScore = (map.getScore()>maxScore)?map.getScore():maxScore;
				}
				Color rightColor = scc.getColorFromScore(maxScore);
				setIcon(new ImageIcon(TreeNodeRenderer.getCircleOfColor(rightColor, numberOfMaps)));
			}
			// ToolTip
			setToolTipText(alignmentControler.getURIOfConcept(ontologyNodeObject, ontologyNumber).toString());			
		}
		else {
			setIcon(impossibleIcon);
		}
		return this;
	}
}