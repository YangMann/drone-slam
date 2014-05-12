package drone_slam.apps.controlcenter.plugins.attitudechart;


import drone_slam.apps.controlcenter.ICCPlugin;
import drone_slam.base.IARDrone;
import drone_slam.base.navdata.AttitudeListener;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;

public class AttitudeChartPanel extends JPanel implements ICCPlugin {
    private IARDrone drone;

    private AttitudeChart chart;

    public AttitudeChartPanel() {
        super(new GridBagLayout());

        this.chart = new AttitudeChart();
        JPanel chartPanel = new ChartPanel(chart.getChart(), true, true, true, true, true);

        add(chartPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
    }

    private AttitudeListener attitudeListener = new AttitudeListener() {
        public void windCompensation(float pitch, float roll) {

        }

        public void attitudeUpdated(float pitch, float roll) {

        }

        public void attitudeUpdated(float pitch, float roll, float yaw) {
            chart.setAttitude(pitch / 1000, roll / 1000, yaw / 1000);
        }
    };

    public void activate(IARDrone drone) {
        this.drone = drone;

        drone.getNavDataManager().addAttitudeListener(attitudeListener);
    }

    public void deactivate() {
        drone.getNavDataManager().removeAttitudeListener(attitudeListener);
    }

    public String getTitle() {
        return "Attitude Chart";
    }

    public String getDescription() {
        return "Displays a chart with the latest pitch, roll and yaw";
    }

    public boolean isVisual() {
        return true;
    }

    public Dimension getScreenSize() {
        return new Dimension(330, 250);
    }

    public Point getScreenLocation() {
        return new Point(330, 390);
    }

    public JPanel getPanel() {
        return this;
    }
}
