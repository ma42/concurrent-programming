package train.simulation;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import train.model.Segment;

public class TrainMonitor {
	private Set<Segment> occupiedSegments = new HashSet<>();
	private Lock mutex = new ReentrantLock();

	public TrainMonitor() {}
	
	public synchronized void enterSegment(Segment s) throws InterruptedException {
		while (occupiedSegments.contains(s)) {
			wait();
		}
		mutex.lock();
		occupiedSegments.add(s);
		s.enter();
		mutex.unlock();
		notifyAll();
	}
	
	public void exitSegment(Segment s) {
		mutex.lock();
		occupiedSegments.remove(s);
		s.exit();
		mutex.unlock();
	}
	
	
}
