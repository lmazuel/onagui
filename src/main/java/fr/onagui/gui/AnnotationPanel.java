/**
 * 
 */
package fr.onagui.gui;

import java.awt.BorderLayout;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

/**
 * @author Laurent Mazuel
 */
public class AnnotationPanel extends JPanel {
	
	/** To make Java happy */
	private static final long serialVersionUID = -1650013759466384672L;
	
	private JTextArea labelPan= null;
	
	public static final String EMPTY_LABEL = Messages.getString("LexicalisationPanel.NoInfoLabel"); //$NON-NLS-1$
	private static final int COLUMN_WIDTH = 5;
	
	public AnnotationPanel() {
		setLayout(new BorderLayout());
		setBorder(new TitledBorder(""));

		labelPan = new JTextArea(EMPTY_LABEL, 2, COLUMN_WIDTH);
		labelPan.setEditable(false);
		labelPan.setLineWrap(true);
		JScrollPane prefLabelScrollPane = new JScrollPane(labelPan);
		prefLabelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		JPanel prefPanel = new JPanel(new BorderLayout());
		prefPanel.add(labelPan, BorderLayout.CENTER);
		add(prefPanel, BorderLayout.CENTER);		
	}
	
	public void setLabelsToPanel(Set<String> labels) {
		if(labels == null || labels.isEmpty()) labelPan.setText(EMPTY_LABEL);
		else {
			StringBuffer buf = new StringBuffer();
			boolean firstLine = true;
			for(String lab : labels) {
				if(!firstLine) {
					buf.append('\n');
				}
				buf.append(lab);
				firstLine = false;
			}
			labelPan.setRows(labels.size());
			labelPan.setText(buf.toString());
		}
	}
}
