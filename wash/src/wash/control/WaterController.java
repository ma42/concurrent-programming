package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class WaterController extends ActorThread<WashingMessage> {
	private final int SECOND = 1000;
	
    private WashingIO io;
    private WashingMessage currentTask;

    public WaterController(WashingIO io) {
    	this.io = io;
    }

    @Override
    public void run() {
    	try {
    		while(true) {
    			WashingMessage m = receiveWithTimeout(2*SECOND / Settings.SPEEDUP);
    			if (m != null) {
    				System.out.println("WaterController got " + m);
    				currentTask = m;
    			}
    			
    			if (currentTask != null) {
    				switch (currentTask.getCommand()) {
	    				case WashingMessage.WATER_IDLE:
	                		io.fill(false);
	                		io.drain(false);
	                		currentTask = null;
	                		break;
	    					
	    				case WashingMessage.WATER_DRAIN:
	                		if (io.getWaterLevel() == 0) {
	                			io.drain(false);
	                			currentTask.getSender().send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
	                			currentTask = null;
	                		} else {
	                			io.drain(true);
	                		}
	                		io.fill(false);
	                		break;
	    					
	    				case WashingMessage.WATER_FILL:	
	    					if (io.getWaterLevel() < currentTask.getValue()) {
	    						io.fill(true); 		// Input valve open => asserting drain pump is off,
	    						io.drain(false);	// according to SR2. 
	    					} else {
	    						io.fill(false);
	    						io.drain(false);
	    						currentTask.getSender().send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
	    						currentTask = null;
	    					}
	    					break;
    				}
    			}
    		}
    	} catch (InterruptedException unexpected) {
			throw new Error(unexpected);
		}
    }
}
