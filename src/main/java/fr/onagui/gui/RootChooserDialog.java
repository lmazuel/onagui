/**
 * 
 */
package fr.onagui.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * @author Laurent Mazuel
 */
public class RootChooserDialog extends JDialog implements PropertyChangeListener {

	/** To make Java happy... */
	private static final long serialVersionUID = -7337578716936382192L;

	private JOptionPane optionPane;

	private JRadioButton rootOnto1Button = null;
	private JRadioButton selectedOnto1Button = null;
	private JRadioButton rootOnto2Button = null;
	private JRadioButton selectedOnto2Button = null;

	private static final String btnString1 = Messages.getString("RootChooserDialogValidButton"); //$NON-NLS-1$
	private static final String btnString2 = Messages.getString("RootChooserDialogCancelButton"); //$NON-NLS-1$

	private boolean useRootFor1 = false;
	private boolean useRootFor2 = false;
	private int userAnswer = JOptionPane.CANCEL_OPTION;

	/**
	 * @return the useRootFor1
	 */
	public boolean isUseRootFor1() {
		return useRootFor1;
	}

	/**
	 * @return the useRootFor2
	 */
	public boolean isUseRootFor2() {
		return useRootFor2;
	}

	public int getUserAnswer() {
		return userAnswer;
	}

	public RootChooserDialog(JFrame view) {
		super(view, true);

		setTitle(Messages.getString("RootChooserDialogTitle")); //$NON-NLS-1$

		// Creation des boutons
		rootOnto1Button = new JRadioButton(Messages.getString("RootChooserDialogChoice1Root"), true); //$NON-NLS-1$
		selectedOnto1Button = new JRadioButton(Messages.getString("RootChooserDialogChoice1Concept")); //$NON-NLS-1$
		rootOnto2Button = new JRadioButton(Messages.getString("RootChooserDialogChoice2Root"), true); //$NON-NLS-1$
		selectedOnto2Button = new JRadioButton(Messages.getString("RootChooserDialogChoice2Concept")); //$NON-NLS-1$

		// Creation des groupes, pour la cohérence des selections
		ButtonGroup groupOnto1 = new ButtonGroup();
		groupOnto1.add(rootOnto1Button);
		groupOnto1.add(selectedOnto1Button);
		ButtonGroup groupOnto2 = new ButtonGroup();
		groupOnto2.add(rootOnto2Button);
		groupOnto2.add(selectedOnto2Button);

		// Creation du panneau 1
		JPanel panel1 = new JPanel(new BorderLayout());
		panel1.add(new JLabel(Messages.getString("RootChooserDialogOnto1Title")), BorderLayout.NORTH); //$NON-NLS-1$
		JPanel panel1Button = new JPanel(new GridLayout(2,1));
		panel1Button.add(rootOnto1Button);
		panel1Button.add(selectedOnto1Button);
		panel1.add(panel1Button, BorderLayout.CENTER);

		// Creation du panneau 2
		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(new JLabel(Messages.getString("RootChooserDialogOnto2Title")), BorderLayout.NORTH); //$NON-NLS-1$
		JPanel panel2Button = new JPanel(new GridLayout(2,1));
		panel2Button.add(rootOnto2Button);
		panel2Button.add(selectedOnto2Button);
		panel2.add(panel2Button, BorderLayout.CENTER);

		// Disposition de l'affichage
		JPanel centerPanel = new JPanel(new GridLayout(1,2));
		centerPanel.add(panel1);
		centerPanel.add(panel2);

		Object[] options = {btnString1, btnString2};

		//Create the JOptionPane.
		optionPane = new JOptionPane(centerPanel,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION,
				null,
				options,
				options[0]);

		//Make this dialog display it.
		setContentPane(optionPane);

		//Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);

		//Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();

		if (isVisible()
				&& (evt.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) ||
						JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			Object value = optionPane.getValue();

			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				//ignore reset
				return;
			}

			//Reset the JOptionPane's value.
			//If you don't do this, then if the user
			//presses the same button next time, no
			//property change event will be fired.
			optionPane.setValue(
					JOptionPane.UNINITIALIZED_VALUE);

			if (btnString1.equals(value)) {

				useRootFor1 = rootOnto1Button.isSelected();
				useRootFor2 = rootOnto2Button.isSelected();
				// TODO Actuellement, pas de verification des deux autres boutons.
				// Normalement, le ButtonGroup assure la cohérence. Test à faire quand même?
				userAnswer = JOptionPane.OK_OPTION;            	
			}
			else { //user closed dialog or clicked cancel
				userAnswer = JOptionPane.CANCEL_OPTION;
			}
			clearAndHide();
		}

	}

	/** This method clears the dialog and hides it. */
	public void clearAndHide() {
		setVisible(false);
	}


}
