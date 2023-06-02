package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Message;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    private List<Message> newList;
    private EditText inputMessageEditText;
    private Button sendMessageButton;

    private Timer timer;
    private Handler handler;

    private String sender;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            sender = bundle.getString("sender");
            username = bundle.getString("username");
        }
        // 初始化消息列表
        messageList = new ArrayList<>();
        newList = new ArrayList<>();

        // 初始化RecyclerView和Adapter
        recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setAdapter(chatAdapter);

        // 初始化输入框和发送按钮
        inputMessageEditText = findViewById(R.id.message_edit_text);
        sendMessageButton = findViewById(R.id.send_button);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendMessage();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

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
                            getmsgList();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    private void sendMessage() throws IOException {
        String messageText = inputMessageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {

            HashMap<String, String> requestArgs = new HashMap<>();
            requestArgs.put("chat_sender", username);
            requestArgs.put("chat_receiver", sender);
            requestArgs.put("msg", messageText);

            Context context = this;
            WebRequest.sendPostRequest("/chat/send", requestArgs, (result) -> {
                String status = (String) result.get("status");
                if(status.equals("success")) {
                    // seems nothing to do
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "发送失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return null;
            });

            // 清空输入框
            inputMessageEditText.setText("");

        } else {
            Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show();
        }
    }

    private void getmsgList() throws IOException{
        HashMap<String, String> requestArgs = new HashMap<>();
        requestArgs.put("user", username);
        requestArgs.put("chater", sender);
        Context context = this;

        WebRequest.sendPostRequest("/chat/msglist", requestArgs, (result) -> {
            String status = (String) result.get("status");
            if(status.equals("success")) {
                newList.clear();
                JSONArray msglist = (JSONArray)result.get("messageList");
                for (int i = 0; i < msglist.length(); i++) {
                    try {
                        JSONObject item = (JSONObject)msglist.get(i);
                        String snder = item.getString("sender");
                        String msg = item.getString("message");
                        String time = item.getString("created_time");
                        Message t_msg = new Message(snder,msg,time);
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
                        Toast.makeText(context, "获取聊天记录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        });
        // 刷新RecyclerView
        chatAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在Activity销毁时取消计时器
        timer.cancel();
    }
}
