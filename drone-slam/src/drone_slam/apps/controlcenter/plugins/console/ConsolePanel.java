package drone_slam.apps.controlcenter.plugins.console;


import drone_slam.apps.controlcenter.ICCPlugin;
import drone_slam.base.IARDrone;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsolePanel extends JPanel implements ICCPlugin {
    private JTextArea text;
    private JCheckBox checkBox;

    public ConsolePanel() {
        super(new GridBagLayout());

        checkBox = new JCheckBox("Redirect Console", false);
        checkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redirectSystemStreams(checkBox.isSelected());
            }
        });

        text = new JTextArea("Waiting for State ...");
//		text.setEditable(false);
        text.setFont(new Font("Courier", Font.PLAIN, 10));
        DefaultCaret caret = (DefaultCaret) text.getCaret(); // auto scroll
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        add(new JScrollPane(text), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        add(checkBox, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    }

    public void focus() {
        text.setFocusable(true);
        text.setRequestFocusEnabled(true);
        text.requestFocus();
    }

    private void redirectSystemStreams(boolean enableRedirect) {
        if (enableRedirect) {
            OutputStream out = new OutputStream() {
                public void write(int b) throws IOException {
                    updateTextArea(String.valueOf((char) b));
                }

                public void write(byte[] b, int off, int len) throws IOException {
                    updateTextArea(new String(b, off, len));
                }

                public void write(byte[] b) throws IOException {
                    write(b, 0, b.length);
                }
            };
            System.setOut(new PrintStream(out, true));
            System.setErr(new PrintStream(out, true));
        } else {
            System.setOut(System.out);
            System.setErr(System.err);
        }
    }

    private void updateTextArea(final String str) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                text.append(str);
                text.setCaretPosition(text.getDocument().getLength());
            }
        });
    }

    public void activate(IARDrone drone) {
        redirectSystemStreams(checkBox.isSelected());
    }

    public void deactivate() {
        redirectSystemStreams(false);
    }

    public String getTitle() {
        return "Logging Console";
    }

    public String getDescription() {
        return "Redirects System.out/err and displays log information in an own panel.";
    }

    public boolean isVisual() {
        return true;
    }

    public Dimension getScreenSize() {
        return new Dimension(400, 300);
    }

    public Point getScreenLocation() {
        return new Point(600, 400);
    }

    public JPanel getPanel() {
        return this;
    }
}
