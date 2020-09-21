package lift;

import lift.LiftView;

public class LiftThread extends Thread {
	private Monitor monitor;
	private LiftView view;
	private int currentFloor;
	private int nextFloor;

	public LiftThread(LiftView view, Monitor monitor) {
		this.monitor = monitor;
		this.view = view;
	}

	public void run() {
		while (true) {
			monitor.awaitMoveLift();
			currentFloor = monitor.getCurrentFloor();
			nextFloor = monitor.getNextFloor();
			moveElevator();
		}
	}
	
	private void moveElevator() {
		if (monitor.doorsOpen()) {
			view.closeDoors();
			monitor.toggleDoors();
		}
		view.moveLift(currentFloor, nextFloor);
		monitor.updateFloor(nextFloor);	
	}
}