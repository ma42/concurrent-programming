package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class WashingProgram1 extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;
    
    public WashingProgram1(WashingIO io,
                           ActorThread<WashingMessage> temp,
                           ActorThread<WashingMessage> water,
                           ActorThread<WashingMessage> spin) 
    {
        this.io = io;
        this.temp = temp;
        this.water = water;
        this.spin = spin;
    }
    
    @Override
    public void run() {
        try {
            System.out.println("washing program 1 started");
            // Lock the hatch
            io.lock(true);
            // Let water into the machine 
            water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10));
            receive();
            water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));
            
            // Heat water to 40 degrees
            temp.send(new WashingMessage(this, WashingMessage.TEMP_SET, 40));
            receive();
            // When water is heated spinning starts
            spin.send(new WashingMessage(this, WashingMessage.SPIN_SLOW));
            receive();
            
            // Spin for 30 simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(30 * 60000 / Settings.SPEEDUP);
            
            // Drain water and stop controlling temperature
            water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
            receive();
            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
            receive();
            
            // Rinse 5 times 2 minutes in cold water
            for (int i=0; i < 5; i++) {
            	water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10));
            	receive();
            	sleep(2 * 60 * 1000 / Settings.SPEEDUP);
            	water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
                receive();
            }
            // Centrifuge for 5 minutes
            spin.send(new WashingMessage(this, WashingMessage.SPIN_FAST));
            receive();
            sleep(5 * 60 * 1000 / Settings.SPEEDUP);
             
            // Instruct SpinController to stop spin barrel spin.
            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
            receive();
            
            // Asserting machine is empty of water.
        	water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
            WashingMessage noWaterAck = receive();
            
            // Checking so machine is empty of water before opening hatch, SR3. 
            if (noWaterAck.getCommand() == WashingMessage.ACKNOWLEDGMENT && noWaterAck.getSender() == water) {
            	io.lock(false);	
            	System.out.println("Hatch is now open");
            }
            
            System.out.println("--- Washing program 1 finished sucessfully ---");
            
        } catch (InterruptedException e) {
            
            // If we end up here, it means the program was interrupt()'ed:
            // set all controllers to idle

            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
            water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));
            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
            System.out.println("Washing program terminated");
        }
    }
}
