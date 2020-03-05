package datastreamer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

import udpdatastreamer.DataObserver;
import udpdatastreamer.UDPIXXATDataStreamer;

import com.codamotion.system.Align;
import com.codamotion.system.CODANETClient;
import com.codamotion.system.CodaError;
import com.codamotion.system.FrameData;
import com.codamotion.system.Mode;
import com.sun.jna.WString;

/**
 * This is the main class that is responsible to run continuous acquisition
 * on Coda or xsens unit or time server. <br>It retrieves each frame and send its values to any registered
 * observer (these observers must implement DataObserver interface) .
 * Each sent frame can have up to height by eight bits pattern.<br><br>
 * 
 * CODA SYTEM :<br>
 * For coda system, frame must be read like this : <br>
 * FIRST BYTE :
 * b7b6 b5 b4b3b2b1b0<br>
 * b7b6 are system code on two bits : 00 for coda<br>
 * b5 is visibility bit<br>
 * b4b3b2b1b0 are sensor number (max 32)<br>
 * SECOND BYTE : frameID<br>
 * THIRD AND FOURTH BYTES : on two bytes - X sensor value<br>
 * FIFTH AND SIXTH : on two bytes - Y sensor value<br>
 * SEVENTH AND NINTH : on two bytes - Z sensor value<br><br>
 * 
 * XSENS SYSTEM :<br>
 * For xsens system, frame must be read like this : <br>
 * FIRST BYTE : b7b6 b5b4b3b2b1b0 - b7b6 are system code on two bits : 01 for xsens - b5b4b3b2b1b0 are not used<br>
 * SECOND AND THIRD BYTES : on two bytes - X accel sensor value<br>
 * FOURTH AND FIFTH BYTES : on two bytes - Y accel sensor value<br>
 * SIXTH AND SEVENTH BYTES : on two bytes - Z gyro sensor value<br><br>
 * 
 * TIME STAMP SYSTEM :<br>
 * For time stamp system, frame must be read like this : <br>
 * FIRST BYTE : b7b6 b5b4b3b2b1b0 - 
 * b7b6 are system code on two bits : 10 for time stamp - 
 * b5b4b3b2b1b0 : hours on five bits, b5 is always zero<br>
 * SECOND BYTE : minutes on six bits, b7 and b6 are always zero<br>
 * THIRD BYTE : seconds on six bits, b7 and b6 are always zero<br>
 * 
 * @author centricoda
 */
public class DataStreamer extends Thread {
	/*
	 * CODA
	 */
//	private final static int CODA_ACQ_SAVE_RAM = 0;
//	private final static int CODA_ACQ_SAVE_DISK = 1;
//	private final static int CODA_ACQ_SAVE_BUFFER_ONLY = 2;
	private final static int CODA_ACQ_SAVE_NONE = 3;
	
	private final static String codaServerIPToken = "-codaserverip";
	private final static String frameRateToken = "-framerate";
	private final static String decimationToken = "-decimation";
	private final static String nbMarkersToken = "-nbmarkers";
	private final static String firstMarkerIndexToken = "-firstmarkerindex";
	private final static String framesNumberToken = "-framesnumber";
	private final static String autoGrabToken = "-autograb";
	private final static String simulModeToken = "-simulmode";
	
	private final static CODANETClient codaUnit = new CODANETClient();
	private final static Mode codaUnitMode = new Mode();
	private final static FrameData frame = new FrameData();
	
	private static String codaServerIP = "localhost";
	private static int frameRate = 100;
	private static int nbMarkers = 1;
	private static int firstMarkerIndex = 1;
	private static int framesNumber = -1;
	private static boolean autoGrab = true;
	private static float[] codaValues;
	private static byte[] codaVisibilities;
	
	private static int codaLastFrameID;
	private static int codaFrameID;
	private static byte frameID;
	private static int codaLastFrameIDFromBufferUpdate;
	
	private static int[] framesIDs;
	private static long[] framesTimes;
	
	private static short codaXValue;
	private static short codaYValue ;
	private static short codaZValue ;
	
	private static byte[] codaBytesBuffer = new byte[8];
	
	private static ArrayList<DataObserver> observers = new ArrayList<DataObserver>(0);
	
	private static boolean pause = false;
	private static boolean display = false;

	private static XSensLibrary xSens;
	
	private static byte codaSystemCode = 0;
	
	private static boolean simulMode = false;
	
