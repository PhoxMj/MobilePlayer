package com.atguigu.mobileplayer.domain;

/**
 * Created by Administrator on 2017-12-18.
 * 作用：歌词类
 */
public class Lyric {
    private String content;//歌词内容
    private long timePoint;//时间戳
    private long sleepTime;//休眠时间或者高亮显示时间

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(long timePoint) {
        this.timePoint = timePoint;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public String toString() {
        return "Lyric{" +
                "content='" + content + '\'' +
                ", timePoint=" + timePoint +
                ", sleepTime=" + sleepTime +
                '}';
    }
}
