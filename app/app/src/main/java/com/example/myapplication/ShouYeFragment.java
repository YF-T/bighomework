package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShouYeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShouYeFragment extends Fragment {

    public ArrayList<DongTaiContent> dongTaiContents = new ArrayList<>();
    RecyclerView recyclerView;
    DongTaiAdapter adapter;
    TextInputEditText textInputEditText;
    Button button;
    public ActivityResultLauncher<Intent> dongTaiContentActivityResultLauncher;
    // 声明搜索按钮
    private Button searchButton;
    // 声明编辑文本框
    private EditText editText;
    // 声明标签的 Spinner
    private Spinner tagSpinner;
    // 声明排序的 Spinner
    private Spinner orderSpinner;
    // 声明关注的 Spinner
    private Spinner followSpinner;
    public ShouYeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ShouYeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShouYeFragment newInstance(String param1, String param2) {
        ShouYeFragment fragment = new ShouYeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化数据
        initDongTaiContents();
        ((MainActivity)getContext()).setShouYeFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_shou_ye, container, false);
        // Create recycler view.
        recyclerView = view.findViewById(R.id.recyclerview);
        // Create an adapter and supply the data to be displayed.
        adapter = new DongTaiAdapter(getContext(), dongTaiContents);
        // Connect the adapter with the recycler view.
        recyclerView.setAdapter(adapter);
        // Give the recycler view a default layout manager.
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        button = view.findViewById(R.id.newdongtaibutton);
        // 获取搜索按钮
        searchButton = view.findViewById(R.id.searchButton);

        // 获取编辑文本框
        editText = view.findViewById(R.id.edittext);

        // 获取标签的 Spinner
        tagSpinner = view.findViewById(R.id.gettag);

        // 获取排序的 Spinner
        orderSpinner = view.findViewById(R.id.order);

        // 获取关注的 Spinner
        followSpinner = view.findViewById(R.id.follow);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), adddongtai.class);
                dongTaiContentActivityResultLauncher.launch(intent);
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDongTai();
            }
        });

        dongTaiContentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK) {
                    searchDongTai();
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        searchDongTai();
    }

    public void initDongTaiContents() {
//        dongTaiContents.add(
//                new DongTaiContent("FrantGuo", GlobalVariable.defaultImage, "14:00 Mar 23rd",
//                        "盛典即将开启，让世界更美。", 1,2,3,"微博盛典",
//                        new ArrayList<String>(Arrays.asList(GlobalVariable.defaultImage))));
//        dongTaiContents.add(
//                new DongTaiContent("FrantGuo", R.drawable.touxiang, "15:25 Feb 25th",
//                        "感谢徐工集团的大力支持！\n体验很好，下次还来！", 7,10,2, "徐工集团拜访记",
//                        new ArrayList<Integer>(Arrays.asList(R.drawable.xugongjituan1, R.drawable.xugongjituan2,
//                                R.drawable.xugongjituan3, R.drawable.xugongjituan4, R.drawable.xugongjituan6)), getContext()));
//        dongTaiContents.add(
//                new DongTaiContent("Royan", R.drawable.royantouxiang, "18:11 Feb 16th",
//                        "我画了一些马鸥盲盒", 8,0,5, "马鸥二创",
//                        new ArrayList<Integer>(Arrays.asList(R.drawable.mls1, R.drawable.mls2,
//                                R.drawable.mls3, R.drawable.mls4, R.drawable.mls5,
//                                R.drawable.mls6, R.drawable.mls7)), getContext()));
//        dongTaiContents.add(
//                new DongTaiContent("FrantGuo", R.drawable.touxiang, "19:56 Feb 8th",
//                        "哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈", 100,8,3, "有趣的动漫分享",
//                        new ArrayList<Integer>(Arrays.asList(R.drawable.manhua1, R.drawable.manhua2,
//                                R.drawable.manhua3, R.drawable.manhua4, R.drawable.manhua5, R.drawable.manhua6)), getContext()));
    }

    public void searchDongTai() {
        // 获取搜索文本
        String searchText = editText.getText().toString();
        // 获取选中的标签
        String selectedTag = tagSpinner.getSelectedItem().toString();
        // 获取选中的排序方式
        String selectedOrder = orderSpinner.getSelectedItem().toString();
        // 获取选中的关注状态
        String selectedFollow = followSpinner.getSelectedItem().toString();
        Loading loading = new Loading(getContext());
        HashMap<String, String> args = new HashMap<>();
        args.put("key", searchText);
        args.put("tag", selectedTag);
        args.put("sortkey", selectedOrder);
        if (selectedFollow.equals("仅关注")){
            args.put("iffollow", "follow");
        } else {
            args.put("iffollow", "");
        }
        args.put("type", "all");
        try {
            WebRequest.sendGetRequest("/dongtai/search", args, hashMap -> {
                dongTaiContents.clear();
                try {
                    ArrayList<Object> arrayList = JsonUtil.jsonArrayToArrayList((JSONArray) hashMap.get("dongtais"));
                    for (Object o: arrayList) {
                        dongTaiContents.add(new DongTaiContent((HashMap<String, Object>) o));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading.dismiss();
                        adapter.notifyDataSetChanged();
                    }
                });
                return null;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loading.show();
    }
}