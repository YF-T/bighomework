package com.example.myapplication;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.ResultReceiver;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ViewPager2 viewPager2;
    public ShouYeFragment shouYeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobalVariable.getInstance().setContext(this);
        WebRequest.context = this;

        // 页面切换
        bottomNavigationView = findViewById(R.id.bottomnavigation);
        viewPager2 = findViewById(R.id.rootviewpager);
        initBottomNavigationView();
        initViewPager2();

        // 初始化时设置显示fragment1
        viewPager2.setCurrentItem(0);
    }

    public void initBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.shouye:
                        viewPager2.setCurrentItem(0);
                        break;
                    case R.id.huati:
                        viewPager2.setCurrentItem(1);
                        break;
                    case R.id.daodu:
                        viewPager2.setCurrentItem(2);
                        break;
                    case R.id.wode:
                        viewPager2.setCurrentItem(3);
                        break;
                }
                return true;
            }
        });
    }

    public void initViewPager2() {
        // 设置段
        FragmentStateAdapter fragmentStateAdapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new ShouYeFragment();
                    case 1:
                        return new HuaTiAndDaoDuAndWoDeFragment("话题");
                    case 2:
                        return new FollowerAndFolloweeFragment("following");
                    case 3:
                        return new GeRenZhongXinFragment();
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    public void setShouYeFragment(ShouYeFragment shouYeFragment) {
        this.shouYeFragment = shouYeFragment;
    }
}