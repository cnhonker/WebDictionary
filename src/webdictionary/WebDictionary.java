package webdictionary;

import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author ry
 */
public class WebDictionary {

    public static void main(String[] args) {
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
        frame.getContentPane().add(new JPanel() {

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 300);
            }
        });
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
