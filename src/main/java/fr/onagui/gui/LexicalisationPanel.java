/**
 * 
 */
package fr.onagui.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

/**
 * @author Laurent Mazuel
 */
public class LexicalisationPanel extends JPanel {
	
	/** To make Java happy */
	private static final long serialVersionUID = 6375338505045030509L;
	
	private JTextArea prefLabels= null;
	private JTextArea altLabels= null;
	private JTextArea fragURI=null;
	
	public static final String EMPTY_LABEL = Messages.getString("LexicalisationPanel.NoInfoLabel"); //$NON-NLS-1$
	private static final int COLUMN_WIDTH = 5;
	
	public LexicalisationPanel() {
		prefLabels = new JTextArea(EMPTY_LABEL, 2, COLUMN_WIDTH);
		altLabels = new JTextArea(EMPTY_LABEL, 5, COLUMN_WIDTH);
		fragURI = new JTextArea(EMPTY_LABEL, 1, COLUMN_WIDTH);
		prefLabels.setEditable(false);
		altLabels.setEditable(false);
		fragURI.setEditable(false);
		prefLabels.setLineWrap(true);
		altLabels.setLineWrap(true);
		fragURI.setLineWrap(true);
		JScrollPane prefLabelScrollPane = new JScrollPane(prefLabels);
		JScrollPane altLabelScrollPane = new JScrollPane(altLabels);
		JScrollPane fragURIScrollPane = new JScrollPane(fragURI);
		prefLabelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		altLabelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		fragURIScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		setLayout(new GridLayout(3,1));
		JPanel fragPanel = new JPanel(new BorderLayout());
		fragPanel.add(new JLabel(Messages.getString("LexicalisationPanel.FragURI")), BorderLayout.NORTH); //$NON-NLS-1$
		fragPanel.add(fragURI, BorderLayout.CENTER);
		JPanel prefPanel = new JPanel(new BorderLayout());
		prefPanel.add(new JLabel(Messages.getString("LexicalisationPanel.PrefLabel")), BorderLayout.NORTH); //$NON-NLS-1$
		prefPanel.add(prefLabels, BorderLayout.CENTER);
		JPanel altPanel = new JPanel(new BorderLayout());
		altPanel.add(new JLabel(Messages.getString("LexicalisationPanel.AltLabel")), BorderLayout.NORTH); //$NON-NLS-1$
		altPanel.add(altLabels, BorderLayout.CENTER);
		
		add(prefPanel);
		add(altPanel);
		add(fragPanel);
		
		setBorder(new TitledBorder(Messages.getString("LexicalisationPanel.LexiconPanelMainTitle"))); //$NON-NLS-1$
	}
	
	/**
	 * @param fragURI the fragURI to set
	 */
	void setFragURI(String fragURI) {
		if(fragURI == null) this.fragURI.setText(EMPTY_LABEL);
		else this.fragURI.setText(fragURI);
	}

	private void setLabelsToPanel(JTextArea labelPan, Set<String> labels) {
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

	/**
	 * @param altLabels the altLabel to set
	 */
	void setAltLabel(Set<String> altLabelStrings) {
		setLabelsToPanel(altLabels, altLabelStrings);
	}

	/**
	 * @param prefLabels the prefLabel to set
	 */
	void setPrefLabel(Set<String> prefLabelStrings) {
		setLabelsToPanel(prefLabels, prefLabelStrings);
	}
}
