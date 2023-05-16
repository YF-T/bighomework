package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;

public class MessageListActivity extends AppCompatActivity {

    private RecyclerView messageRecyclerView;
    private MessageListAdapter adapter;
    private ArrayList<Message> messageList;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        username = getIntent().getExtras().getString("username");
        messageRecyclerView = findViewById(R.id.message_list_recycler_view);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<Message>();

        // 添加一些模拟的消息
        messageList.add(new Message("Tom", "Hi, how are you?", new Date()));
        messageList.add(new Message("Jerry", "I'm good. What about you?", new Date()));
        messageList.add(new Message("Tom", "I'm doing great. Thanks for asking.", new Date()));
        messageList.add(new Message("Tom", "What have you been up to?", new Date()));
        messageList.add(new Message("Jerry", "Just working and hanging out with friends.", new Date()));


        adapter = new MessageListAdapter(this, messageList,username);
        messageRecyclerView.setAdapter(adapter);
//        adapter.setOnItemClickListener(new MessageListAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                Message message = messageList.get(position);
//                Intent intent = new Intent(MessageListActivity.this, ChatActivity.class);
//                intent.putExtra("recipient", message.getSender());
//                startActivity(intent);
//            }
//        });
    }

}
