package edu.SJTU.android_camera_sensor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.*;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import edu.SJTU.CameraAndSensor.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {
    SurfaceView sView;
    SurfaceHolder surfaceHolder;
    int screenWidth, screenHeight;
    Camera camera;                    // 定义系统所用的照相机	
    boolean isPreview = false;        //是否在浏览中
    private String ipname;
    private String videoport;
    private String dataport;
    private int fpsdata, fpsvideo;

    // 传感器数据 由OnSensorChanged 更新
    private SensorManager mSensorManager;
    private Sensor mAccelerometer = null;
    private Sensor mGravitySensor = null;
    private Sensor mMagneticSensor = null;
    private Sensor mGyroScope = null;
    private Sensor mRotation = null;
    private Sensor mProximity = null;

    // 传感器数据
    float[] dataAccel;
    float[] dataGravity;
    float[] dataMag;
    float[] dataGyro;
    float[] dataRotation;

    // 发送定时器
    private Timer mainTimer;
    private TimerTask UDPTimerTask;
    private TimerTask TCPTimerTask;

    // UDP SOCKET 用于数据传输     这是从某demo里面抄出来的 也许用不上
    boolean mConnected = false;
    private DatagramSocket mClient = null;
    private DatagramPacket mClientIP = null;

    private DatagramSocket NavDataSocket = null;
    private InetAddress IPaddress = null;
    int dataportnum;
    int videoportnum;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        // 获取IP地址
        // 是否还要增加其他的参数设置？
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        ipname = data.getString("ipname");
        videoport = data.getString("videoport");
        dataport = data.getString("dataport");

        Log.v("VIEWipname", "ipname:" + ipname);

        screenWidth = 640;
        screenHeight = 480;

        screenHeight = Integer.parseInt(data.getString("height"));
        screenWidth = Integer.parseInt(data.getString("width"));

        fpsdata = Integer.parseInt(data.getString("fpsdata"));
        fpsvideo = Integer.parseInt(data.getString("fpsvideo"));

        sView = (SurfaceView) findViewById(R.id.sView);                  // 获取界面中SurfaceView组件		
        surfaceHolder = sView.getHolder();                               // 获得SurfaceView的SurfaceHolder

        dataportnum = Integer.parseInt(dataport);
        videoportnum = Integer.parseInt(videoport);

        // 创建UDP连接 转换ip地址
        try {
            NavDataSocket = new DatagramSocket(dataportnum);
        } catch (SocketException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            IPaddress = InetAddress.getByName(ipname);
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        //初始化Timer和TimerTask
        mainTimer = new Timer(true); // 要以DAEMON 方式运行

        UDPTimerTask = new TimerTask() {
            @Override
            public void run() {
                // 转换 数据格式 并send 出去
                Long timetmp = System.currentTimeMillis();
                String tmpString = "Nav " + "Acc " + Arrays.toString(dataAccel) + "\nMag " + Arrays.toString(dataMag)
                        + "\nGyro " + Arrays.toString(dataGyro) + "\nGrav " + Arrays.toString(dataGravity) + "\nRot " +
                        Arrays.toString(dataRotation) + "\nSysTime " + timetmp.toString();
                mySendUDP(tmpString);
            }
        };

        TCPTimerTask = new TimerTask() {
            @Override
            public void run() {
                // 将图片发送出去。。 怎么做？
            }
        };

        // 按照设定的频率来发送数据包
        mainTimer.scheduleAtFixedRate(UDPTimerTask, 1000, 1000 / fpsdata);


        // 为surfaceHolder添加一个回调监听器
        surfaceHolder.addCallback(new Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCamera();                                            // 打开摄像头
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // 如果camera不为null ,释放摄像头
                if (camera != null) {
                    if (isPreview)
                        camera.stopPreview();
                    camera.release();
                    camera = null;
                }
                System.exit(0);
            }
        });
        // 设置该SurfaceView自己不维护缓冲    
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // 初始化传感器对象
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroScope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        /// 初始化SOCKET


    }

    private void mySendUDP(String msg) {
        if (NavDataSocket != null) {
            try {
                byte[] buf = msg.getBytes();
                DatagramPacket p = new DatagramPacket(buf, buf.length, IPaddress, dataportnum);
                NavDataSocket.send(p);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void sendToClient(String msg) {
        if (mClient != null) {
            try {
                byte[] buf = msg.getBytes();
                DatagramPacket p = new DatagramPacket(buf, buf.length, mClientIP.getAddress(), mClientIP.getPort());
                mClient.send(p);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class StartServer extends AsyncTask<Integer, String, DatagramSocket> {
        protected void onPreExecute() {
        }

        protected DatagramSocket doInBackground(Integer... ports) {
            int port = ports[0];
            // Connect to port
            byte[] message = new byte[1500];
            DatagramSocket s = null;
            InetAddress clientIP = null;
            int clientPort = -1;
            try {
                // bind to local port
                s = new DatagramSocket(port);
                // Wait for connection requests
                publishProgress("Waiting for clients.");
                while (mConnected == false) {
                    DatagramPacket p = new DatagramPacket(message, message.length);
                    s.receive(p);

                    String received = new String(p.getData(), 0, p.getLength());
                    publishProgress("Received message: " + received);
                    if (received.equals("client:KnockKnock")) {
                        mConnected = true;
                        clientIP = p.getAddress();
                        clientPort = p.getPort();
                        // Store client data for later retrieval
                        mClientIP = p;
                        // Tell the client we hear them.
                        byte[] buf = new String("server:Welcome").getBytes();
                        p = new DatagramPacket(buf, buf.length, clientIP, clientPort);
                        s.send(p);
                        publishProgress("Valid client @" + clientIP.getHostAddress() + ":" + clientPort);
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //return loadImageFromNetwork(urls[0]);
            return s;
        }

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //mClientSays.setText(values[0]);
        }

        protected void onPostExecute(DatagramSocket s) {
            mClient = s;
            if (mConnected == true) {
                // mClientSays.setText("Connected to client.");
                // mServerSendButton.setEnabled(true);
            } else {
                // mClientSays.setText("Session ended.");
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mClient != null) {
            mClient.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);  //为何？ 不可用FASTEST
        mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroScope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);   // 是可能没有么？
        mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private void initCamera() {
        if (!isPreview) {
            camera = Camera.open();
        }
        if (camera != null && !isPreview) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(screenWidth, screenHeight);    // 设置预览照片的大小				
                parameters.setPreviewFpsRange(20, 30);                    // 每秒显示20~30帧
                parameters.setPictureFormat(ImageFormat.NV21);           // 设置图片格式				
                parameters.setPictureSize(screenWidth, screenHeight);    // 设置照片的大小
                //camera.setParameters(parameters);                      // android2.3.3以后不需要此行代码
                camera.setPreviewDisplay(surfaceHolder);                 // 通过SurfaceView显示取景画面				
                camera.setPreviewCallback(new StreamIt(ipname, videoportnum));         // 设置回调的类
                camera.startPreview();                                   // 开始预览				
                camera.autoFocus(null);                                  // 自动对焦
            } catch (Exception e) {
                e.printStackTrace();
            }
            isPreview = true;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                dataAccel = event.values.clone();
                break;
            case Sensor.TYPE_GYROSCOPE:
                dataGyro = event.values.clone();
                break;
            case Sensor.TYPE_GRAVITY:
                dataGravity = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                dataMag = event.values.clone();
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                dataRotation = event.values.clone();
                break;
        }
    }
}

class StreamIt implements Camera.PreviewCallback {
    private String ipname;
    private int videoportnum;

    public StreamIt(String ipname, int videoportnum) {
        this.ipname = ipname;
        this.videoportnum = videoportnum;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Size size = camera.getParameters().getPreviewSize();
        try {
            //调用image.compressToJpeg（）将YUV格式图像数据data转为jpg格式
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            if (image != null) {
                ByteArrayOutputStream outstream = new ByteArrayOutputStream();

                long temp = System.currentTimeMillis();
                outstream.write((int) temp); // i1
                outstream.write((int) (temp << 32)); // i2

            	/*
                    long l1 = (i2 & 0x000000ffffffffL) << 32;
       				long l2 = i1 & 0x00000000ffffffffL;  
       				long l = l1 | l2;
            	 */

                image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, outstream);
                outstream.flush();
                //启用线程将图像数据发送出去
                Thread th = new MyThread(outstream, ipname, videoportnum);
                th.start();
            }
        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }
    }
}

class MyThread extends Thread {
    private byte byteBuffer[] = new byte[1024];
    private OutputStream outsocket;
    private ByteArrayOutputStream myoutputstream;
    private String ipname;
    private int videoportnum;

    public MyThread(ByteArrayOutputStream myoutputstream, String ipname, int videoportnum) {
        this.myoutputstream = myoutputstream;
        this.ipname = ipname;
        this.videoportnum = videoportnum;
        try {
            myoutputstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            //将图像数据通过Socket发送出去
            Socket tempSocket = new Socket(ipname, videoportnum);

            //System.currentTimeMillis();

            outsocket = tempSocket.getOutputStream();
            ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream.toByteArray());
            int amount;
            while ((amount = inputstream.read(byteBuffer)) != -1) {
                outsocket.write(byteBuffer, 0, amount);
            }
            myoutputstream.flush();
            myoutputstream.close();
            tempSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}