package optitrack;

public final class OptitrackData {

	public static int frameID;
	public static int lastFrameID;
	public static UnlabeledMarker[] unlabeledMarkers;
	
	public static void initMarkers(int optitrackNbUnlabeledMarkers) {
		OptitrackData.unlabeledMarkers = new UnlabeledMarker[optitrackNbUnlabeledMarkers];
		for (int i = 0; i < unlabeledMarkers.length; i++) {
			unlabeledMarkers[i] = new UnlabeledMarker();
		}
		
	}

}
