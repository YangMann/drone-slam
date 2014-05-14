package edu.SJTU.android_camera_sensor;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TableLayout;
import edu.SJTU.CameraAndSensor.R;

public class SetAndLogin extends Activity {
    String ipname = null;
    String gateway = null;

    private static WifiManager mywifiManager;
    private static DhcpInfo dhcpInfo;
    private static WifiInfo wifiInfo;

    public static String getGateWay(Context context) {
        mywifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        dhcpInfo = mywifiManager.getDhcpInfo();

        //dhcpInfo获取的是最后一次成功的相关信息，包括网关、ip等
        return Formatter.formatIpAddress(dhcpInfo.gateway);
    }

    //

    //String port=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {                   // 2014.5.12 加入获取默认网关的 程序
        super.onCreate(savedInstanceState);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        final Builder builder = new Builder(this);   //定义一个AlertDialog.Builder对象
        builder.setTitle("登录服务器对话框");                          // 设置对话框的标题

        //装载/res/layout/login.xml界面布局
        TableLayout loginForm = (TableLayout) getLayoutInflater().inflate(R.layout.login, null);
        final EditText iptext = (EditText) loginForm.findViewById(R.id.ipedittext);
        final EditText port1text = (EditText) loginForm.findViewById(R.id.port1);
        final EditText port2text = (EditText) loginForm.findViewById(R.id.port2);
        final EditText widthtext = (EditText) loginForm.findViewById(R.id.editwidth);
        final EditText heighttext = (EditText) loginForm.findViewById(R.id.editheight);
        final EditText datafpstext = (EditText) loginForm.findViewById(R.id.fpsdata);
        final EditText videofpstext = (EditText) loginForm.findViewById(R.id.fpsvideo);


        //Toast.makeText(getApplicationContext(), "当前系统网关为： " + getGateWay(getApplicationContext()), Toast.LENGTH_LONG);


        gateway = getGateWay(getApplicationContext());
        Log.v("GATEWAY", gateway);
        iptext.setText(gateway);

        //gateway.


        //final EditText iptext = (EditText)loginForm.findViewById(R.id.ipedittext);
        //final EditText iptext = (EditText)loginForm.findViewById(R.id.ipedittext);
        builder.setView(loginForm);                              // 设置对话框显示的View对象
        // 为对话框设置一个“登录”按钮
        builder.setPositiveButton("登录"
                // 为按钮设置监听器
                , new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //此处可执行登录处理
                ipname = iptext.getText().toString().trim();
                Bundle data = new Bundle();
                data.putString("ipname", ipname);

                data.putString("videoport", port1text.getText().toString().trim());
                data.putString("dataport", port2text.getText().toString().trim());
                data.putString("width", widthtext.getText().toString().trim());
                data.putString("height", heighttext.getText().toString().trim());
                data.putString("fpsdata", datafpstext.getText().toString().trim());
                data.putString("fpsvideo", videofpstext.getText().toString().trim());

                Intent intent = new Intent(SetAndLogin.this, MainActivity.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        });
        // 为对话框设置一个“取消”按钮
        builder.setNegativeButton("取消"
                , new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //取消登录，不做任何事情。
                System.exit(1);
            }
        });
        //创建、并显示对话框
        builder.create().show();
    }
}