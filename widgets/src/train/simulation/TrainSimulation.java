package train.simulation;

import train.view.TrainView;

public class TrainSimulation {
	
    private static final int NBR_TRAINS = 20;

    public static void main(String[] args) {

        TrainView view = new TrainView();
        TrainMonitor monitor = new TrainMonitor();
        
        for(int i = 0; i < NBR_TRAINS; i++) {
        	Thread train = new Thread(new Train(view, monitor));
        	train.start();
        }
    }
}
