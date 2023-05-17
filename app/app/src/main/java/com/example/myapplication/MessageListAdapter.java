package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private Context context;
    private ArrayList<Message> messageList;

    private String username;


    public MessageListAdapter(Context context, ArrayList<Message> messageList, String username) {
        this.context = context;
        this.messageList = messageList;
        this.username = username;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        MessageViewHolder messageViewHolder = new MessageViewHolder(view);
        messageViewHolder.item_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                Message message = messageList.get(messageViewHolder.getAdapterPosition());
                Bundle bundle = new Bundle();
                bundle.putString("sender", message.getSender());
                bundle.putString("username",username);
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });
        return messageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.senderTextView.setText(message.getSender());
        holder.contentTextView.setText(message.getContent());
        holder.timeTextView.setText(message.getTime().toString());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}

class MessageViewHolder extends RecyclerView.ViewHolder {

    public TextView senderTextView;
    public TextView contentTextView;
    public TextView timeTextView;

    public LinearLayout item_message;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        senderTextView = itemView.findViewById(R.id.sender_text_view);
        contentTextView = itemView.findViewById(R.id.content_text_view);
        timeTextView = itemView.findViewById(R.id.time_text_view);
        item_message = itemView.findViewById(R.id.item_message);
    }
}
