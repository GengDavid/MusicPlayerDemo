package com.example.geng.musicplayer;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by Geng on 2017/11/20.
 */

public class MusicService extends Service {
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static ObjectAnimator animator;
    private String folder_path = Environment.getExternalStorageDirectory().getPath()+ File.separator+"music";


    public final IBinder binder = new MyBinder();
    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            switch (code) {
                case 100:
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        animator.pause();
                        reply.writeInt(1);
                    } else {
                        mediaPlayer.start();
                        if (animator.isStarted()) {
                            animator.resume();
                        }
                        else {
                            animator.setDuration(5000);
                            animator.setInterpolator(new LinearInterpolator());
                            animator.setRepeatCount(ValueAnimator.INFINITE);
                            animator.setRepeatMode(ValueAnimator.RESTART);
                            animator.start();
                        }
                        reply.writeInt(0);
                    }
                    break;
                case 101:
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.prepare();
                        mediaPlayer.seekTo(0);
                        animator.end();
                        animator.pause();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    reply.writeInt(2);
                    break;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    public MusicService() {
        try {
            mediaPlayer.setDataSource(folder_path+"/melt.mp3");
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}