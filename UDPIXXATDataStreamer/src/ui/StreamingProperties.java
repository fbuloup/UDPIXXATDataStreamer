package ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.jface.dialogs.MessageDialog;

import datastreamer.DataStreamer;
import udpixxatdatastreamer.UDPIXXATDataStreamer;

public final class StreamingProperties {
	
	private static String propertiesFileName = "CanBridge.properties";
	
	protected static Properties properties = new Properties();
	
	protected static int xPosition;
	protected static int yPosition;
	protected static int width;
	protected static int height;
	
	protected static String xPositionKey = "xPosition";
	protected static String yPositionKey = "yPosition";
	protected static String widthKey = "width";
	protected static String heightKey = "height";
	
	protected static boolean useUDP;
	protected static boolean useIXXAT;
	
	protected static boolean useCodamotion;
	protected static boolean useOptitrack;
	protected static boolean useXSens;
	protected static boolean useTimer;
	

	protected static String useGUIToken = "-useGUI";
	protected static boolean useGUI;
	
	protected static String useUDPKey = "-useUDP";
	protected static String useIXXATKey = UDPIXXATDataStreamer.ixxatstreamerToken;
	
	protected static String useCodamotionKey = UDPIXXATDataStreamer.useCodaToken;
	protected static String useOptitrackKey = UDPIXXATDataStreamer.useOptitrackToken;
	protected static String useXSensKey = UDPIXXATDataStreamer.useXSensToken;
	protected static String useTimerKey = UDPIXXATDataStreamer.useTimeStampToken;
	
	protected static String selectedTabItem = "";
	protected static String selectedTabItemKey = "selectedTabItem";
	
	protected static String streamingOptions = "STREAMING_OPTIONS";
	protected static String udpOptions = "UDP_OPTIONS";
	protected static String codamotionSystem = "CODAMOTION";
	protected static String optitrackSystem = "OPTITRACK";
	protected static String xSensSystem = "XSENS";
	protected static String timerSystem = "TIMER";
	
	// UDP OPTIONS
	protected static String udpClientIP;
	protected static String udpClientIPKey = UDPIXXATDataStreamer.udpClientIPToken;
	protected static String udpSourcePort;
	protected static String udpSourcePortKey = UDPIXXATDataStreamer.udpSourcePortToken;
	protected static String udpDestinationPort;
	protected static String udpDestinationPortKey = UDPIXXATDataStreamer.udpDestinationPortToken;
	
	// TIMER
	protected static int timerFrequency;
	protected static String timerFrequencyKey = DataStreamer.timeStampSampleFrequencyToken;
	
	// XSens
	protected static int xSensFrequency;
	protected static String xSensFrequencyKey = DataStreamer.xsensSampleFrequencyToken;
	protected static String xSensSerialNumber;
	protected static String xSensSerialNumberKey = DataStreamer.xsensSerialkeyToken;
	
	// OPTITRACK
	protected static boolean otUseMulticast;
	protected static String otUseMulticastKey = DataStreamer.useMulticastToken;
	protected static int otNbUnlabeledMarkers;
	protected static String otNbUnlabeledMarkersKey = DataStreamer.optitrackNbUnlabeledMarkersToken;
	protected static int otFirstMarkerIndex;
	protected static String otFirstMarkerIndexKey = DataStreamer.optitrackfirstMarkerIndexToken;
	protected static String otMulticastIP;
	protected static String otMulticastIPKey = DataStreamer.optitrackMulticastIPToken;
	protected static String otUdpSourcePort;
	protected static String otUdpSourcePortKey = DataStreamer.optitrackUDPDataPortToken;
	protected static String otUdpClientIP;
	protected static String otUdpClientIPKey = DataStreamer.optitrackUDPServerIPToken;
	
