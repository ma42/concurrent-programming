package clock;

public class TimeThread implements Runnable {
	private ClockData clockData;
	private int TICK_INTERVAL = 1000;
	private long prevTime;
	private long currentTime;
	private long dt; 

	
	public TimeThread (ClockData clockData) {
		this.clockData = clockData;
	}

	@Override
	public void run() {
		prevTime = System.currentTimeMillis();
		while(true) {
			currentTime = System.currentTimeMillis();
			dt = currentTime - prevTime;
			
			if(dt >= TICK_INTERVAL) {
				clockData.clockTick();
				prevTime = currentTime;
				
				clockData.checkActivateAlarm();
				
				try {
					Thread.sleep(1500 - dt);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	
	
}
