package com.example.iotProject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public class In_Training extends AppCompatActivity implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }
    private String deviceAddress;
    private SerialService service;
    private Connected connected = Connected.False;
    private boolean initialStart = true;

    private boolean inTrain = false, music_on = false;
    private float lastTime = 0, startTime = -1;
    private String msg;

    private ArrayList<Entry> data;
    private MediaPlayer player;
    private TrainingPlan plan;
    private ImageView imageView;

    private int progress;
    private ProgressBar progressBar;
    private TextView progressTextView, setsTextView;
    private String progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_training);
        imageView = findViewById(R.id.imageView);
        deviceAddress = MainActivity.deviceAddress;
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(view -> startTraining());
        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(view -> stopTraining(2));
        player = MediaPlayer.create(this, R.raw.bring_sally_up);
        setsTextView = findViewById(R.id.setsTextView);
        progressTextView = findViewById(R.id.progressTextView);
        progressBar = findViewById(R.id.progressBar);
        progress = progressBar.getProgress();
        progressText = "Set " + progress + " out of " + progressBar.getMax();
        progressTextView.setText(progressText);
        // TODO couple sets and initialize data to array [num_sets]
        // TODO set max progress bar max;
        // TODO set sets counter

    }

    private void startTraining(){
        if (!inTrain){
            player = MediaPlayer.create(this, R.raw.bring_sally_up);
            player.setLooping(false);
            player.start();
            initMusicPlayer();
            music_on = true;
        }
        inTrain = true;
    }

    private void stopTraining(int reason) {
        if (inTrain){
            // TODO update set count setsTextView
            progressTextView.setText("");
            // 0: training end, 1: the user wasn't down when he should, 2: the user quited
            // TODO: calc train stats and create a dialog - home screen and progress
        }
        inTrain = false;
    }

    private void update_progress(){
        progress += 1;
        progressBar.setProgress(progress);
        progressText = "Set " + progress + " out of " + progressBar.getMax();
        progressTextView.setText(progressText);
    }

    private int checkState() {
        // TODO: 0 if up 1 if down
//        if (state == 0)
//            imageView.setImageResource(R.drawable.pushup);
//        else
//            imageView.setImageResource(R.drawable.pushdown);
        return 1;
    }


    private void receive(byte[] message) {
        if (!inTrain)
            return;
        msg = new String(message);
        // check message length
        if (msg.length() <= 0) // ! newline.equals(TextUtil.newline_crlf) ||
            return;
        msg = msg.replace(TextUtil.newline_crlf, TextUtil.emptyString);
        // split message string by ',' char and trim blank spaces
        String[] parts = clean_str(msg.split(","));
        // TODO update arduino - only time and y
        // TODO upgrade speed by scanning all of parts
        if (startTime == -1)
            startTime = Float.parseFloat(parts[0]);
        if (Float.parseFloat(parts[0]) <= 5){
            int realTime = 5 - (int)(Float.parseFloat(parts[0]) - startTime);
            progressTextView.setText(realTime);
            return;
        }
        if (music_on) {
            player.stop();
            player.release();
            music_on = false;
        }
        // TODO get push ups and update progress bar / stop train (or set)
        lastTime = Float.parseFloat(parts[0]) - startTime;
//        if (lastTime > LENGTH) {
//            stopTraining(0);
//            progressBar.setProgress(100);
//            return;
//        }
        checkState();
        data.add(new Entry(lastTime,  Float.parseFloat(parts[1])));
        // TODO update progress
        progressTextView.setText("");
    }

    private String[] clean_str(String[] stringsArr){
        for (int i = 0; i < stringsArr.length; i++)  {
            stringsArr[i]=stringsArr[i].replaceAll(" ","");
        }
        return stringsArr;
    }


    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        stopService(new Intent(this, SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else {
            startService(new Intent(this, SerialService.class));
            // prevents service destroy on unbind from recreated activity caused by orientation change
            bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onStop() {
        if(service != null && !isChangingConfigurations()) {
            service.detach();
            try {
                unbindService(this);
            } catch (Exception ignored) {
            }
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
//        initialStart && isResumed()
        if(initialStart) {
            initialStart = false;
            runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    @Override
    public void onBindingDied(ComponentName name) {
        ServiceConnection.super.onBindingDied(name);
    }

    @Override
    public void onNullBinding(ComponentName name) {
        ServiceConnection.super.onNullBinding(name);
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)),
                0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        receiveText.append(spn);
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        try {
            receive(data);}
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSerialIoError(Exception e) {}

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

}