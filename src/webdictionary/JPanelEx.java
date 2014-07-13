package webdictionary;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author ry
 */
public class JPanelEx extends JPanel {

    private static final SocketAddress ADDRESS = new InetSocketAddress("dict.org", 2628);
    private static final int TIMEOUT = 15000;
    private final Socket soc;
    private final JTextField inputField;
    private final JButton submit;
    private final JTextArea displayArea;
    private BufferedWriter bw;
    private BufferedReader br;

    public JPanelEx() {
        setLayout(new GridBagLayout());
        Dimension d = new Dimension(350, 300);
        setPreferredSize(d);
        inputField = new JTextField();
        submit = new JButton(getSubmitAction());
        displayArea = new JTextArea();
        soc = new Socket();
        init();
        addToPanel();
    }

    private void init() {
        inputField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    submit.doClick();
                }
            }
        });
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem(new AbstractAction("Clear") {

            @Override
            public void actionPerformed(ActionEvent e) {
                displayArea.setText("");
            }
        }));
        displayArea.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void addToPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 0.9;
        c.gridx = 0;
        c.gridy = 0;
        add(inputField, c);
        c.weightx = 0.1;
        c.gridx = 1;
        add(submit, c);
        c.weighty = 1.0d;
        c.ipady = 40;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        add(new JScrollPane(displayArea), c);
    }

    private AbstractAction getSubmitAction() {
        AbstractAction action = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
                String word = inputField.getText();
                if (!word.isEmpty()) {
                    write(word);
                    read();
                }
                inputField.setText("");
                inputField.requestFocus();
            }
        };
        action.putValue(Action.NAME, "Submit");
        return action;
    }

    private void read() {
        try {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (line.startsWith("250 ")) {
                    return;
                } else if (line.startsWith("552 ")) {
                    displayArea.append("Not found!\r\n");
                    return;
                } else if (line.matches("\\d\\d\\d .*")) {
                } else if (line.trim().equals(".")) {
                } else {
                    displayArea.append(line + "\r\n");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(JPanelEx.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void write(String word) {
        try {
            bw.write("DEFINE fd-eng-lat " + word + "\r\n");
            bw.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void connect() {
        if (!soc.isConnected()) {
            ExecutorService es = Executors.newCachedThreadPool();
            es.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        soc.connect(ADDRESS, TIMEOUT);
                        soc.setSoTimeout(TIMEOUT);
                        soc.setKeepAlive(true);
                        br = new BufferedReader(new InputStreamReader(soc.getInputStream(), "UTF-8"));
                        bw = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
                        br.readLine();
                    } catch (IOException ex) {
                        displayArea.append("Connection Failed\n");
                        throw new IllegalStateException("Connection Failed");
                    }
                }
            });
            es.shutdown();
            try {
                es.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                displayArea.append("Connection Failed");
                throw new IllegalStateException("Connection Failed");
            }
        }
    }

    public void terminate() {
        try {
            if (soc != null) {
                bw.write("QUIT\r\n");
                bw.flush();
                soc.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
