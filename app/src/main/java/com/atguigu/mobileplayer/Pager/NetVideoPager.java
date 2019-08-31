package com.atguigu.mobileplayer.Pager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.atguigu.mobileplayer.R;
import com.atguigu.mobileplayer.activity.SystemVideoPlayer;
import com.atguigu.mobileplayer.adapter.NetVideoPageAdapter;
import com.atguigu.mobileplayer.base.BasePager;
import com.atguigu.mobileplayer.domain.MediaItem;
import com.atguigu.mobileplayer.utils.CacheUtils;
import com.atguigu.mobileplayer.utils.Constants;
import com.atguigu.mobileplayer.utils.LogUtil;
import com.atguigu.mobileplayer.view.XListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Administrator on 2017-09-28.
 * 网络视频页面
 */
public class NetVideoPager extends BasePager {

    @ViewInject(R.id.listview)
    private XListView mListview;

    @ViewInject(R.id.tv_nonet)
    private TextView mTv_nonet;

    @ViewInject(R.id.pb_loading)
    private ProgressBar mPb_loading;

    private ArrayList<MediaItem> mediaItems;//装数据集合

    private NetVideoPageAdapter adapter;

    private boolean isLoadMore = false;//是否已经加载更多数据

    public NetVideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.netvideo_pager, null);

        //第一个参数是：NetVideoPager.this,第二个参数：布局
        x.view().inject(this, view);
        //设置mListview的Item的点击事件
        mListview.setOnItemClickListener(new MyOnItemClickListener());
        mListview.setPullLoadEnable(true);
        mListview.setXListViewListener(new MyIXListViewListener());
        return view;
    }

    class MyIXListViewListener implements XListView.IXListViewListener {
        @Override
        public void onRefresh() {
            getDataFromNet();
        }

        @Override
        public void onLoadMore() {
            getMoreDataFromNet();
        }
    }

    private void getMoreDataFromNet() {
        //联网
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("联网成功——" + result);
                isLoadMore = true;
                //主线程
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("联网失败——" + ex.getMessage());
                showData();
                isLoadMore = false;
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled——" + cex.getMessage());
                isLoadMore = false;
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished——");
                isLoadMore = false;
            }
        });
    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //传递列表数据-对象-序列化
            Intent intent = new Intent(context, SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position-1);
            context.startActivity(intent);
        }
    }

    @Override
    public void initData() {
        super.initData();
        LogUtil.e("网络视频页面的数据被初始化了");
        String saveJson = CacheUtils.getString(context,Constants.NET_URL);
        if(!TextUtils.isEmpty(saveJson)) {
            processData(saveJson);
        }
        getDataFromNet();
    }

    private void getDataFromNet() {
        //联网
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("联网成功——" + result);
                //缓存数据
                CacheUtils.putString(context,Constants.NET_URL,result);
                //主线程
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("联网失败——" + ex.getMessage());
                showData();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled——" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished——");
            }
        });
    }

    private void processData(String json) {
        if (!isLoadMore) {
            mediaItems = parseJson(json);
            showData();
        } else {
            //加载更多,要把得到更多的数据，添加到原来的数据集合中
            isLoadMore = false;
            //ArrayList<MediaItem> moreDatas = parseJson(json);
            mediaItems.addAll(parseJson(json));
            //刷新适配器
            adapter.notifyDataSetChanged();
            onLoad();
        }
    }

    private void showData() {
        //设置适配器
        if (mediaItems != null && mediaItems.size() > 0) {
            //有数据
            //设置适配器
            adapter = new NetVideoPageAdapter(context, mediaItems);
            mListview.setAdapter(adapter);
            onLoad();
            //把文本隐藏
            mTv_nonet.setVisibility(View.GONE);
        } else {
            //没有数据
            //文本显示
            mTv_nonet.setVisibility(View.VISIBLE);
        }
        //ProgressBar隐藏
        mPb_loading.setVisibility(View.GONE);
    }

    private void onLoad() {
        mListview.stopRefresh();
        mListview.stopLoadMore();
        mListview.setRefreshTime("更新时间:" + getSystemTime());
    }

    /**
     * 得到系统时间
     *
     * @return
     */
    public String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * 解决json数据：
     * 1.用系统接口解析json数据
     * 2.使用第三方解决工具（Gson,fastjson）
     * @param json
     * @return
     */
    private ArrayList<MediaItem> parseJson(String json) {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");//用optJSONArray()
            // 来解析json时"trailers"为空也不会崩溃
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);
                    if (jsonObjectItem != null) {
                        MediaItem mediaItem = new MediaItem();
                        String movieName = jsonObjectItem.optString("movieName");//name
                        mediaItem.setName(movieName);

                        String videoTitle = jsonObjectItem.optString("videoTitle");//desc
                        mediaItem.setDesc(videoTitle);

                        String imageUrl = jsonObjectItem.optString("coverImg");//imageUrl
                        mediaItem.setImageUrl(imageUrl);

                        String hightUrl = jsonObjectItem.optString("hightUrl");//data
                        mediaItem.setData(hightUrl);

                        //把数据添加到集合
                        mediaItems.add(mediaItem);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mediaItems;
    }
}
