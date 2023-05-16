package com.example.myapplication;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private EditText inputMessageEditText;
    private Button sendMessageButton;

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
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String messageText = inputMessageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // 创建新的消息对象
            Message message = new Message(username,messageText, new Date());

            // 将消息添加到列表中
            messageList.add(message);

            // 刷新RecyclerView
            chatAdapter.notifyDataSetChanged();

            // 清空输入框
            inputMessageEditText.setText("");

            // 模拟接收对方的回复消息
            receiveMessage();
        } else {
            Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show();
        }
    }

    private void receiveMessage() {
        // 模拟接收对方的回复消息
        String replyMessage = "这是对方的回复";
        Message message = new Message(sender,replyMessage, new Date());

        // 将消息添加到列表中
        messageList.add(message);

        // 刷新RecyclerView
        chatAdapter.notifyDataSetChanged();
    }
}
