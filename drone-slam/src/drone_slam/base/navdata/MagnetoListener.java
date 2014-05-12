package drone_slam.base.navdata;

import java.util.EventListener;


public interface MagnetoListener extends EventListener {
    public void received(MagnetoData d);
}
