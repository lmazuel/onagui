/**
 * 
 */
package fr.onagui.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePicker;

import fr.onagui.alignment.method.LabelAlignmentMethod;




/**
 * @author Laurent Mazuel
 *
 */
public class LabelMethodParameterDialog extends JDialog implements ChangeListener, ActionListener {

	/** To make Java happy */
	private static final long serialVersionUID = 3730765536056426003L;

	private static final int NUMBER_OF_LINES_IN_LIST = 6;

	private int DIALOG_WIDTH = 1100;
	private int DIALOG_HEIGHT = 800;

	private Double selectedValue = null;
	private String date1=null;
	private String date2=null; 

	private JSlider slider = null;
	private JTextField textField = null;
	private DatePicker dateField1 = null;
	private DatePicker dateField2 = null;
	protected String dateFields ;
	private JList<String> langList1 = null;
	private JList<String> langList2 = null;
	private JButton okButton = null;
	private JButton cancelButton = null;


	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	public LabelMethodParameterDialog(
			JFrame view,
			double initialValue,
			Set<String> langsFrom1,
			Set<String> langsFrom2) {
		super(view, Messages.getString("LabelMethodParameterDialogTitle"), true); //$NON-NLS-1$
		setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);

		selectedValue = initialValue;

		slider = new JSlider(0, 100, (int)(initialValue*100));
		slider.addChangeListener(this);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		labels.put(0, new JLabel("0.0")); //$NON-NLS-1$
		labels.put(100, new JLabel("1.0")); //$NON-NLS-1$
		slider.setLabelTable(labels);
		slider.setPaintLabels(true);

		textField = new JTextField(String.valueOf(selectedValue), 5);

		List<String> localLangsFrom1 = new ArrayList<String>(langsFrom1);
		localLangsFrom1.add(LabelAlignmentMethod.NO_TAG);
		localLangsFrom1.add(LabelAlignmentMethod.FRAG_URI);
		langList1 = new JList<>(localLangsFrom1.toArray(new String[0]));
		langList1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//		langList1.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		langList1.setVisibleRowCount(NUMBER_OF_LINES_IN_LIST);
		langList1.setSelectedValue(localLangsFrom1.get(0), true);
		JPanel langPanel1 = new JPanel(new BorderLayout());
		langPanel1.add(new JLabel(Messages.getString("LabelMethodParameterDialogLang1")), BorderLayout.NORTH); //$NON-NLS-1$
		JScrollPane scroll1 = new JScrollPane(langList1);
		langPanel1.add(scroll1, BorderLayout.CENTER);

		List<String> localLangsFrom2 = new ArrayList<String>(langsFrom2);
		localLangsFrom2.add(LabelAlignmentMethod.NO_TAG);
		localLangsFrom2.add(LabelAlignmentMethod.FRAG_URI);
		langList2 = new JList<>(localLangsFrom2.toArray(new String[0]));
		langList2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//		langList2.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		langList2.setVisibleRowCount(NUMBER_OF_LINES_IN_LIST);
		langList2.setSelectedValue(localLangsFrom2.get(0), true);
		JPanel langPanel2 = new JPanel(new BorderLayout());
		langPanel2.add(new JLabel(Messages.getString("LabelMethodParameterDialogLang2")), BorderLayout.NORTH); //$NON-NLS-1$
		JScrollPane scroll2 = new JScrollPane(langList2);
		langPanel2.add(scroll2, BorderLayout.CENTER);

		okButton = new JButton(Messages.getString("LabelMethodParameterDialogOkButton")); //$NON-NLS-1$
		okButton.addActionListener(this);

		cancelButton = new JButton(Messages.getString("LabelMethodParameterDialogCancelButton")); //$NON-NLS-1$
		cancelButton.addActionListener(this);

		// Position:
		JPanel textFieldPanel = new JPanel(new BorderLayout());
		textFieldPanel.add(new JLabel(Messages.getString("LabelMethodParameterDialogThresholdLabel")), BorderLayout.WEST); //$NON-NLS-1$
		textFieldPanel.add(textField, BorderLayout.CENTER);

		//set calendar for the two concepts

		JPanel dateFieldPanel1 = new JPanel(new BorderLayout());
		dateFieldPanel1.add(new JLabel(Messages.getString("LabelDate1ParameterDialog")), BorderLayout.NORTH);
		//	calendar onto 1
		DatePickerSettings dateSettings1 = new DatePickerSettings();
		dateSettings1.setFormatForDatesCommonEra("yyyy-MM-dd");
		dateSettings1.setFirstDayOfWeek(DayOfWeek.MONDAY);
		this.dateField1 = new DatePicker(dateSettings1);
		dateFieldPanel1.add(this.dateField1,BorderLayout.CENTER);

		JPanel dateFieldPanel2 = new JPanel(new BorderLayout());
		dateFieldPanel2.add(new JLabel(Messages.getString("LabelDate2ParameterDialog")), BorderLayout.NORTH);
		DatePickerSettings dateSettings2 = new DatePickerSettings();
		dateSettings2.setFormatForDatesCommonEra("yyyy-MM-dd");
		dateSettings2.setFirstDayOfWeek(DayOfWeek.MONDAY);
		this.dateField2 = new DatePicker(dateSettings2);
		dateFieldPanel2.add(this.dateField2, BorderLayout.CENTER);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(textFieldPanel, c);
		this.getContentPane().add(textFieldPanel);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(slider, c);
		this.getContentPane().add(slider);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.VERTICAL;
		gridbag.setConstraints(langPanel1, c);
		this.getContentPane().add(langPanel1);

		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.VERTICAL;
		gridbag.setConstraints(langPanel2, c);
		this.getContentPane().add(langPanel2);

