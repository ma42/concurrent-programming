package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {

	private final double TEMPERATURE_MARGIN = 2.0;
	private final int TO_MILLI = 1000;
	private final int dt = 10;
	private final double M_U = (dt * 0.0478) - 0.2;
	private final double M_L = (dt * 0.000952) + 0.2;
	
	private WashingIO io; 
	private ActorThread<WashingMessage> sendAck; 
	boolean stopHeating = false; 
	private WashingMessage currentTask = null; 
	
    public TemperatureController(WashingIO io) {
    	this.io = io;
    }

    @Override
    public void run() {
    	try {
        	while(true) {
                WashingMessage m = receiveWithTimeout(dt * TO_MILLI / Settings.SPEEDUP);
                if (m != null) {
                	System.out.println("TempController got " + m);
                	currentTask = m;
                	sendAck = m.getSender();
                }
                
                if (currentTask != null) {

	                switch (currentTask.getCommand()) {
	                case WashingMessage.TEMP_IDLE: 
	                	io.heat(false);
		                if (sendAck != null) {
		                	sendAck.send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
							sendAck = null;
		                }
	                	break;
	                	
	                case WashingMessage.TEMP_SET:
	                	if (safeToHeat()) {
	                		io.heat(true);
	                	}
	                	else {
	                		io.heat(false);
	                	}
	                	break;
	                }
                }
        	}
    	} catch (InterruptedException unexpected) {
			throw new Error(unexpected);
		}
    }
    private boolean safeToHeat() {
    	double tempSet = currentTask.getValue();
    	double currentTemp = io.getTemperature();
    	boolean isWater = io.getWaterLevel() != 0; // Asserting water in machine before heating, SR1. 
    	
    	boolean reachedLowerBound = currentTemp <= tempSet - TEMPERATURE_MARGIN + M_L;
    	boolean underUpperBound = currentTemp < tempSet - M_U - 1;
    	boolean reachedUpperBound = currentTemp >= tempSet - M_U - 1;
    	
    	if (isWater && underUpperBound && !stopHeating) {
    		return true;
    	}
    	else if (isWater && reachedUpperBound) {
    		stopHeating = true; 
            if (sendAck != null) {
            	sendAck.send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
				sendAck = null;
            }
    		return false; 
    	}
    	else if (isWater && reachedLowerBound) {
    		stopHeating = false; 
    		return true;
    	}
    	return false; 
    }
}