package ui;

import java.io.IOException;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;

public class UDPXIXXATWindow extends ApplicationWindow {

	private Button udpButton;
	private Button ixxatButton;

	/**
	 * Create the application window,
	 */
	public UDPXIXXATWindow() {
		super(null);
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		
		try {
			StreamingProperties.loadProperties();
		} catch (IOException e) {
			StreamingProperties.useUDP = true;
			MessageDialog.openWarning(getShell(), "Error reading properties file", e.getMessage());
			e.printStackTrace();
		}
		
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		Group grpStreaminType = new Group(container, SWT.NONE);
		grpStreaminType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpStreaminType.setText(" Streaming type ");
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.marginWidth = 10;
		grpStreaminType.setLayout(gridLayout);
		udpButton = new Button(grpStreaminType, SWT.RADIO);
		udpButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		udpButton.setText("UDP");
		udpButton.setSelection(StreamingProperties.useUDP);
		udpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				changeStreamingType();
			}
		});
		ixxatButton = new Button(grpStreaminType, SWT.RADIO);
		ixxatButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		ixxatButton.setText("IXXAT");
		ixxatButton.setSelection(StreamingProperties.useIXXAT);
		ixxatButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				changeStreamingType();
			}
		});
		
			
		Group groupDataType = new Group(container, SWT.NONE);
		groupDataType.setLayout(new GridLayout(2, true));
		groupDataType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		groupDataType.setText(" Streamed Data type ");
		
		Button useCodaButton = new Button(groupDataType, SWT.CHECK);
		useCodaButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		useCodaButton.setText("Use Codamotion");
		useCodaButton.setSelection(StreamingProperties.useCodamotion);
		useCodaButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.useCodamotion = useCodaButton.getSelection();
				StreamingProperties.saveProperties();
			}
		});

		Button useOptitrackButton = new Button(groupDataType, SWT.CHECK);
		useOptitrackButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		useOptitrackButton.setText("Use Optitrack");
		useOptitrackButton.setSelection(StreamingProperties.useOptitrack);
		useOptitrackButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.useOptitrack = useOptitrackButton.getSelection();
				StreamingProperties.saveProperties();
			}
		});
		
		Button useXSensButton = new Button(groupDataType, SWT.CHECK);
		useXSensButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		useXSensButton.setText("Use XSens");
		useXSensButton.setSelection(StreamingProperties.useXSens);
		useXSensButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.useXSens = useXSensButton.getSelection();
				StreamingProperties.saveProperties();
			}
		});
		
		Button useTimerButton = new Button(groupDataType, SWT.CHECK);
		useTimerButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		useTimerButton.setText("Use Timer");
		useTimerButton.setSelection(StreamingProperties.useTimer);
		useTimerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.useTimer = useTimerButton.getSelection();
				StreamingProperties.saveProperties();
			}
		});
		
		
		CTabFolder systemsTabFolder = new CTabFolder(container, SWT.BORDER);
		systemsTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		systemsTabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		
		CTabItem codaTabItem = new CTabItem(systemsTabFolder, SWT.NONE);
		codaTabItem.setText("Codamotion");
		CTabItem optitrackTabItem = new CTabItem(systemsTabFolder, SWT.NONE);
		optitrackTabItem.setText("Optitrack");
		CTabItem xSensTabItem = new CTabItem(systemsTabFolder, SWT.NONE);
		xSensTabItem.setText("XSens");
		CTabItem timerTabItem = new CTabItem(systemsTabFolder, SWT.NONE);
		timerTabItem.setText("Timer");
		
		if(StreamingProperties.selectedSytem.equals(StreamingProperties.codamotionSystem)) systemsTabFolder.setSelection(codaTabItem);
		else if(StreamingProperties.selectedSytem.equals(StreamingProperties.optitrackSystem)) systemsTabFolder.setSelection(optitrackTabItem);
		else if(StreamingProperties.selectedSytem.equals(StreamingProperties.xSensSystem)) systemsTabFolder.setSelection(xSensTabItem);
		else if(StreamingProperties.selectedSytem.equals(StreamingProperties.timerSystem)) systemsTabFolder.setSelection(timerTabItem);
		
		systemsTabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(systemsTabFolder.getSelection() == codaTabItem) StreamingProperties.selectedSytem = StreamingProperties.codamotionSystem;
				if(systemsTabFolder.getSelection() == optitrackTabItem) StreamingProperties.selectedSytem = StreamingProperties.optitrackSystem;
				if(systemsTabFolder.getSelection() == xSensTabItem) StreamingProperties.selectedSytem = StreamingProperties.xSensSystem;
				if(systemsTabFolder.getSelection() == timerTabItem) StreamingProperties.selectedSytem = StreamingProperties.timerSystem;
				StreamingProperties.saveProperties();
			}
		});

		systemsTabFolder.setFocus();
		
		return container;
	}
	
	private void changeStreamingType() {
		StreamingProperties.useIXXAT = ixxatButton.getSelection();
		StreamingProperties.useUDP = udpButton.getSelection();
		StreamingProperties.saveProperties();
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			UDPXIXXATWindow window = new UDPXIXXATWindow();
			window.setBlockOnOpen(true);
			window.open();
			StreamingProperties.saveProperties();
			Display.getCurrent().dispose();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("UDP/IXXAT Data Streamer");
		newShell.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				StreamingProperties.width = newShell.getSize().x;
				StreamingProperties.height = newShell.getSize().y;
				StreamingProperties.saveProperties();
			}
			@Override
			public void controlMoved(ControlEvent e) {
				StreamingProperties.xPosition = newShell.getLocation().x;
				StreamingProperties.yPosition = newShell.getLocation().y;
				StreamingProperties.saveProperties();
			}
		});
	}
	

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(StreamingProperties.width, StreamingProperties.height);
	}
	
	@Override
	protected Point getInitialLocation(Point initialSize) {
		return new Point(StreamingProperties.xPosition, StreamingProperties.yPosition);
	}
	
}
