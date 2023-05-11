package com.example.myapplication;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link XinFaBiaoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class XinFaBiaoFragment extends Fragment {
    public ArrayList<DongTaiContent> dongTaiContents = new ArrayList<>();
    RecyclerView recyclerView;
    DongTaiAdapter adapter;
    TextInputEditText textInputEditText;
    Button button;
    public ActivityResultLauncher<Intent> dongTaiContentActivityResultLauncher;
    public XinFaBiaoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment XinFaBiaoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static XinFaBiaoFragment newInstance(String param1, String param2) {
        XinFaBiaoFragment fragment = new XinFaBiaoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化数据
        initDongTaiContents();
        ((MainActivity)getContext()).setXinFaBiaoFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_xin_fa_biao, container, false);
        // Create recycler view.
        recyclerView = view.findViewById(R.id.recyclerview);
        // Create an adapter and supply the data to be displayed.
        adapter = new DongTaiAdapter(getContext(), dongTaiContents);
        // Connect the adapter with the recycler view.
        recyclerView.setAdapter(adapter);
        // Give the recycler view a default layout manager.
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        button = view.findViewById(R.id.newdongtaibutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), adddongtai.class);
                dongTaiContentActivityResultLauncher.launch(intent);
            }
        });

        dongTaiContentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK) {
                    Log.d("Recieve", "1");
                    DongTaiContent dongTaiContent = (DongTaiContent) result.getData().getSerializableExtra("result");
                    dongTaiContents.add(0, dongTaiContent);
                    adapter.notifyItemInserted(0);
                    recyclerView.getLayoutManager().scrollToPosition(0);
                    Log.d("Recieve", dongTaiContent.publisher);
                }
            }
        });

        return view;
    }

    public void initDongTaiContents() {
        dongTaiContents.add(
                new DongTaiContent("FrantGuo", R.drawable.touxiang, "14:00 Mar 23rd",
                        "盛典即将开启，让世界更美。", 1,2,3,"微博盛典",
                        new ArrayList<Integer>(Arrays.asList(R.drawable.touxiang)), getContext()));
        dongTaiContents.add(
                new DongTaiContent("FrantGuo", R.drawable.touxiang, "15:25 Feb 25th",
                        "感谢徐工集团的大力支持！\n体验很好，下次还来！", 7,10,2, "徐工集团拜访记",
                        new ArrayList<Integer>(Arrays.asList(R.drawable.xugongjituan1, R.drawable.xugongjituan2,
                                R.drawable.xugongjituan3, R.drawable.xugongjituan4, R.drawable.xugongjituan6)), getContext()));
        dongTaiContents.add(
                new DongTaiContent("Royan", R.drawable.royantouxiang, "18:11 Feb 16th",
                        "我画了一些马鸥盲盒", 8,0,5, "马鸥二创",
                        new ArrayList<Integer>(Arrays.asList(R.drawable.mls1, R.drawable.mls2,
                                R.drawable.mls3, R.drawable.mls4, R.drawable.mls5,
                                R.drawable.mls6, R.drawable.mls7)), getContext()));
        dongTaiContents.add(
                new DongTaiContent("FrantGuo", R.drawable.touxiang, "19:56 Feb 8th",
                        "哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈", 100,8,3, "有趣的动漫分享",
                        new ArrayList<Integer>(Arrays.asList(R.drawable.manhua1, R.drawable.manhua2,
                                R.drawable.manhua3, R.drawable.manhua4, R.drawable.manhua5, R.drawable.manhua6)), getContext()));
    }
}