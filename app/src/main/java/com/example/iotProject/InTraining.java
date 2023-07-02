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

public class InTraining extends AppCompatActivity implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }
    private String deviceAddress;
    private SerialService service;
    private Connected connected = Connected.False;
    private boolean initialStart = true;

    private boolean inTrain = false, music_on = false;
    private int setsCounter = 0, repsCounter = 0;
    private float lastTime = 0, startTime = -1;
    private String msg, progressText;

    private ArrayList<Entry> data = new ArrayList<>();
    private MediaPlayer player;
    private TrainingPlan plan;
    private ImageView imageView;

    private ProgressBar progressBar;
    private TextView progressTextView, setsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_training);
        imageView = findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.pushup);
        deviceAddress = MainActivity.deviceAddress;
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(view -> startTraining());
        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(view -> stopTraining(false));
        player = MediaPlayer.create(this, R.raw.bring_sally_up);
        plan = (TrainingPlan) getIntent().getSerializableExtra("trainingPlan");

        TextView trainingNameTextView = findViewById(R.id.trainingNameTextView);
        trainingNameTextView.setText(plan.getTrainingName());
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(plan.reps);
        setsTextView = findViewById(R.id.setsTextView);
        progressText = "Set " + 0 + " out of " + plan.setsAmount;
        setsTextView.setText(progressText);
        progressTextView = findViewById(R.id.progressTextView);
        progressTextView.setText("");

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

    private void stopTraining(boolean finished) {
        // 0: training end, 2: the user quited
        if (inTrain){
            if (finished){
                progressBar.setProgress(0);
                progressTextView.setText("");
                setsCounter++;
                progressText = "Set " + setsCounter + " out of " + plan.setsAmount;
                setsTextView.setText(progressText);
                if (setsCounter == plan.setsAmount){
                    // TODO: calc train stats and create a dialog - home screen and progress
                }
            }
            else {
                // TODO: calc train stats and create a dialog - home screen and progress
            }
        }
        inTrain = false;
    }

    private void update_progress(){
        repsCounter = 2;
        if (repsCounter >= plan.reps)
            stopTraining(true);
        progressBar.setProgress(repsCounter);
        int required = plan.reps - repsCounter;
        progressText = required + "more push ups";
        progressTextView.setText(progressText);
    }

    private int checkState() {
        // TODO: 0 if up 1 if down
        int state = 1;
        if (state == 0)
            imageView.setImageResource(R.drawable.pushup);
        else
            imageView.setImageResource(R.drawable.pushdown);
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
            progressText = "The set start in " + realTime + " seconds";
            progressTextView.setText(progressText);
            return;
        }
        if (music_on) {
            player.stop();
            player.release();
            music_on = false;
        }
        update_progress();
        lastTime = Float.parseFloat(parts[0]) - startTime;
//        if (lastTime > LENGTH) {
//            stopTraining(0);
//            progressBar.setProgress(100);
//            return;
//        }
        checkState();
        data.add(new Entry(lastTime,  Float.parseFloat(parts[1])));
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