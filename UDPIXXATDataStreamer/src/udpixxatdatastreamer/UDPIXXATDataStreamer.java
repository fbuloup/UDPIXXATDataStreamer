package udpixxatdatastreamer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import datastreamer.DataStreamer;
import de.ixxat.vci3.IVciDevice;
import de.ixxat.vci3.IVciDeviceManager;
import de.ixxat.vci3.IVciEnumDevice;
import de.ixxat.vci3.VciDeviceInfo;
import de.ixxat.vci3.VciServer;
import de.ixxat.vci3.bal.IBalObject;
import de.ixxat.vci3.bal.IBalResource;
import de.ixxat.vci3.bal.can.CanBitrate;
import de.ixxat.vci3.bal.can.CanMessage;
import de.ixxat.vci3.bal.can.ICanChannel;
import de.ixxat.vci3.bal.can.ICanControl;
import de.ixxat.vci3.bal.can.ICanMessageWriter;
import de.ixxat.vci3.bal.can.ICanSocket;


/**
 * This class is responsible for sending frames values, coming from
 * {@link DataStreamer}, via Ethernet to any UDP client.
 * @author centricoda
 *
 */
public class UDPIXXATDataStreamer implements DataObserver {
	
	private static int freeCount;
	public static String warningMessage;
	
	private final static String ixxatstreamerToken = "-ixxatstreamer";
	private final static String useCodaToken = "-usecoda";
	private final static String useXSensToken = "-usexsens";
	private final static String useTimeStampToken = "-usetimestamp";
	
	private final static String udpClientIPToken = "-udpclientip";
	private final static String udpSourcePortToken = "-udpsourceport";
	private final static String udpDestinationPortToken = "-udpdestinationport";
	private final static String doAlignmentToken = "-doalignment";
	
	private static String udpClientIP = "localhost";
	private static int udpSourcePort = 15000;
	private static int udpDestinationPort = Integer.MIN_VALUE;
	
	private static DatagramSocket dgSocket;
	private static InetAddress clientIP;
	
	public static boolean ixxatstreamer = false;
	public static boolean useCodamotion = false, useCodamotionSpecified = false, doAlignment = true;
	public static boolean useXSens = false, useXSensSpecified = false;;
	public static boolean useTimeStamp = false, useTimeStampSpecified = false;
	
