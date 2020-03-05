package udpdatastreamer;

import datastreamer.DataStreamer;

/**
 * Any object that wants to be notified of any
 * coda frame values must implement this method and 
 * register itself via {@link DataStreamer#addObserver(DataObserver)}
 * @author centricoda
 */
public interface DataObserver {
	public void update(byte[] bytesBuffer);
}
