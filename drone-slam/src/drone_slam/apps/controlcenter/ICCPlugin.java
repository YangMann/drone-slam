package drone_slam.apps.controlcenter;

import drone_slam.base.IARDrone;

import javax.swing.*;
import java.awt.*;

public interface ICCPlugin {

    public void activate(IARDrone drone);

    public void deactivate();

    public String getTitle();

    public String getDescription();

    public boolean isVisual();

    public Dimension getScreenSize();

    public Point getScreenLocation();

    public JPanel getPanel();
}
