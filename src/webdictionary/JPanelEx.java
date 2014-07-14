package webdictionary;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 *
 * @author ry
 */
public class JPanelEx extends JPanel {

    private static final Logger logger = Logger.getLogger("audit");
    private static final SocketAddress ADDRESS = new InetSocketAddress("dict.org", 2628);
    private static final int TIMEOUT = 15000;
    private final Socket soc;
    private final JTextField inputField;
    private final JButton submit;
    private final JTextArea displayArea;
    private BufferedWriter bw;
    private BufferedReader br;

    static {
        FileHandler fh;
        try {
            fh = new FileHandler("C:/err.log", true);
            logger.addHandler(fh);
        } catch (IOException | SecurityException ex) {
            showExceptionPane(ex);
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }

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
        inputField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                submit.doClick();
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

    private class Lookup extends SwingWorker<Object, Object> {

        private final String w;
        private final StringBuilder builder = new StringBuilder();

        public Lookup(String word) {
            w = word;
        }

        @Override
        protected Object doInBackground() throws Exception {
            write(w);
            read();
            return null;
        }

        private void read() {
            try {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    if (line.startsWith("250 ")) {
                        return;
                    } else if (line.startsWith("552 ")) {
                        builder.append("Not found!\r\n");
                        return;
                    } else if (line.matches("\\d\\d\\d .*")) {
                    } else if (line.trim().equals(".")) {
                    } else {
                        builder.append(line + "\r\n");
                    }
                }
            } catch (IOException ex) {
                showExceptionPane(ex);
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }

        private void write(String word) {
            try {
                bw.write("DEFINE fd-eng-lat " + word + "\r\n");
                bw.flush();
            } catch (IOException ex) {
                showExceptionPane(ex);
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }

        @Override
        protected void done() {
            displayArea.append(builder.toString());
        }
    }

    private AbstractAction getSubmitAction() {
        AbstractAction action = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                connect();
                String word = inputField.getText();
                if (!word.isEmpty()) {
                    new Lookup(word).execute();
                }
                inputField.setText("");
                inputField.requestFocus();
            }
        };
        action.putValue(Action.NAME, "Submit");
        return action;
    }

    private void connect() {
        if (!isOnline()) {
            ExecutorService es = Executors.newCachedThreadPool();
            es.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        soc.connect(ADDRESS, TIMEOUT);
                        soc.setSoTimeout(TIMEOUT);
                        soc.setKeepAlive(true);
                        soc.setTcpNoDelay(true);
                        br = new BufferedReader(new InputStreamReader(soc.getInputStream(), "UTF-8"));
                        bw = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
                        br.readLine();
                    } catch (IOException ex) {
                        showExceptionPane(ex);
                        logger.log(Level.SEVERE, ex.getMessage());
                    }
                }
            });
            es.shutdown();
            try {
                es.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                showExceptionPane(ex);
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }
    }

    private static void showExceptionPane(Exception ex) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, ex.getClass().getName());
            }
        });
    }

    private boolean isOnline() {
        return (soc.isConnected() && !soc.isClosed());
    }

    public void terminate() {
        try {
            if (bw != null) {
                bw.write("QUIT\r\n");
                bw.flush();
            }
            if (soc != null && isOnline()) {
                soc.close();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }
}
