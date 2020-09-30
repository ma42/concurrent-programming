import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import client.view.ProgressItem;
import client.view.StatusWindow;
import client.view.WorklistItem;
import network.Sniffer;
import network.SnifferCallback;
import rsa.Factorizer;
import rsa.ProgressTracker;

public class CodeBreaker implements SnifferCallback {

    private final JPanel workList;
    private final JPanel progressList;
    private final JProgressBar mainProgressBar;
    private static final int MAIN_PROGRESS_VALUE_UPDATE = 1000000;
    private ExecutorService threadPool = Executors.newFixedThreadPool(2);

    // -----------------------------------------------------------------------
    
    private CodeBreaker() {
        StatusWindow w  = new StatusWindow();

        workList        = w.getWorkList();
        progressList    = w.getProgressList();
        mainProgressBar = w.getProgressBar();
        
        w.enableErrorChecks();
    }
    // -----------------------------------------------------------------------
    
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            CodeBreaker codeBreaker = new CodeBreaker();
            new Sniffer(codeBreaker).start();
        });
    }
    // -----------------------------------------------------------------------

    /** Called by a Sniffer thread when an encrypted message is obtained. */
    @Override
    public void onMessageIntercepted(String message, BigInteger n) {	
        SwingUtilities.invokeLater(() -> {
        	handleRecievedMessage(message, n);
        });
    }
    
    /** Creates WorklistItem with a button. When button is clicked, the startBreaking 
     * method is called. */
	private void handleRecievedMessage(String message, BigInteger n) {
    	WorklistItem recievedMessage = new WorklistItem(n, message);
    	
    	recievedMessage.getBreakButton().addActionListener((e) -> {
    		startBreaking(recievedMessage, message, n);	
    	});
    	workList.add(recievedMessage);
	}
	
	/** When "Break" button is clicked, a ProgressItem is created and the WorklistItem removed.
	 *  The crackMessage method is called that starts the process of cracking the message. */
	private void startBreaking(WorklistItem worklistItem, String message, BigInteger n) {
		 ProgressItem progressItem = new ProgressItem(n, message);
		 mainProgressBar.setMaximum(mainProgressBar.getMaximum() + MAIN_PROGRESS_VALUE_UPDATE);
		 
		 progressItem.getCancelButton().addActionListener((e) -> {
    		progressItem.getTask().cancel(true);
    	});
		 progressList.add(progressItem);
		 workList.remove(worklistItem);
		 handleMessageEncryption(message, n, progressItem);
	}
	
	/** Creates an anonymous runnable object that is added to the threadPool.
	 *  The ProgessItems progress bar and the main progress bar are continuously updated.*/
	private void handleMessageEncryption(String message, BigInteger n, ProgressItem progressItem) {
		
		/** Anonymous tracker class handling process updates. */  
		ProgressTracker tracker = new ProgressTracker() {
			private int totalProgress = 0;
            @Override
            public void onProgress(int ppmDelta) { 
            	totalProgress += ppmDelta; 
            	SwingUtilities.invokeLater(() -> {
                    progressItem.getProgressBar().setValue(totalProgress);
                    mainProgressBar.setValue(mainProgressBar.getValue() + ppmDelta);
                });
            }
       };
       /** Anonymous Runnable class handling updates in message encryption. */  
       Runnable crackMessage = () -> {
	        try {
				String plainText = Factorizer.crack(message, n, tracker);
				SwingUtilities.invokeLater(() -> {
					progressItem.getTextArea().setText(plainText);
					addRemoveButton(progressItem);
                });
			} catch (InterruptedException e) {
		        SwingUtilities.invokeLater(() -> {
			    	progressItem.getTextArea().setText("[cancelled]");
			    	progressItem.getProgressBar().getValue();
			    	int value = progressItem.getProgressBar().getValue();
			    	int max = progressItem.getProgressBar().getMaximum();
			    	addRemoveButton(progressItem);
			    	progressItem.getProgressBar().setValue(max);
					mainProgressBar.setValue(mainProgressBar.getValue() + max-value);
		        });
			}
       };
       progressItem.addTask(threadPool.submit(crackMessage));
	}

	private void addRemoveButton(ProgressItem progressItem) {
		progressItem.addRemoveButton().addActionListener(e -> { 
			progressList.remove(progressItem); 
	        mainProgressBar.setValue(mainProgressBar.getValue() - MAIN_PROGRESS_VALUE_UPDATE);
	        mainProgressBar.setMaximum(mainProgressBar.getMaximum() - MAIN_PROGRESS_VALUE_UPDATE);
		});
		
	}
}
