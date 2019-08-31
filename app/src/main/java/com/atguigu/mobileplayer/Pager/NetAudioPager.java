package com.atguigu.mobileplayer.Pager;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.atguigu.mobileplayer.R;
import com.atguigu.mobileplayer.adapter.NetAudioPageAdapter;
import com.atguigu.mobileplayer.base.BasePager;
import com.atguigu.mobileplayer.domain.NetAudioPagerData;
import com.atguigu.mobileplayer.utils.CacheUtils;
import com.atguigu.mobileplayer.utils.Constants;
import com.atguigu.mobileplayer.utils.LogUtil;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

/**
 * Created by Administrator on 2017-09-28.
 * 网络音频页面
 */
public class NetAudioPager extends BasePager {
    //通过注释方式实例化控件
    @ViewInject(R.id.listview)
    private ListView mListView;
    @ViewInject(R.id.tv_nonet)
    private TextView tv_nonet;
    @ViewInject(R.id.pb_loading)
    private ProgressBar pb_loading;
    private List<NetAudioPagerData.ListEntity> datas;//页面的数据
    private NetAudioPageAdapter adapter;

    public NetAudioPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.netaudio_pager, null);
        x.view().inject(NetAudioPager.this, view);
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.e("网络音频页面的数据被初始化了");
        String saveJson = CacheUtils.getString(context, Constants.ALL_RES_URL);
        if (!TextUtils.isEmpty(saveJson)) {
            //解析数据
            processData(saveJson);
        }
        //联网
        getDataFromNet();
    }

    /**
     * 解析json数据和显示数据
     * 解析数据：1.使用GsonFormat生成bean对象;2.用gson解析数据
     *
     * @param json
     */
    private void processData(String json) {
        //解析数据
        NetAudioPagerData data = parsedJson(json);
        datas = data.getList();
        if (datas != null && datas.size() > 0) {
            //有数据
            tv_nonet.setVisibility(View.GONE);
            //设置适配器
            adapter = new NetAudioPageAdapter(context,datas);
            mListView.setAdapter(adapter);
        } else {
            tv_nonet.setText("没有对应的数据...");
            //没有数据
            tv_nonet.setVisibility(View.VISIBLE);
        }

        pb_loading.setVisibility(View.GONE);
    }

    /**
     * Gson解析数据
     *
     * @param json
     * @return
     */
    private NetAudioPagerData parsedJson(String json) {
        return new Gson().fromJson(json, NetAudioPagerData.class);
    }

    private void getDataFromNet() {
        RequestParams params = new RequestParams(Constants.ALL_RES_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("请求数据成功==" + result);
                //保存数据
                CacheUtils.putString(context, Constants.ALL_RES_URL, result);
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("请求数据失败==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("请求数据取消==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("请求数据完成");
            }
        });

    }
}