	/*
	 * XSens
	 */
	private final static String xsensSerialkeyToken = "-xsensserialkey";
	private final static String xsensSampleFrequencyToken = "-xsenssamplefrequency";
	
	private static String xsensSerialKey = "NE3B-79EA-K6RP-WDNH-QA2Y";
	private static int xsensSampleFrequency = XSensLibrary.MAX_FREQUENCY;
	
	private static byte[] xsensBytesBuffer = new byte[7];
	
	private static short xsensAccX;
	private static short xsensAccY;
	private static short xsensGyroZ;

	private static long lastTimeStamp;

	private static byte xSensSystemCode = 1;
	
	/*
	 * Time stamp
	 */
	
	private final static String timeStampSampleFrequencyToken = "-timestampsamplefrequency";

	private static int timeStampSampleFrequency;
	
	private static byte hours;
	private static byte minutes;
	private static byte seconds;
	
	private static byte[] timeStampBytesBuffer = new byte[3];
	
	private static byte timeStampSystemCode = 2;
	
	/*
	 * Other
	 */

	private static long lastTimeStampDisplay;
	private static boolean doDisplay;
	
	/**
	 * See {@link UDPIXXATDataStreamer#main(String[])} for details.
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		DataStreamer dataStreamer = new DataStreamer(args);
		dataStreamer.start();
		
		try {
			System.in.read();
			dataStreamer.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Command line arguments : it may or not contain following parameters.<br>
	 * If codamotion system is used :
	 * <ul>
	 * <li>-codaserverip : default localhost. IP where CX1Server is running </li>                    
	 * <li>-framerate : default 100. Valid values : 100, 120, 200, 400 or 800 any other values will result to 100</li>                                   
	 * <li>-decimation : default 1</li>                                                
	 * <li>-nbmarkers : default 1</li>                                                
	 * <li>-firstmarkerindex : default 1</li>                                    
	 * <li>-updclientip : defaut localhost</li>                
	 * <li>-udpsourceport : default 15000</li>        
	 * <li>-udpdestinationport : default udpsourceport</li>
	 * <li>-framesNumber : default -1. Number of frames to send before stopping streaming. Specifying -1 means infinite streaming.</li>
	 * <li>-autograb : default true. Use or not autograd frame.</li>
	 * </ul>
	 * For instance :
	 * -codaServerIP 192.168.0.3 -frameRate 800 -decimation 1 -nbMarkers 1 -firstMarkerIndex 1 -udpclientip 192.168.0.30
	 * <br><br>
	 * If gyrscope is used :
	 * <ul>
	 * <li>-xsensserialkey : default "NE3B-79EA-K6RP-WDNH-QA2Y"</li>                    
	 * <li>-xsenssamplefrequency : default 100. Valid values are integers.</li>                                   
	 * </ul>
	 * For instance :
	 * -usexsens true -xsensserialkey NE3B-79EA-K6RP-WDNH-QA2Y -xsenssamplefrequency 1000
	 *  <br>
	 *  <br>
	 * If time stamp server is used :
	 * <ul>
	 * <li>-timestampsamplefrequency : default 1. Valid values are integer</li> 
	 * </ul>
	 * For instance :
	 * -usetimestamp true -timestampsamplefrequency 100
	 *  <br><br>
	 * @param params list of parameters configuration. See above.
	 */
	public DataStreamer(String[] params) {
		this.setPriority(Thread.MAX_PRIORITY);
		
		//Use coda
		if(UDPIXXATDataStreamer.useCodamotion) {
			codaUnitMode.setActiveCoda(new byte[]{1,0,0,0});
			codaUnitMode.setMarkerMode(Mode.markerMode100);
			codaUnitMode.setDecimation(1);
			codaUnitMode.setMaxMarker(1);
			
			for (int i = 0; i < params.length; i++) {
				if(params[i].toLowerCase().equals(codaServerIPToken)) codaServerIP = params[i+1];
				if(params[i].toLowerCase().equals(frameRateToken)) {
					frameRate = Integer.parseInt(params[i+1]);
					switch (frameRate) {
					case 100:
						codaUnitMode.setMarkerMode(Mode.markerMode100);
						break;
					case 120:
						codaUnitMode.setMarkerMode(Mode.markerMode120);
						break;
					case 200:
						codaUnitMode.setMarkerMode(Mode.markerMode200);
						break;
					case 400:
						codaUnitMode.setMarkerMode(Mode.markerMode400);
						break;
					case 800:
						codaUnitMode.setMarkerMode(Mode.markerMode800);
						break;
					default:
						frameRate = 100;
						codaUnitMode.setMarkerMode(Mode.markerMode100);
						break;
					}
				}
				if(params[i].toLowerCase().equals(decimationToken)) codaUnitMode.setDecimation(Integer.parseInt(params[i+1]));
				if(params[i].toLowerCase().equals(nbMarkersToken)) {
					codaUnitMode.setMaxMarker(Integer.parseInt(params[i+1]));
					nbMarkers = codaUnitMode.getMaxMarker();
				}
				if(params[i].toLowerCase().equals(firstMarkerIndexToken)) firstMarkerIndex = Integer.parseInt(params[i+1]);
				if(params[i].toLowerCase().equals(framesNumberToken)) framesNumber = Integer.parseInt(params[i+1]);
				if(params[i].toLowerCase().equals(autoGrabToken)) autoGrab = Boolean.parseBoolean(params[i+1]);
				if(params[i].toLowerCase().equals(simulModeToken)) simulMode = Boolean.parseBoolean(params[i+1]);
			}
			
			if(framesNumber != -1) {
				framesIDs = new int[framesNumber];
				framesTimes = new long[framesNumber];
			}
			
			codaBytesBuffer = new byte[8*nbMarkers];
			
			frame.setChannelStart(firstMarkerIndex - 1);
			frame.setNumChannels(nbMarkers);
			
			
			try {
				
				System.out.println("Coda server Connection IP : " + codaServerIP);
				System.out.println("At frameRate : " + frameRate);
				System.out.println("And decimation : " + codaUnitMode.getDecimation());
				System.out.println("With " + nbMarkers + " markers");
				System.out.println("First marker index : " + firstMarkerIndex);
				System.out.println((framesNumber == -1)?"For infinite streaming":"For " + framesNumber + " frames.");
				
				System.out.print("Connecting to server...");
				codaUnit.connect(codaServerIP);
				System.out.println(" OK");
				System.out.print("Connecting CX1 unit...");
				codaUnit.startup(null);
				System.out.println(" OK");
				codaUnit.modeSet(codaUnitMode);
				codaUnit.acqSetSaveMode(CODA_ACQ_SAVE_NONE);
				
				
			} catch (CodaError e) {
				e.printStackTrace();
			}
		}
		
		//Use Gyro
		if(UDPIXXATDataStreamer.useXSens) {
			for (int i = 0; i < params.length; i++) {
				if(params[i].toLowerCase().equals(xsensSerialkeyToken)) xsensSerialKey = params[i+1];
				if(params[i].toLowerCase().equals(xsensSampleFrequencyToken)) xsensSampleFrequency = Integer.parseInt(params[i+1]);
			}
			
			System.setProperty("jna.library.path", "./libs/");
			WString serialKey = new WString(xsensSerialKey);
			xSens = XSensLibrary.INSTANCE;
			int response = xSens.findFirstUSBDevice(serialKey);
			if(response != 0) {
				response = xSens.getDeviceID();
				System.out.println("XSens Device found at ID : " + response);
				xSens.setSampleFrequency(xsensSampleFrequency);
				response = xSens.openPort();
				if(response != 1) UDPIXXATDataStreamer.useXSens = false;
				System.out.println("XSens Device port opened : " + response);
				response = xSens.configureDevice();
				if(response != 1) UDPIXXATDataStreamer.useXSens = false;
				System.out.println("XSens Device configured : " + response);
				if(response != 1) System.out.println("WARNING : XSens Device not properly opened or configured. XSense device not used !");
			} else {
				System.out.println("Unabled to find XSens Device !");
				UDPIXXATDataStreamer.useXSens = false;
			}
			
			
		}
		
		//Use time stamp
		if(UDPIXXATDataStreamer.useTimeStamp) {
			for (int i = 0; i < params.length; i++) {
				if(params[i].toLowerCase().equals(timeStampSampleFrequencyToken)) timeStampSampleFrequency = Integer.parseInt(params[i+1]);
			}
		}
		
	}
	