		//		c.gridx = 0;
		//		c.gridy = 3;
		//		c.gridwidth = 2;
		//		c.weightx = 0.0;
		//		c.weighty = 0.0;
		//		c.insets = new Insets(10, 10, 10, 10);
		//		c.anchor = GridBagConstraints.WEST;
		//		c.fill = GridBagConstraints.NONE;
		//		gridbag.setConstraints(label1, c);
		//		this.getContentPane().add(label1);

		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.VERTICAL;
		gridbag.setConstraints(dateFieldPanel1, c);
		this.getContentPane().add(dateFieldPanel1);


		//		c.gridx = 0;
		//		c.gridy = 5;
		//		c.gridwidth = 2;
		//		c.weightx = 0.0;
		//		c.weighty = 0.0;
		//		c.insets = new Insets(10, 10, 10, 10);
		//		c.anchor = GridBagConstraints.WEST;
		//		c.fill = GridBagConstraints.NONE;
		//		gridbag.setConstraints(label2, c);
		//		this.getContentPane().add(label2);

		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.VERTICAL;
		gridbag.setConstraints(dateFieldPanel2, c);
		this.getContentPane().add(dateFieldPanel2);

		c.gridx = 0;
		c.gridy = 7;
		c.gridwidth = 2;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.VERTICAL;
		gridbag.setConstraints(okButton, c);
		this.getContentPane().add(okButton);

		c.gridx = 1;
		c.gridy = 7;
		c.gridwidth =2;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(10, 10, 10, 10);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.VERTICAL;
		gridbag.setConstraints(cancelButton, c);
		this.getContentPane().add(cancelButton);

		pack();
		setLocationRelativeTo(null);
	}

	public Double getSelectedValue() {
		return selectedValue;
	}

	private SortedSet<String> getSelectedLangsFromJList(JList<String> list) {
		SortedSet<String> result = new TreeSet<String>();
		List<String> values = list.getSelectedValuesList();
		for(Object val : values) {
			String valS = (String)val;
			result.add(valS);
		}
		return result;
	}

	public SortedSet<String> getSelectedLangFor1() {
		return getSelectedLangsFromJList(langList1);
	}

	public SortedSet<String> getSelectedLangFor2() {
		return getSelectedLangsFromJList(langList2);
	}
	/**
	 * recupère la date sous forme de texte
	 * @return
	 */
	public String getDate1() {
		return date1;
	}

	public void setDate1(String date) {
		this.date1 = date;
	}
	/**
	 * recupère la date sous forme de texte
	 */
	public String getDate2() {
		return date2;
	}

	public void setDate2(String date) {
		this.date2 = date;
	}

	/**
	 * Renvoie la date 1 saisie sous forme de Date java, ou null si le parsing échoue.
	 * @return
	 */
	public Optional<Date> getDate1AsDate(){	
		return asDate(getDate1());
	}

	/**
	 * Renvoie la date 2 saisie sous forme de Date java, ou null si le parsing échoue.
	 * @return
	 */
	public Optional<Date> getDate2AsDate() {
		return asDate(getDate2());
	}

	private Optional<Date> asDate(String s) {
		try {
			Optional<Date> date=Optional.of(dateFormatter.parse(s));
			return date;
		} catch (Exception e) {
			System.err.print("Cannot parse date value - '"+e.getMessage()+"'");
			return Optional.empty();
		}
	}

	// For the slider
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
		int intThreshold = source.getValue();
		double floatThreshold= intThreshold/100.0;
		// Change in text field
		textField.setText(String.valueOf(floatThreshold));
		// Change return value
		selectedValue = floatThreshold;
	}
	// For the text field and the buttons
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == textField) {
			// Pression "entrée"
			String value = textField.getText();
			double floatValue = selectedValue;
			try {
				floatValue = Double.valueOf(value);
			}
			catch(NumberFormatException e1) {
				textField.setText(String.valueOf(selectedValue));
				return;
			}
			selectedValue = floatValue;
			slider.setValue((int)(selectedValue*100));
		}
		else if(e.getSource() == okButton) {
			String date1 = dateField1.getText();
			String date2 = dateField2.getText();
			setDate1(date1);
			setDate2(date2);
			setVisible(false);
		}
		else if(e.getSource() == cancelButton) {
			selectedValue = -1.0; // Cancel
			setVisible(false);
		}
		else {
			System.err.println("ERROR: inatendu! Event: "+e); //$NON-NLS-1$
		}
	}


	public static void main(String[] args) {
		double value = 0.9;
		List<String> list1 = Arrays.asList("fr","de"); //$NON-NLS-1$ //$NON-NLS-2$
		List<String> list2 = Arrays.asList("fr","de","hg","frt","ddg"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		LabelMethodParameterDialog testDialog = new LabelMethodParameterDialog(
				null,
				value,
				new TreeSet<String>(list1),
				new TreeSet<String>(list2));
		testDialog.setVisible(true);

		if(testDialog.getSelectedValue() >= 0) {
			System.out.println(testDialog.getSelectedLangFor1());
			System.out.println(testDialog.getSelectedLangFor2());
		}
		else {
			System.out.println("Cancel!!!!!!!"); //$NON-NLS-1$
		}
	}

}
