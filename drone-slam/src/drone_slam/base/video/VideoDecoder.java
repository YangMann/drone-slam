package drone_slam.base.video;

import java.io.InputStream;

public interface VideoDecoder {

    public void decode(InputStream is);

    public void stop();

    public void setImageListener(ImageListener listener);

}
