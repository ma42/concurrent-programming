import clock.ClockData;
import clock.io.ClockInput;
import clock.io.ClockInput.UserInput;
import clock.io.ClockOutput;
import clock.TimeThread;
import clock.AlarmThread;
import java.util.concurrent.Semaphore;


public class ClockMain {

    public static void main(String[] args) throws InterruptedException {
    	ClockData clockData = new ClockData();
    	
        ClockInput  in  = clockData.getInput();
        ClockOutput out = clockData.getOutput();
        
        clockData.setTime(00, 00, 00);
        
        Semaphore sem = in.getSemaphore();

        Thread timeThread = new Thread(new TimeThread(clockData));
        Thread alarmThread = new Thread(new AlarmThread(clockData));
        timeThread.start();
        alarmThread.start();
        
        
        while (true) {
        	sem.acquire();
        	
            UserInput userInput = in.getUserInput();
            int choice = userInput.getChoice();
            
            int h = userInput.getHours();
            int m = userInput.getMinutes();
            int s = userInput.getSeconds();
      
            
            switch (choice) {
            	case 1: clockData.setTime(h, m, s);
            		break;
            	case 2: clockData.setAlarm(h, m, s);
                	break;
            	case 3: clockData.toggleAlarm();
                	break;
        }

//            System.out.println("choice=" + choice + " h=" + h + " m=" + m + " s=" + s);
        }
    }
}
