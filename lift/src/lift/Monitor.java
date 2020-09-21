package lift;

import java.util.stream.*;

public class Monitor extends Thread {
	private int currentFloor = 0, load = 0, persEntering = 0, persExiting = 0; 
	private boolean blockedLift = false; 
	private boolean goingUp = false;
	private boolean doorsOpen = false;
	private int[] persWaitingEnter = new int[7];
	private int[] persWaitingExit = new int[7];
	private LiftView view;
	
	public Monitor(LiftView view) {
		this.view = view;
	}

	public synchronized void awaitEntering(int floor) {
		persWaitingEnter[floor]++;
		notifyAll();
		while (floor != currentFloor || load + persEntering - persExiting == 4 || !blockedLift || !doorsOpen) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		persEntering++;
	}

	public synchronized void awaitExiting(int floor) {
		while (floor != currentFloor) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		persExiting++;
	}
	
	public synchronized void personLeft(int destinationFloor) {
		persWaitingEnter[currentFloor]--;
		persEntering--;
		load++;
		persWaitingExit[destinationFloor]++;
		
		if (persWaitingEnter[currentFloor] == 0 && persWaitingExit[currentFloor] == 0) {
			blockedLift = false;	
		}
		
		if (load == 4 && persEntering == 0 && persExiting == 0) {
			blockedLift = false;	
		}
		notifyAll();
	}

	public synchronized void personExited() {
		persWaitingExit[currentFloor]--;
		persExiting--;
		load--;
		if ((persWaitingEnter[currentFloor] == 0 && persWaitingExit[currentFloor] == 0) || (load == 4 && persEntering == 0 && persExiting == 0)) {
			blockedLift = false;
		}
		notifyAll();
	}

	public synchronized void awaitMoveLift() {
		while (blockedLift || (IntStream.of(persWaitingEnter).sum() == 0 && IntStream.of(persWaitingExit).sum() == 0)) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public int getCurrentFloor() {
		return currentFloor;
	}
	
	public int getNextFloor() {
		if (currentFloor == 0 || currentFloor == 6) {
			goingUp = !goingUp;
		}
		if (goingUp) {
			return currentFloor + 1;
		} else {
			return currentFloor- 1;
		}
	}
	
	public boolean doorsOpen() {
		return doorsOpen;
	}
	
	public synchronized void toggleDoors() {
		doorsOpen = !doorsOpen;
	}

	public synchronized void updateFloor(int newFloor) {
		currentFloor = newFloor;
		if ((persWaitingEnter[currentFloor] != 0 && load != 4) || persWaitingExit[currentFloor] != 0) {
			blockedLift = true;
			view.openDoors(newFloor);
			doorsOpen = true;
		}
		notifyAll();
	}
}