package lift;

import java.util.Random;
import lift.Passenger;

public class PersonThread extends Thread {
	private Passenger passenger;
	private Monitor monitor;
	private LiftView view;
	private Random r;
	private int startFloor;
	private int destiFloor;
	
	public PersonThread(LiftView view, Monitor monitor) {
		this.monitor = monitor;
		this.view = view;		
	}

	public void run() {
		while(true) { 
			try {
				r = new Random();
				sleep(1000 * r.nextInt(46));
			} catch (InterruptedException e) { 
				e.printStackTrace();
			}
			passenger = view.createPassenger();
			startFloor = passenger.getStartFloor();
			destiFloor = passenger.getDestinationFloor();
			
			passenger.begin();
			monitor.awaitEntering(startFloor);
			passenger.enterLift();
			monitor.personLeft(destiFloor);
			monitor.awaitExiting(destiFloor);
			passenger.exitLift();
			monitor.personExited();
			passenger.end();	
		}
	}
}