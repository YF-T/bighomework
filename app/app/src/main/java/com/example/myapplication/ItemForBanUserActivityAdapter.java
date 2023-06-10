package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class ItemForBanUserActivityAdapter extends RecyclerView.Adapter<ItemForBanUserActivityAdapter.ViewHolder> {
    private List<UserContent> userList;

    public ItemForBanUserActivityAdapter(List<UserContent> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ItemForBanUserActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_for_follow_fragment, parent, false);
        return new ItemForBanUserActivityAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemForBanUserActivityAdapter.ViewHolder holder, int position) {
        UserContent user = userList.get(position);
        holder.publisher.setText(user.getPublisher());
        holder.introduction.setText(user.getIntroduction());
        WebRequest.setImageByUrl(holder.headImg, user.getImageurl());
        String followOrUnfollowtext;
        if (user.isIfban()) {
            followOrUnfollowtext = "取消屏蔽";
        } else {
            followOrUnfollowtext = "将TA屏蔽";
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
                    WebRequest.sendPostRequest("/user/ban", inputValues, new Function<HashMap<String, Object>, Void>() {
                        @Override
                        public Void apply(HashMap<String, Object> stringObjectHashMap) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    // Code to run on the UI thread
                                    if((boolean) stringObjectHashMap.get("bool_banned")){
                                        // 点击表示从未关注变为关注
                                        holder.button.setText("取消屏蔽");
                                    }
                                    else{
                                        holder.button.setText("将TA屏蔽");
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
