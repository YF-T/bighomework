package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemForFollowFragmentAdapter extends RecyclerView.Adapter<ItemForFollowFragmentAdapter.ViewHolder> {

    private List<UserContent> userList;

    public ItemForFollowFragmentAdapter(List<UserContent> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_for_follow_fragment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserContent user = userList.get(position);
        holder.publisher.setText(user.getPublisher());
        holder.introduction.setText(user.getIntroduction());
        WebRequest.setImageByUrl(holder.headImg, user.getImageurl());
        String followOrUnfollowtext;
        if (user.isIffollow()) {
            followOrUnfollowtext = "取关";
        } else {
            followOrUnfollowtext = "关注";
        }
        holder.button.setText(followOrUnfollowtext);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView headImg;
        private TextView publisher;
        private TextView introduction;
        private Button button;
        private View borderView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            headImg = itemView.findViewById(R.id.headimg);
            publisher = itemView.findViewById(R.id.publisher);
            introduction = itemView.findViewById(R.id.introduction);
            button = itemView.findViewById(R.id.button);
            borderView = itemView.findViewById(R.id.borderview);
        }
    }
}