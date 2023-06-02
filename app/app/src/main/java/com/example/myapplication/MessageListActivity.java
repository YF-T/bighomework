package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MessageListActivity extends AppCompatActivity {

    private RecyclerView messageRecyclerView;
    private MessageListAdapter adapter;
    private ArrayList<Message> messageList;
    private ArrayList<Message> newList;
    private Handler handler;
    private Timer timer;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        username = getIntent().getExtras().getString("username");
        messageRecyclerView = findViewById(R.id.message_list_recycler_view);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<Message>();
        newList = new ArrayList<Message>();

        adapter = new MessageListAdapter(this, messageList,username);
        messageRecyclerView.setAdapter(adapter);
        handler = new Handler(Looper.getMainLooper());

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 更新UI视图
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getchaterList();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    private void getchaterList() throws IOException {
        HashMap<String, String> requestArgs = new HashMap<>();
        requestArgs.put("user", username);
        Context context = this;

        WebRequest.sendPostRequest("/chat/chaterlist", requestArgs, (result) -> {
            String status = (String) result.get("status");
            if(status.equals("success")) {
                newList.clear();
                JSONArray chaterlist = (JSONArray)result.get("chaterList");
                for (int i = 0; i < chaterlist.length(); i++) {
                    try {
                        String snder = (String)chaterlist.get(i);
                        Message t_msg = new Message(snder,"","");
                        newList.add(t_msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                messageList.clear();
                messageList.addAll(newList);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "获取联系人列表失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        });
        // 刷新RecyclerView
        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在Activity销毁时取消计时器
        timer.cancel();
    }
}
