package factory.controller;

import factory.model.DigitalSignal;
import factory.model.WidgetKind;
import factory.simulation.Painter;
import factory.simulation.Press;
import factory.swingview.Factory;

/**
 * Implementation of the ToolController interface,
 * to be used for the Widget Factory lab.
 * 
 * @see ToolController
 */
public class LabToolController implements ToolController {
    private final DigitalSignal conveyor, press, paint;
    private final long pressingMillis, paintingMillis;
    private boolean isPressing = false;
    private boolean isPainting = false;
    
    public LabToolController(DigitalSignal conveyor, DigitalSignal press, DigitalSignal paint, long pressingMillis, long paintingMillis) {
        this.conveyor = conveyor;
        this.press = press;
        this.paint = paint;
        this.pressingMillis = pressingMillis;
        this.paintingMillis = paintingMillis;
    }

    @Override
    public synchronized void onPressSensorHigh(WidgetKind widgetKind) throws InterruptedException {
        if (widgetKind == WidgetKind.BLUE_RECTANGULAR_WIDGET) {
        	isPressing = true;
        	stopConveyor();
            press.on();
            waitOutside(pressingMillis);
            press.off();
            waitOutside(pressingMillis);   // press needs this time to retract
            isPressing = false;
            startConveyor();
        }
    }

    @Override
    public synchronized void onPaintSensorHigh(WidgetKind widgetKind) throws InterruptedException {
        if (widgetKind == WidgetKind.ORANGE_ROUND_WIDGET) {
        	isPainting = true;
        	stopConveyor();
            paint.on();
            waitOutside(paintingMillis);
            paint.off();
            isPainting = false;
            startConveyor();
        }
    }
    
	private void startConveyor() {
		if (!isPainting && !isPressing) {
			conveyor.on();
		}
	}

	private void stopConveyor() {
			conveyor.off();
	}
    
	/** Helper method: sleep outside of monitor for ’millis’ milliseconds. */
    private void waitOutside(long millis) throws InterruptedException {
	    long timeToWakeUp = System.currentTimeMillis() + millis;
	    
	    while (System.currentTimeMillis() < timeToWakeUp) {
	    	long dt = timeToWakeUp - System.currentTimeMillis();
	    	wait(dt);
	    }
    }
    
    // -----------------------------------------------------------------------
    
    public static void main(String[] args) {
        Factory factory = new Factory();
        ToolController toolController = new LabToolController(factory.getConveyor(),
                                                              factory.getPress(),
                                                              factory.getPaint(),
                                                              Press.PRESSING_MILLIS,
                                                              Painter.PAINTING_MILLIS);
        factory.startSimulation(toolController);
    }
}