	// CODAMOTION
	protected static String codaServerIP;
	protected static String codaServerIPKey = DataStreamer.codaServerIPToken;
	protected static String codaFrameRate;
	protected static String codaFrameRateKey = DataStreamer.frameRateToken;
	protected static int codaDecimation;
	protected static String codaDecimationKey = DataStreamer.decimationToken;
	protected static int codaNBMarkers;
	protected static String codaNBMarkersKey = DataStreamer.nbMarkersToken;
	protected static int codaFirstMarkerIndex;
	protected static String codaFirstMarkerIndexKey = DataStreamer.firstMarkerIndexToken;
	protected static String codaFrameNumber;
	protected static String codaFrameNumberKey = DataStreamer.framesNumberToken;
	protected static boolean codaAutoGrab;
	protected static String codaAutoGrabKey = DataStreamer.autoGrabToken;
	protected static boolean codaDoAlignment;
	protected static String codaDoAlignmentKey = DataStreamer.doAlignmentToken;
	protected static boolean codaSimulMode;
	protected static String codaSimulModeKey = DataStreamer.simulModeToken;
	
	protected static void loadProperties() throws IOException {
		
		FileInputStream fileInputStream = new FileInputStream(propertiesFileName);
		
		properties.load(fileInputStream);
		
		xPosition = Integer.parseInt(properties.getProperty(xPositionKey, "0"));
		yPosition = Integer.parseInt(properties.getProperty(yPositionKey, "0"));
		width = Integer.parseInt(properties.getProperty(widthKey, "640"));
		height = Integer.parseInt(properties.getProperty(heightKey, "480"));
		
		useUDP = Boolean.parseBoolean(properties.getProperty(useUDPKey, "true"));
		useIXXAT = Boolean.parseBoolean(properties.getProperty(useIXXATKey, "false"));
		
		useCodamotion = Boolean.parseBoolean(properties.getProperty(useCodamotionKey, "false"));
		useOptitrack = Boolean.parseBoolean(properties.getProperty(useOptitrackKey, "false"));
		useXSens = Boolean.parseBoolean(properties.getProperty(useXSensKey, "false"));
		useTimer = Boolean.parseBoolean(properties.getProperty(useTimerKey, "false"));
		
		selectedTabItem = properties.getProperty(selectedTabItemKey, streamingOptions);
		
		udpClientIP = properties.getProperty(udpClientIPKey, "localhost");
		udpSourcePort = properties.getProperty(udpSourcePortKey, "15000");
		udpDestinationPort = properties.getProperty(udpDestinationPortKey, "15000");
		
		timerFrequency = Integer.parseInt(properties.getProperty(timerFrequencyKey, "1"));
		
		xSensFrequency = Integer.parseInt(properties.getProperty(xSensFrequencyKey, "100"));
		xSensSerialNumber = properties.getProperty(xSensSerialNumberKey, "NE3B-79EA-K6RP-WDNH-QA2Y");
		
		otUseMulticast = Boolean.parseBoolean(properties.getProperty(otUseMulticastKey, "true"));
		otNbUnlabeledMarkers = Integer.parseInt(properties.getProperty(otNbUnlabeledMarkersKey, "1"));
		otFirstMarkerIndex = Integer.parseInt(properties.getProperty(otFirstMarkerIndexKey, "0"));
		otMulticastIP = properties.getProperty(otMulticastIPKey, "239.255.42.99");
		otUdpSourcePort = properties.getProperty(otUdpSourcePortKey, "1511");
		otUdpClientIP = properties.getProperty(otUdpClientIPKey, "localhost");
		
		codaServerIP = properties.getProperty(codaServerIPKey, "localhost");
		codaFrameRate = properties.getProperty(codaFrameRateKey, "100");
		codaDecimation = Integer.parseInt(properties.getProperty(codaDecimationKey, "1"));
		codaNBMarkers = Integer.parseInt(properties.getProperty(codaNBMarkersKey, "1"));
		codaFirstMarkerIndex = Integer.parseInt(properties.getProperty(codaFirstMarkerIndexKey, "1"));
//		codaUDPClientIP = properties.getProperty(codaUDPClientIPKey, "localhost");
//		codaUDPSourcePort = properties.getProperty(codaUDPSourcePortKey, "15000");
//		codaUDPDestinationPort = properties.getProperty(codaUDPDestinationPortKey, "15000");
		codaFrameNumber = properties.getProperty(codaFrameNumberKey, "-1");
		codaAutoGrab = Boolean.parseBoolean(properties.getProperty(codaAutoGrabKey, "true"));
		codaDoAlignment = Boolean.parseBoolean(properties.getProperty(codaDoAlignmentKey, "false"));
		codaSimulMode = Boolean.parseBoolean(properties.getProperty(codaSimulModeKey, "false"));
		
	}
	
