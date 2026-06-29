package ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;

public class UDPXIXXATWindow extends ApplicationWindow {

	private Button udpButton;
	private Button ixxatButton;
	private boolean running;
	private StyledText consoleStyledText;

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
		
		
		Label udpClientIPLabel = new Label(container, SWT.BORDER);
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
		
		Label udpSourcePortLabel = new Label(container, SWT.BORDER);
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
		
		Label udpDestinationPortLabel = new Label(container, SWT.BORDER);
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
		
		Composite consoleToolBar = new Composite(container, SWT.NONE);
		consoleToolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		consoleToolBar.setLayout(new GridLayout(2, false));
		
		Label consoleLabel = new Label(consoleToolBar, SWT.NONE);
		consoleLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		consoleLabel.setText("Console");
		
		Button startStopButton = new Button(consoleToolBar, SWT.FLAT);
		startStopButton.setText("Start");
		startStopButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
		startStopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startStopHandler();
				if(running) startStopButton.setText("Stop");
				else startStopButton.setText("Start");
			}
		});
		
		consoleStyledText = new StyledText(container, SWT.BORDER);
		consoleStyledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tabItem.setControl(container);
	}

	protected void startStopHandler() {
		if(!running) {
			// Compute command line parameters
			String cmdLine = "-ixxatstreamer false -udpclientip ";
			cmdLine = cmdLine + StreamingProperties.udpClientIP;
			cmdLine = cmdLine + " -udpsourceport " + StreamingProperties.udpSourcePort;
			cmdLine = cmdLine + " -udpdestinationport " + StreamingProperties.udpDestinationPort;
			if(StreamingProperties.useIXXAT) cmdLine = "-ixxatstreamer true";
			cmdLine = cmdLine + " -usecoda false";
			cmdLine = cmdLine + " -useoptitrack false";
			cmdLine = cmdLine + " -usexsens false";
			if(StreamingProperties.useTimer) {
				cmdLine = cmdLine + " -usetimestamp true";
				cmdLine = cmdLine + " -timestampsamplefrequency " + StreamingProperties.timerFrequency;
			} else cmdLine = cmdLine + " -usetimestamp false";
			
			OutputStream out = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					if (consoleStyledText.isDisposed()) return;
					consoleStyledText.append(String.valueOf((char) b));
				}
			};
			System.setOut(new PrintStream(out));
			
			OutputStream err = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					if (consoleStyledText.isDisposed()) return;
					consoleStyledText.append(String.valueOf((char) b));
				}
			};
			System.setErr(new PrintStream(err));
			
			InputStream in = new InputStream() {
				@Override
				public int read() throws IOException {
					// TODO Auto-generated method stub
					return 'n';
				}
			};
			System.setIn(in);
			
			consoleStyledText.addTraverseListener(new TraverseListener() {
				@Override
				public void keyTraversed(TraverseEvent event) {
					if(event.detail == SWT.TRAVERSE_RETURN) {
						int lineNumber = consoleStyledText.getLineAtOffset(consoleStyledText.getCaretOffset());
						System.out.println(lineNumber);
						System.out.println(consoleStyledText.getLine(lineNumber));
					}
				}
			});
			
			
			UDPIXXATDataStreamer.main(cmdLine.split(" "));
			running = true;
		} else {
			running = false;
		}
		
	}

	private void populateTimerTabItem(CTabItem tabItem) {
		Composite container = new Composite(tabItem.getParent(), SWT.BORDER);
		container.setLayout(new GridLayout(2, false));
		Label label = new Label(container, SWT.BORDER);
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
		
		Label label = new Label(container, SWT.BORDER);
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
		
		
		Label labelSerial = new Label(container, SWT.BORDER);
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
		
		Label nbUnlabeledMarkersLabel = new Label(container, SWT.BORDER);
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
		
		Label firstMarkerIndexLabel = new Label(container, SWT.BORDER);
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
		
		Label udpClientIPLabel = new Label(container, SWT.BORDER);
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
		
		Label udpSourcePortLabel = new Label(container, SWT.BORDER);
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
		
		Label multicastIPLabel = new Label(container, SWT.BORDER);
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
		
		Label codaServerIPLabel = new Label(container, SWT.BORDER);
		codaServerIPLabel.setText("Multicast IP : ");
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
		
		Label frameRateLabel = new Label(container, SWT.BORDER);
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
		
		Label decimationLabel = new Label(container, SWT.BORDER);
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
		
		Label codaNBMarkersLabel = new Label(container, SWT.BORDER);
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
		
		Label codaFirstMarkerIndexLabel = new Label(container, SWT.BORDER);
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
		
		Label frameNumberLabel = new Label(container, SWT.BORDER);
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
		
		Button autoGrabButton = new Button(container, SWT.CHECK);
		autoGrabButton.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 2, 1));
		autoGrabButton.setText("Auto Grab");
		autoGrabButton.setSelection(StreamingProperties.codaAutoGrab);
		autoGrabButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.codaAutoGrab = autoGrabButton.getSelection();
				StreamingProperties.saveProperties();
			}
		});
		
		Button doAlignmentButton = new Button(container, SWT.CHECK);
		doAlignmentButton.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 2, 1));
		doAlignmentButton.setText("Do alignment");
		doAlignmentButton.setSelection(StreamingProperties.codaDoAlignment);
		doAlignmentButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StreamingProperties.codaDoAlignment = doAlignmentButton.getSelection();
				StreamingProperties.saveProperties();
			}
		});
		
		Button simulModeButton = new Button(container, SWT.CHECK);
		simulModeButton.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 2, 1));
		simulModeButton.setText("Auto Grab");
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
