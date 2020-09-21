package lift;

import java.util.Random;
import lift.Passenger;

public class PersonThread extends Thread {
	private Passenger passenger;
	private Monitor monitor;
	private Random r = new Random();
	private int startFloor;
	private int destiFloor;
	
	public PersonThread(Passenger passenger, Monitor monitor) {
		this.monitor = monitor;
		this.passenger = passenger;		
	}

	public void run() {
		try {
			sleep(1000 * r.nextInt(46));
		} catch (InterruptedException e) { 
			e.printStackTrace();
		}
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