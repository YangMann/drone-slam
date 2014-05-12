package drone_slam.base.navdata;

import java.util.EventListener;


public interface AltitudeListener extends EventListener {

    public void receivedAltitude(int altitude);

    public void receivedExtendedAltitude(Altitude d);

}
