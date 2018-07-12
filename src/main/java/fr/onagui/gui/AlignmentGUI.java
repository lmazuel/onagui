package fr.onagui.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.google.common.collect.Sets;

import fr.onagui.alignment.AbstractAlignmentMethod;
import fr.onagui.alignment.Alignment;
import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.Mapping.MAPPING_TYPE;
import fr.onagui.alignment.Mapping.VALIDITY;
import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.io.AlignmentFormat;
import fr.onagui.alignment.method.LabelAlignmentMethod;
import fr.onagui.config.OnaguiConfigImpl;
import fr.onagui.config.OnaguiConfiguration;
import fr.onagui.config.ScoreColorConfiguration;
import fr.onagui.control.AlignmentControler;
import fr.onagui.control.MyTreeExpansionListener;
import fr.onagui.control.MyTreeModel;
import fr.onagui.control.TreeNodeOntologyObject;
import fr.onagui.gui.OntologyType.OntologyFormat;


public class AlignmentGUI extends JFrame implements TreeSelectionListener {

	/** To make Java Happy */
	private static final long serialVersionUID = 5024022329735890457L;

	/* Common file filter */
	private static final FileNameExtensionFilter RDF_ALIGNMENT_FILTER = new FileNameExtensionFilter(Messages.getString("RdfAlignmentFilterName"), "rdf", "xml"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final FileNameExtensionFilter CSV_ALIGNMENT_FILTER = new FileNameExtensionFilter(Messages.getString("CsvAlignmentFilterName"), "csv"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final FileNameExtensionFilter SKOS_ALIGNMENT_FILTER = new FileNameExtensionFilter(Messages.getString("SkosAlignmentFilterName"), "rdf", "xml"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/* general String */
	private static final String GUI_TITLE = "OnAGUI - Ontology Alignement GUI"; //$NON-NLS-1$
	private static final String REF_PREFIX_1 = Messages.getString("Ontology1LabelTitle"); //$NON-NLS-1$
	private static final String REF_PREFIX_2 = Messages.getString("Ontology2LabelTitle"); //$NON-NLS-1$

	/* current param file */
	private static final String DYNAMIC_CONFIGFILE = "onagui_dynamic_param.txt" ; //$NON-NLS-1$

	/* a progress bar */
	private IndeterminateProgressDialog progressBar = null;

	/* Some flag */
	private OntologyType ontology1Type = null;
	private OntologyType ontology2Type = null;
	private URI ontology1File = null;
	private URI ontology2File = null;
	private boolean filterOnto1Enabled = false;
	private boolean filterOnto2Enabled = false;

	/* configuration */
	OnaguiConfiguration configuration = null;

	/** the model */
	AlignmentControler alignmentControler = null;

	/* From menu bar */
	private Map<Integer, JMenuItem> langMenus;
	private JMenuItem reload1;
	private JMenuItem reload2;
	private JMenuItem importRDF;
	private JMenuItem importSKOS;
	private JMenuItem importCSV;
	private JMenuItem exportRDFAll;
	private JMenuItem exportRDFInvalid;
	private JMenuItem exportRDFToConfirm;
	private JMenuItem exportRDFValid;
	private JMenuItem exportCSV;
	private JMenuItem exportSkos;
	private JMenuItem statAlignItem;
	private Collection<JMenuItem> syntacticAlignItem = null;
	private JMenuItem searchIn1;
	private JMenuItem searchIn2;

	/* formulaire de rentrée des alignements */
	private JTextField scoreFied = null;
	private JComboBox<MAPPING_TYPE> typeField = null;

	/* center pane */
	private JTextArea snoComment;
	private JCheckBox notFoundFrom1;
	private JCheckBox notFoundFrom2;
	private JTable centerTable;
	private TableRowSorter<MappingTableModel> sorter = null;
	private MappingTableModel tableModel;

	/* left pane */
	private JTree treeFrom1;

	/* right pane */
	private JTree treeFrom2;

	/** the label of state bar */
	private JLabel stateText = new JLabel(GUI_TITLE);
//	private JLabel state1Text = new JLabel(Messages.getString("LocalStatisticLabel")); //$NON-NLS-1$
//	private JLabel state2Text = new JLabel(Messages.getString("LocalStatisticLabel")); //$NON-NLS-1$

	/** the label of reference bar */
	private JLabel refText1 = new JLabel(REF_PREFIX_1);
	private JLabel refText2 = new JLabel(REF_PREFIX_2);
	
	
	/** Lexicalisation panel */
	LexicalisationPanel lexic1 = null;
	LexicalisationPanel lexic2 = null;
	
	/** Annotations panel */
	JComboBox<String> annotationChoice = null;
	AnnotationPanel annot1 = null;
	AnnotationPanel annot2 = null;

	/***
	 * label
	 */
	LabelMethodParameterDialog labelParameterDialog = null;
	
	/** Memoire du dernier dossier ou j'ai ouvert un truc */
	private File lastDirectory = null;

	/* used for debug only */
	//	private static boolean DEBUG = false;

	//Optionally play with line styles.  Possible values are
	//"Angled" (the default), "Horizontal", and "None".
	private static boolean playWithLineStyle = false;
	private static String lineStyle = "Horizontal"; //$NON-NLS-1$

	public AlignmentGUI() {
		super(GUI_TITLE);

		/* ****************** *
		 * Configuration file *
		 * ****************** */

		// FIXME generalize path elsewhere ?
		File config_file = new File("onagui_config.xml"); //$NON-NLS-1$
		configuration = new OnaguiConfigImpl(config_file);

		/* ********** *
		 * Controleur *
		 * ********** */
		alignmentControler = new AlignmentControler();

		/* *********************************** *
		 * Chargement de l'interface graphique *
		 * *********************************** */
		setLayout(new BorderLayout());

		JPanel northPan = new JPanel(new GridLayout(2,1));
		JPanel centerPan = new JPanel(new GridLayout(1,1));
		JPanel southPan = new JPanel();

		/* ********* *
		 * North Bar *
		 * ********* */

		northPan.add(refText1);
		northPan.add(refText2);
		add(northPan, BorderLayout.NORTH);

		/* ********* *
		 * South bar *
		 * ********* */

		southPan.add(stateText);
		add(southPan, BorderLayout.SOUTH);

		/* ********** *
		 * Center pan *
		 * ********** */

		/* Create the tree */
		//Create a tree that allows one selection at a time.
		treeFrom1 = new JTree(new DefaultMutableTreeNode("Empty")); //$NON-NLS-1$
		treeFrom1.setShowsRootHandles(true);
		treeFrom1.setLargeModel(true);
		treeFrom1.setCellRenderer(new TreeNodeRenderer(1,
				configuration.getScoreColorConfiguration(),
				alignmentControler));
		treeFrom1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		// Enable tool tips.
		ToolTipManager.sharedInstance().registerComponent(treeFrom1);

		//Create a tree that allows one selection at a time.
		treeFrom2 = new JTree(new DefaultMutableTreeNode("Empty")); //$NON-NLS-1$
		treeFrom2.setShowsRootHandles(true);
		treeFrom2.setLargeModel(true);
		treeFrom2.setCellRenderer(new TreeNodeRenderer(2,
				configuration.getScoreColorConfiguration(),
				alignmentControler));
		treeFrom2.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		// Enable tool tips.
		ToolTipManager.sharedInstance().registerComponent(treeFrom2);

		//Listen for when the selection changes.
		treeFrom1.addTreeSelectionListener(this);
		treeFrom2.addTreeSelectionListener(this);

		//Listen for when the expand/collapse.
		treeFrom1.addTreeExpansionListener(new MyTreeExpansionListener(this, treeFrom1));
		treeFrom2.addTreeExpansionListener(new MyTreeExpansionListener(this, treeFrom2));
		
		if (playWithLineStyle) {
			System.out.println("line style = " + lineStyle); //$NON-NLS-1$
			treeFrom1.putClientProperty("JTree.lineStyle", lineStyle); //$NON-NLS-1$
		}

		//Create the scroll panes and add the tree to it. 
		JScrollPane treeViewFrom1 = new JScrollPane(treeFrom1);
		JScrollPane treeViewFrom2 = new JScrollPane(treeFrom2);

		/* Create the real center pan */

		//Create the label view
		JPanel labelPane = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		labelPane.setLayout(gridbag);
		labelPane.setBorder(BorderFactory.createEmptyBorder());

		// The not found box for 1
		notFoundFrom1 = new JCheckBox(Messages.getString("AlignNotPossible.1")); //$NON-NLS-1$
		notFoundFrom1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode selected = (DefaultMutableTreeNode)treeFrom1.getLastSelectedPathComponent();
				if(selected == null) return;
				if(selected.getUserObject() instanceof TreeNodeOntologyObject) {
					// Demander au controleur de mettre à jour le modèle
					alignmentControler.notFoundChange(notFoundFrom1.isSelected(), 1, (TreeNodeOntologyObject)selected.getUserObject());
					// Refaire l'affichage
					// FIXME grisé la saisie des alignements?
					treeFrom1.repaint();
				}
			}
		});
		// The not found box for 2
		notFoundFrom2 = new JCheckBox(Messages.getString("AlignNotPossible.2")); //$NON-NLS-1$
		notFoundFrom2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode selected = (DefaultMutableTreeNode)treeFrom2.getLastSelectedPathComponent();
				if(selected == null) return;
				if(selected.getUserObject() instanceof TreeNodeOntologyObject) {
					// Demander au controleur de mettre à jour le modèle
					alignmentControler.notFoundChange(notFoundFrom2.isSelected(), 2, (TreeNodeOntologyObject)selected.getUserObject());
					// Refaire l'affichage
					// FIXME grisé la saisie des alignements?
					treeFrom2.repaint();
				}
			}
		});

		// Le formulaire d'ajout
		JPanel scorePanel = new JPanel(new GridLayout(2,1));
		scorePanel.add(new JLabel(Messages.getString("ScoreCenterPanLabel"))); //$NON-NLS-1$
		scoreFied = new JTextField("1.0"); //$NON-NLS-1$
		scorePanel.add(scoreFied);
		JPanel typePanel = new JPanel(new GridLayout(2,1));
		typePanel.add(new JLabel(Messages.getString("TypeCenterPanLabel"))); //$NON-NLS-1$
		typeField = new JComboBox<MAPPING_TYPE>(MAPPING_TYPE.values());
		typeField.setRenderer(new MappingTypeRenderer());
		typeField.setSelectedItem(MAPPING_TYPE.EQUIV);
		typePanel.add(typeField);
		JButton addButton = new JButton(Messages.getString("AddCenterPanButtonLabel")); //$NON-NLS-1$
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get information to build a mapping
				DefaultMutableTreeNode node1 = (DefaultMutableTreeNode)treeFrom1.getLastSelectedPathComponent();
				DefaultMutableTreeNode node2 = (DefaultMutableTreeNode)treeFrom2.getLastSelectedPathComponent();
				double score = 1.0; // Score par default...
				try {
					score = Double.valueOf(scoreFied.getText());
				}
				catch(NumberFormatException f) {}
				MAPPING_TYPE type = (MAPPING_TYPE)typeField.getSelectedItem();
				alignmentControler.addMapping(
						(TreeNodeOntologyObject)node1.getUserObject(),
						(TreeNodeOntologyObject)node2.getUserObject(),
						score,
						type,
						"manual"); //$NON-NLS-1$
				// Mise à jour de l'affichage
				refreshGUIFromModel();
			}
		});
		JButton removeButton = new JButton(Messages.getString("DelCenterPanButtonLabel")); //$NON-NLS-1$
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get information to build a mapping
				for(int index : centerTable.getSelectedRows()) {
					Mapping map = tableModel.getMappingAt(centerTable.convertRowIndexToModel(index));
					alignmentControler.removeMapping(map);
				}
				// On vide la selection
				centerTable.clearSelection();
				// Mise à jour de l'affichage
				refreshGUIFromModel();
			}
		});

		JButton removeAllButton = new JButton(Messages.getString("DelAllCenterPanButtonLabel")); //$NON-NLS-1$
		removeAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				alignmentControler.clearAllAlignments();
				// Mise à jour de l'affichage
				refreshGUIFromModel();
			}
		});

		// The table
		tableModel = new MappingTableModel(alignmentControler);
		centerTable = new JTable(tableModel) {
			public void tableChanged(TableModelEvent e) {
				super.tableChanged(e);
				repaint();
			}
		};
		sorter = new TableRowSorter<MappingTableModel>(tableModel);
		// tri de la table sur la colonne 'Score' par défaut
		// we call it twice to have descending sort order
		sorter.toggleSortOrder(3);
		sorter.toggleSortOrder(3);
		// to avoid interactions when editing mapping types in table,
		// we set only 2 sort keys. This means that only the last 2 clicked columns are used for sorting
		// see https://docs.oracle.com/javase/7/docs/api/javax/swing/DefaultRowSorter.html#setMaxSortKeys(int)
		sorter.setMaxSortKeys(2);
		centerTable.setRowSorter(sorter);
		//		centerTable.setAutoCreateRowSorter(true);
		DefaultListSelectionModel listSelectionModel = new DefaultListSelectionModel();
		listSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listSelectionModel.addListSelectionListener(new MyTableSelectionListerner());
		centerTable.setSelectionModel(listSelectionModel);
		centerTable.setRowSelectionAllowed(true);
		centerTable.setColumnSelectionAllowed(false);
		//		centerTable.setCellSelectionEnabled(false);
		centerTable.setFillsViewportHeight(true);
		JScrollPane tableScrollPan = new JScrollPane(centerTable);
		// Color renderer
		ScoreColorConfiguration scm  = configuration.getScoreColorConfiguration();
		centerTable.setDefaultRenderer(Double.class, new ScoreColorRenderer(scm));
		centerTable.setDefaultRenderer(MAPPING_TYPE.class, new TypeRenderer());
		centerTable.setDefaultRenderer(VALIDITY.class, new ValidityRenderer());
		centerTable.setDefaultEditor(VALIDITY.class, new ValidityEditor(new JCheckBox(), tableModel, this));
		centerTable.setDefaultEditor(MAPPING_TYPE.class, new TypeEditor(new JCheckBox(), tableModel, this));
		
		// The comment pane (with JLabel included)		
		JPanel commentPane = new JPanel(new BorderLayout());
		commentPane.add(new JLabel(Messages.getString("CommentTitleLabel")), BorderLayout.NORTH); //$NON-NLS-1$
		snoComment = new JTextArea(10,40);
		commentPane.add(new JScrollPane(snoComment), BorderLayout.CENTER);
		JButton majBut = new JButton(Messages.getString("UpdateCommentButtonLabel")); //$NON-NLS-1$
		majBut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Je recupère le mapping associée à cette ligne
				if(centerTable.getSelectedRowCount() == 1) {
					int index = centerTable.getSelectedRow();
					
					Mapping map = tableModel.getMappingAt(centerTable.convertRowIndexToModel(index));
					map.setComment(snoComment.getText());
				}
			
			}
		});
		commentPane.add(majBut, BorderLayout.SOUTH);

		// Panneau de lexicalisation
		JPanel lexicalisationPanel = new JPanel(new GridLayout(1,2));
		lexic1 = new LexicalisationPanel();
		lexic2 = new LexicalisationPanel();
		lexicalisationPanel.add(lexic1);
		lexicalisationPanel.add(lexic2);

		// Panneau d'annotation
		JPanel annotationPanel = new JPanel(new BorderLayout());
		annotationChoice = new JComboBox<String>();
		annotationPanel.add(annotationChoice, BorderLayout.NORTH);
		JPanel annotationContentPanel = new JPanel(new GridLayout(1,2));
		annot1 = new AnnotationPanel();
		annot2 = new AnnotationPanel();
		annotationContentPanel.add(annot1);
		annotationContentPanel.add(annot2);
		annotationPanel.add(annotationContentPanel, BorderLayout.CENTER);
		annotationChoice.addActionListener(e -> {
			refreshGenericAnnotationPanel();
		});
		
		// Tabbed pane
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab(Messages.getString("LexicTabLabel"), lexicalisationPanel); //$NON-NLS-1$
		tabbedPane.addTab(Messages.getString("CommentTabLabel"), commentPane); //$NON-NLS-1$
		tabbedPane.addTab(Messages.getString("AnnotationLabel"), annotationPanel); //$NON-NLS-1$

		// Add to label view
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(notFoundFrom1, c);
		labelPane.add(notFoundFrom1);

		c.gridx = 2;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(notFoundFrom2, c);
		labelPane.add(notFoundFrom2);

		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(scorePanel, c);
		labelPane.add(scorePanel);

		c.gridx = 1;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(typePanel, c);
		labelPane.add(typePanel);

		c.gridx = 2;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(addButton, c);
		labelPane.add(addButton);

		c.gridx = 3;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(removeButton, c);
		labelPane.add(removeButton);

		c.gridx = 4;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(removeAllButton, c);
		labelPane.add(removeAllButton);

		c.gridx = 0;
		c.gridy = 2;
		c.gridheight = 1;
		c.gridwidth = 5;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(tableScrollPan, c);
		labelPane.add(tableScrollPan);

		c.gridx = 0;
		c.gridy = 3;
		c.gridheight = 1;
		c.gridwidth = 5;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.VERTICAL;
		gridbag.setConstraints(tabbedPane, c);
		labelPane.add(tabbedPane);

		// Create the pane from the tree
		// For tree 1
		JPanel onto1Panel = new JPanel(new BorderLayout());
		JPanel panelHaut1 = new JPanel(new GridLayout(2,1));		
		JCheckBox onto1Filter = new JCheckBox(Messages.getString("Filter1Checkbox")); //$NON-NLS-1$
		onto1Filter.setSelected(filterOnto1Enabled);
		onto1Filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Mise à jour du flag
				filterOnto1Enabled = ((JCheckBox)e.getSource()).isSelected();
				// Juste un refresh, c'est lui qui lira le flag
				refreshGUIFromModel();
			};
		});
		JButton locateConcept1 = new JButton(Messages.getString("FindConceptButtonLabel")); //$NON-NLS-1$
		locateConcept1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				locateConceptsInTree();
			}
		});
		panelHaut1.add(onto1Filter);
		panelHaut1.add(locateConcept1);
		onto1Panel.add(panelHaut1, BorderLayout.NORTH);
		onto1Panel.add(treeViewFrom1, BorderLayout.CENTER);
