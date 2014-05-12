package drone_slam.base.navdata;

import java.util.EventListener;


public interface WindListener extends EventListener {

    public void receivedEstimation(WindEstimationData d);

}
