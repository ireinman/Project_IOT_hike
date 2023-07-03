package com.example.iotProject;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

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

import com.github.mikephil.charting.data.Entry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;

public class BringUp extends AppCompatActivity implements ServiceConnection, SerialListener {


    private enum Connected { False, Pending, True }
    private String deviceAddress;
    private SerialService service;
    private Connected connected = Connected.False;
    private boolean initialStart = true;

    private boolean inTrain = false;
    private float startTime = -1;
    private final float GRAVITY = 9.81f;
    private final float[] checkPoints = {12, 17, 25, 30, 37, 42, 48, 56, 59, 64,
            72, 77, 83, 92, 95, 101, 106, 112, 118, 124, 130, 136, 142, 149,
            154, 160, 167, 174, 178, 184, 190, 196, 203, 207};
    private int checkIndex = 0;
    private String msg, textTime;
    private final String startText = "0:00";

    private final ArrayList<Entry> data = new ArrayList<>();
    private MediaPlayer player;
    private ProgressBar progressBar;
    private TextView progressTextView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bring_up);
        imageView = findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.pushup);
        deviceAddress = DeviceActivity.deviceAddress;
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(view -> startTraining());
        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(view -> stopTraining(2));
        player = MediaPlayer.create(this, R.raw.bring_sally_up);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        progressTextView = findViewById(R.id.progressTextView);
        progressTextView.setText(startText);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                stopTraining(2);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void writeSession(BSUSession bsuSession){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null){
            String uid = currentUser.getUid();
            DatabaseReference dataBase = FirebaseDatabase.
                    getInstance("https://iot-project-e6e76-default-rtdb.europe-west1.firebasedatabase.app/").
                    getReference("bsu_sessions/"+uid+"/"+bsuSession.returnDate());
            dataBase.setValue(bsuSession);
        }
    }

    private void startTraining(){
        if (!inTrain){
            player = MediaPlayer.create(this, R.raw.bring_sally_up);
            player.setLooping(false);
            player.start();
            initMusicPlayer();
        }
        inTrain = true;
    }

    private void stopTraining(int reason) {
        if (inTrain){
            player.stop();
            player.release();
            progressTextView.setText(startText);
            // 0: training end, 1: the user wasn't down when he should, 2: the user quited
            // TODO: calc train stats, save to firebase and create a dialog to HomeScreen / progress
        }
        inTrain = false;
    }


    private int checkState() {
        // TODO: 0 if up 1 if down
        // TODO update picture
        return 1;
    }


    private void receive(byte[] message) {
        if (!inTrain)
            return;
        msg = new String(message);
        String[] parts = clean_str(msg);
        if (parts.length <= 0)
            return;
        if (startTime == -1)
            startTime = Float.parseFloat(parts[parts.length - 2]);
        float lastTime = Float.parseFloat(parts[parts.length - 2]) - startTime;
        int realTime;
        if (lastTime < 0)
            return;
        if (lastTime <= 5){
            textTime = Integer.toString(5 - (int)(lastTime));
            progressTextView.setText(textTime);
            return;
        }
        lastTime = Float.parseFloat(parts[parts.length - 2]) - startTime;
        float LENGTH = 60 * 3 + 30;
        if (lastTime > LENGTH) {
            stopTraining(0);
            progressBar.setProgress(100);
            return;
        }
        int state = checkState();
        if (checkIndex < checkPoints.length && checkPoints[checkIndex] <= lastTime){
            checkIndex++;
            if (state == 0) { // he is up
                stopTraining(1);
                return;
            }
        }
        for (int i = 0; i < parts.length; i+=2) {
            data.add(new Entry(Float.parseFloat(parts[i]) - startTime,
                    Float.parseFloat(parts[i + 1]) - GRAVITY));
        }
        progressBar.setProgress((int) (100 * (lastTime - 5) / (LENGTH - 5)));
        realTime = (int)(lastTime - 5);
        textTime = (realTime / 60) + ":" + (realTime % 60);
        progressTextView.setText(textTime);
    }

    private String[] clean_str(String msg){
        msg = msg.replace(TextUtil.newline_crlf, TextUtil.emptyString);
        String[] stringsArr = msg.replace("  ", ",").split(",");
        int length = stringsArr.length;
        if (length % 2 != 0)
            length--;
        String[] res = new String[length];
        for (int i = 0; i < length; i++)  {
            res[i] = stringsArr[i].replaceAll(" ","");
        }
        return res;
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
    }

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
            throw e;
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