	protected static void saveProperties() {
		
		properties.setProperty(xPositionKey, Integer.toString(xPosition));
		properties.setProperty(yPositionKey, Integer.toString(yPosition));
		properties.setProperty(widthKey, Integer.toString(width));
		properties.setProperty(heightKey, Integer.toString(height));
		
		properties.setProperty(useUDPKey, Boolean.toString(useUDP));
		properties.setProperty(useIXXATKey, Boolean.toString(useIXXAT));

		properties.setProperty(useCodamotionKey, Boolean.toString(useCodamotion));
		properties.setProperty(useOptitrackKey, Boolean.toString(useOptitrack));
		properties.setProperty(useXSensKey, Boolean.toString(useXSens));
		properties.setProperty(useTimerKey, Boolean.toString(useTimer));
		
		properties.setProperty(selectedTabItemKey, selectedTabItem);
		
		properties.setProperty(udpClientIPKey, udpClientIP);
		properties.setProperty(udpSourcePortKey, udpSourcePort);
		properties.setProperty(udpDestinationPortKey, udpDestinationPort);
		
		properties.setProperty(timerFrequencyKey, Integer.toString(timerFrequency));
		
		properties.setProperty(xSensFrequencyKey, Integer.toString(xSensFrequency));
		properties.setProperty(xSensSerialNumberKey, xSensSerialNumber);
		
		properties.setProperty(otUseMulticastKey, Boolean.toString(otUseMulticast));
		properties.setProperty(otNbUnlabeledMarkersKey, Integer.toString(otNbUnlabeledMarkers));
		properties.setProperty(otFirstMarkerIndexKey, Integer.toString(otFirstMarkerIndex));
		properties.setProperty(otMulticastIPKey, otMulticastIP);
		properties.setProperty(otUdpSourcePortKey, otUdpSourcePort);
		properties.setProperty(otUdpClientIPKey, otUdpClientIP);
		
		properties.setProperty(codaServerIPKey, codaServerIP);
		properties.setProperty(codaFrameRateKey, codaFrameRate);
		properties.setProperty(codaDecimationKey, Integer.toString(codaDecimation));
		properties.setProperty(codaNBMarkersKey, Integer.toString(codaNBMarkers));
		properties.setProperty(codaFirstMarkerIndexKey, Integer.toString(codaFirstMarkerIndex));
//		properties.setProperty(codaUDPClientIPKey, codaUDPClientIP);
//		properties.setProperty(codaUDPSourcePortKey, codaUDPSourcePort);
//		properties.setProperty(codaUDPDestinationPortKey, codaUDPDestinationPort);
		properties.setProperty(codaFrameNumberKey, codaFrameNumber);
		properties.setProperty(codaAutoGrabKey, Boolean.toString(codaAutoGrab));
		
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(propertiesFileName);
			properties.store(fileOutputStream, "UDP/IXXAT to CAN bridge configuration file");
		} catch (IOException e) {
			MessageDialog.openError(null, "Error saving properties file", e.getMessage());
			e.printStackTrace();
		}
	}
}
