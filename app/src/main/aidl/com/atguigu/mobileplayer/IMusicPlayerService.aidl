// IMusicPlayerService.aidl
package com.atguigu.mobileplayer;

// Declare any non-default types here with import statements

interface IMusicPlayerService {
/**
     * 根据位置打开对应的音频文件
     * @param position
     */
    void openAudio(int position);

    /**
     * 播放音乐
     */
    void start();

    /**
     * 播放暂停音乐
     */
    void pause();

    /**
     * 播放停止音乐
     */
    void stop();

    /**
     * 得到当前播放进度
     * @return
     */
    int getCurrentPosition();

    /**
     * 得到当前音频播放时长
     * @return
     */
    int getDuration();

    /**
     * 得到当前歌唱者
     * @return
     */
    String getArtist();

    /**
     * 得到当前歌曲名
     * @return
     */
    String getName();

    /**
     * 得到当前歌曲播放路径
     * @return
     */
    String getAudioPath();

    /**
     * 播放下一个音频
     */
    void next();

    /**
     * 播放上一个音频
     */
    void pre();

    /**
     * 设置播放模式
     * @param playMode
     */
    void setPlayMode(int playMode);

    /**
     * 获得播放模式
     * @return
     */
    int getPlayMode();

    /**
     * 是否正在播放
     * @return
     */
    boolean isPlaying();

    /**
     * 拖动音频
     */
    void seekTo(int position);

    int getAudioSessionId();
}

