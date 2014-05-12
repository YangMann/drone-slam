package drone_slam.apps.controlcenter.plugins.connection;


import drone_slam.apps.controlcenter.ICCPlugin;
import drone_slam.base.IARDrone;
import drone_slam.base.exception.*;

import javax.swing.*;
import java.awt.*;

public class ConnectionState extends JPanel implements ICCPlugin {
    private IARDrone drone;

    private static Icon greenIcon;
    private static Icon redIcon;

    private JLabel commandLabel;
    private JLabel configurationLabel;
    private JLabel videoLabel;
    private JLabel navdataLabel;

    private IExceptionListener exceptionListener;

    public ConnectionState() {
        super(new GridBagLayout());

        greenIcon = new ImageIcon(this.getClass().getResource("dot_green.png"));
        redIcon = new ImageIcon(this.getClass().getResource("dot_red.png"));

        commandLabel = new JLabel("Command Channel", greenIcon, SwingConstants.LEFT);
        configurationLabel = new JLabel("Configuration Channel", greenIcon, SwingConstants.LEFT);
        navdataLabel = new JLabel("Navdata Channel", greenIcon, SwingConstants.LEFT);
        videoLabel = new JLabel("Video Channel", greenIcon, SwingConstants.LEFT);

        exceptionListener = new IExceptionListener() {
            public void exeptionOccurred(ARDroneException exc) {
                if (exc instanceof ConfigurationException) {
                    configurationLabel.setIcon(redIcon);
                    configurationLabel.setToolTipText(exc + "");
                } else if (exc instanceof CommandException) {
                    commandLabel.setIcon(redIcon);
                    commandLabel.setToolTipText(exc + "");
                } else if (exc instanceof NavDataException) {
                    navdataLabel.setIcon(redIcon);
                    navdataLabel.setToolTipText(exc + "");
                } else if (exc instanceof VideoException) {
                    videoLabel.setIcon(redIcon);
                    videoLabel.setToolTipText(exc + "");
                }
            }
        };

        add(commandLabel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        add(navdataLabel, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        add(configurationLabel, new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        add(videoLabel, new GridBagConstraints(0, 3, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }

    public void activate(IARDrone drone) {
        this.drone = drone;
        drone.addExceptionListener(exceptionListener);
    }

    public void deactivate() {
        drone.removeExceptionListener(exceptionListener);
    }

    public String getTitle() {
        return "Connection State";
    }

    public String getDescription() {
        return "Shows the status of the current connections to the drone.";
    }

    public boolean isVisual() {
        return true;
    }

    public Dimension getScreenSize() {
        return new Dimension(150, 100);
    }

    public Point getScreenLocation() {
        return new Point(0, 330);
    }

    public JPanel getPanel() {
        return this;
    }
}
