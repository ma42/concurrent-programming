package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class SpinController extends ActorThread<WashingMessage> {
	
	private final int ONE_MINUTE = 60000;
	
	private WashingIO io;
	private int spinState;
	private boolean sentAck; 
	private WashingMessage currentTask;

    public SpinController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // wait for up to a (simulated) minute for a WashingMessage
                WashingMessage m = receiveWithTimeout(ONE_MINUTE / Settings.SPEEDUP);

                // if m is null, it means a minute passed and no message was received
                if (m != null) {
                    System.out.println("SpinController got " + m);
                    currentTask = m;
                    sentAck = false;
                }
                
                if (currentTask != null) {   
                	switch (currentTask.getCommand()) {
                	
	                    case WashingMessage.SPIN_SLOW:
	                    	io.lock(true); // Asserting locked hatch before spinning, SR4.
	                        if (spinState == WashingIO.SPIN_LEFT) {
	                            spinState = WashingIO.SPIN_RIGHT;
	                        } else {
	                            spinState = WashingIO.SPIN_LEFT;
	                        }
	                        break;
	
	                    case WashingMessage.SPIN_FAST:
	                    	// Asserting centrifuging not performed with water, SR5. 
	                    	if (io.getWaterLevel() == 0) {
	                    		io.lock(true); // Asserting locked hatch before spinning, SR4.
	                    		io.drain(true);
	                    		spinState = WashingIO.SPIN_FAST;	
	                    	}
	                        break;
	
	                    case WashingMessage.SPIN_OFF:
	                    	io.drain(false);
	                        spinState = WashingIO.SPIN_IDLE;
	                        break;
                	}
	                io.setSpinMode(spinState);
	                if (!sentAck) {
	                	currentTask.getSender().send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
	                	sentAck = true; 
	                }
                }        
            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}
