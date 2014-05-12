package drone_slam.base.navdata;

import java.util.EventListener;


public interface CounterListener extends EventListener {

    public void update(Counters d);

}
