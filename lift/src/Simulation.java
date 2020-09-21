import lift.LiftView;
import lift.Monitor;
import lift.PersonThread;
import lift.LiftThread;

public class Simulation {
	private static final int NBR_PERSONS = 20;
	
	public static void main(String[] args) {
        LiftView view = new LiftView();
        Monitor monitor = new Monitor(view);

        PersonThread[] persons = new PersonThread[NBR_PERSONS];

        for(int i = 0; i < NBR_PERSONS; i++) {
        	PersonThread person = new PersonThread(view.createPassenger(), monitor);
            persons[i] = person;
            person.start();
        }
        
        LiftThread lift = new LiftThread(view, monitor);
        lift.start();
        
        while(true) {
        	for(int i = 0; i < NBR_PERSONS; i++) {
        		if(!persons[i].isAlive()) {
            		persons[i] = new PersonThread(view.createPassenger(), monitor);
            		persons[i].start();
        		}
        	}
        }
    }
}
