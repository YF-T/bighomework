package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Message;
import com.example.myapplication.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.messageTextView.setText(message.getContent());
        holder.senderTextView.setText(message.getSender());
        holder.timeTextView.setText(message.getTime().toString());
//        if (message.isSentByUser()) {
//            holder.messageTextView.setBackgroundResource(R.drawable.shape_chat_message_user);
//        } else {
//            holder.messageTextView.setBackgroundResource(R.drawable.shape_chat_message_other);
//        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageTextView;
        public TextView senderTextView;
        public TextView timeTextView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.content_text_view);
            senderTextView = itemView.findViewById(R.id.sender_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
        }
    }
}
