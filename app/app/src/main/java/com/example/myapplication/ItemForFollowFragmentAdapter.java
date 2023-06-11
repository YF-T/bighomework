package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

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
        Log.d("asdfg", user.getPublisher());
        holder.publisher.setText(user.getPublisher());
        holder.introduction.setText(user.getIntroduction());
        WebRequest.setImageByUrl(holder.headImg, user.getImageurl());
        String followOrUnfollowtext;
        if (user.isIffollow()) {
            followOrUnfollowtext = "取关";
        } else {
            followOrUnfollowtext = "关注";
        }
        holder.headImg.setClickable(true);
        holder.headImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = holder.borderView.getContext();
                Intent intent = new Intent(context, PersonalHomepageActivity.class);
                intent.putExtra("username", user.getPublisher());
                context.startActivity(intent);
            }
        });
        holder.button.setText(followOrUnfollowtext);
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean iflogin = true;
                iflogin = GlobalVariable.get("iflogin", iflogin);
                if(!iflogin){
                    return;
                }
                HashMap<String, String> inputValues = new HashMap<>();
                inputValues.put("username", user.getPublisher());
                try {
                    WebRequest.sendPostRequest("/user/follow", inputValues, new Function<HashMap<String, Object>, Void>() {
                        @Override
                        public Void apply(HashMap<String, Object> stringObjectHashMap) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    // Code to run on the UI thread
                                    if((boolean) stringObjectHashMap.get("bool_follow")){
                                        // 点击表示从未关注变为关注
                                        holder.button.setText("取关");
                                    }
                                    else{
                                        holder.button.setText("关注");
                                    }
                                }
                            });
                            return null;
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
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