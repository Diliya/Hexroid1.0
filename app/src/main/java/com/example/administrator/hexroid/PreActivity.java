package com.example.administrator.hexroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PreActivity extends AppCompatActivity {
    private PackageManager mPackageManager;//用于判断termux是否存在
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre);

        //先检测是否第一次使用，项目是否生成

        //设置启动图片

        //检测是否安装了termux
        if (!termuxStatus())
        {

            gotoAppShop(this, "com.termux");
            this.finish();
        }
        else
        {

        }
        //计时跳转至新页面
        Timer timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                Intent StartIntent =new Intent(PreActivity.this,MainActivity.class);
                startActivity(StartIntent);
                PreActivity.this.finish();
            }
        };
        //timer.schedule(timerTask,5000);
    }
    //检测termux安装状态
    public boolean termuxStatus () {
        mPackageManager = this.getPackageManager();
        if (!isInstallApp(getApplicationContext(), "com.termux")) {
            //termux不存在
            Toast.makeText(getApplicationContext(),this.getString(R.string.termuxReq) ,
                    Toast.LENGTH_SHORT).show();

            return false;
        } else {
            Toast.makeText(getApplicationContext(), this.getString(R.string.termuxExist),
                    Toast.LENGTH_SHORT).show();
            return  true;
        }
    }

    /**
     * 根据应用包名，跳转到应用市场
     *
     * @param activity    承载跳转的Activity
     * @param packageName 所需下载（评论）的应用包名
     */
    public static void gotoAppShop(Activity activity, String packageName) {
        try {
            Uri uri = Uri.parse("market://details?id="+ packageName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "您没有安装应用市场", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 判断相对应的APP是否存在
     *
     * @param context
     * @param packageName(包名)(若想判断QQ，则改为com.tencent.mobileqq，若想判断微信，则改为com.tencent.mm)
     * @return
     */
    public boolean isInstallApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        //获取手机系统的所有APP包名，然后进行一一比较
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (((PackageInfo) pinfo.get(i)).packageName
                    .equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }
    /**
     * 执行指定的终端指令
     *@param command 指令语句
     */
    public static int executeShellCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        BufferedReader osReader = null;
        BufferedReader osErrorReader = null;
        int processResult = -1;
        try {
            //执行命令
            process = Runtime.getRuntime().exec(command);

            //获得进程的输入输出流
            os = new DataOutputStream(process.getOutputStream());
            osReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            osErrorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            //输入 exit 命令以确保进程退出
            os.writeBytes("exit\n");
            os.flush();


            String shellMessage;
            String errorMessage;

            //获取命令执行信息
            shellMessage = readOSMessage(osReader);
            errorMessage = readOSMessage(osErrorReader);

            //获得退出状态
            processResult = process.waitFor();

            System.out.println("processResult : " + processResult);
            System.out.println("shellMessage : " + shellMessage);
            System.out.println("errorMessage : " + errorMessage);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (osReader != null) {
                try {
                    osReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (osErrorReader != null) {
                try {
                    osErrorReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (process != null) {
                process.destroy();
            }
        }
        return processResult;
    }

    //读取执行命令后返回的信息
    private static String readOSMessage(BufferedReader messageReader) throws IOException {
        StringBuilder content = new StringBuilder();
        String lineString;
        while ((lineString = messageReader.readLine()) != null) {

            System.out.println("lineString : " + lineString);

            content.append(lineString).append("\n");
        }

        return content.toString();
    }
}
