package com.example.iotProject;

import static android.content.ContentValues.TAG;

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class InTraining extends AppCompatActivity implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }
    private String deviceAddress;
    private SerialService service;
    private Connected connected = Connected.False;
    private boolean initialStart = true;

    private boolean inTrain = false, music_on = false, is_up = true;
    private int setsCounter = 0, repsCounter = 0, state;
    private float startTime = -1, lastTime = 0, maxAcc = -1000, sumPushUpTime = 0;
    private final float GRAVITY = 9.81f;
    private String msg, progressText;

    private final ArrayList<Float> times = new ArrayList<>();
    private final ArrayList<Float> acc = new ArrayList<>();
    private MediaPlayer player;
    private TrainingPlan plan;
    private ImageView imageView;
    private Python py;
    private PyObject pyModule;
    private Thread thread;

    private ProgressBar progressBar;
    private TextView progressTextView, setsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_training);
        imageView = findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.pushup);
        deviceAddress = DeviceActivity.deviceAddress;
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
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                stopTraining(false);
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        if (!Python.isStarted()){
            Python.start(new AndroidPlatform(getApplicationContext()));
        }
        py = Python.getInstance();
        pyModule = py.getModule("aux_functions");

        thread = new Thread(() -> {
            while (true) {
                if (times.size() <= 10)
                    continue;
                List<PyObject> res = pyModule.callAttr("is_up", times.toArray(), acc.toArray()).asList();
                repsCounter = res.get(0).toInt();
                state = res.get(1).toInt();
                Log.d("checking", "reps: " + repsCounter + " state " + state);
            }
        });
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
            thread.interrupt();
            if (finished){
                List<PyObject> res = pyModule.callAttr
                        ("extract_data", times.toArray(), acc.toArray(), times.get(times.size() - 1)).asList();
                maxAcc = Math.max(res.get(0).toFloat(), maxAcc);
                sumPushUpTime += res.get(1).toFloat();
                progressBar.setProgress(0);
                progressTextView.setText("");
                setsCounter++;
                progressText = "Set " + setsCounter + " out of " + plan.setsAmount;
                setsTextView.setText(progressText);

                if (setsCounter == plan.setsAmount){
                    TrainingSession ts = new TrainingSession(plan.reps * plan.setsAmount,
                            plan.getTrainingName(), plan.setsAmount, maxAcc, sumPushUpTime / plan.setsAmount
                            , new Date());
                    writeSession(ts);
                    AlertDialog dialog = new AlertDialog.Builder(InTraining.this)
                            .setTitle("Training completed!")
                            .setMessage(ts.toString())
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
                progressBar.setProgress(0);
                progressTextView.setText("");
                TrainingSession ts = new TrainingSession(plan.reps * setsCounter,
                        plan.getTrainingName(), setsCounter, maxAcc, sumPushUpTime / setsCounter
                        , new Date());
                writeSession(ts);
                AlertDialog dialog = new AlertDialog.Builder(InTraining.this)
                        .setTitle("Training isn't complete :(")
                        .setMessage(ts.toString())
                        .setPositiveButton("Home Screen", (dialogInterface, i) -> {
                            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                            startActivity(intent);
                            finish();
                        })
                        .create();
                dialog.show();
                Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                startActivity(intent);
                finish();
            }
        }
        inTrain = false;
        Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
        startActivity(intent);
        finish();
    }

    private void update_progress(){
        if (repsCounter >= plan.reps)
            stopTraining(true);
        progressBar.setProgress(repsCounter);
        int required = plan.reps - repsCounter;
        progressText = required + " more push ups";
        if (!progressTextView.getText().toString().equals(progressText))
            progressTextView.setText(progressText);
        // 0 if up 1 if down
        if (state == 0 && !is_up) {
            imageView.setImageResource(R.drawable.pushup);
            is_up = true;
        }
        else if ((state == 1 && is_up)) {
            imageView.setImageResource(R.drawable.pushdown);
            is_up = false;
        }
    }

    private void receive(byte[] message) {
        if (!inTrain)
            return;
        msg = new String(message);
        // check message length
        if (msg.length() <= 0)
            return;
        String[] parts = clean_str(msg);
        if (parts.length <= 0)
            return;
        if (startTime == -1)
            startTime = Float.parseFloat(parts[parts.length - 2]);
        float time = Float.parseFloat(parts[parts.length - 2]) - startTime;
        if (time < 0)
            return;
        if (time <= 5.2){
            int realTime = (int)(5.2 - time);
            progressText = "The set start in " + realTime + " seconds";
            progressTextView.setText(progressText);
            return;
        }
        if (music_on) {
            player.stop();
            player.release();
            music_on = false;
            thread.start();
        }
        for (int i = 0; i < parts.length; i+=2) {
            times.add(Float.parseFloat(parts[i]) - startTime);
            acc.add(Float.parseFloat(parts[i + 1]) - GRAVITY);
        }
        update_progress();
    }

    private void writeSession(TrainingSession ts){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null){
            String uid = currentUser.getUid();
            DatabaseReference dataBase = FirebaseDatabase.
                    getInstance("https://iot-project-e6e76-default-rtdb.europe-west1.firebasedatabase.app/").
                    getReference("training_sessions/"+uid+"/"+ts.getDate());
            dataBase.setValue(ts);
        }
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