package ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import udpixxatdatastreamer.UDPIXXATDataStreamer;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;

public class UDPIXXATWindow extends ApplicationWindow {

	private static Button udpButton;
	private static Button ixxatButton;
	private static boolean running;

	/**
	 * Create the application window,
	 */
	public UDPIXXATWindow() {
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
		
		CTabFolder optionsTabFolder = new CTabFolder(container, SWT.BORDER);
		optionsTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		optionsTabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		
		CTabItem streamingOptionsTabItem = new CTabItem(optionsTabFolder, SWT.NONE);
		streamingOptionsTabItem.setText("Streaming");
		CTabItem udpOptionsTabItem = new CTabItem(optionsTabFolder, SWT.NONE);
		udpOptionsTabItem.setText("UDP Options");
		CTabItem codaTabItem = new CTabItem(optionsTabFolder, SWT.NONE);
		codaTabItem.setText("Codamotion");
		CTabItem optitrackTabItem = new CTabItem(optionsTabFolder, SWT.NONE);
		optitrackTabItem.setText("Optitrack");
		CTabItem xSensTabItem = new CTabItem(optionsTabFolder, SWT.NONE);
		xSensTabItem.setText("XSens");
		CTabItem timerTabItem = new CTabItem(optionsTabFolder, SWT.NONE);
		timerTabItem.setText("Timer");
		if(StreamingProperties.selectedTabItem.equals(StreamingProperties.streamingOptions)) optionsTabFolder.setSelection(streamingOptionsTabItem);
		else if(StreamingProperties.selectedTabItem.equals(StreamingProperties.udpOptions)) optionsTabFolder.setSelection(udpOptionsTabItem);
		else if(StreamingProperties.selectedTabItem.equals(StreamingProperties.codamotionSystem)) optionsTabFolder.setSelection(codaTabItem);
		else if(StreamingProperties.selectedTabItem.equals(StreamingProperties.optitrackSystem)) optionsTabFolder.setSelection(optitrackTabItem);
		else if(StreamingProperties.selectedTabItem.equals(StreamingProperties.xSensSystem)) optionsTabFolder.setSelection(xSensTabItem);
		else if(StreamingProperties.selectedTabItem.equals(StreamingProperties.timerSystem)) optionsTabFolder.setSelection(timerTabItem);
		
		optionsTabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(optionsTabFolder.getSelection() == streamingOptionsTabItem) StreamingProperties.selectedTabItem = StreamingProperties.streamingOptions;
				else if(optionsTabFolder.getSelection() == udpOptionsTabItem) StreamingProperties.selectedTabItem = StreamingProperties.udpOptions;
				else if(optionsTabFolder.getSelection() == codaTabItem) StreamingProperties.selectedTabItem = StreamingProperties.codamotionSystem;
				else if(optionsTabFolder.getSelection() == optitrackTabItem) StreamingProperties.selectedTabItem = StreamingProperties.optitrackSystem;
				else if(optionsTabFolder.getSelection() == xSensTabItem) StreamingProperties.selectedTabItem = StreamingProperties.xSensSystem;
				else if(optionsTabFolder.getSelection() == timerTabItem) StreamingProperties.selectedTabItem = StreamingProperties.timerSystem;
				StreamingProperties.saveProperties();
			}
		});

		optionsTabFolder.setFocus();
		
		populateStreamingOptions(streamingOptionsTabItem);
		populateUDPOptionsTabItem(udpOptionsTabItem);
		populateTimerTabItem(timerTabItem);
		populateXSensTabItem(xSensTabItem);
		populateOptitrackTabItem(optitrackTabItem);
		populateCodamotionTabItem(codaTabItem);
		
		return container;
	}
	
	private void populateUDPOptionsTabItem(CTabItem tabItem) {
		Composite container = new Composite(tabItem.getParent(), SWT.BORDER);
		container.setLayout(new GridLayout(2, false));
		/*
		* <li>-updclientip : When UDP is used : ip Of CAN/Ethernet bridge. defaut localhost</li>                
		 * <li>-udpsourceport : When UDP is used : source port of CAN/Ethernet bridge. Default is 15000</li>        
		 * <li>-udpdestinationport 
		*/
		
		
		Label udpClientIPLabel = new Label(container, SWT.NONE);
		udpClientIPLabel.setText("UDP Client IP : ");
		udpClientIPLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text udpClientIPText = new Text(container, SWT.BORDER);
		udpClientIPText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		udpClientIPText.setText(StreamingProperties.udpClientIP);
		udpClientIPText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.udpClientIP = udpClientIPText.getText();
				StreamingProperties.saveProperties();
			}
		});
		
		Label udpSourcePortLabel = new Label(container, SWT.NONE);
		udpSourcePortLabel.setText("UDP Source port : ");
		udpSourcePortLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text udpSourcePortText = new Text(container, SWT.BORDER);
		udpSourcePortText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		udpSourcePortText.setText(StreamingProperties.udpSourcePort);
		udpSourcePortText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.udpSourcePort = udpSourcePortText.getText();
				StreamingProperties.saveProperties();
			}
		});
		
		Label udpDestinationPortLabel = new Label(container, SWT.NONE);
		udpDestinationPortLabel.setText("UDP Destination port : ");
		udpDestinationPortLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text udpDestinationePortText = new Text(container, SWT.BORDER);
		udpDestinationePortText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		udpDestinationePortText.setText(StreamingProperties.udpDestinationPort);
		udpDestinationePortText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.udpDestinationPort = udpDestinationePortText.getText();
				StreamingProperties.saveProperties();
			}
		});
		
		tabItem.setControl(container);
	}

	private void populateStreamingOptions(CTabItem tabItem) {
		Composite container = new Composite(tabItem.getParent(), SWT.BORDER);
		container.setLayout(new GridLayout());
		
		Group grpStreamingType = new Group(container, SWT.NONE);
		grpStreamingType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		grpStreamingType.setText(" Streaming type ");
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.marginWidth = 10;
		grpStreamingType.setLayout(gridLayout);
		udpButton = new Button(grpStreamingType, SWT.RADIO);
		udpButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		udpButton.setText("UDP\n(configure UDP options)");
		udpButton.setSelection(StreamingProperties.useUDP);
		udpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				changeStreamingType();
			}
		});
		ixxatButton = new Button(grpStreamingType, SWT.RADIO);
		ixxatButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
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
		