	/**
	 * Use this method to add any coda observer to this streamer
	 * @param codaObserver any object that implements {@link DataObserver}.
	 */
	public void addObserver(DataObserver codaObserver) {
		observers.add(codaObserver);
	}
	
	/**
	 * Use this method to rempove any coda observer to this streamer
	 * @param codaObserver any object that implements {@link DataObserver}.
	 */
	public void removeObserver(DataObserver codaObserver) {
		observers.remove(codaObserver);
	}
	
	/**
	 * This is main thread method. It is responsible for starting acquisition
	 * and handling all data frames in order to notify any notified observer
	 */
	public void run() {
		
		int nbCodaMessageSent = 0;
		int nbXsensMessageSent = 0;
		int nbTimerMessageSent = 0;
		
		//int systemNumber = (UDPDataStreamer.useCodamotion?1:0) + (UDPDataStreamer.useXSens?1:0) + (UDPDataStreamer.useTimeStampSpecified?1:0);
		
		long t = System.nanoTime();
		int n = 0;
		
		boolean paused = false;
		try {
			
			if(UDPIXXATDataStreamer.useCodamotion) {
				if(autoGrab) codaUnit.autoGrab(true);
				codaUnit.acqPrepare();
				codaUnit.framePrepare();
				codaUnit.acqStart(Integer.MAX_VALUE);
			}
			
			if(UDPIXXATDataStreamer.useXSens) xSens.gotoMeasurement();
			
			t = System.nanoTime();
			
			while( ((n < framesNumber) || (framesNumber == -1)) && !isInterrupted()) {
				if(!pause) {
					
					if((System.currentTimeMillis() - lastTimeStampDisplay > 2000) && display) doDisplay = true;
					
					if(UDPIXXATDataStreamer.useCodamotion) {
						
						codaUnit.frameGrab();
						codaUnit.frameGetMarker(frame);
						codaFrameID = frame.getFrame();
						
						if(codaFrameID != codaLastFrameID) {
							
							codaValues = frame.getData();
							codaVisibilities = frame.getValid();
							frameID += (codaFrameID - codaLastFrameID);
							codaLastFrameID = codaFrameID;
							
							if(!simulMode)
								if((codaFrameID - codaLastFrameIDFromBufferUpdate) > 2000 ) {
									codaUnit.acqBufferUpdate();
									codaLastFrameIDFromBufferUpdate = codaFrameID;
									System.out.println("Coda Buffer updated at frame number " + n);
								}
							
							if(framesNumber != -1) {
								framesTimes[n] = System.nanoTime();
								framesIDs[n] = frameID;
							}
							
							for (int j = 0; j < nbMarkers; j++) {
								codaXValue = (short) (10*codaValues[3*j]);
								codaYValue = (short) (10*codaValues[3*j + 1]);
								codaZValue = (short) (10*codaValues[3*j + 2]);
								
								if(codaVisibilities[j] == 0) {
									System.out.println("Marker " + (j + 1) + " invisible at frame ID " + frameID);
								}
								
								codaBytesBuffer[0 + 8*j] = (byte) ( (codaSystemCode << 6) | ((codaVisibilities[j] == 0) ? (byte)(j+1) : (byte)((j+1) | 0x20)) );
								codaBytesBuffer[1 + 8*j] = frameID;
								codaBytesBuffer[2 + 8*j] = (byte) (codaXValue >> 8);
								codaBytesBuffer[3 + 8*j] = (byte) (codaXValue & 0xFF);
								codaBytesBuffer[4 + 8*j] = (byte) (codaYValue >> 8);
								codaBytesBuffer[5 + 8*j] = (byte) (codaYValue & 0xFF);
								codaBytesBuffer[6 + 8*j] = (byte) (codaZValue >> 8);
								codaBytesBuffer[7 + 8*j] = (byte) (codaZValue & 0xFF);
								
							}
							
							if(doDisplay) {
								for (int j = 0; j < nbMarkers; j++) {
									codaXValue = (short) (10*codaValues[3*j]);
									codaYValue = (short) (10*codaValues[3*j + 1]);
									codaZValue = (short) (10*codaValues[3*j + 2]);
									System.out.println("Marker " + (j + firstMarkerIndex) + " xValue : " + codaXValue);
									System.out.println("Marker " + (j + firstMarkerIndex) + " yValue : " + codaYValue);
									System.out.println("Marker " + (j + firstMarkerIndex) + " zValue : " + codaZValue);
								}
							}
							
							updateObservers(codaBytesBuffer);
							nbCodaMessageSent++;

							n++;
						}
					}
					
					if(UDPIXXATDataStreamer.useXSens) {
						if(xSens.areNewValuesAvailable() == 1) {
							
							xsensAccX = (short) (1000*xSens.getAccelX());
							xsensAccY = (short) (1000*xSens.getAccelY());
							xsensGyroZ = (short) (100*xSens.getGyroZ()*180/Math.PI);
							
							xsensBytesBuffer[0] = (byte) (xSensSystemCode << 6);
							xsensBytesBuffer[1] = (byte) (xsensAccX >> 8);;
							xsensBytesBuffer[2] = (byte) (xsensAccX & 0xFF);
							xsensBytesBuffer[3] = (byte) (xsensAccY >> 8);;
							xsensBytesBuffer[4] = (byte) (xsensAccY & 0xFF);
							xsensBytesBuffer[5] = (byte) (xsensGyroZ >> 8);;
							xsensBytesBuffer[6] = (byte) (xsensGyroZ & 0xFF);
							
							updateObservers(xsensBytesBuffer);
							nbXsensMessageSent++;
						}
						
						if(doDisplay) {
							System.out.println("XSens accel X :  " + xSens.getAccelX());
							System.out.println("XSens accel Y :  " + xSens.getAccelY());
							System.out.println("XSens gyro Z :  " + xSens.getGyroZ()*180/Math.PI);
						}
						
						if(!UDPIXXATDataStreamer.useCodamotion) n++;		
					}
					
					if(UDPIXXATDataStreamer.useTimeStamp) {
						if((System.currentTimeMillis() - lastTimeStamp) > 1000.0/timeStampSampleFrequency) {
							lastTimeStamp = System.currentTimeMillis();
							
							Calendar now = Calendar.getInstance();
							
							hours = (byte) now.get(Calendar.HOUR_OF_DAY);
							minutes = (byte) now.get(Calendar.MINUTE);
							seconds = (byte) now.get(Calendar.SECOND);
							
							timeStampBytesBuffer[0] = (byte) ((timeStampSystemCode << 6) | hours);
							timeStampBytesBuffer[1] = (byte) (minutes);
							timeStampBytesBuffer[2] = (byte) (seconds);
							
							updateObservers(timeStampBytesBuffer);
							nbTimerMessageSent++;
						}
						
						if(doDisplay) {
							System.out.println("Current time :  " + hours + "h" + minutes + "mn" + seconds + "s");
						}
						
						if(!UDPIXXATDataStreamer.useCodamotion && !UDPIXXATDataStreamer.useXSens) n++;		
					}
					//if(display) System.out.println("Total messages sent : " + (nbTimerMessageSent + nbXsensMessageSent + nbCodaMessageSent));
					//n = n - systemNumber + 2;
					
				}
				
				if(!paused && pause) {
					if(UDPIXXATDataStreamer.useCodamotion) codaUnit.acqStop();
					paused = true;
				}
				
				if(paused && !pause) {
					if(UDPIXXATDataStreamer.useCodamotion) codaUnit.acqStart(Integer.MAX_VALUE);
					paused = false;
				}
				
				if(doDisplay) {
					if(!"".equals(UDPIXXATDataStreamer.warningMessage)) System.out.println(UDPIXXATDataStreamer.warningMessage);
					doDisplay = false;
					lastTimeStampDisplay = System.currentTimeMillis();
				}
			}
			
			t = System.nanoTime() - t;
			
			if(UDPIXXATDataStreamer.useCodamotion) shutDown();
			
			if(UDPIXXATDataStreamer.useXSens) {
				xSens.closePort();
				xSens.freeAllocatedMemory();
			}
			
			
			System.out.println("Nb Coda messages sent : " + nbCodaMessageSent);
			System.out.println("Nb XSens messages sent : " + nbXsensMessageSent);
			System.out.println("Nb Timer messages sent : " + nbTimerMessageSent);
			System.out.println("Total messages sent : " + (nbTimerMessageSent + nbXsensMessageSent + nbCodaMessageSent));
			
			System.out.println("Duration (s) : " + t/1000000000.0);
			System.out.println("Nb grabbed frames : " + n);
			
			if(framesNumber != -1 && UDPIXXATDataStreamer.useCodamotion) {
				FileWriter fileWriter = new FileWriter("result" + frameRate + ".txt");
				PrintWriter file = new PrintWriter(fileWriter);
				file.println("Frame ID; deltaT");
				double d = 0;
				double max = Double.MIN_VALUE;
				double min = Double.MAX_VALUE;
				for (int i = 0; i < n; i++) {
					if(i>0) {
						double dLocal = (framesTimes[i] - framesTimes[i-1])/1000000000.0;
						max = Math.max(dLocal, max);
						min = Math.min(dLocal, min);
						d = d + dLocal;
						file.println(framesIDs[i] + ";" + dLocal);
					}
					else file.println(framesIDs[i] + ";" + 0);
				}
				file.println("Min time (s) : " + min);
				file.println("Mean time (s) : " + d/(n-1));
				file.println("Max time (s) : " + max);
				file.close();
				
				System.out.println("Coda Min time (s) : " + min);
				System.out.println("Coda Mean time (s) : " + d/(n-1));
				System.out.println("Coda Max time (s) : " + max);
			}
			
			
		} catch (CodaError e) {
			if(UDPIXXATDataStreamer.useXSens) {
				xSens.closePort();
				xSens.freeAllocatedMemory();
			}
			t = System.nanoTime() - t;
			e.printStackTrace();
			System.out.println("Duration (s) : " + t/1000000000.0);
			System.out.println("Nb grabbed frames : " + n);
			try {
				System.out.print("Stopping CX1 unit...");
				codaUnit.shutdown();
				System.out.println("OK");
				System.out.print("Disconnection from server...");
				codaUnit.disconnect();
				System.out.println("OK");
			} catch (CodaError e1) {
				e1.printStackTrace();
			}
			System.out.println("Nb Coda messages sent : " + nbCodaMessageSent);
			System.out.println("Nb XSens messages sent : " + nbXsensMessageSent);
			System.out.println("Nb Timer messages sent : " + nbTimerMessageSent);						
			System.out.println("Press ENTER to exit...");
		} catch (Exception e) {
			if(UDPIXXATDataStreamer.useXSens) {
				xSens.closePort();
				xSens.freeAllocatedMemory();
			}
			t = System.nanoTime() - t;
			e.printStackTrace();
			System.out.println("Duration (s) : " + t/1000000000.0);
			System.out.println("Nb grabbed frames : " + n);
			if(UDPIXXATDataStreamer.useCodamotion) {
				try {
					System.out.print("Stopping CX1 unit...");
					codaUnit.shutdown();
					System.out.println("OK");
					System.out.print("Disconnection from server...");
					codaUnit.disconnect();
					System.out.println("OK");
				} catch (CodaError e1) {
					e1.printStackTrace();
				}
				System.out.println("Nb Coda messages sent : " + nbCodaMessageSent);
				System.out.println("Nb XSens messages sent : " + nbXsensMessageSent);
				System.out.println("Nb Timer messages sent : " + nbTimerMessageSent);
				System.out.println("Press ENTER to exit...");
			}
		}
		
	}

