package webdictionary;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 *
 * @author ry
 */
public class WebDictionary {

    public static void main(String[] args) {
        //Master
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                createAndShowGui();
            }
        });
    }

    public static void createAndShowGui() {
        JFrame frame = new JFrame("dict.org");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(350, 300));
        final JPanelEx panel = new JPanelEx();
        frame.getContentPane().add(panel);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
               panel.terminate();
            }        
        });
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