//		Composite consoleToolBar = new Composite(container, SWT.NONE);
//		consoleToolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
//		consoleToolBar.setLayout(new GridLayout(2, false));
//		
//		Label consoleLabel = new Label(consoleToolBar, SWT.NONE);
//		consoleLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
//		consoleLabel.setText("Console");
		
		Button startStopButton = new Button(container, SWT.FLAT);
		startStopButton.setText("<<<< Start >>>>");
		startStopButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		startStopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startStopHandler();
//				if(running) startStopButton.setText("Stop");
//				else startStopButton.setText("Start");
			}
		});
		
		tabItem.setControl(container);
	}

	protected void startStopHandler() {
		if(!running) {
			// Compute command line parameters
			String cmdLine = "java -jar UDPIXXATDataStreamer.jar " + StreamingProperties.useGUIToken + " true";
			if(StreamingProperties.useIXXAT) cmdLine = cmdLine + " " + StreamingProperties.useIXXATKey + " true";
			else {
				cmdLine = cmdLine + " " + StreamingProperties.useIXXATKey + " false";
				cmdLine = cmdLine + " " + StreamingProperties.udpClientIPKey + " " + StreamingProperties.udpClientIP;
				cmdLine = cmdLine + " " + StreamingProperties.udpSourcePortKey + " " + StreamingProperties.udpSourcePort;
				cmdLine = cmdLine + " " + StreamingProperties.udpDestinationPortKey + " " + StreamingProperties.udpDestinationPort;
			}

			cmdLine = cmdLine + " " + StreamingProperties.useCodamotionKey + " " + StreamingProperties.useCodamotion;
			if(StreamingProperties.useCodamotion) {
				cmdLine = cmdLine + " " + StreamingProperties.codaServerIPKey + " " + StreamingProperties.codaServerIP;
				cmdLine = cmdLine + " " + StreamingProperties.codaFrameRateKey + " " + StreamingProperties.codaFrameRate;
				cmdLine = cmdLine + " " + StreamingProperties.codaDecimationKey + " " + StreamingProperties.codaDecimation;
				cmdLine = cmdLine + " " + StreamingProperties.codaNBMarkersKey + " " + StreamingProperties.codaNBMarkers;
				cmdLine = cmdLine + " " + StreamingProperties.codaFirstMarkerIndexKey + " " + StreamingProperties.codaFirstMarkerIndex;
				cmdLine = cmdLine + " " + StreamingProperties.codaFrameNumberKey + " " + StreamingProperties.codaFrameNumber;
				cmdLine = cmdLine + " " + StreamingProperties.codaAutoGrabKey + " " + StreamingProperties.codaAutoGrab;
				cmdLine = cmdLine + " " + StreamingProperties.codaSimulModeKey + " " + StreamingProperties.codaSimulMode;
				cmdLine = cmdLine + " " + StreamingProperties.codaDoAlignmentKey + " " + StreamingProperties.codaDoAlignment;
			}
			
			cmdLine = cmdLine + " " + StreamingProperties.useXSensKey + " " + StreamingProperties.useXSens;
			if(StreamingProperties.useXSens) {
				cmdLine = cmdLine + " " + StreamingProperties.xSensSerialNumberKey + " " + StreamingProperties.xSensSerialNumber;
				cmdLine = cmdLine + " " + StreamingProperties.xSensFrequencyKey + " " + StreamingProperties.xSensFrequency;
			}
			
			cmdLine = cmdLine + " " + StreamingProperties.useTimerKey + " " + StreamingProperties.useTimer;
			if(StreamingProperties.useTimer) {
				cmdLine = cmdLine + " " + StreamingProperties.timerFrequencyKey + " " + StreamingProperties.timerFrequency;
			} 
			
			cmdLine = cmdLine + " " + StreamingProperties.useOptitrackKey + " " + StreamingProperties.useOptitrack;
			if(StreamingProperties.useOptitrack) {
				cmdLine = cmdLine + " " + StreamingProperties.otUseMulticastKey + " " + StreamingProperties.otUseMulticast;
				cmdLine = cmdLine + " " + StreamingProperties.otNbUnlabeledMarkersKey + " " + StreamingProperties.otNbUnlabeledMarkers;
				cmdLine = cmdLine + " " + StreamingProperties.otFirstMarkerIndexKey + " " + StreamingProperties.otFirstMarkerIndex;
				cmdLine = cmdLine + " " + StreamingProperties.otMulticastIPKey + " " + StreamingProperties.otMulticastIP;
				cmdLine = cmdLine + " " + StreamingProperties.otUdpClientIPKey + " " + StreamingProperties.otUdpClientIP;
				cmdLine = cmdLine + " " + StreamingProperties.otUdpSourcePortKey + " " + StreamingProperties.otUdpSourcePort;
				cmdLine = cmdLine + " " + StreamingProperties.otMulticastIPKey + " " + StreamingProperties.otMulticastIP;
			}
			
			if(System.getProperty("os.name").toLowerCase().contains("windows")) cmdLine = cmdLine + "\n" + "exit";
			if(System.getProperty("os.name").toLowerCase().contains("mac")) {
				//cmdLine = "#!/bin/bash\n" /*+ "cd " + System.getProperty("user.dir") + "\n"*/ + cmdLine;
				
			}
			try {
			
				String batchFilePath = null;
				
				if(System.getProperty("os.name").toLowerCase().contains("windows")) batchFilePath = "UDPIXXATDataStreamer.bat";
				if(System.getProperty("os.name").toLowerCase().contains("mac")) batchFilePath = "UDPIXXATDataStreamer.sh";
				
				if(batchFilePath != null) {
					
					File batchFile = new File(batchFilePath);
					if(batchFile.exists()) batchFile.delete();
					FileWriter fileWriter = new FileWriter(batchFile);
					
					fileWriter.write(cmdLine);
					fileWriter.close();
					
					IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								monitor.beginTask("Streaming thread is running...", IProgressMonitor.UNKNOWN);
								Process process = null;
								if(System.getProperty("os.name").toLowerCase().contains("windows"))
									process = Runtime.getRuntime().exec(new String[] {"cmd.exe", "/c", "START",  "/WAIT", "UDPIXXATDataStreamer.bat"});
								if(System.getProperty("os.name").toLowerCase().contains("mac")) {
									process = Runtime.getRuntime().exec(new String[] {"chmod", "777", "UDPIXXATDataStreamer.sh"});
									process.waitFor();
									process = Runtime.getRuntime().exec(
										    new String[] {
										        "osascript",
										        "-e", "tell application \"Terminal\"",
										        "-e", "do script \"/bin/bash -c \\\"cd " + System.getProperty("user.dir") + "; ./UDPIXXATDataStreamer.sh; \\\"\"",
										        "-e", "end tell"
										    }
										);
								}
								running = true;
								int exitCode = process.waitFor();
								while(process.isAlive());
								System.out.println("UDPIXXATDataStreamer exit code : " + exitCode);
								running = false;
							} catch (IOException | InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
					run(true, false, runnableWithProgress);			
				}
			} catch (InvocationTargetException | InterruptedException | IOException e) {
				e.printStackTrace();
				running = false;
			}
		} else {
			running = false;
		}
		
	}

	private void populateTimerTabItem(CTabItem tabItem) {
		Composite container = new Composite(tabItem.getParent(), SWT.BORDER);
		container.setLayout(new GridLayout(2, false));
		Label label = new Label(container, SWT.NONE);
		label.setText("Frequency (Hz) : ");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Combo combo = new Combo(container, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		String[] freqs = new String[100];
		for (int i = 1; i <= freqs.length; i++) {
			freqs[i-1] = Integer.toString(i);
		}
		combo.setItems(freqs);
		combo.select(StreamingProperties.timerFrequency - 1);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.timerFrequency = combo.getSelectionIndex() + 1;
				StreamingProperties.saveProperties();
			}
		});
		tabItem.setControl(container);
		
	}
	
	private void populateXSensTabItem(CTabItem tabItem) {
		Composite container = new Composite(tabItem.getParent(), SWT.BORDER);
		container.setLayout(new GridLayout(2, false));
		
		Label label = new Label(container, SWT.NONE);
		label.setText("Frequency (Hz) : ");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Combo combo = new Combo(container, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		String[] freqs = new String[500];
		for (int i = 1; i <= freqs.length; i++) {
			freqs[i-1] = Integer.toString(i);
		}
		combo.setItems(freqs);
		combo.select(StreamingProperties.xSensFrequency - 1);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.xSensFrequency = combo.getSelectionIndex() + 1;
				StreamingProperties.saveProperties();
			}
		});
		
		
		Label labelSerial = new Label(container, SWT.NONE);
		labelSerial.setText("XSens serial key : ");
		labelSerial.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text textSerialKey = new Text(container, SWT.BORDER);
		textSerialKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textSerialKey.setText(StreamingProperties.xSensSerialNumber);
		textSerialKey.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.xSensSerialNumber = textSerialKey.getText();
				StreamingProperties.saveProperties();
			}
		});
		
		tabItem.setControl(container);
		
	}
	
	private void populateOptitrackTabItem(CTabItem tabItem) {
		Composite container = new Composite(tabItem.getParent(), SWT.BORDER);
		container.setLayout(new GridLayout(2, false));
		
		Button useMulticastbutton = new Button(container, SWT.CHECK);
		useMulticastbutton.setText("Use multicast");
		useMulticastbutton.setSelection(StreamingProperties.otUseMulticast);
		useMulticastbutton.setEnabled(false);
		useMulticastbutton.setLayoutData(new GridData(SWT.RIGHT, SWT.LEFT, true, false, 2, 1));
		
		Label nbUnlabeledMarkersLabel = new Label(container, SWT.NONE);
		nbUnlabeledMarkersLabel.setText("Unlabeled markers number : ");
		nbUnlabeledMarkersLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Spinner nbUnlabeledMarkersSpinner = new Spinner(container, SWT.READ_ONLY | SWT.BORDER);
		nbUnlabeledMarkersSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nbUnlabeledMarkersSpinner.setMinimum(1);
		nbUnlabeledMarkersSpinner.setMaximum(10);
		nbUnlabeledMarkersSpinner.setSelection(StreamingProperties.otNbUnlabeledMarkers);
		nbUnlabeledMarkersSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.otNbUnlabeledMarkers = nbUnlabeledMarkersSpinner.getSelection();
				StreamingProperties.saveProperties();
			}
		});
		
		Label firstMarkerIndexLabel = new Label(container, SWT.NONE);
		firstMarkerIndexLabel.setText("First marker index : ");
		firstMarkerIndexLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Spinner firstMarkerIndexSpinner = new Spinner(container, SWT.READ_ONLY | SWT.BORDER);
		firstMarkerIndexSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		firstMarkerIndexSpinner.setMinimum(1);
		firstMarkerIndexSpinner.setMaximum(10);
		firstMarkerIndexSpinner.setSelection(StreamingProperties.otFirstMarkerIndex);
		firstMarkerIndexSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.otFirstMarkerIndex = firstMarkerIndexSpinner.getSelection();
				StreamingProperties.saveProperties();
			}
		});
		
		Label udpClientIPLabel = new Label(container, SWT.NONE);
		udpClientIPLabel.setText("UDP Client IP : ");
		udpClientIPLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text udpClientIPText = new Text(container, SWT.BORDER);
		udpClientIPText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		udpClientIPText.setText(StreamingProperties.otUdpClientIP);
		udpClientIPText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.otUdpClientIP = udpClientIPText.getText();
				StreamingProperties.saveProperties();
			}
		});
		
		Label udpSourcePortLabel = new Label(container, SWT.NONE);
		udpSourcePortLabel.setText("UDP Source port : ");
		udpSourcePortLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text udpSourcePortText = new Text(container, SWT.BORDER);
		udpSourcePortText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		udpSourcePortText.setText(StreamingProperties.otUdpSourcePort);
		udpSourcePortText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.otUdpSourcePort = udpSourcePortText.getText();
				StreamingProperties.saveProperties();
			}
		});
		
		Label multicastIPLabel = new Label(container, SWT.NONE);
		multicastIPLabel.setText("Multicast IP : ");
		multicastIPLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text multicastIPText = new Text(container, SWT.BORDER);
		multicastIPText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		multicastIPText.setText(StreamingProperties.otMulticastIP);
		multicastIPText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.otMulticastIP = multicastIPText.getText();
				StreamingProperties.saveProperties();
			}
		});

		tabItem.setControl(container);
	}
	
	private void populateCodamotionTabItem(CTabItem tabItem) {
		Composite container = new Composite(tabItem.getParent(), SWT.BORDER);
		container.setLayout(new GridLayout(2, false));
		
		Label codaServerIPLabel = new Label(container, SWT.NONE);
		codaServerIPLabel.setText("Coda server IP : ");
		codaServerIPLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text codaServerIP = new Text(container, SWT.BORDER);
		codaServerIP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		codaServerIP.setText(StreamingProperties.codaServerIP);
		codaServerIP.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.codaServerIP = codaServerIP.getText();
				StreamingProperties.saveProperties();
			}
		});
		
		Label frameRateLabel = new Label(container, SWT.NONE);
		frameRateLabel.setText("Frequency (Hz) : ");
		frameRateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Combo frameRateCombo = new Combo(container, SWT.READ_ONLY);
		frameRateCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		frameRateCombo.setItems(new String[] {"100", "120", "200", "400", "800"});
		frameRateCombo.select(frameRateCombo.indexOf(StreamingProperties.codaFrameRate));
		frameRateCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.codaFrameRate = frameRateCombo.getText();
				StreamingProperties.saveProperties();
			}
		});
		
		Label decimationLabel = new Label(container, SWT.NONE);
		decimationLabel.setText("Decimation : ");
		decimationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Spinner decimationSpinner = new Spinner(container, SWT.READ_ONLY | SWT.BORDER);
		decimationSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		decimationSpinner.setMinimum(1);
		decimationSpinner.setMaximum(10);
		decimationSpinner.setSelection(StreamingProperties.codaDecimation - 1);
		decimationSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.codaDecimation = decimationSpinner.getSelection() + 1;
				StreamingProperties.saveProperties();
			}
		});
		
		Label codaNBMarkersLabel = new Label(container, SWT.NONE);
		codaNBMarkersLabel.setText("Markers number : ");
		codaNBMarkersLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Spinner codaNBMarkersSpinner = new Spinner(container, SWT.READ_ONLY | SWT.BORDER);
		codaNBMarkersSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		codaNBMarkersSpinner.setMinimum(1);
		codaNBMarkersSpinner.setMaximum(10);
		codaNBMarkersSpinner.setSelection(StreamingProperties.codaNBMarkers - 1);
		codaNBMarkersSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.codaNBMarkers = codaNBMarkersSpinner.getSelection() + 1;
				StreamingProperties.saveProperties();
			}
		});
		
		Label codaFirstMarkerIndexLabel = new Label(container, SWT.NONE);
		codaFirstMarkerIndexLabel.setText("First marker index : ");
		codaFirstMarkerIndexLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Spinner firstMarkerIndexSpinner = new Spinner(container, SWT.READ_ONLY | SWT.BORDER);
		firstMarkerIndexSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		firstMarkerIndexSpinner.setMinimum(1);
		firstMarkerIndexSpinner.setMaximum(10);
		firstMarkerIndexSpinner.setSelection(StreamingProperties.codaFirstMarkerIndex - 1);
		firstMarkerIndexSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.codaFirstMarkerIndex = firstMarkerIndexSpinner.getSelection() + 1;
				StreamingProperties.saveProperties();
			}
		});
		
		Label frameNumberLabel = new Label(container, SWT.NONE);
		frameNumberLabel.setText("Frame number : ");
		frameNumberLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text frameNumberText = new Text(container, SWT.BORDER);
		frameNumberText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		frameNumberText.setText(StreamingProperties.codaFrameNumber);
		frameNumberText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				StreamingProperties.codaFrameNumber = frameNumberText.getText();
				StreamingProperties.saveProperties();
			}
		});
		
		Composite buttonsContainer = new Composite(container, SWT.NORMAL);
		buttonsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		buttonsContainer.setLayout(new GridLayout(3, true));
		
		Button autoGrabButton = new Button(buttonsContainer, SWT.CHECK);
		autoGrabButton.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));
		autoGrabButton.setText("Auto Grab");
		autoGrabButton.setSelection(StreamingProperties.codaAutoGrab);
		autoGrabButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.codaAutoGrab = autoGrabButton.getSelection();
				StreamingProperties.saveProperties();
			}
		});
		
		Button doAlignmentButton = new Button(buttonsContainer, SWT.CHECK);
		doAlignmentButton.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));
		doAlignmentButton.setText("Do alignment");
		doAlignmentButton.setSelection(StreamingProperties.codaDoAlignment);
		doAlignmentButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.codaDoAlignment = doAlignmentButton.getSelection();
				StreamingProperties.saveProperties();
			}
		});
		
		Button simulModeButton = new Button(buttonsContainer, SWT.CHECK);
		simulModeButton.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));
		simulModeButton.setText("Simul Mode");
		simulModeButton.setSelection(StreamingProperties.codaSimulMode);
		simulModeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.codaSimulMode = simulModeButton.getSelection();
				StreamingProperties.saveProperties();
			}
		});

		tabItem.setControl(container);
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
			
			for (int i = 0; i < args.length; i++) if(args[i].equalsIgnoreCase(StreamingProperties.useGUIToken)) 
				StreamingProperties.useGUI = Boolean.parseBoolean(args[i+1]);
			
			if(!StreamingProperties.useGUI) {
				UDPIXXATDataStreamer.main(args);
			} else {
				UDPIXXATWindow window = new UDPIXXATWindow();			
				window.setBlockOnOpen(true);
				window.open();
				StreamingProperties.saveProperties();
				Display.getCurrent().dispose();
			}
			
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
