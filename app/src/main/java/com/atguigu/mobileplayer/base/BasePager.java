package com.atguigu.mobileplayer.base;

import android.content.Context;
import android.view.View;

/**
 * Created by Administrator on 2017-09-28.
 * 基类、公共类
 * VideoPager
 * AudioPager
 * NetVideoPager
 * NetAudioPager
 * 都继承该类
 */
public abstract class BasePager {
    //上下文
    public final Context context;//final：最终的，如果变量定义为final必须赋初始值，而且值不能改变，final方法，子类不能实现
    public View rootView;
    public boolean isInitData;

    public BasePager(Context context) {
        this.context = context;
        rootView = initView();
    }

    /*
    强制由孩子实现特定效果
     */
    public abstract View initView();

    /*
    当子页面，需要绑定数据，或者联网请求数据并且绑定的时候，重写该方法
     */
    public void initData() {

    }
}