//		onto1Panel.add(state1Text, BorderLayout.SOUTH);
		// For tree 2
		JPanel onto2Panel = new JPanel(new BorderLayout());
		JPanel panelHaut2 = new JPanel(new GridLayout(2,1));		
		JCheckBox onto2Filter = new JCheckBox(Messages.getString("Filter2Checkbox")); //$NON-NLS-1$
		onto2Filter.setSelected(filterOnto2Enabled);
		onto2Filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Mise à jour du flag
				filterOnto2Enabled = ((JCheckBox)e.getSource()).isSelected();
				// Juste un refresh, c'est lui qui lira le flag
				refreshGUIFromModel();
			};
		});
		JButton locateConcept2 = new JButton(Messages.getString("FindConceptButtonLabel")); //$NON-NLS-1$
		locateConcept2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				locateConceptsInTree();
			}
		});
		panelHaut2.add(onto2Filter);
		panelHaut2.add(locateConcept2);
		onto2Panel.add(panelHaut2, BorderLayout.NORTH);
		onto2Panel.add(treeViewFrom2, BorderLayout.CENTER);
//		onto2Panel.add(state2Text, BorderLayout.SOUTH);

		//Add the scroll panes to a split pane.
		JSplitPane splitPaneDroit = new JSplitPane();
		splitPaneDroit.setLeftComponent(labelPane);
		splitPaneDroit.setRightComponent(onto2Panel);
		JSplitPane splitPaneGauche = new JSplitPane();
		splitPaneGauche.setLeftComponent(onto1Panel);
		splitPaneGauche.setRightComponent(splitPaneDroit);

		Dimension minimumSize = new Dimension(150, 50);
		labelPane.setMinimumSize(minimumSize);
		onto1Panel.setMinimumSize(minimumSize);
		onto2Panel.setMinimumSize(minimumSize);
		//		splitPaneGauche.setDividerLocation(350); 
		//		splitPaneGauche.setPreferredSize(new Dimension(500, 300));

		//Add the split pane to this panel.
		centerPan.add(splitPaneGauche);

		/* ************************************** *
		 * Verify I can load temporary attributes *
		 * ************************************** */

		loadDynamicOnaguiInfos();

		/* ********* *
		 * Final.... *
		 * ********* */

		// Add the center pan to the frame
		add(centerPan, BorderLayout.CENTER);

		// Final assert
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// affectation du menu
		this.setJMenuBar(createJMenuBar());

		// creation des popup-menu
		this.createCenterPopupMenu();

		// progress bar
		progressBar = new IndeterminateProgressDialog(this);

		//Display the window.
		this.pack();
		this.setVisible(true);

	}

	private void loadDynamicOnaguiInfos() {
		String tmpdir = System.getProperty("java.io.tmpdir") ; //$NON-NLS-1$
		if (tmpdir != null) {
			// Tmpdir exist
			String onaguiFileName = tmpdir+"/"+DYNAMIC_CONFIGFILE ; //$NON-NLS-1$
			File onaguiDynamicFile = new File(onaguiFileName) ;
			if (onaguiDynamicFile.isFile()) {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(onaguiDynamicFile)) ;
					String fileName = reader.readLine();
					reader.close();
					File lastDirUsedInFilechooser = new File(fileName) ;
					if(lastDirUsedInFilechooser.isDirectory()) {
						lastDirectory = lastDirUsedInFilechooser ;
					}
				} catch (Exception e1) {
					// Silent exception
				}
			}
		}
		// FIXME beurk...
		lastDirectory = configuration.getOntologyLastOpenDirectory();
	}

	private void saveDynamicOnaguiInfos(File file) {
		String tmpdir = System.getProperty("java.io.tmpdir") ; //$NON-NLS-1$
		if (tmpdir != null) {
			// Tmpdir exist
			String onaguiFileName = tmpdir+"/"+DYNAMIC_CONFIGFILE ; //$NON-NLS-1$
			File onaguiDynamicFile = new File(onaguiFileName) ;
			// If file exist, delete it
			if (onaguiDynamicFile.isFile()) {
				onaguiDynamicFile.delete() ;
			}
			// Write it
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(onaguiDynamicFile)) ;
				writer.write(file.getAbsolutePath()) ;
				writer.flush();
				writer.close();
			} catch (Exception e) {
				// Silent exception
			}
		}		
	}

	private JMenuBar createJMenuBar() {

		/* **************************** *
		 * Creation de la barre de menu *
		 * **************************** */

		JMenuBar menuBar = new JMenuBar();
		
		// La barre "fichier"
		JMenu fichierMenu = new JMenu(Messages.getString("FileMenu")); //$NON-NLS-1$
		menuBar.add(fichierMenu);
		// Menu local
		// Premier menu, ontologie local
		JMenu loadLocalOntologyMenu = new JMenu(Messages.getString("LoadOnto1Menu")); //$NON-NLS-1$
		fichierMenu.add(loadLocalOntologyMenu);
		JMenuItem loadLocalSkosMenu = new JMenuItem(Messages.getString("LoadSkosMenu")); //$NON-NLS-1$
		loadLocalOntologyMenu.add(loadLocalSkosMenu);
		JMenuItem loadLocalOwlMenu = new JMenuItem(Messages.getString("LoadOwlMenu")); //$NON-NLS-1$
		loadLocalOntologyMenu.add(loadLocalOwlMenu);				
		JMenuItem loadLocalRdfMenu = new JMenuItem(Messages.getString("LoadRdfMenu")); //$NON-NLS-1$
		loadLocalOntologyMenu.add(loadLocalRdfMenu);
		
		// Deuxième menu, ontologie de reference
		JMenu loadReferenceOntologyMenu = new JMenu(Messages.getString("LoadOnto2Menu")); //$NON-NLS-1$
		fichierMenu.add(loadReferenceOntologyMenu);
		JMenuItem loadReferenceSkosMenu = new JMenuItem(Messages.getString("LoadSkosMenu")); //$NON-NLS-1$
		loadReferenceOntologyMenu.add(loadReferenceSkosMenu);
		JMenuItem loadReferenceOwlMenu = new JMenuItem(Messages.getString("LoadOwlMenu")); //$NON-NLS-1$
		loadReferenceOntologyMenu.add(loadReferenceOwlMenu);
		JMenuItem loadReferenceRdfMenu = new JMenuItem(Messages.getString("LoadRdfMenu")); //$NON-NLS-1$
		loadReferenceOntologyMenu.add(loadReferenceRdfMenu);
		
		fichierMenu.add(new JSeparator());
		// Rechargement
		reload1 = new JMenuItem(Messages.getString("ReloadOnto1")); //$NON-NLS-1$
		fichierMenu.add(reload1);
		reload1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Recharger l'ontologie 1
				reloadOntology(ontology1Type);
			}
		});
		reload2 = new JMenuItem(Messages.getString("ReloadOnto2")); //$NON-NLS-1$
		fichierMenu.add(reload2);
		reload2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Recharger l'ontologie 2
				reloadOntology(ontology2Type);
			}
		});
		fichierMenu.add(new JSeparator());

		// Choisir la label d'affichage
		langMenus = new TreeMap<Integer, JMenuItem>();
		for(int number=1; number<=2; ++number) {
			JMenu lang = new JMenu(Messages.getString("ChangeLabelLanguage")+number); //$NON-NLS-1$
			fichierMenu.add(lang);
			langMenus.put(number, lang);
		}

		fichierMenu.add(new JSeparator());

		// Importation
		JMenu importMenu = new JMenu(Messages.getString("ImportAlignmentMenu")); //$NON-NLS-1$
		fichierMenu.add(importMenu);
		importRDF = new JMenuItem(Messages.getString("ImportRdfAlignmentMenu")); //$NON-NLS-1$
		importMenu.add(importRDF);
		importSKOS = new JMenuItem(Messages.getString("ImportSkosAlignmentMenu")); //$NON-NLS-1$
		importMenu.add(importSKOS);
		importCSV = new JMenuItem(Messages.getString("ImportCsvAlignmentMenu")); //$NON-NLS-1$
		importMenu.add(importCSV);

		// Exportation
		JMenu exportMenu = new JMenu(Messages.getString("ExportAlignmentMenu")); //$NON-NLS-1$
		fichierMenu.add(exportMenu);
		JMenu exportRDF = new JMenu(Messages.getString("ExportRdfAlignmentMenu")); //$NON-NLS-1$
		exportMenu.add(exportRDF);
		exportRDFAll = new JMenuItem(Messages.getString("ExportAllRdfAlignmentMenu")); //$NON-NLS-1$
		exportRDFInvalid = new JMenuItem(Messages.getString("ExportInvalidAlignmentMenu")); //$NON-NLS-1$
		exportRDFToConfirm = new JMenuItem(Messages.getString("ExportWaitingAlignmentMenu")); //$NON-NLS-1$
		exportRDFValid = new JMenuItem(Messages.getString("ExportValidatedAlignmentMenu")); //$NON-NLS-1$
		exportRDF.add(exportRDFAll);
		exportRDF.add(new JSeparator());
		exportRDF.add(exportRDFInvalid);
		exportRDF.add(exportRDFToConfirm);
		exportRDF.add(exportRDFValid);
		exportCSV = new JMenuItem(Messages.getString("ExportCsvAlignment")); //$NON-NLS-1$
		exportMenu.add(exportCSV);
		exportSkos = new JMenuItem(Messages.getString("ExportSkosAlignment")); //$NON-NLS-1$
		exportMenu.add(exportSkos);

		// Un bouton quitter
		fichierMenu.add(new JSeparator());
		JMenuItem quitItem = new JMenuItem(Messages.getString("ExitMenu")); //$NON-NLS-1$
		fichierMenu.add(quitItem);
		quitItem.addActionListener(l -> AlignmentGUI.this.dispose());

		// Le menu barre "rechercher"
		JMenu searchMenu = new JMenu(Messages.getString("SearchMenu")); //$NON-NLS-1$
		menuBar.add(searchMenu);
		searchIn1 = new JMenuItem(Messages.getString("SearchOnto1Menu")); //$NON-NLS-1$
		searchMenu.add(searchIn1);
		searchIn1.addActionListener(l -> launchFinderDialog(1));
		searchIn2 = new JMenuItem(Messages.getString("SearchOnto2Menu")); //$NON-NLS-1$
		searchMenu.add(searchIn2);	
		searchIn2.addActionListener(l -> launchFinderDialog(2));
		
		// Le menu barre "alignement"
		JMenu alignMenu = new JMenu(Messages.getString("AlignmentMenu")); //$NON-NLS-1$
		menuBar.add(alignMenu);
		syntacticAlignItem = new Vector<JMenuItem>();
		Set<AbstractAlignmentMethod> methods = alignmentControler.getLoadedAlignmentMethods();
		for(AbstractAlignmentMethod method : methods) {
			JMenuItem aMenu = new JMenuItem(method.toString());
			alignMenu.add(aMenu);
			aMenu.addActionListener(new AlignmentAlgorithmMenuListener(method));
			syntacticAlignItem.add(aMenu);
		}
		// La barre "statistiques"
		JMenu statMenu = new JMenu(Messages.getString("StatisticMenu")); //$NON-NLS-1$
		menuBar.add(statMenu);
		statAlignItem = new JMenuItem(Messages.getString("AlignmentStatMenu")); //$NON-NLS-1$
		statMenu.add(statAlignItem);
		
		//Menu Application
		
		JMenu applicationMenu = new JMenu(Messages.getString("Application")); //$NON-NLS-
		menuBar.add(applicationMenu);
		JMenu guiLanguageMenu = new JMenu(Messages.getString("GuiLanguage")); //$NON-NLS-
		applicationMenu.add(guiLanguageMenu);
		//Link github repository
		JMenuItem gitHubLink = new JMenuItem(Messages.getString("GitHubLink")); //$NON-NLS-1$
		applicationMenu.add(gitHubLink);
		
		// French language
		JMenuItem francaisMenu = new JMenuItem(Messages.getString("French")); //$NON-NLS-1$
		if(Locale.getDefault().getLanguage().equals("fr")) {
			francaisMenu.setEnabled(false);
		}
		guiLanguageMenu.add(francaisMenu);
		
		// English language
		JMenuItem anglaisMenu = new JMenuItem(Messages.getString("English")); //$NON-NLS-1$
		if(Locale.getDefault().getLanguage().equals("en")) {
			anglaisMenu.setEnabled(false);
		}		
		guiLanguageMenu.add(anglaisMenu);
		
		//Open a link in a web browser when we click
		gitHubLink.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					Desktop.getDesktop().browse(URI.create("https://github.com/lmazuel/onagui/issues"));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		// Import vocab1 menu
		loadLocalOwlMenu.addActionListener(new LoadOntologyActionListener(OntologyType.FIRST_ONTO_OWL));
		loadLocalSkosMenu.addActionListener(new LoadOntologyActionListener(OntologyType.FIRST_ONTO_SKOS));
		loadLocalRdfMenu.addActionListener(new LoadOntologyActionListener(OntologyType.FIRST_ONTO_RDF));
		// Import vocab 2 menu
		loadReferenceOwlMenu.addActionListener(new LoadOntologyActionListener(OntologyType.SECOND_ONTO_OWL));
		loadReferenceSkosMenu.addActionListener(new LoadOntologyActionListener(OntologyType.SECOND_ONTO_SKOS));
		loadReferenceRdfMenu.addActionListener(new LoadOntologyActionListener(OntologyType.SECOND_ONTO_RDF));
		// Import alignment menu
		importRDF.addActionListener(new ImportAlignmentActionListener(AlignmentFormat.EDOAL, RDF_ALIGNMENT_FILTER));		
		importSKOS.addActionListener(new ImportAlignmentActionListener(AlignmentFormat.SKOS, SKOS_ALIGNMENT_FILTER));
		importCSV.addActionListener(new ImportAlignmentActionListener(AlignmentFormat.CSV, CSV_ALIGNMENT_FILTER));
		// export alignment menu
		exportRDFAll.addActionListener(new ExportAlignmentActionListener(AlignmentFormat.EDOAL, null, RDF_ALIGNMENT_FILTER));
		exportRDFInvalid.addActionListener(new ExportAlignmentActionListener(AlignmentFormat.EDOAL, VALIDITY.INVALID, RDF_ALIGNMENT_FILTER));
		exportRDFToConfirm.addActionListener(new ExportAlignmentActionListener(AlignmentFormat.EDOAL, VALIDITY.TO_CONFIRM, RDF_ALIGNMENT_FILTER));
		exportRDFValid.addActionListener(new ExportAlignmentActionListener(AlignmentFormat.EDOAL, VALIDITY.VALID, RDF_ALIGNMENT_FILTER));
		exportCSV.addActionListener(new ExportAlignmentActionListener(AlignmentFormat.CSV, null, CSV_ALIGNMENT_FILTER));
		exportSkos.addActionListener(new ExportAlignmentActionListener(AlignmentFormat.SKOS, null, SKOS_ALIGNMENT_FILTER));
		
		statAlignItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				StringBuilder toShow1 = new StringBuilder();
				StringBuilder toShow2 = new StringBuilder();
				// Statistiques
				if(alignmentControler.hasContainer1()) {
					String stat1 = alignmentControler.computeStatis((DefaultMutableTreeNode)alignmentControler.getTreeModel1().getRoot(), 1);
					toShow1.append(alignmentControler.getContainer1URI());
					toShow1.append(":\n"); //$NON-NLS-1$
					toShow1.append(stat1);
				}				
				if(alignmentControler.hasContainer2()) {
					String stat2 = alignmentControler.computeStatis((DefaultMutableTreeNode)alignmentControler.getTreeModel2().getRoot(), 2);
					toShow2.append(alignmentControler.getContainer2URI());
					toShow2.append(":\n"); //$NON-NLS-1$
					toShow2.append(stat2);
				}
				StringBuilder toShow = new StringBuilder();
				if(toShow1.length() != 0 && toShow2.length() != 0) {
					toShow.append(toShow1);
					toShow.append("\n\n"); //$NON-NLS-1$
					toShow.append(toShow2);
				}
				else if (toShow1.length() != 0) {
					toShow.append(toShow1);
				}
				else if (toShow2.length() != 0) {
					toShow.append(toShow2);
				}
				else {
					toShow.append(Messages.getString("NoOntoStatError")); //$NON-NLS-1$
				}
				JOptionPane.showMessageDialog(AlignmentGUI.this, toShow.toString());
			}
		});

		// switch languages menu
		francaisMenu.addActionListener(new SwitchLanguageActionListener("fr", this));
		anglaisMenu.addActionListener(new SwitchLanguageActionListener("en", this));
		
		// Set the initial state of menu activation
		refreshMenuActivation();

		return menuBar;
	}

	class LoadOntologyActionListener implements ActionListener {		
		private OntologyType ontologyType;
		
		public LoadOntologyActionListener(OntologyType ontologyType) {
			super();
			this.ontologyType = ontologyType;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			loadOntologyWithFileChooser(this.ontologyType);
		}
	}
	
	class SwitchLanguageActionListener implements ActionListener {
		private String language;
		private AlignmentGUI oldGui;
		
		public SwitchLanguageActionListener(String language, AlignmentGUI oldGui) {
			super();
			this.language = language;
			this.oldGui = oldGui;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Messages.changeLanguage(this.language);
			// close current window and starts a new one
			// see http://stackoverflow.com/questions/26075081/java-application-restart-or-reset-button
			dispose();
			AlignmentGUI newGui = new AlignmentGUI();
			newGui.ontology1File = oldGui.ontology1File;
			newGui.ontology2File = oldGui.ontology2File;
			newGui.ontology1Type = oldGui.ontology1Type;
			newGui.ontology2Type = oldGui.ontology2Type;
			
			if(newGui.ontology1File != null && newGui.ontology1Type != null) {
				newGui.loadOntologyFromFileReference(newGui.ontology1Type, newGui.ontology1File);
			}
			if(newGui.ontology2File != null && newGui.ontology2Type != null) {
				newGui.loadOntologyFromFileReference(newGui.ontology2Type, newGui.ontology2File);
			}
		}
	}
	
	class ExportAlignmentActionListener implements ActionListener {
		// can be null to export all mappings
		private Mapping.VALIDITY validityToExport;
		private AlignmentFormat format;
		private FileNameExtensionFilter filenameFilter;

		public ExportAlignmentActionListener(AlignmentFormat format, VALIDITY validityToExport, FileNameExtensionFilter filenameFilter) {
			super();
			this.format = format;
			this.validityToExport = validityToExport;
			this.filenameFilter = filenameFilter;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.addChoosableFileFilter(this.filenameFilter);
			int returnVal = chooser.showSaveDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				String filename = chooser.getSelectedFile().getAbsolutePath();
				// Add RDF extension if not written by user
				if(!filename.toLowerCase().endsWith("rdf")) //$NON-NLS-1$
					filename = filename + ".rdf"; //$NON-NLS-1$
				System.out.println("You chose to save into file: " + filename); //$NON-NLS-1$
				try {
					AlignmentGUI.this.alignmentControler.saveAlign(filename, validityToExport, format);
				} catch (IOException e1) {
					System.err.println("Ecriture du fichier impossible"); //$NON-NLS-1$
					e1.printStackTrace();
				}
			}
		}
	}
	
	class ImportAlignmentActionListener implements ActionListener {
		private AlignmentFormat format;
		private FileNameExtensionFilter filenameFilter;

		public ImportAlignmentActionListener(AlignmentFormat format, FileNameExtensionFilter filenameFilter) {
			super();
			this.format = format;
			this.filenameFilter = filenameFilter;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.addChoosableFileFilter(this.filenameFilter);
			int returnVal = chooser.showOpenDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				final File selectedFile = chooser.getSelectedFile();
				String filename = selectedFile.getAbsolutePath();
				System.out.println("You chose to open this file: " + filename); //$NON-NLS-1$
				loadAlignmentFromFileReference(selectedFile, this.format);
			}
		}
	}
	
	public void loadAlignmentFromFileReference(File selectedFile, AlignmentFormat format) {
		boolean ok = alignmentControler.openAlign(selectedFile, format);
		if(ok) {
			System.out.println("Load alignment finished successfully"); //$NON-NLS-1$
			refreshGUIFromModel();
		} else {
			System.out.println("Load alignment error..."); //$NON-NLS-1$
		}
	}

	private void launchFinderDialog(int treeNumber) {
		ConceptFinderDialog select = null;
		if(treeNumber == 1) {
			select = new ConceptFinderDialog(alignmentControler.getTreeModel1(), this, true);
		}
		else {
			select = new ConceptFinderDialog(alignmentControler.getTreeModel2(), this, true);
		}
		select.setVisible(true); // Blocking call
		// Result:
		DefaultMutableTreeNode result = select.getReturnValue();
		if(result == null) return;
		if(treeNumber == 1) {
			// Selection de ce noeud
			TreePath pathToRoot1 = new TreePath(alignmentControler.getTreeModel1().getPathToRoot(result));
			treeFrom1.scrollPathToVisible(pathToRoot1);
			treeFrom1.setSelectionPath(pathToRoot1);
		}
		else {
			// Selection de ce noeud
			TreePath pathToRoot2 = new TreePath(alignmentControler.getTreeModel2().getPathToRoot(result));
			treeFrom2.scrollPathToVisible(pathToRoot2);
			treeFrom2.setSelectionPath(pathToRoot2);
		}

	}

	private void locateConceptsInTree() {
		if(centerTable.getSelectedRowCount() == 0) {
			JOptionPane.showMessageDialog(null,
					Messages.getString("FindConceptError1"), //$NON-NLS-1$
					Messages.getString("FindConceptError1Short"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		int index = centerTable.getSelectedRow();
		Mapping map = tableModel.getMappingAt(centerTable.convertRowIndexToModel(index));
		Object cpt1 = map.getFirstConcept();
		Object cpt2 = map.getSecondConcept();
		final MyTreeModel treeModel1 = alignmentControler.getTreeModel1();
		final MyTreeModel treeModel2 = alignmentControler.getTreeModel2();
		DefaultMutableTreeNode nodeFrom1 = treeModel1.getFirstNodeFromConcept(cpt1);
		DefaultMutableTreeNode nodeFrom2 = treeModel2.getFirstNodeFromConcept(cpt2);

		TreePath pathToRoot1 = new TreePath(treeModel1.getPathToRoot(nodeFrom1));
		TreePath pathToRoot2 = new TreePath(treeModel2.getPathToRoot(nodeFrom2));

		treeFrom1.scrollPathToVisible(pathToRoot1);
		treeFrom2.scrollPathToVisible(pathToRoot2);
		treeFrom1.setSelectionPath(pathToRoot1);
		treeFrom2.setSelectionPath(pathToRoot2);
	}

	private void loadOntologyWithFileChooser(final OntologyType ontoType) {
		JFileChooser chooser = (lastDirectory != null)?new JFileChooser(lastDirectory):new JFileChooser();
		FileNameExtensionFilter filter = ontoType.getOntoFormat().getFilter(); 
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = chooser.getSelectedFile();
			loadOntologyFromFileReference(ontoType, selectedFile.toURI());
		}
	}

	public void loadOntologyFromFileReference(final OntologyType ontoType, final URI fileReference) {
		final File selectedFile = new File(fileReference);
		
		// Copy to cache the last directory to use it in future FileChooser
		lastDirectory = selectedFile.getParentFile();
		saveDynamicOnaguiInfos(lastDirectory);
		configuration.setOntologyLastOpenDirectory(lastDirectory);
		final URI filename = selectedFile.toURI();
		System.out.println("Path to ontology: " + filename); //$NON-NLS-1$

		// the task in a new thread
		FutureTask<OntoContainer> task = new FutureTask<OntoContainer>(
				new Callable<OntoContainer>() {
					@Override
					public OntoContainer call() throws Exception {
						OntoContainer container = null;
						try {
							switch(ontoType.getOntoFormat()) {
							case OWL:
								container = GUIUtils.loadDOEOntologyWithGUI(AlignmentGUI.this, filename);
								break;
							case RDF:
								container = GUIUtils.loadRDFOntologyWithGUI(AlignmentGUI.this, filename);
								break;
							case SKOS:
								container = GUIUtils.loadSKOSOntologyWithGUI(AlignmentGUI.this, filename);
								break;
							default:
								break;								
							}
							System.out.println("Loading OK"); //$NON-NLS-1$
							if(!ontoType.isFirstOntology()) { // Reference ontology
								ontology2Type = ontoType;
								ontology2File = filename;
								refText2.setText(REF_PREFIX_2+container.getURI());
								alignmentControler.setContainer2(container); // Compute the model
								treeFrom2.setModel(alignmentControler.getTreeModel2());
								collapseRoot(treeFrom2);
								treeFrom2.repaint();
							}
							else {
								ontology1Type = ontoType;
								ontology1File = filename;
								refText1.setText(REF_PREFIX_1+container.getURI());
								alignmentControler.setContainer1(container); // Compute the model
								treeFrom1.setModel(alignmentControler.getTreeModel1());
								collapseRoot(treeFrom1);
								treeFrom1.repaint();
							}
						} catch (OutOfMemoryError e2) {
							String message = Messages.getString("MemoryError"); //$NON-NLS-1$
							System.err.println(message);
							JOptionPane.showMessageDialog(AlignmentGUI.this, message, Messages.getString("MemoryErrorShort"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
						}
						finally {
							progressBar.setVisible(false); // Unlock application
						}
						return container;
					}
				});
		ExecutorService executors = Executors.newCachedThreadPool();
		executors.execute(task);
		if(!task.isDone()) {// Be careful of very quick task!
			progressBar.setVisible(true);
		}
		try {
			// FIXME plus besoin du retour dans cette version
			OntoContainer container = task.get();

		} catch (InterruptedException e1) {
			// Impossible!
			e1.printStackTrace();
		} catch (ExecutionException e2) {
			// Impossible!
			e2.printStackTrace();
		}

		refreshMenuActivation();
		System.out.println("Loading of ontology finished..."); //$NON-NLS-1$
	}

	
	private void reloadOntology(final OntologyType ontoType) {

		// the task in a new thread
		FutureTask<OntoContainer> task = new FutureTask<OntoContainer>(
				new Callable<OntoContainer>() {
					@Override
					public OntoContainer call() throws Exception {
						OntoContainer container = null;
						try {
							if(!ontoType.isFirstOntology()) { // Reference ontology
								
								switch(ontoType.getOntoFormat()) {
								case OWL:
									container = GUIUtils.loadDOEOntologyWithGUI(AlignmentGUI.this, ontology2File);
									break;
								case RDF:
									container = GUIUtils.loadRDFOntologyWithGUI(AlignmentGUI.this, ontology2File);
									break;
								case SKOS:
									container = GUIUtils.loadSKOSOntologyWithGUI(AlignmentGUI.this, ontology2File);
									break;
								default:
									break;								
								}
								
								refText2.setText(REF_PREFIX_2+container.getURI());
								alignmentControler.setReloadContainer2(container); // Compute the model
								treeFrom2.setModel(alignmentControler.getTreeModel2());
								collapseRoot(treeFrom2);
								treeFrom2.repaint();
							}
							else {
								
								switch(ontoType.getOntoFormat()) {
								case OWL:
									container = GUIUtils.loadDOEOntologyWithGUI(AlignmentGUI.this, ontology1File);
									break;
								case RDF:
									container = GUIUtils.loadRDFOntologyWithGUI(AlignmentGUI.this, ontology1File);
									break;
								case SKOS:
									container = GUIUtils.loadSKOSOntologyWithGUI(AlignmentGUI.this, ontology1File);
									break;
								default:
									break;								
								}
								
								refText1.setText(REF_PREFIX_1+container.getURI());
								alignmentControler.setReloadContainer1(container); // Compute the model
								treeFrom1.setModel(alignmentControler.getTreeModel1());
								collapseRoot(treeFrom1);
								treeFrom1.repaint();
							}
						} catch (OutOfMemoryError e2) {
							String message = Messages.getString("MemoryError"); //$NON-NLS-1$
							System.err.println(message);
							JOptionPane.showMessageDialog(AlignmentGUI.this, message, Messages.getString("MemoryErrorShort"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
						}
						finally {
							progressBar.setVisible(false); // Unlock application
						}
						return container;
					}
				});
		ExecutorService executors = Executors.newCachedThreadPool();
		executors.execute(task);
		if(!task.isDone()) {// Be careful of very quick task!
			progressBar.setVisible(true);
		}
		try {
			// FIXME plus besoin du retour dans cette version
			OntoContainer container = task.get();

		} catch (InterruptedException e1) {
			// Impossible!
			e1.printStackTrace();
		} catch (ExecutionException e2) {
			// Impossible!
			e2.printStackTrace();
		}

		refreshMenuActivation();
		System.out.println("Loading of ontology finished..."); //$NON-NLS-1$
	}
	
	private void collapseRoot(JTree tree) {
		TreePath rootTreePath = new TreePath(((DefaultMutableTreeNode)tree.getModel().getRoot()).getPath());
		tree.collapsePath(rootTreePath);
	}

	private boolean isOntologyLoaded(int number) {
		switch(number) {
		case 1:
			return ontology1File != null && ontology1Type !=null;
		case 2:
			return ontology2File != null && ontology2Type !=null;
		}
		throw new IllegalArgumentException("Arg must be 1 or 2, not: "+number); //$NON-NLS-1$
	}

	public void refreshMenuActivation() {
		for(JMenuItem menu : syntacticAlignItem) {
			menu.setEnabled(isOntologyLoaded(1) && isOntologyLoaded(2));
		}
		importRDF.setEnabled(isOntologyLoaded(1) && isOntologyLoaded(2));
		importSKOS.setEnabled(isOntologyLoaded(1) && isOntologyLoaded(2));
		importCSV.setEnabled(isOntologyLoaded(1) && isOntologyLoaded(2));
		exportRDFAll.setEnabled(isOntologyLoaded(1) && isOntologyLoaded(2));
		exportRDFInvalid.setEnabled(isOntologyLoaded(1) && isOntologyLoaded(2));
		exportRDFToConfirm.setEnabled(isOntologyLoaded(1) && isOntologyLoaded(2));
		exportRDFValid.setEnabled(isOntologyLoaded(1) && isOntologyLoaded(2));
		exportCSV.setEnabled(isOntologyLoaded(1) && isOntologyLoaded(2));
		exportSkos.setEnabled(isOntologyLoaded(1) && isOntologyLoaded(2));
		statAlignItem.setEnabled(isOntologyLoaded(1) || isOntologyLoaded(2));
		searchIn1.setEnabled(isOntologyLoaded(1));
		searchIn2.setEnabled(isOntologyLoaded(2));
		reload1.setEnabled(isOntologyLoaded(1));
		reload2.setEnabled(isOntologyLoaded(2));

		for(int number=1; number<=2; number++) {
			final int intern_number = number;
			final JMenuItem langMenu = langMenus.get(number);
			langMenu.removeAll();
			if(isOntologyLoaded(number)) {
				ButtonGroup grp = new ButtonGroup();
				SortedSet<String> languages = alignmentControler.getLanguagesUsedInOnto(number);
				for(String lang : languages) {
					JRadioButtonMenuItem lg = new JRadioButtonMenuItem(lang);
					grp.add(lg);
					lg.addActionListener(new ActionListener() {					
						@Override
						public void actionPerformed(ActionEvent e) {
							System.out.println("Set lang to: "+e.getActionCommand()); //$NON-NLS-1$
							AlignmentGUI.this.alignmentControler.setCurrentLanguage(intern_number, e.getActionCommand());
							AlignmentGUI.this.refreshGUIFromModel();
						}
					});
					langMenu.add(lg);
				}
				JRadioButtonMenuItem noLangTagItem = new JRadioButtonMenuItem(Messages.getString("NoLangTag"));
				grp.add(noLangTagItem);
				noLangTagItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						AlignmentGUI.this.alignmentControler.setCurrentLanguage(intern_number, ""); //$NON-NLS-1$
					}
				});
				langMenu.add(noLangTagItem);
				grp.getElements().nextElement().setSelected(true);
			}
			else {
				langMenu.add(new JMenuItem("None")); //$NON-NLS-1$
			}
		}
	}
	
	private void refreshGenericAnnotationPanel() {
		// Get the selected nodes
		DefaultMutableTreeNode node1 = (DefaultMutableTreeNode)treeFrom1.getLastSelectedPathComponent();
		DefaultMutableTreeNode node2 = (DefaultMutableTreeNode)treeFrom2.getLastSelectedPathComponent();

		if(node1 != null) {
			Object userObject = node1.getUserObject();
			if(userObject instanceof TreeNodeOntologyObject) {
				TreeNodeOntologyObject nodeOntologyObject = (TreeNodeOntologyObject)userObject;
				
				String predicateUri = (String) annotationChoice.getSelectedItem();
				Set<String> labels = alignmentControler.getLabels(nodeOntologyObject, predicateUri, 1);
				annot1.setLabelsToPanel(labels);
			}			
		}
		else {
			annot1.setLabelsToPanel(Sets.newHashSet());
		}

		if(node2 != null) {
			Object userObject = node2.getUserObject();
			if(userObject instanceof TreeNodeOntologyObject) {
				TreeNodeOntologyObject nodeOntologyObject = (TreeNodeOntologyObject)userObject;
				
				String predicateUri = (String) annotationChoice.getSelectedItem();
				Set<String> labels = alignmentControler.getLabels(nodeOntologyObject, predicateUri, 2);
				annot2.setLabelsToPanel(labels);
			}			
		}
		else {
			annot2.setLabelsToPanel(Sets.newHashSet());
		}
	}

	public void refreshGUIFromModel() {
		// Avant de faire quoique que ce soit, memoriser la selection courante au centre:
		Set<Mapping> currentMaps = new HashSet<>();
		if(centerTable.getSelectedRowCount() != 0 && tableModel.getRowCount() != 0) {
			for(int index: centerTable.getSelectedRows())
				currentMaps.add(tableModel.getMappingAt(centerTable.convertRowIndexToModel(index)));
		}

		// Priorité: la synchro du modele de JTable et du modele alignement
		// FIXME Par defaut, on change à chaque fois, à faire plus proprement
		tableModel.setMapping(alignmentControler.getAllMapping());

		// Get the selected nodes
		DefaultMutableTreeNode node1 = (DefaultMutableTreeNode)treeFrom1.getLastSelectedPathComponent();
		DefaultMutableTreeNode node2 = (DefaultMutableTreeNode)treeFrom2.getLastSelectedPathComponent();

		String currentPredicateUri = (String) annotationChoice.getSelectedItem();
		Set<String> annotationUris = new HashSet<String>();
		
		// Get the index in the table
		if(node1 != null) {
			Object userObject = node1.getUserObject();
			if(userObject instanceof TreeNodeOntologyObject) {
				TreeNodeOntologyObject nodeOntologyObject = (TreeNodeOntologyObject)userObject;
				// Gestion des selection impossible
				boolean impossible1 = alignmentControler.isImpossibleToAlign1(nodeOntologyObject);
				notFoundFrom1.setSelected(impossible1);
				// Les statistiques en bas de chaque arbre
				// FIXME
				// state1Text.setText(alignmentControler.computeStatFor(node1, 1).toString());
				// Gestion du lexique
				String fragUri = alignmentControler.getURIOfConcept(nodeOntologyObject, 1).getFragment();
				Set<String> prefLabels = alignmentControler.getPrefLabelsOfConcept(nodeOntologyObject, 1);
				Set<String> altLabels = alignmentControler.getAltLabelsOfConcept(nodeOntologyObject, 1);
				lexic1.setFragURI(fragUri);
				lexic1.setPrefLabel(prefLabels);
				lexic1.setAltLabel(altLabels);
				
				annotationUris.addAll(alignmentControler.getAnnotationsUri(nodeOntologyObject, 1));
			}
		}
		else {
			lexic1.setFragURI(null);
			lexic1.setPrefLabel(null);
			lexic1.setAltLabel(null);
		}
		if(node2 != null) {
			Object userObject = node2.getUserObject();
			if(userObject instanceof TreeNodeOntologyObject) {
				TreeNodeOntologyObject nodeOntologyObject = (TreeNodeOntologyObject)userObject;
				// Gestion des selection impossible
				boolean impossible2 = alignmentControler.isImpossibleToAlign2(nodeOntologyObject);
				notFoundFrom2.setSelected(impossible2);
				// Les statistiques en bas de chaque arbre
				// FIXME
				// state2Text.setText(alignmentControler.computeStatFor(node2, 2).toString());
				// Gestion du lexique
				String fragUri = alignmentControler.getURIOfConcept(nodeOntologyObject, 2).getFragment();
				Set<String> prefLabels = alignmentControler.getPrefLabelsOfConcept(nodeOntologyObject, 2);
				Set<String> altLabels = alignmentControler.getAltLabelsOfConcept(nodeOntologyObject, 2);
				lexic2.setFragURI(fragUri);
				lexic2.setPrefLabel(prefLabels);
				lexic2.setAltLabel(altLabels);

				annotationUris.addAll(alignmentControler.getAnnotationsUri(nodeOntologyObject, 1));
			}
		}
		else {
			lexic2.setFragURI(null);
			lexic2.setPrefLabel(null);
			lexic2.setAltLabel(null);
		}

		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(
				annotationUris.toArray(new String[annotationUris.size()]));
		annotationChoice.setModel(model);
		/* If same predicate is still here, reload it */
		if(annotationUris.contains(currentPredicateUri)) {
			annotationChoice.getModel().setSelectedItem(currentPredicateUri);
			refreshGenericAnnotationPanel();
		}
		/* Force one to have something to show */
		else if(!annotationUris.isEmpty()) {
			annotationChoice.getModel().setSelectedItem(annotationUris.stream().findFirst().get());
			refreshGenericAnnotationPanel();
		}
		/* Empty */
		else {
			annot1.setLabelsToPanel(null);
			annot2.setLabelsToPanel(null);
		}
		
		/* La table maintenant */
		// Concept 1 à retenir?
		Object concept1 = null;
		if(filterOnto1Enabled && node1 != null) {
			Object userObject = node1.getUserObject();
			if(userObject instanceof TreeNodeOntologyObject) {
				TreeNodeOntologyObject nodeOntologyObject = (TreeNodeOntologyObject)userObject;
				concept1 = nodeOntologyObject.getConcept();
			}

		}
		// Concept 2 à retenir?
		Object concept2 = null;
		if(filterOnto2Enabled && node2 != null) {
			Object userObject = node2.getUserObject();
			if(userObject instanceof TreeNodeOntologyObject) {
				TreeNodeOntologyObject nodeOntologyObject = (TreeNodeOntologyObject)userObject;
				concept2 = nodeOntologyObject.getConcept();
			}
		}
		// Si la table n'est pas vide, trier et activer l'interaction
		if(tableModel.getRowCount() != 0) {
			centerTable.setEnabled(true);
			sorter.setRowFilter(new MappingRowFilter(concept1, concept2));
		}
		// Sinon, couper toute interaction possible
		else {
			centerTable.setEnabled(false);
		}
		centerTable.repaint();
		// Mise à jour du champs commentaires
		if(!currentMaps.isEmpty()) {
			for(Mapping currentMap: currentMaps) {
				Integer index = tableModel.getIndexOfMapping(currentMap);
				if(index != null) {
					int realIndex = centerTable.convertRowIndexToView(index);
					centerTable.addRowSelectionInterval(realIndex, realIndex);
				}
			}
		}
		else {
			snoComment.setText(""); //$NON-NLS-1$
		}

		// Mise à jour des couleurs des abres
		treeFrom1.repaint();
		treeFrom2.repaint();
		// Les commentaires on verra plus tard...
	}

	/** Required by TreeSelectionListener interface. */
	public void valueChanged(TreeSelectionEvent e) {
		refreshGUIFromModel();
	}

	public class AlignmentAlgorithmMenuListener<ONTORES1, ONTORES2> implements ActionListener {

		private AbstractAlignmentMethod<ONTORES1, ONTORES2> method = null;

		public AlignmentAlgorithmMenuListener(AbstractAlignmentMethod<ONTORES1, ONTORES2> method) {
			this.method = method;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// FIXME plus d'ecraesement necessaire maintenant..
			//			final ERASE_TYPE eraseType = GUIUtils.chooseEraseType(AlignmentGUI.this);
			//			if(eraseType == null) return; // User cancel the dialog

			// Choisir la racine
			RootChooserDialog rootChooserDialog = new RootChooserDialog(AlignmentGUI.this);
			rootChooserDialog.pack();
			rootChooserDialog.setLocationRelativeTo(AlignmentGUI.this);
			rootChooserDialog.setVisible(true);
			if(rootChooserDialog.getUserAnswer() == JOptionPane.CANCEL_OPTION) return;
			// Assume que c'est JOptionPane.OK_OPTION
			final boolean useRoot1 = rootChooserDialog.isUseRootFor1();
			final boolean useRoot2 = rootChooserDialog.isUseRootFor2();
			// Si je suis une methode label, j'ai des parametres générique à donner
			if(method instanceof LabelAlignmentMethod) {
				LabelAlignmentMethod labelMethod = (LabelAlignmentMethod)method;

				 labelParameterDialog = new LabelMethodParameterDialog(
						AlignmentGUI.this,
						labelMethod.getThreshold(),
						alignmentControler.getLanguagesUsedInOnto(1),
						alignmentControler.getLanguagesUsedInOnto(2));
				labelParameterDialog.setVisible(true); // Blocking call
				double newThreshold = labelParameterDialog.getSelectedValue();
				if(newThreshold == -1.0) {
					return;
				}
				labelMethod.setThreshold(newThreshold);
				labelMethod.setLangsFrom1(labelParameterDialog.getSelectedLangFor1());
				labelMethod.setLangsFrom2(labelParameterDialog.getSelectedLangFor2());
	
			}
			
			// Map<OWLEntity, Set<Mapping<OWLEntity, SKOSConcept>>> return type
			FutureTask<Alignment<ONTORES1, ONTORES2>> task = new FutureTask<Alignment<ONTORES1, ONTORES2>>(
					new Callable<Alignment<ONTORES1, ONTORES2>>() {
						public Alignment<ONTORES1, ONTORES2> call() throws Exception {
							PropertyChangeListener listener = new PropertyChangeListener() {
								public void propertyChange(PropertyChangeEvent evt) {
									Double value = (Double)evt.getNewValue();
									int percent = (int)(value.doubleValue()*100);
									// Faire quelque chose...
									progressBar.setProgress(percent);
								}
							};
							if(useRoot1 && useRoot2) {
								alignmentControler.computeAndAddMapping(method, listener);
							}
							else {
								// En fonction de la selection
								DefaultMutableTreeNode selected1 = (DefaultMutableTreeNode)treeFrom1.getLastSelectedPathComponent();
								if(selected1 == null) {
									System.err.println("Pas de noeud sélectionné dans l'arbre 1, j'envoie la racine pour l'alignement"); //$NON-NLS-1$
									selected1 = (DefaultMutableTreeNode)treeFrom1.getModel().getRoot();
								}
								DefaultMutableTreeNode selected2 = (DefaultMutableTreeNode)treeFrom2.getLastSelectedPathComponent();
								if(selected2 == null) {
									System.err.println("Pas de noeud sélectionné dans l'arbre 2, j'envoie la racine pour l'alignement"); //$NON-NLS-1$
									selected2 = (DefaultMutableTreeNode)treeFrom2.getModel().getRoot();
								}
								try {
									System.out.println("Dates : "+labelParameterDialog.getDate1AsDate()+" and "+labelParameterDialog.getDate2AsDate());
									alignmentControler.computeAndAddMapping(method, listener, selected1, selected2,labelParameterDialog.getDate1AsDate(),labelParameterDialog.getDate2AsDate());
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							progressBar.setVisible(false);
							// FIXME le retour n'est plus utilisé maintenant
							return null;
						}
					});
			ExecutorService executors = Executors.newCachedThreadPool();
			executors.execute(task);
			if(!task.isDone()) {// Be careful of very quick task!
				progressBar.setVisible(true);
			}
			try {
				task.get();
			} catch (InterruptedException | ExecutionException e1) {
				e1.printStackTrace();
			}
			refreshGUIFromModel();
		}
	}

	public class MyTableSelectionListerner implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			// Verifier que la table est en etat d'afficher quelque chose...
			if(centerTable.getSelectedRowCount() == 1 && !e.getValueIsAdjusting()) {
				// Plus sur que de recuperer l'index de l'evenement...
				int index = centerTable.getSelectedRow();
				Mapping selectedMapping = tableModel.getMappingAt(centerTable.convertRowIndexToModel(index));
				snoComment.setText(selectedMapping.getComment());
			}
			else {
				// On vas considérer que c'est un evenement de "deselection"
				snoComment.setText(""); //$NON-NLS-1$
			}
		}	
	}

	public void createCenterPopupMenu() {
		//Create the popup menu.
		JPopupMenu popup = new JPopupMenu();
		JMenu menuvalidity = new JMenu(Messages.getString("PopupMenuLabel1")); //$NON-NLS-1$
		popup.add(menuvalidity);
		JMenuItem valid = new JMenuItem(Messages.getString("ValidChoice")); //$NON-NLS-1$
		menuvalidity.add(valid);
		JMenuItem to_confirm = new JMenuItem(Messages.getString("ToConfirmChoice")); //$NON-NLS-1$
		menuvalidity.add(to_confirm);
		JMenuItem invalid = new JMenuItem(Messages.getString("InvalidChoice")); //$NON-NLS-1$
		menuvalidity.add(invalid);
		
		JMenu menutype = new JMenu(Messages.getString("PopupMenuLabel2")); //$NON-NLS-1$
		popup.add(menutype);
		JMenuItem exactmatch = new JMenuItem(Messages.getString("ExactMatch")); //$NON-NLS-1$
		menutype.add(exactmatch);
		JMenuItem closematch = new JMenuItem(Messages.getString("CloseMatch")); //$NON-NLS-1$
		menutype.add(closematch);
		JMenuItem relatedMatch = new JMenuItem(Messages.getString("RelatedMatch")); //$NON-NLS-1$
		menutype.add(relatedMatch);
		JMenuItem broaderMatch = new JMenuItem(Messages.getString("BroaderMatch")); //$NON-NLS-1$
		menutype.add(broaderMatch);
		JMenuItem narrowMatch = new JMenuItem(Messages.getString("NarrowMatch")); //$NON-NLS-1$
		menutype.add(narrowMatch);
		JMenuItem disjoint = new JMenuItem(Messages.getString("Disjoint")); //$NON-NLS-1$
		menutype.add(disjoint);
		JMenuItem undefined = new JMenuItem(Messages.getString("Undefined")); //$NON-NLS-1$
		menutype.add(undefined);
		
		valid.addActionListener(new SwitchValidityActionListener(VALIDITY.VALID));
		to_confirm.addActionListener(new SwitchValidityActionListener(VALIDITY.TO_CONFIRM));
		invalid.addActionListener(new SwitchValidityActionListener(VALIDITY.INVALID));
		
		exactmatch.addActionListener(new SwitchTypeActionListener(MAPPING_TYPE.EQUIV));
		closematch.addActionListener(new SwitchTypeActionListener(MAPPING_TYPE.OVERLAP));
		relatedMatch.addActionListener(new SwitchTypeActionListener(MAPPING_TYPE.RELATED));
		disjoint.addActionListener(new SwitchTypeActionListener(MAPPING_TYPE.DISJOINT));
		narrowMatch.addActionListener(new SwitchTypeActionListener(MAPPING_TYPE.SUBSUMES));
		broaderMatch.addActionListener(new SwitchTypeActionListener(MAPPING_TYPE.SUBSUMEDBY));
		undefined.addActionListener(new SwitchTypeActionListener(MAPPING_TYPE.UNDEFINED));

		//Add listener to the text area so the popup menu can come up.
		MouseListener popupListener = new PopupListener(popup);
		centerTable.addMouseListener(popupListener);
	}

	
	class SwitchTypeActionListener implements ActionListener {
		private Mapping.MAPPING_TYPE newType;

		public SwitchTypeActionListener(MAPPING_TYPE newType) {
			super();
			this.newType = newType;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			for(int index : centerTable.getSelectedRows()) {
				Mapping<?, ?> map = tableModel.getMappingAt(centerTable.convertRowIndexToModel(index));
				alignmentControler.updateMappingType(map, newType);
			}
			
			// Refresh GUI
			AlignmentGUI.this.refreshGUIFromModel();
		}
	}
	
	class SwitchValidityActionListener implements ActionListener {
		private Mapping.VALIDITY newValidity;

		public SwitchValidityActionListener(VALIDITY newValidity) {
			super();
			this.newValidity = newValidity;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			for(int index : centerTable.getSelectedRows()) {
				Mapping<?, ?> map = tableModel.getMappingAt(centerTable.convertRowIndexToModel(index));
				alignmentControler.updateMappingValidity(map, newValidity);
			}
			
			// Refresh GUI
			AlignmentGUI.this.refreshGUIFromModel();
		}
	}

	class PopupListener extends MouseAdapter {
		JPopupMenu popup;

		PopupListener(JPopupMenu popupMenu) {
			popup = popupMenu;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(),
						e.getX(), e.getY());
			}
		}
	}

	public AlignmentControler getAlignmentControler() {
		return alignmentControler;
	}

	public static void main(String[] args) {	
		System.out.println("Starting OnaGUI..."); //$NON-NLS-1$
		//Schedule a job for the event dispatch thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new AlignmentGUI();
			}
		});
	}
}

