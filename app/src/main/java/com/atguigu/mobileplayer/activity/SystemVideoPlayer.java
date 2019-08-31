package com.atguigu.mobileplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.atguigu.mobileplayer.R;
import com.atguigu.mobileplayer.domain.MediaItem;
import com.atguigu.mobileplayer.utils.LogUtil;
import com.atguigu.mobileplayer.utils.Utils;
import com.atguigu.mobileplayer.view.VideoView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 系统播放器，Created by G480 on 2017-10-07.
 */
public class SystemVideoPlayer extends Activity implements View.OnClickListener {
    private boolean isUseSystem = false;
    private static final int PROGRESS = 1;//视频进度的更新
    private static final int HIDE_MEDIACONTROLLER = 2;//隐藏控制面板
    private static final int SHOW_SPEED = 3;//显示网速
    private static final int FULL_SCREEN = 1;//全屏尺寸
    private static final int DEFAULT_SCREEN = 2;//默认屏幕尺寸
    private VideoView videoview;
    private Uri uri;

    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwithPlayer;
    private LinearLayout llCenter;
    private TextView tvShow;//显示音量跟亮度的调节数值
    private LinearLayout llBottom;
    private RelativeLayout media_controller;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnStartPause;
    private Button btnVideoNext;
    private Button btnVideoSwitchScreen;
    private Utils utils;
    private MyReceiver receiver;//监听电量变化的广播
    private ArrayList<MediaItem> mediaItems;//传入进来的视频列表
    private int position;//播放列表中的具体位置
    private GestureDetector detector;//定义手势识别器
    private boolean isshowMediaController = false;//是否显示控制面板
    private boolean isFullScreen = false;//是否显示全屏
    private int screenWidth = 0;//屏幕的宽
    private int screenHeight = 0;//屏幕的高
    private int videoWidth;//真实视频的宽
    private int videoHeight;//真实视频的高
    private AudioManager audioManager;//调节声音
    private int currentVoice;//当前音量
    private int maxVoice;//最大音量(0~15)
    private boolean isMute = false;//是否静音
    private boolean isNetUri;//是否是网络的Uri
    private TextView tv_buffer_netspeed;
    private LinearLayout ll_buffer;
    private int precurrentPosition;//上一次的播放进度
    private LinearLayout ll_loading;
    private TextView tv_loading_netspeed;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2017-10-10 08:54:37 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        setContentView(R.layout.activity_system_video_player);
        llTop = (LinearLayout) findViewById(R.id.ll_top);
        tvName = (TextView) findViewById(R.id.tv_name);
        ivBattery = (ImageView) findViewById(R.id.iv_battery);
        tvSystemTime = (TextView) findViewById(R.id.tv_system_time);
        btnVoice = (Button) findViewById(R.id.btn_voice);
        seekbarVoice = (SeekBar) findViewById(R.id.seekbar_voice);
        btnSwithPlayer = (Button) findViewById(R.id.btn_swith_player);

        llCenter = (LinearLayout) findViewById(R.id.ll_center);
        tvShow = (TextView) findViewById(R.id.tv_show);

        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        seekbarVideo = (SeekBar) findViewById(R.id.seekbar_video);
        tvDuration = (TextView) findViewById(R.id.tv_duration);
        btnExit = (Button) findViewById(R.id.btn_exit);
        btnVideoPre = (Button) findViewById(R.id.btn_video_pre);
        btnStartPause = (Button) findViewById(R.id.btn_start_pause);
        btnVideoNext = (Button) findViewById(R.id.btn_video_next);
        btnVideoSwitchScreen = (Button) findViewById(R.id.btn_video_switch_screen);
        videoview = (VideoView) findViewById(R.id.videoview);
        media_controller = (RelativeLayout) findViewById(R.id.media_controller);
        tv_buffer_netspeed = (TextView) findViewById(R.id.tv_buffer_netspeed);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        tv_loading_netspeed = (TextView) findViewById(R.id.tv_loading_netspeed);

        btnVoice.setOnClickListener(this);
        btnSwithPlayer.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSwitchScreen.setOnClickListener(this);

        seekbarVoice.setMax(maxVoice);//设置seekbar的最大音量
        seekbarVoice.setProgress(currentVoice);//设置seekbar的当前音量

