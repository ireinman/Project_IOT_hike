package com.example.iotProject;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

//מחלקת עזר שאחראית על ה-service של המוזיקה
public class MusicService extends Service{

    private MediaPlayer player; //media player

    private final IBinder musicBind = new MusicBinder();//music binder

    @Override
    public void onCreate() {

        //create music Service
        super.onCreate();

        //הגדרה של המוזיקה ב-MediaPlayer
        player = MediaPlayer.create(this, R.raw.bring_sally_up);
        player.setLooping(true);
        player.start();

        // הגדרה של מאפייני נגן המוסיקה
        initMusicPlayer();

    }

    //כשה-service מופסק הוא עוצר גם את המוסיקה
    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        super.onDestroy();
    }

    //פעולת set לווליום
    public void setVolume(float volume){
        player.setVolume(volume,volume);
    }

    //פעולה שמחזירה את ה-music binder
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    // הגדרה של מאפייני נגן המוזיקה
    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);//משאיר את ה-CPU פעיל
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    class MusicBinder extends Binder implements IBinder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    //אם המוזיקה פעילה אז הפעולה מפסיקה אותה
    public void pause() {
        if (player != null&&player.isPlaying())
            player.pause();
    }

    //אם המוזיקה בהפסקה אז הפעולה ממשיכה אותה
    public void resume() {
        if (player != null&&!player.isPlaying())
            player.start();
    }

}




