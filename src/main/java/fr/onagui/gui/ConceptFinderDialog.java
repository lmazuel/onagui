/*
 * The Original Code is DOE (Differential Ontology Editor).
 *
 * The Initial Developer of the Original Code is INA.
 * Copyright (C) 2001. All Rights Reserved.
 *
 * DOE was developed by INA (http://www.ina.fr)
 *
 */

// Title:       NotionSelectionDialog
// Version:     1.0
// Copyright:   Copyright (c) 2001
// Authors:     Antoine Isaac & Raphael Troncy
// Company:     INA
// Description:


package fr.onagui.gui;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.PatternSyntaxException;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.google.common.base.Predicate;

import fr.onagui.alignment.OntoContainer;
import fr.onagui.control.MyTreeModel;
import fr.onagui.control.TreeNodeOntologyObject;


public class ConceptFinderDialog<ONTORES> extends JDialog implements ListSelectionListener, ActionListener, KeyListener {

    /** To make Java Happy... */
	private static final long serialVersionUID = -1706035218874738881L;
	
//	private int DIALOG_WIDTH = 350;
//  private int DIALOG_HEIGHT = 400;
    private JFrame mainFrame;
    private JTextField textAnswer;
    private JList conceptsList;
    private DefaultListModel conceptsListModel;
    private JScrollPane conceptsListScrollPane;
    private JButton OKButton, cancelButton;
    
    private JRadioButton containsSearchButton = null;
    private JRadioButton startsWithSearchButton = null;
    private JRadioButton endsWithSearchButton = null;
    private JRadioButton patternRadioButton = null;
    
    private JTree contextTree = null;

    private MyTreeModel<ONTORES> treeModel;
    
    private DefaultMutableTreeNode returnValue = null;
    
    public DefaultMutableTreeNode getReturnValue() {
		return returnValue;
	}
    