        handler.sendEmptyMessage(SHOW_SPEED);//开始更新网速
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2017-10-10 08:54:37 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            isMute = !isMute;
            // Handle clicks for btnVoice
            updateVoice(currentVoice, isMute);
        } else if (v == btnSwithPlayer) {
            // Handle clicks for btnSwithPlayer
            showSwitchPlayerDialog();
        } else if (v == btnExit) {
            // Handle clicks for btnExit
            finish();
        } else if (v == btnVideoPre) {
            // Handle clicks for btnVideoPre
            playPreVideo();

        } else if (v == btnStartPause) {
            // Handle clicks for
            StartAndPause();
        } else if (v == btnVideoNext) {
            // Handle clicks for btnVideoNext
            playNextVideo();
        } else if (v == btnVideoSwitchScreen) {
            // Handle clicks for btnVideoSwitchScreen
            setFullScreenAndDefault();
        }

        handler.removeMessages(HIDE_MEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
    }

    private void showSwitchPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("系统播放器提示");
        builder.setMessage("当所播放视频有声音无画面时，请切换万能播放器");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVitamioPlayer();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void StartAndPause() {
        if (videoview.isPlaying()) {
            //视频在播放-设置暂停
            videoview.pause();
            //按钮状态设置成播放
            btnStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        } else {
            //视频播放
            videoview.start();
            //按钮状态设置成暂停
            btnStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    /**
     * 播放上一个视频
     */
    private void playPreVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //播放上一个
            position--;
            if (position >= 0) {
                ll_loading.setVisibility(View.VISIBLE);//加载页面显示
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoview.setVideoPath(mediaItem.getData());

                //设置按钮状态
                setButtonState();
            }
        } else if (uri != null) {
            //设置按钮状态-上一个跟下一个按钮设置成灰色并且不可用
            setButtonState();
        }
    }

    /**
     * 播放下一个视频
     */
    private void playNextVideo() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //播放下一个
            position++;
            if (position < mediaItems.size()) {
                ll_loading.setVisibility(View.VISIBLE);//加载页面显示
                MediaItem mediaItem = mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());
                videoview.setVideoPath(mediaItem.getData());

                //设置按钮状态
                setButtonState();
            }
        } else if (uri != null) {
            //设置按钮状态-上一个跟下一个按钮设置成灰色并且不可用
            setButtonState();
        }
    }

    private void setButtonState() {
        if (mediaItems != null && mediaItems.size() > 0) {
            if (mediaItems.size() == 1) {
                setEnable(false);
            } else if (mediaItems.size() == 2) {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);
                } else if (position == mediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);
                }
            } else {
                if (position == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                } else if (position == mediaItems.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                } else {
                    setEnable(true);
                }
            }
        } else if (uri != null) {
            setEnable(false);
        }
    }

    private void setEnable(boolean isEnable) {
        if (isEnable) {
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btnVideoPre.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            btnVideoNext.setEnabled(true);
        } else {
            //两个按钮设置灰色
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_SPEED://显示网速
                    String netSpeed = utils.getNetSpeed(SystemVideoPlayer.this);

                    tv_loading_netspeed.setText("加载中..." + netSpeed);
                    tv_buffer_netspeed.setText("缓冲中..." + netSpeed);
                    //每两秒钟调用一次
                    removeMessages(SHOW_SPEED);
                    sendEmptyMessageDelayed(SHOW_SPEED, 2000);
                    break;
                case HIDE_MEDIACONTROLLER://隐藏控制面板
                    hideMediaController();
                    break;
                case PROGRESS:
                    //得到当前视频的播放进度
                    int currentPosition = videoview.getCurrentPosition();
                    //seekbarVideo.setProgress(当前进度);
                    seekbarVideo.setProgress(currentPosition);
                    //更新文本播放进度
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));
                    //设置系统时间
                    tvSystemTime.setText(getSystemTime());
                    //缓冲进度的更新
                    if (isNetUri) {
                        //只有网络资源才会有缓冲效果
                        int buffer = videoview.getBufferPercentage();//0~100
                        int totalBuffer = buffer * seekbarVideo.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    } else {
                        //本地视频没有缓冲效果
                        seekbarVideo.setSecondaryProgress(0);
                    }

                    //监听卡
                    if (!isUseSystem) {
                        if (videoview.isPlaying()) {
                            int buffer = currentPosition - precurrentPosition;
                            if (buffer < 500) {
                                //视频卡
                                ll_buffer.setVisibility(View.VISIBLE);
                            } else {
                                //视频不卡
                                ll_buffer.setVisibility(View.GONE);
                            }
                        } else {
                            ll_buffer.setVisibility(View.GONE);
                        }
                    }
                    precurrentPosition = currentPosition;

                    //每秒更新一次
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
            }
        }
    };

    /**
     * 得到系统时间
     *
     * @return
     */
    public String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//初始化类时先要初始化父类，再初始化子类
        LogUtil.e("onCreate...");
        initData();
        findViews();
        setListener();
        getData();
        setData();
        //设置控制面板(系统内设的控制面板)
        //videoview.setMediaController(new MediaController(this));
    }

    private void setData() {
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);
            tvName.setText(mediaItem.getName());//设置视频的名称
            isNetUri = utils.isNetUri(mediaItem.getData());
            videoview.setVideoPath(mediaItem.getData());
        } else if (uri != null) {
            tvName.setText(uri.toString());//设置视频的名称
            isNetUri = utils.isNetUri(uri.toString());
            videoview.setVideoURI(uri);//设置播放地址
        } else {
            Toast.makeText(SystemVideoPlayer.this, "没有传递数据！", Toast.LENGTH_SHORT).show();
        }
        setButtonState();
    }

    private void getData() {
        //得到播放地址
        uri = getIntent().getData();//文件夹、图片浏览器

        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
    }

    private void initData() {
        utils = new Utils();
        //注册电量广播
        receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //当电量变化的时候发出广播
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, intentFilter);

        //实例化手势识别器，并且重写长按、双击、单击事件
        detector = new GestureDetector(this, new MySimpleOnGestureListener());

        //得到屏幕的宽和高,过时的方式
