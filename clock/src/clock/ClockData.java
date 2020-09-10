package clock;

import clock.AlarmClockEmulator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import clock.io.ClockInput;
import clock.io.ClockOutput;

public class ClockData {
	private AlarmClockEmulator emulator;
	private ClockInput in;
	private ClockOutput out;
	private int hAlarm = 0;
	private int mAlarm = 0;
	private int sAlarm = 0;
	private int hTime = 0;
	private int mTime = 0;
	private int sTime = 0;
	private boolean alarmSet;
	public boolean alarmActive;
	private Semaphore activateAlarm;
	private final int MAX_NBR_BEEPS = 20;
	private final Lock mutexLock;
	
	public ClockData() {
		this.emulator = new AlarmClockEmulator();
		this.in = emulator.getInput();
		this.out = emulator.getOutput();
		this.alarmSet = false;
		this.alarmActive = false;
		this.mutexLock = new ReentrantLock(); 
		this.activateAlarm = new Semaphore(0);
	}
	
	public ClockInput getInput() {
		return in;
	}
	
	public ClockOutput getOutput() {
		return out;
	}
	
	public Semaphore getAlarmSem() {
		return activateAlarm;
	}
	
	public void setTime(int hh, int mm, int ss) {
		mutexLock.lock();
		hTime = hh;
		mTime = mm;
		sTime = ss;
		out.displayTime(hTime, mTime, sTime);
		mutexLock.unlock();
	}
	
	private int[] incrementTime(int hh, int mm, int ss) {
		int seconds = hh * 3600 + mm * 60 + ss; 
		seconds++;
		int[] time = { (seconds / 3600) , ((seconds / 60) % 60) , (seconds % 60) };	
		return time;
	}
	
    public void clockTick() {
        mutexLock.lock();
        int[] time = incrementTime(hTime, mTime, sTime);
        hTime = time[0];
        mTime = time[1];
        sTime = time[2];
        mutexLock.unlock();
        
        out.displayTime(hTime, mTime, sTime);
    }
    
    public void setAlarm(int hh, int mm, int ss) {	
        mutexLock.lock();
        hAlarm = hh;
        mAlarm = mm;
        sAlarm = ss;
    	alarmSet = true; 
        mutexLock.unlock();
 
        out.setAlarmIndicator(true);
    }
    
    public void checkActivateAlarm() {
    	if (alarmSet && (hTime * 3600 + mTime * 60 + sTime) >= (hAlarm * 3600 + mAlarm * 60 + sAlarm)) { 
    		activateAlarm.release();
    	}
    }
    
    public void soundAlarm() {
        out.alarm();
        mutexLock.lock();
        
        if ((hAlarm * 3600 + mAlarm * 60 + sAlarm + MAX_NBR_BEEPS) <= (hTime * 3600 + mTime * 60 + sTime)) {
            alarmSet = false;
            out.setAlarmIndicator(false);
        }

        mutexLock.unlock();
    }
    
    public void toggleAlarm() {
        mutexLock.lock();
        alarmSet = !alarmSet;
        mutexLock.unlock();
        out.setAlarmIndicator(alarmSet);
    }
	
	
	
}
