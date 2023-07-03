package com.example.iotProject;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BringUp extends AppCompatActivity implements ServiceConnection, SerialListener {


    private enum Connected { False, Pending, True }
    private String deviceAddress;
    private SerialService service;
    private Connected connected = Connected.False;
    private boolean initialStart = true;

    private boolean inTrain = false, is_up = true;
    private float startTime = -1;
//    private final float[] checkPoints = {12, 17, 25, 30, 37, 42, 48, 56, 59, 64,
//            72, 77, 83, 92, 95, 101, 106, 112, 118, 124, 130, 136, 142, 149,
//            154, 160, 167, 174, 178, 184, 190, 196, 203, 207};
    private final float[][] intervals = new float[][]{{14, 17}, {19, 22}, {25, 28}, {31, 34},
            {37, 40}, {43, 46}, {50, 53}, {61, 64}, {67, 70}, {73, 76},
        {79, 82}, {85, 88}, {97, 100}, {103, 106}, {109, 112}, {115, 118}, {121, 124}, {127, 130}, {133, 136},
        {139, 142}, {145, 148}, {151, 154}, {157, 160}, {163, 166}, {169, 172}, {175, 178}, {181, 184},
        {187, 190}, {193, 196}, {198, 201}
        };
    private int checkIndex = 0, state = 0;
    private final String startText = "0:00";

    private final ArrayList<Float> times = new ArrayList<>();
    private final ArrayList<Float> acc = new ArrayList<>();
    private MediaPlayer player;
    private ProgressBar progressBar;
    private TextView progressTextView;
    private ImageView imageView;
    private PyObject pyModule;
//    private Thread thread;

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

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(getApplicationContext()));
        }
        Python py = Python.getInstance();
        pyModule = py.getModule("aux_functions");
//        thread = new Thread(() -> {
//            while (true) {
//                // TODO change python?
//                if (acc.size() > 10) {
//                    List<PyObject> res = pyModule.callAttr("is_up", times.toArray(), acc.toArray()).asList();
//                    state = res.get(1).toInt();
//                    Log.d("checking", "reps: " + res.get(0).toInt() + " state " + state);
//                }
//            }
//        });
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

    @SuppressLint("NewApi")
    private void stopTraining(int reason) {
        // TODO organize
        Date date = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        // 0: training end, 1: the user wasn't down when he should, 2: the user quited
        if (inTrain){
            inTrain = false;
            player.stop();
            player.release();
            progressTextView.setText(startText);
//            thread.interrupt();
            if (reason == 0){
                float maxAcc = pyModule.callAttr
                        ("extract_data_bsu", times.toArray(), acc.toArray()).toFloat();
                BSUSession bsuSession = new BSUSession(times.get(times.size() - 1), maxAcc,
                        date);
                writeSession(bsuSession);
                AlertDialog dialog = new AlertDialog.Builder(BringUp.this)
                        .setTitle("Bring Sally Up completed!")
                        .setMessage(bsuSession.toString())
                        .setPositiveButton("Home Screen", (dialogInterface, i) -> {
                            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                            startActivity(intent);
                            finish();
                        })
                        .create();
                dialog.show();
            }
            else{ // TODO maybe split into cases - quit or cheat
                // TODO maybe cancel if we can't check it
                float maxAcc = pyModule.callAttr
                        ("extract_data_bsu", times.toArray(), acc.toArray()).toFloat();
                BSUSession bsuSession = new BSUSession(times.get(times.size() - 1), maxAcc,
                        date);
                writeSession(bsuSession);
                AlertDialog dialog = new AlertDialog.Builder(BringUp.this)
                        .setTitle("Training isn't complete - good luck in the next time!")
                        .setMessage(bsuSession.toString())
                        .setPositiveButton("Home Screen", (dialogInterface, i) -> {
                            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                            startActivity(intent);
                            finish();
                        })
                        .create();
                dialog.show();
            }
        }
        else {
            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
            startActivity(intent);
            finish();
        }
    }


    private void updateImage() {
        boolean up = false;
        float temp = times.get(times.size() - 1);
        for (float[] interval : intervals) {
            if (interval[0] <= temp && temp <= interval[1]) {
                up = true;
                break;
            }
        }
        if (temp <= 9)
            up = true;
        if (up && !is_up) {
            imageView.setImageResource(R.drawable.pushup);
            is_up = true;
        }
        else if ((!up && is_up)) {
            imageView.setImageResource(R.drawable.pushdown);
            is_up = false;
        }
    }


    private void receive(byte[] message) {
        if (!inTrain)
            return;
        String msg = new String(message);
        String[] parts = clean_str(msg);
        if (parts.length <= 0)
            return;
        if (startTime == -1)
            startTime = Float.parseFloat(parts[parts.length - 2]);
//            thread.start();
        float lastTime = Float.parseFloat(parts[parts.length - 2]) - startTime;
        int realTime;
        if (lastTime < 0)
            return;
        String textTime;
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
        for (int i = 0; i < parts.length; i+=2) {
            times.add(Float.parseFloat(parts[i]) - startTime);
            float GRAVITY = 9.81f;
            acc.add(Float.parseFloat(parts[i + 1]) - GRAVITY);
        }
        if (times.size() > 10 && checkIndex < intervals.length && intervals[checkIndex][1] <= lastTime){
            boolean up = pyModule.callAttr("bsu_up", times.toArray(), acc.toArray(),
                    intervals[checkIndex][0], intervals[checkIndex][1]).toBoolean();
            checkIndex++;
            if (!up) { // he is down
                stopTraining(1);
                return;
            }
        }
        progressBar.setProgress((int) (100 * (lastTime - 5) / (LENGTH - 5)));
        realTime = (int)(lastTime - 5);
        textTime = (realTime / 60) + ":" + (realTime % 60);
        progressTextView.setText(textTime);
        updateImage();
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