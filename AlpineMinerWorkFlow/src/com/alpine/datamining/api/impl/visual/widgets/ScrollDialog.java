package com.alpine.datamining.api.impl.visual.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ScrollDialog extends Dialog {

	private Text text;
	protected Object result;

	protected Shell shell;

	private String message;
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Create the dialog
	 * @param parent
	 * @param style
	 */
	public ScrollDialog(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the dialog
	 * @param parent
	 */
	public ScrollDialog(Shell parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}
	/**
	 * Create contents of the dialog
	 */
	protected void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setLayout(new FillLayout());
		shell.setSize(400, 300);
		shell.setLocation(300, 300);
		text = new Text(shell, SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL|SWT.READ_ONLY);
		text.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				shell.dispose();
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				
			}
		});
		text.setText(message);
		text.setFocus();
	}

	public static void openInformation(Shell shell,String message){
		ScrollDialog dialog = new ScrollDialog(shell);
		dialog.setMessage(message);
		dialog.open();
	}
}
