package clock.io;

public class AlarmThread implements Runnable{
	private ClockData clockData;
	
	public AlarmThread(ClockData clockData) {
		this.clockData = clockData;
	}

	@Override
	public void run() {
		while(true) {
			try {
                clockData.getAlarmSem().acquire(); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
			
			clockData.soundAlarm(); 
		}
		
	}
}