	/**
	 * This method is called when it is necessary to notify obaservers
	 */
	private void updateObservers(byte[] buffer) {
		for (int i = 0; i < observers.size(); i++) {
			observers.get(i).update(buffer);
		}
	}

	public void setPause(boolean value) {
		if(pause) {
			codaLastFrameIDFromBufferUpdate = 0;
			codaLastFrameID = 0;
		}
		pause = value;
	}

	public boolean isPaused() {
		return pause;
	}
	
	public void setDisplayValues(boolean value) {
		display = value;
	}

	public boolean isValuesDisplayed() {
		return display;
	}

	public boolean performAlignment(int orginMarker, int xAxisMarker, int planeMarker) {
		try {
			codaUnit.alignmentClear();
			Align align = new Align();
			align.setAlignmentMode(Align.custom);
			align.setOriginMarker(--orginMarker);
			align.setXAxisMarker(--xAxisMarker);
			align.setPlaneMarker(--planeMarker);
			codaUnit.align(align);
			return true;
		} catch (CodaError e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean shutDown() {
		try {
			System.out.print("Stopping acquisition...");
			codaUnit.acqStop();
			System.out.println(" OK");
			System.out.print("Stopping CX1 unit...");
			codaUnit.shutdown();
			System.out.println(" OK");
			System.out.print("Disconnection from server...");
			codaUnit.disconnect();
			System.out.println(" OK");
			return true;
		} catch (CodaError e) {
			e.printStackTrace();
			return false;
		}
	}

}
