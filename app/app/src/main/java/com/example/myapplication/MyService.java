package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MyService extends Service {
    private Thread myThread;
    private String lastmsgtime = "";
    private String lastdongtaitime = "";
    private boolean first_created = true;
    private boolean first_created_dongtai = true;

    private Handler handler;
    private Runnable runnable;

    private int notificationId = 111; // 通知的唯一ID

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("open service:","succeed");
        // 创建通知渠道（仅需要在Android 8.0及更高版本上执行一次）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("111", "Channel Name", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel Description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        // 初始化Handler和Runnable
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // 在这里执行你的函数
                // 这部分代码会在后台线程中执行
                boolean ifl = GlobalVariable.get("iflogin",false);
                Log.d("ifl:",""+ifl);
                if (ifl) {
                    try {
                        newmsgtime();
                        newdongtaitime();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                // 定时执行函数
                handler.postDelayed(this, 1000); // 1000毫秒，即1秒
            }
        };
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 创建并启动线程
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(runnable, 1000); // 1000毫秒，即1秒
            }
        });
        myThread.start();

        // 如果希望Service在任务完成后自动停止，可以返回START_NOT_STICKY
        return START_NOT_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void newmsgtime() throws IOException{
        HashMap<String, String> requestArgs = new HashMap<>();
        String username = GlobalVariable.get("username","");
        requestArgs.put("user", username);
        WebRequest.sendPostRequest("/chat/newmsgtime", requestArgs, (result) -> {
            String status = (String) result.get("status");
            if(status.equals("success")) {
                String newtime = (String) result.get("last_time");
                if (first_created){
                    first_created = false;
                    lastmsgtime = newtime;
                }
                else{
                    if (!newtime.equals(lastmsgtime)){
                        lastmsgtime = newtime;
                        Log.d("service:","newmsg");
                        // 创建通知构建器
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyService.this, "111")
                                .setSmallIcon(R.drawable.touxiang)
                                .setContentTitle("大作业")
                                .setContentText("您有新的私信消息")
                                .setPriority(NotificationCompat.PRIORITY_MAX);

                        // 显示通知
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MyService.this);
                        notificationManager.notify(notificationId, builder.build());
                    }
                }
            } else {
                //no new messages
            }
            return null;
        });
    }
    private void newdongtaitime() throws IOException {
        HashMap<String, String> requestArgs = new HashMap<>();
        WebRequest.sendGetRequest("/dongtai/message", requestArgs, (result) -> {
            String status = (String) result.get("status");
            if(status.equals("success")) {
                String newtime = (String) result.get("last_time");
                if (first_created_dongtai){
                    first_created_dongtai = false;
                    lastdongtaitime = newtime;
                }
                else{
                    if (!newtime.equals(lastdongtaitime)){
                        Log.d("service:","newcomment");
                        lastdongtaitime = newtime;
                        // 创建通知构建器
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyService.this, "111")
                                .setSmallIcon(R.drawable.touxiang)
                                .setContentTitle("大作业")
                                .setContentText("您有新的动态消息")
                                .setPriority(NotificationCompat.PRIORITY_MAX);

                        // 显示通知
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MyService.this);
                        notificationManager.notify(notificationId, builder.build());

//                        List<NotificationChannel> channelList = notificationManager.getNotificationChannels();
//
//// 遍历通知渠道列表并显示每个通知渠道的名称和优先级
//                        for (NotificationChannel channel : channelList) {
//                            String channelId = channel.getId();
//                            int channelPriority = channel.getImportance();
//
//                            // 输出通知渠道的名称和优先级
//                            Log.d("NotificationChannel", "Channel ID: " + channelId);
//                            Log.d("NotificationChannel", "Channel Priority: " + channelPriority);
//                        }
                    }
                }
            } else {
                //no new messages
            }
            return null;
        });
    }
}
