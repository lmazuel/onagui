/**
 * 
 */
package fr.onagui.gui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

/**
 * @author Laurent Mazuel
 */
public class IndeterminateProgressDialog extends JDialog {

	/** To make Java happy... */
	private static final long serialVersionUID = 3808755726552286899L;
	
	private JProgressBar progressBar = null;

	public IndeterminateProgressDialog(JFrame frame) {
		super(frame, Messages.getString("IndeterminateProgressDialogLabel"), Dialog.ModalityType.APPLICATION_MODAL); //$NON-NLS-1$

		GridBagLayout bag = new GridBagLayout();
		setLayout(bag);
		
		// the progress bar
		progressBar = new JProgressBar(0, 100);
		progressBar.setIndeterminate(true);
		progressBar.setMinimumSize(new Dimension(160, 50));
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(10,10,10,10);
		c.fill = GridBagConstraints.BOTH;
		bag.setConstraints(progressBar, c);
		add(progressBar);
		
		setMinimumSize(new Dimension(200, 70));
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
	}
		
	public void setProgress(int n) {
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		progressBar.setValue(n);
	}
	
	public void becomesIndeterminate() {
		progressBar.setStringPainted(false);
		progressBar.setIndeterminate(true);
	}
	
	@Override
	public void setVisible(boolean b) {
		becomesIndeterminate();
		super.setVisible(b);
	}
}
