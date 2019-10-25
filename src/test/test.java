
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 * This program demonstrates the use of a progress bar to monitor the progress
 * of a thread.
 *
 * @version 1.04 2007-08-01
 * @author Cay Horstmann
 */
public class test {

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new ProgressBarFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }
}

/**
 * A frame that contains a button to launch a simulated activity, a progress
 * bar, and a text area for the activity output.
 */
class ProgressBarFrame extends JFrame {

    private JButton startButton;
    private JProgressBar progressBar;
    private SimulatedActivity activity;
    private int current;
    private int target;

    public static final int DEFAULT_WIDTH = 400;
    public static final int DEFAULT_HEIGHT = 200;

    public ProgressBarFrame() {
        setTitle("ProgressBarTest");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        final int MAX = 50;
        JPanel panel = new JPanel();
        startButton = new JButton("Start");
        progressBar = new JProgressBar(0, MAX);
        progressBar.setStringPainted(true);
        panel.add(startButton);
        panel.add(progressBar);

        add(panel, BorderLayout.SOUTH);

        // set up the button action
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                startButton.setEnabled(false);
                activity = new SimulatedActivity(MAX);
                activity.execute();
            }
        });
    }

    class SimulatedActivity extends SwingWorker<Void, Integer> {

        public SimulatedActivity(int t) {
            current = 0;
            target = t;
        }

        protected Void doInBackground() throws Exception {
            try {
                while (current < target) {
                    Thread.sleep(100);
                    current++;
                    publish(current);
                }
            } catch (InterruptedException e) {
            }
            return null;
        }

        protected void process(List<Integer> chunks) {
            for (Integer chunk : chunks) {
                progressBar.setValue(chunk);
            }
        }

        protected void done() {
            startButton.setEnabled(true);
        }

    }
}
