package drone_slam.base.navdata;

import java.util.EventListener;


public interface TemperatureListener extends EventListener {

    void receivedTemperature(Temperature d);

}
