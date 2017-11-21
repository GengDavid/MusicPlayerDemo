package com.example.geng.musicplayer;

import android.os.Parcel;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private Button play;
    private Button stop;
    private Button quit;

    private ImageView Image;
    private TextView totalTime;
    private TextView playingTime;
    private TextView playingState;
    private SeekBar seekBar;

    private Integer status;
    private MusicService musicService;
    public Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermission(this);
        bindServiceConnection();
        musicService = new MusicService();
        Image = (ImageView) findViewById(R.id.img);
        musicService.animator = ObjectAnimator.ofFloat(Image, "rotation", 0, 359);
        play = (Button) findViewById(R.id.PlayButton);
        stop = (Button) findViewById(R.id.stopButton);
        stop.setEnabled(false);
        quit = (Button) findViewById(R.id.quitButton);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Parcel reply = Parcel.obtain();
                    musicService.binder.transact(100, null, reply, 0);
                    status = reply.readInt();
                    updateUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Parcel reply = Parcel.obtain();
                    musicService.binder.transact(101, null, reply, 0);
                    status = reply.readInt();
                    updateUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quit();
            }
        });
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
        seekBar.setMax(musicService.mediaPlayer.getDuration());

        totalTime = (TextView) findViewById(R.id.totalTime);
        playingTime = (TextView) findViewById(R.id.playTime);
        playingState = (TextView) findViewById(R.id.playingState);
    }


    private void updateUI() throws RemoteException{
        switch (status){
            case 0: //play
                play.setText("PAUSE");
                stop.setEnabled(true);
                playingState.setText("playing");
                break;
            case 1: //pause
                play.setText("PLAY");
                playingState.setText("paused");
                break;
            case 2: //stop
                play.setText("PLAY");
                stop.setEnabled(false);
                playingState.setText("stopped");
                break;
        }
    }
    private void quit() {
        musicService.animator.end();
        unbindService(sc);
        try {
            finish();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder) iBinder).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
        }
    };

    private void bindServiceConnection() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, sc, this.BIND_AUTO_CREATE);
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            playingTime.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
            totalTime.setText(time.format(musicService.mediaPlayer.getDuration()));
            seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        musicService.mediaPlayer.seekTo(seekBar.getProgress());
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            handler.postDelayed(runnable, 100);
        }
    };

    @Override
    protected void onResume() {
        verifyStoragePermission(this);
        try{
            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
        seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
        seekBar.setMax(musicService.mediaPlayer.getDuration());
        handler.post(runnable);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        unbindService(sc);
        super.onDestroy();
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermission(Activity activity){
        int permission = ActivityCompat.checkSelfPermission(activity,
                "android.permission.READ_EXTERNAL_STORAGE");
        try {
            if(permission!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}