    private Comparator<DefaultMutableTreeNode> nodeComparator = new Comparator<DefaultMutableTreeNode>() {
    	@Override
		public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
    		return o1.toString().compareTo(o2.toString());
    	}
    };

    public static int FIND_DIFF_CONCEPT_ACTION=-1;
    public static int FIND_DIFF_RELATION_ACTION=-2;

    /*****************
    ** Constructeur **
    *****************/

    public ConceptFinderDialog (MyTreeModel<ONTORES> t, JFrame parentView, boolean modal) {

        super(parentView,modal);
        treeModel=t;
        mainFrame=parentView;
        try {
            initialize();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*****************************
    ** Component Initialization **
    *****************************/

    private void initialize() throws Exception {

//        this.setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
//        this.setResizable(false);
        this.setTitle(Messages.getString("ConceptFinderDialogTitle")); //$NON-NLS-1$

        JLabel questionLabel = new JLabel(Messages.getString("ConceptFinderDialogExplain"), SwingConstants.LEFT); //$NON-NLS-1$
        
        containsSearchButton = new JRadioButton(Messages.getString("ConceptFinderDialogCheckContains")); //$NON-NLS-1$
        containsSearchButton.setSelected(true);
        startsWithSearchButton = new JRadioButton(Messages.getString("ConceptFinderDialogCheckStartsWith")); //$NON-NLS-1$
        endsWithSearchButton = new JRadioButton(Messages.getString("ConceptFinderDialogCheckEndsWith")); //$NON-NLS-1$
        patternRadioButton = new JRadioButton(Messages.getString("ConceptFinderDialogCheckRegExp")); //$NON-NLS-1$
        ButtonGroup searchButtonGroup = new ButtonGroup();
        searchButtonGroup.add(containsSearchButton);
        searchButtonGroup.add(startsWithSearchButton);
        searchButtonGroup.add(endsWithSearchButton);
        searchButtonGroup.add(patternRadioButton);
        
        textAnswer = new JTextField();
        textAnswer.addKeyListener(this);

        conceptsListModel = buildListModel();
        conceptsList = new JList(conceptsListModel);
        conceptsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conceptsList.addListSelectionListener(this);
        conceptsListScrollPane = new JScrollPane(conceptsList);
        conceptsListScrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        /* La boite de recherche est une border avec un panneau central coupé en deux
         * et une barre de boutons (ok, cancel) en "sud".
         */
        setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel(new GridLayout(1,2));
        add(contentPanel, BorderLayout.CENTER);
        JPanel southPanel = new JPanel();
        add(southPanel, BorderLayout.SOUTH);
        
        // Je m'occuper de la boite de gauche: champ texte, radio boutons, list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(conceptsListScrollPane, BorderLayout.CENTER);
        
        //Put the radio buttons in a column in a panel.
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.add(containsSearchButton);
        radioPanel.add(startsWithSearchButton);
        radioPanel.add(endsWithSearchButton);
        radioPanel.add(patternRadioButton);

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel topleftPanel = new JPanel(gridbag);
        leftPanel.add(topleftPanel, BorderLayout.NORTH);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(10, 10, 0, 10);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(questionLabel, c);
        topleftPanel.add(questionLabel);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weightx = 0.5;
        c.weighty = 0.0;
        c.insets = new Insets(5, 10, 0, 10);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(textAnswer, c);
        topleftPanel.add(textAnswer);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.insets = new Insets(5, 10, 0, 10);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(radioPanel, c);
        topleftPanel.add(radioPanel);
        
        // J'ajoute le panneau gauche au panneau principal
        contentPanel.add(leftPanel);
        
        // Je m'occupe du panneau de droite
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        rightPanel.add(new JLabel(Messages.getString("ConceptFinderDialogTreeTitle")), BorderLayout.NORTH); //$NON-NLS-1$
        contextTree = new JTree(new DefaultMutableTreeNode(Messages.getString("ConceptFinderDialogDefaultTreeLabel"))); //$NON-NLS-1$
        JScrollPane treeScrollPane = new JScrollPane(contextTree);
        rightPanel.add(treeScrollPane, BorderLayout.CENTER);
        // J'ajoute le panneau droit au panneau central
        //contentPanel.add(rightPanel);
        
        // Les boutons de la barre du bas
        Dimension generalButtonDimension = new Dimension(90, 30);
        OKButton = new JButton(Messages.getString("ConceptFinderDialogOkButton")); //$NON-NLS-1$
        OKButton.setMinimumSize(generalButtonDimension);
        OKButton.setPreferredSize(generalButtonDimension);
        OKButton.addActionListener(this);
        cancelButton = new JButton(Messages.getString("ConceptFinderDialogCancelButton")); //$NON-NLS-1$
        cancelButton.setMinimumSize(generalButtonDimension);
        cancelButton.setPreferredSize(generalButtonDimension);
        cancelButton.addActionListener(this);
        southPanel.add(OKButton);
        southPanel.add(cancelButton);

		pack();
		setLocationRelativeTo(null);
    }

    /************
    ** Actions **
    ************/
    
	/** Build a ListModel using concept in parameters.
	 * @param nodes
	 * @return
	 */
	private DefaultListModel buildListModel(Collection<DefaultMutableTreeNode> nodes) {
		SortedSet<DefaultMutableTreeNode> sortedNodeList = new TreeSet<DefaultMutableTreeNode>(nodeComparator);
		sortedNodeList.addAll(nodes);
		
		DefaultListModel newListModel = new DefaultListModel();
		for(DefaultMutableTreeNode refEntity : sortedNodeList)
			// Directly the DefaultMutableTreeNode instance since the "toString" method is overrided
			newListModel.addElement(refEntity);
		return newListModel;		
	}
	
	/** Build a ListModel using collection "conceptsCollection"
	 * @return
	 */
	private DefaultListModel buildListModel() {
		return buildListModel(Collections.<DefaultMutableTreeNode> emptyList());
	}

    // Text Answer action performed
    public void textAnswer_keyReleased() {

        final String answer = textAnswer.getText();
        
		Collection<DefaultMutableTreeNode> entities = treeModel.accept(new Predicate<DefaultMutableTreeNode>() {
			@Override
			public boolean apply(DefaultMutableTreeNode node) {
				String cptFromList = node.toString();
				// Consider the test with the radio button selected
				if(containsSearchButton.isSelected()) {
					return cptFromList.toUpperCase().contains(answer.toUpperCase());
				}
				else if(startsWithSearchButton.isSelected()) {
					return cptFromList.toUpperCase().startsWith(answer.toUpperCase());
				}
				else if(endsWithSearchButton.isSelected()) {
					return cptFromList.toUpperCase().endsWith(answer.toUpperCase());
				}
				else if(patternRadioButton.isSelected()) {
					try {
						return cptFromList.toUpperCase().matches(answer);
					} catch (PatternSyntaxException e) {
						return false;
					}
				}
				return false;
			}
		}, 50);
		if(!entities.isEmpty()) {
			DefaultListModel newListModel = buildListModel(entities);
			conceptsList.setModel(newListModel);
			conceptsList.setSelectedIndex(0); // Select the first by default
		}
		else {
			conceptsList.clearSelection();
			conceptsList.setModel(new DefaultListModel());
		}
		textAnswer.setText(answer);        
    }

    // Clear button mouse released
    public void cancelButton_actionPerformed() {

        this.processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    // Done button mouse released
    public void OKButton_actionPerformed() {

    	if(conceptsList.getSelectedIndex() != -1) {
    		returnValue = (DefaultMutableTreeNode)conceptsList.getSelectedValue();
    		// Find the REAL DefaultMutableTreeNode
    		Object userObject = returnValue.getUserObject();
    		if(userObject instanceof TreeNodeOntologyObject) {
    			TreeNodeOntologyObject<ONTORES> ontoObject = (TreeNodeOntologyObject<ONTORES>) userObject;
    			returnValue = treeModel.getFirstNodeFromConcept(ontoObject.getConcept());
    		}
    		else {
    			returnValue = null;
    		}
    		this.processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    	}
    	
        else {
            JOptionPane.showMessageDialog(mainFrame, Messages.getString("ConceptFinderDialogErrorFound"), Messages.getString("ConceptFinderDialogErrorFoundTitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
            textAnswer.setText(""); //$NON-NLS-1$
        }
    }

    // Overriden Method actionPerformed of ActionListener
    @Override
	public void actionPerformed(ActionEvent e) {

        if (e.getSource()==OKButton)
            OKButton_actionPerformed();
        else if (e.getSource()==cancelButton)
            cancelButton_actionPerformed();
        else
            System.out.println("ERROR : buttons allowed are OK and Cancel !"); //$NON-NLS-1$
    }

    public void keyPressed(KeyEvent e) {


    }

    public void keyReleased(KeyEvent e) {

        if (e.getSource()==textAnswer) textAnswer_keyReleased();
    }

    public void keyTyped(KeyEvent e) {


    }

    // Overriden Method valueChanged of ListSelectionListener on the JList
    public void valueChanged(ListSelectionEvent e) {

		DefaultMutableTreeNode selectedValue = (DefaultMutableTreeNode)((JList)e.getSource()).getSelectedValue();
		if(selectedValue != null) {
			// Changer le text de la boite de recherche
			textAnswer.setText(selectedValue.toString());
			// Mettre à jour l'arbre
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedValue.getParent();
			DefaultTreeModel treeModel = new DefaultTreeModel(parent);
			contextTree.setModel(treeModel);
			contextTree.repaint();
			// Selectionner le bon concept dans l'arbre
			TreePath pathToRoot = new TreePath(treeModel.getPathToRoot(selectedValue));
			contextTree.scrollPathToVisible(pathToRoot);
			contextTree.setSelectionPath(pathToRoot);
		}
			
    }

}