	/**
	 * Command line arguments : it may or not contain following parameters.<br><br>
	 * For codamotion system :
	 * <ul>
	 * <li>-ixxatstreamer : default false. If false, will use UDP streamer. Valid values are true or false</li>
	 * <li>-usecoda : default false. Valid values are true or false</li> 
	 * <li>-codaserverip : default localhost. IP where CX1Server is running </li>                    
	 * <li>-framerate : default 100. Valid values : 100, 120, 200, 400 or 800 any other values will result to 100</li>                                   
	 * <li>-decimation : default 1</li>                                                
	 * <li>-nbmarkers : default 1</li>                                                
	 * <li>-firstmarkerindex : default 1</li>                                    
	 * <li>-updclientip : defaut localhost</li>                
	 * <li>-udpsourceport : default 15000</li>        
	 * <li>-udpdestinationport : default udpsourceport</li>
	 * <li>-framesNumber : default -1. Number of frames to send before stopping streaming. Specifying -1 means infinite streaming.</li>
	 * <li>-autograb : default true. Use or not autograd frame. Do not use autograb with simulmode</li>
	 * <li>-simulmode : default false. Use or not simulation mode (CodaServerSimulate).</li>
	 * <li>-doalignment : default true. Do you want to do an alignment ?</li>
	 * </ul>
	 * For instance :
	 * -codaServerIP 192.168.0.3 -frameRate 800 -decimation 1 -nbMarkers 1 -firstMarkerIndex 1 -udpclientip 192.168.0.30
	 * <br><br>
	 * For gyroscope :
	 * <ul>
	 * <li>-usexsens : default false. Valid values are true or false</li>          
	 * <li>-xsensserialkey : default "NE3B-79EA-K6RP-WDNH-QA2Y"</li>                    
	 * <li>-xsenssamplefrequency : default 100. Valid values are integers.</li>                                   
	 * </ul>
	 * For instance :
	 * -usexsens true -xsensserialkey NE3B-79EA-K6RP-WDNH-QA2Y -xsenssamplefrequency 1000
	 *  <br>
	 *  <br>
	 * For time stamp :
	 * <ul>
	 * <li>-usetimestamp : default false. Valid values are true or false</li>    
	 * <li>-timestampsamplefrequency : default 1. Valid values are integer</li> 
	 * </ul>
	 * For instance :
	 * -usetimestamp true
	 *  <br><br>
	 * @param params list of parameters configuration. See above.
	 */
	public static void main(String[] params) {
		
		System.out.println("OS Arch. :" + System.getProperty("sun.arch.data.model"));  
		
		for (int i = 0; i < params.length; i++) {
			if(params[i].toLowerCase().equals(ixxatstreamerToken)) {
				ixxatstreamer = Boolean.parseBoolean(params[i+1]);
			}
			if(params[i].toLowerCase().equals(useCodaToken)) {
				useCodamotion = Boolean.parseBoolean(params[i+1]);
				useCodamotionSpecified = true;
			}
			if(params[i].toLowerCase().equals(useXSensToken)) {
				useXSens = Boolean.parseBoolean(params[i+1]);
				useXSensSpecified = true;
			}
			if(params[i].toLowerCase().equals(useTimeStampToken)) {
				useTimeStamp = Boolean.parseBoolean(params[i+1]);
				useTimeStampSpecified = true;
			}
			
			if(params[i].toLowerCase().equals(doAlignmentToken)) {
				doAlignment = Boolean.parseBoolean(params[i+1]);
			}
			
		}
		
		if(!useCodamotion && !useXSens && !useTimeStamp) {
			System.out.println("Nothing to stream ! Bye bye !");
			return;
		}
		
		
		try {
			
			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(System.in));
			String response;
			
			if(!useCodamotionSpecified) {
				System.out.println("Do you want to use Codamotion System (y/Y) ? ");
				response = bufferedReader.readLine();
				if(response.equalsIgnoreCase("y")) useCodamotion = true;
			}
			
			if(!useXSensSpecified) {
				System.out.println("Do you want to use XSens Gyroscope System (y/Y) ? ");
				response = bufferedReader.readLine();
				if(response.equalsIgnoreCase("y")) useXSens = true;
			}
			
			if(!useTimeStampSpecified) {
				System.out.println("Do you want to use time stamp server (y/Y) ? ");
				response = bufferedReader.readLine();
				if(response.equalsIgnoreCase("y")) useTimeStamp = true;
			}
			
			DataStreamer dataStreamer = new DataStreamer(params);
			UDPIXXATDataStreamer canBridgeStreamer = new UDPIXXATDataStreamer(params);
			dataStreamer.addObserver(canBridgeStreamer);
			
			if(useCodamotion) {
				if(doAlignment) {
					System.out.println("Do you want to do an alignment (y/Y) ? ");
					response = bufferedReader.readLine();
					if(response.equalsIgnoreCase("y")) {
						System.out.println("Enter origin marker number ? ");
						response = bufferedReader.readLine();
						int orginMarker = Integer.parseInt(response);
						System.out.println("Enter x axis marker number ? ");
						response = bufferedReader.readLine();
						int xAxisMarker = Integer.parseInt(response);
						System.out.println("Enter x/y or x/z plane marker number ? ");
						response = bufferedReader.readLine();
						int planeMarker = Integer.parseInt(response);
						if(!dataStreamer.performAlignment(orginMarker, xAxisMarker, planeMarker)) {
							System.out.println("Error while performing CODA Alignment !");
							dataStreamer.shutDown();
							return;
						}
					}
				}
			}
				
//			System.out.println(response);
			System.out.println("Press ENTER to start streaming...");
			System.out.println("Then Press 'S' + ENTER to stop streaming...");
			System.out.println("Or Press 'P' + ENTER to pause streaming...");
			System.out.println("Or Press 'D' + ENTER to toggle markers values display...");
			bufferedReader.readLine();
			dataStreamer.start();
			boolean stop = false;
			while(!stop) {
				String value = bufferedReader.readLine();
				System.out.println(value);
				if(value.equalsIgnoreCase("P") || value.equalsIgnoreCase("R")) {
					dataStreamer.setPause(!dataStreamer.isPaused());
					if(dataStreamer.isPaused()) {
						System.out.println("System is in paused mode");
						System.out.println("Press 'P' or 'R' + ENTER to restart");
						System.out.println("Press 'S' + ENTER to stop");
					}
					if(!dataStreamer.isPaused()) {
						System.out.println("System is in streaming mode");
					}
				}
				if(value.equalsIgnoreCase("D")) {
					dataStreamer.setDisplayValues(!dataStreamer.isValuesDisplayed());
				}
				if(value.equalsIgnoreCase("S")) {
					dataStreamer.interrupt();
					stop = true;
				}
			}
			
			canBridgeStreamer.dispose();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		/*CodaStreamer codaStreamer = new CodaStreamer(args);
		UDPDataStreamer ethernetToCANBridgeStreamer = new UDPDataStreamer(args);
		codaStreamer.addObserver(ethernetToCANBridgeStreamer);
		try {
			System.out.println("Do you want to do an alignment (y/Y) ? ");
			String response = bufferedReader.readLine();
			if(response.equalsIgnoreCase("y")) {
				System.out.println("Enter origin marker number ? ");
				response = bufferedReader.readLine();
				int orginMarker = Integer.parseInt(response);
				System.out.println("Enter x axis marker number ? ");
				response = bufferedReader.readLine();
				int xAxisMarker = Integer.parseInt(response);
				System.out.println("Enter x/y or x/z plane marker number ? ");
				response = bufferedReader.readLine();
				int planeMarker = Integer.parseInt(response);
				if(!codaStreamer.performAlignment(orginMarker, xAxisMarker, planeMarker)) {
					System.out.println("Error while performing CODA Alignment !");
					codaStreamer.shutDown();
					return;
				}
			}
			
			System.out.println(response);
			System.out.println("Press ENTER to start streaming...");
			System.out.println("Then Press 'S' + ENTER to stop streaming...");
			System.out.println("Or Press 'P' + ENTER to pause streaming...");
			System.out.println("Or Press 'D' + ENTER to toggle markers values display...");
			bufferedReader.readLine();
			codaStreamer.start();
			boolean stop = false;
			while(!stop) {
				String value = bufferedReader.readLine();
				System.out.println(value);
				if(value.equalsIgnoreCase("P") || value.equalsIgnoreCase("R")) {
					codaStreamer.setPause(!codaStreamer.isPaused());
					if(codaStreamer.isPaused()) {
						System.out.println("CodaUnit is in paused mode");
						System.out.println("Press 'P' or 'R' + ENTER to restart");
						System.out.println("Press 'S' + ENTER to stop");
					}
					if(!codaStreamer.isPaused()) {
						System.out.println("CodaUnit is in streaming mode");
					}
				}
				if(value.equalsIgnoreCase("D")) {
					codaStreamer.setDisplayMarkersValues(!codaStreamer.isMarkersValuesDisplayed());
				}
				if(value.equalsIgnoreCase("S")) {
					codaStreamer.interrupt();
					stop = true;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
	}

	private VciServer vciServer;
	private IVciDeviceManager vciDeviceManager;
	private IVciEnumDevice vciEnumDevices;
	private VciDeviceInfo vciDeviceInfo;
	private IVciDevice vciDevice;
	private IBalObject busAccessLayer;
	private ICanControl canControl;
	private ICanSocket canSocket;
	private ICanChannel canChannel;
	private ICanMessageWriter canMessageWriter;
	private CanMessage canMessage;
	
	/**
	 * See {@link #main(String[])} for details.
	 * @param args list of parameters configuration.
	 */
	public UDPIXXATDataStreamer(String[] args) {
		if(!ixxatstreamer) {
			try {
				for (int i = 0; i < args.length; i++) {
					if(args[i].toLowerCase().equals(udpClientIPToken)) udpClientIP = args[i+1];
					if(args[i].toLowerCase().equals(udpSourcePortToken)) udpSourcePort = Integer.parseInt(args[i+1]);
					if(args[i].toLowerCase().equals(udpDestinationPortToken)) udpDestinationPort = Integer.parseInt(args[i+1]);
				}
				if(udpDestinationPort == Integer.MIN_VALUE) udpDestinationPort = udpSourcePort;
				dgSocket = new DatagramSocket(udpSourcePort);
				clientIP = InetAddress.getByName(udpClientIP);
				System.out.println("UDP Streaming configured to client IP " + udpClientIP + " from port " + udpSourcePort + " to port " + udpDestinationPort);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		} else {
			
			try {
				vciServer = new VciServer();
				vciDeviceManager = vciServer.GetDeviceManager();
				vciEnumDevices = vciDeviceManager.EnumDevices();
				vciDeviceInfo = vciEnumDevices.Next();
				vciDevice = vciDeviceManager.OpenDevice(vciDeviceInfo.m_qwVciObjectId);
				busAccessLayer = vciDevice.OpenBusAccessLayer();
				canControl = (ICanControl)busAccessLayer.OpenSocket(0, IBalResource.IID_ICanControl);
				canSocket = (ICanSocket)busAccessLayer.OpenSocket(0, IBalResource.IID_ICanSocket);
				canChannel = canSocket.CreateChannel(false);
				canChannel.Initialize((short)1, (short)1);
				canChannel.Activate();
				canMessageWriter = canChannel.GetMessageWriter();
				canControl.InitLine(ICanControl.CAN_OPMODE_STANDARD, new CanBitrate(CanBitrate.Cia1000KBit));
				canControl.SetAccFilter(ICanControl.CAN_FILTER_STD, 2, 0x0FFF);
				canControl.StartLine();
				canMessage = new CanMessage();
				canMessage.m_dwIdentifier = 2;
				canMessage.m_dwTimestamp = 0;
				canMessage.m_fExtendedFrameFormat = false;
				canMessage.m_fRemoteTransmissionRequest = false;
				canMessage.m_fSelfReception = false;
				canMessage.m_bDataLength = (byte)8;
			} catch (Throwable e) {
				e.printStackTrace();
			}
			
		}
		
	}

	public void dispose() {
		if(ixxatstreamer) {
			try {
				canControl.StopLine();
				canControl.ResetLine();
				canMessageWriter.Dispose();
				canSocket.Dispose();
				canControl.Dispose();
				busAccessLayer.Dispose();
				vciDevice.Dispose();
				vciEnumDevices.Dispose();
				vciDeviceManager.Dispose();
				vciServer.Dispose();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Method called when new values are available. It sends these values to any UDP client
	 * or to IXXAT CAN/USB bridge.
	 * @param bytesBuffer values to send to the bridge : an array of bytes values
	 */
	public void update(byte[] bytesBuffer) {
		warningMessage = "";
		if(!ixxatstreamer) {
			try {
				DatagramPacket dgPacket = new DatagramPacket(bytesBuffer, bytesBuffer.length, clientIP, udpDestinationPort);
				dgSocket.send(dgPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				byte[] message = new byte[] {0, 0, 0, 0, 0, 0, 0, 0};
				freeCount = canMessageWriter.GetFreeCount();
				if(bytesBuffer.length >= 8) {
					int n = 0;
					boolean writeMessage = true;
					while(writeMessage) {
						System.arraycopy(bytesBuffer, n, message, 0, 8);
						canMessage.m_abData = message;
						canMessageWriter.WriteMessage(canMessage);
						n += 8;
						writeMessage = n < bytesBuffer.length;
					}
				} else {
					if(canMessageWriter.GetFillCount() < canMessageWriter.GetCapacity()) {
						System.arraycopy(bytesBuffer, 0, message, 0, bytesBuffer.length);
						canMessage.m_abData = message;
						canMessageWriter.WriteMessage(canMessage);
					} else {
						warningMessage = "ERROR : can message writer fifo full. Message not sent.";
						System.out.println("Fifo buffer filled : " + canMessageWriter.GetFillCount());
						System.out.println("Fifo buffer free : " + canMessageWriter.GetFreeCount());
						System.out.println("Fifo capacity : " + canMessageWriter.GetCapacity());
					}
					
				}
			} catch (Throwable e) {
				if(e instanceof UnsatisfiedLinkError) {
					if(e.getMessage().equals("VciFormatErrorA") && freeCount == 0) {
						warningMessage = "WARNING : ADWin PRO CAN interface has never been opened !";
					}
				} else e.printStackTrace();
			}
		}
		
	}

}
