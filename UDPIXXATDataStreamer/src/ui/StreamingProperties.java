package ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.jface.dialogs.MessageDialog;

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
	
	protected static String useUDPKey = "useUDP";
	protected static String useIXXATKey = "useIXXAT";
	
	protected static String useCodamotionKey = "useCodamotion";
	protected static String useOptitrackKey = "useOptitrack";
	protected static String useXSensKey = "useXSens";
	protected static String useTimerKey = "useTimer";
	
	protected static String selectedSytem = "";
	protected static String selectedSytemKey = "selectedSytem";
	
	protected static String codamotionSystem = "CODAMOTION";
	protected static String optitrackSystem = "OPTITRACK";
	protected static String xSensSystem = "XSENS";
	protected static String timerSystem = "TIMER";
	
	
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
		
		selectedSytem = properties.getProperty(selectedSytemKey, codamotionSystem);
		
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
		
		properties.setProperty(selectedSytemKey, selectedSytem);
		
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(propertiesFileName);
			properties.store(fileOutputStream, "UDP/IXXAT to CAN bridge configuration file");
		} catch (IOException e) {
			MessageDialog.openError(null, "Error saving properties file", e.getMessage());
			e.printStackTrace();
		}
	}
}
