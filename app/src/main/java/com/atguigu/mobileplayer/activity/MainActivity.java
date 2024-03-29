package com.atguigu.mobileplayer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.atguigu.mobileplayer.Pager.AudioPager;
import com.atguigu.mobileplayer.Pager.NetAudioPager;
import com.atguigu.mobileplayer.Pager.NetVideoPager;
import com.atguigu.mobileplayer.Pager.VideoPager;
import com.atguigu.mobileplayer.R;
import com.atguigu.mobileplayer.base.BasePager;

import java.util.ArrayList;

/**
 * 主页面
 */
public class MainActivity extends FragmentActivity {

    //private FrameLayout fl_main_content;
    private RadioGroup rg_bottom_tag;
    private ArrayList<BasePager> basePagers;//页面的集合
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitymain);

        //fl_main_content = (FrameLayout) findViewById(R.id.fl_main_content);
        rg_bottom_tag = (RadioGroup) findViewById(R.id.rg_bottom_tag);

        basePagers = new ArrayList<>();
        basePagers.add(new VideoPager(this));
        basePagers.add(new AudioPager(this));
        basePagers.add(new NetVideoPager(this));
        basePagers.add(new NetAudioPager(this));

        //设置radiogroup监听
        rg_bottom_tag.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
        rg_bottom_tag.check(R.id.rb_video);//默认选中第一个标签页
    }

    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            switch (checkedId) {
                default:
                    position = 0;
                    break;
                case R.id.rb_audio://音频
                    position = 1;
                    break;
                case R.id.rb_netvideo://网络视频
                    position = 2;
                    break;
                case R.id.rb_netaudio://网络音频
                    position = 3;
                    break;
            }
            setFragment();
        }
    }

    /**
     * 把页面添加到Fragment中
     */
    private void setFragment() {
        //1.得到FragmentManger
        FragmentManager manager = getSupportFragmentManager();
        //2.开启事务
        FragmentTransaction ft = manager.beginTransaction();
        //3.替换
        ft.replace(R.id.fl_main_content, new Fragment() {
            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                BasePager basePager = getBasePager();
                if (basePager != null) {
                    //各个页面的视图
                    return basePager.rootView;
                }
                return null;
            }
        });
        //4.提交事务
        ft.commit();

    }

    /**
     * 根据位置得到对应的页面
     *
     * @return
     */
    private BasePager getBasePager() {
        BasePager basePager = basePagers.get(position);
        if (basePager != null && !basePager.isInitData) {
            basePager.initData();//联网请求或者绑定数据
            basePager.isInitData = true;
        }
        return basePager;
    }

    private boolean isExit = false;//是否已经退出

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (position != 0) {//不是第一个页面
                position = 0;
                rg_bottom_tag.check(R.id.rb_video);//首页
                return true;
            } else if (!isExit) {
                isExit = true;
                Toast.makeText(MainActivity.this, "再按一次退出！", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                }, 2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}