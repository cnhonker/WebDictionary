package webdictionary;

import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author ry
 */
public class JPanelEx extends JPanel{

    public JPanelEx() {
        setPreferredSize(new Dimension(300, 300));
        add(new JButton("Click"));
    }
}