//        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
//        screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        //得到屏幕的宽和高，最新方式
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        //得到音量
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            //Toast.makeText(SystemVideoPlayer.this, "长按事件触发", Toast.LENGTH_SHORT).show();
            StartAndPause();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            //Toast.makeText(SystemVideoPlayer.this, "双击事件触发", Toast.LENGTH_SHORT).show();
            setFullScreenAndDefault();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //Toast.makeText(SystemVideoPlayer.this, "单击事件触发", Toast.LENGTH_SHORT).show();
            if (isshowMediaController) {
                //隐藏
                hideMediaController();
                //把隐藏消息移除
                handler.removeMessages(HIDE_MEDIACONTROLLER);
            } else {
                //显示
                showMediaController();
                //发消息隐藏
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    private void setFullScreenAndDefault() {
        if (isFullScreen) {
            //默认
            setVideoType(DEFAULT_SCREEN);
        } else {
            //全屏
            setVideoType(FULL_SCREEN);
        }
    }

    private void setVideoType(int defaultScreen) {
        switch (defaultScreen) {
            case FULL_SCREEN://全屏
                //1.设置视频画面的大小-屏幕有多大就是多大
                videoview.setVideoSize(screenWidth, screenHeight);
                //2.设置按钮的状态-默认
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_video_switch_screen_default_selector);
                isFullScreen = true;
                break;
            case DEFAULT_SCREEN://默认
                //1.设置视频画面的大小
                //视频真实的宽和高
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;

                //屏幕的宽和高
                int width = screenWidth;
                int height = screenHeight;
                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
                videoview.setVideoSize(width, height);
                //2.设置按钮的状态--全屏
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_video_switch_screen_full_selector);
                isFullScreen = false;
                break;
        }
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int level = intent.getIntExtra("level", 0);//0~100
            setBattery(level);
        }
    }

    private void setBattery(int level) {

        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    private void setListener() {
        //准备好的监听
        videoview.setOnPreparedListener(new MyOnPreparedListener());

        //播放出错的监听
        videoview.setOnErrorListener(new MyOnErrorListener());

        //播放完成的监听
        videoview.setOnCompletionListener(new MyOnCompletionListener());

        //设置seekbar状态变化的监听
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());

        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());

        if (isUseSystem) {
            //监听视频播放卡--系统的API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                videoview.setOnInfoListener(new MyOnInfoListener());
            }
        }

    }

    class MyOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://视频卡了，拖动卡
                    //Toast.makeText(SystemVideoPlayer.this, "卡了", Toast.LENGTH_SHORT).show();
                    ll_buffer.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END://视频卡结束了，拖动卡结束了
                    //Toast.makeText(SystemVideoPlayer.this, "卡结束了", Toast.LENGTH_SHORT).show();
                    ll_buffer.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (fromUser) {
                if (progress > 0) {
                    isMute = false;
                } else {
                    isMute = true;
                }
                updateVoice(progress, isMute);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    /**
     * 设置音量的大小
     *
     * @param progress
     */
    private void updateVoice(int progress, boolean isMute) {
        if (isMute) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);
        } else {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            seekbarVoice.setProgress(progress);
            currentVoice = progress;
            int percentage = (progress * 100) / maxVoice;
            tvShow.setText(percentage + "%");
        }
    }

    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 当seekbar进度变化的时候回调该方法
         *
         * @param seekBar
         * @param progress
         * @param fromUser，用户引起的为true，不是的话为false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (fromUser) {
                videoview.seekTo(progress);
            }
        }

        /**
         * 当手指触碰的时候回调该方法
         *
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        /**
         * 当手指离开的时候回调该方法
         *
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        //当底层解码准备好的时候
        @Override
        public void onPrepared(MediaPlayer mp) {
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();

            videoview.start();//开始播放
            //视频的总时长，关联总时长
            int duration = videoview.getDuration();
            seekbarVideo.setMax(duration);
            tvDuration.setText(utils.stringForTime(duration));
            hideMediaController();//默认隐藏控制面板

            //发消息
            handler.sendEmptyMessage(PROGRESS);
            //videoview.setVideoSize(mp.getVideoWidth(), mp.getVideoHeight());

            setVideoType(DEFAULT_SCREEN);//设置屏幕的默认播放

            ll_loading.setVisibility(View.GONE);//加载页面消失

//            mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
//                @Override
//                public void onSeekComplete(MediaPlayer mp) {
//                    Toast.makeText(SystemVideoPlayer.this, "拖动完成", Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            //Toast.makeText(SystemVideoPlayer.this, "播放出错了!", Toast.LENGTH_SHORT).show();
            //1.播放的视频格式不支持--跳转到万能播放器继续播放
            startVitamioPlayer();
            //2.播放网络视频的时候，网络中断---1.如果网络确实断了，可以提示用户网络断了；2.网络断断续续的，重新播放
            //3.播放的时候本地文件中间有空白---下载做完成
            return true;
        }
    }

    /**
     * a,把数据按照原样传入VtaimoVideoPlayer播放器
     * b,关闭系统播放器
     */
    private void startVitamioPlayer() {
        if (videoview != null) {
            videoview.stopPlayback();
        }
        Intent intent = new Intent(this, VitamioVideoPlayer.class);
        if (mediaItems != null && mediaItems.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
        } else if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
        finish();//关闭页面
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            //Toast.makeText(SystemVideoPlayer.this, "播放完成=" + uri, Toast.LENGTH_SHORT).show();
            playNextVideo();
        }
    }

    @Override
    protected void onDestroy() {

        //移除所有的消息
        handler.removeCallbacksAndMessages(null);

        //释放资源的时候，先释放子类，再释放父类
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        LogUtil.e("onDestroy--");
        super.onDestroy();

    }

    private float startY;
    private float startX;
    private float touchRang;//屏幕的高
    private int mVol;//当一按下的音量

    /**
     * http://www.cnblogs.com/over140/archive/2012/05/22/2473019.html
     * http://blog.csdn.net/zanelove/article/details/46759321
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);//把事件传递给手势识别器
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://手指按下
                //1.按下记录的值
                startY = event.getY();
                startX = event.getX();
                mVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRang = Math.min(screenHeight, screenWidth);//screenHeight
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE://手指移动
                //2.移动时记录相关值
                float endY = event.getY();
                float endX = event.getX();
                float distanceY = startY - endY;

                if (endX < screenWidth / 2) {
                    //左边屏幕-调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(20);
                    }
                    if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(-20);
                    }
                } else {
                    //右边屏幕-调节声音
                    //改变声音 = （滑动屏幕的距离： 总距离）*音量最大值
                    float delta = (distanceY / touchRang) * maxVoice;
                    //最终声音 = 原来的 + 改变声音；
                    int voice = (int) Math.min(Math.max(mVol + delta, 0), maxVoice);
                    if (delta != 0) {
                        isMute = false;
                        updateVoice(voice, isMute);
                    }
                }
                break;
            case MotionEvent.ACTION_UP://手指离开
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                break;
        }
        return super.onTouchEvent(event);
    }

    //    /**
//     * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
//     *
//     * @param brightness
//     */
//    private void setBrightness(float brightness) {
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
//        if (lp.screenBrightness > 1) {
//            lp.screenBrightness = 1;
//        } else if (lp.screenBrightness < 0.1) {
//            lp.screenBrightness = (float) 0.1;
//        }
//        getWindow().setAttributes(lp);
//
//        float sb = lp.screenBrightness;
//        tvShow.setText((int) Math.ceil(sb * 100) + "%");
//    }
    private Vibrator vibrator;

    /*
     * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
     */
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200}; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {10, 200}; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        }
        getWindow().setAttributes(lp);
        float sb = lp.screenBrightness;
        tvShow.setText((int) Math.ceil(sb * 100) + "%");
    }


    /**
     * 显示控制面板
     */
    private void showMediaController() {
        media_controller.setVisibility(View.VISIBLE);
        isshowMediaController = true;
    }

    /**
     * 隐藏控制面板
     */
    private void hideMediaController() {
        media_controller.setVisibility(View.GONE);
        isshowMediaController = false;
    }

    /**
     * 监听物理键，实现手机声音调节大小
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVoice--;
            updateVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVoice++;
            updateVoice(currentVoice, false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
