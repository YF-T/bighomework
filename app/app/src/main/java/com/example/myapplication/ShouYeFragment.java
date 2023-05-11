package com.example.myapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShouYeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShouYeFragment extends Fragment {

    TabLayout tabLayout;
    ViewPager2 viewPager2;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shou_ye, container, false);
        viewPager2 = view.findViewById(R.id.leafviewpager2);
        tabLayout = view.findViewById(R.id.tablayout);

        // 初始化viewpager中的fragment
        initViewPager2();

        // 绑定TabLayout和viewpager2
        new TabLayoutMediator(tabLayout, viewPager2, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("新发表");
                        break;
                    case 1:
                        tab.setText("新回复");
                        break;
                    case 2:
                        tab.setText("热门");
                        break;
                    case 3:
                        tab.setText("关注");
                        break;
                }
            }
        }).attach();
        return view;
    }

    public void initViewPager2() {
        // 设置段
        FragmentStateAdapter fragmentStateAdapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new XinFaBiaoFragment();
                    case 1:
                        return new XinHuiFuAndReMenAndGuanZhuFragment("新回复");
                    case 2:
                        return new XinHuiFuAndReMenAndGuanZhuFragment("热门");
                    case 3:
                        return new XinHuiFuAndReMenAndGuanZhuFragment("关注");
                }
                return null;
            }

            @Override
            public int getItemCount() {
                return 4;
            }
        };
        viewPager2.setAdapter(fragmentStateAdapter);

        // 设置禁止滑动
        viewPager2.setUserInputEnabled(false);
    }
}