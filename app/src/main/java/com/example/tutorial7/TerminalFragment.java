package com.example.tutorial7;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }
    private String deviceAddress;
    private SerialService service;

    private TextView receiveText;
    private TextView sendText;
    private TextUtil.HexWatcher hexWatcher;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;


    LineChart mpLineChart;
    LineDataSet lineDataSet;
    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
    LineData data;
    EditText realSteps;
    boolean workout = false;
    String timeStr = "";
    float firstTime;
    String fileName = "";


    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");

        if (!Python.isStarted()){
            Python.start(new AndroidPlatform(getActivity().getApplicationContext()));
        }
        // TODO real python file
        Python py = Python.getInstance();
        PyObject pyObject = py.getModule("test");
        PyObject obj = pyObject.callAttr("func", 1,2,3);
        Toast.makeText(getActivity(),obj.toString() + " from python",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        receiveText = view.findViewById(R.id.receive_text);                          // TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());


        mpLineChart = (LineChart) view.findViewById(R.id.line_chart);
        lineDataSet =  new LineDataSet(emptyDataValues(), "N value");
        lineDataSet.setColor(Color.BLACK);
        lineDataSet.setCircleColor(Color.BLACK);


        dataSets.add(lineDataSet);
        data = new LineData(dataSets);
        mpLineChart.getDescription().setEnabled(false);
        mpLineChart.setData(data);
        mpLineChart.invalidate();

        Button buttonCsvShow = (Button) view.findViewById(R.id.openCsv);
        Button buttonStart = (Button) view.findViewById(R.id.btnStart);
        Button buttonStop = (Button) view.findViewById(R.id.btnStop);
        Button buttonReset = (Button) view.findViewById(R.id.btnReset);
        Button buttonSave = (Button) view.findViewById(R.id.btnSave);

        realSteps = (EditText) view.findViewById(R.id.editSteps);


        buttonStart.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (!workout)
                    timeStr = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(
                            LocalDateTime.now());
                workout = true;
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workout = false;

            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        buttonCsvShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenLoadCSV();

            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
        menu.findItem(R.id.hex).setChecked(hexEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            receiveText.setText("");
            return true;
        } else if (id == R.id.newline) {
            String[] newlineNames = getResources().getStringArray(R.array.newline_names);
            String[] newlineValues = getResources().getStringArray(R.array.newline_values);
            int pos = java.util.Arrays.asList(newlineValues).indexOf(newline);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Newline");
            builder.setSingleChoiceItems(newlineNames, pos, (dialog, item1) -> {
                newline = newlineValues[item1];
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } else if (id == R.id.hex) {
            hexEnabled = !hexEnabled;
            sendText.setText("");
            hexWatcher.enable(hexEnabled);
            sendText.setHint(hexEnabled ? "HEX mode" : "");
            item.setChecked(hexEnabled);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Serial + UI
     */
    private String[] clean_str(String[] stringsArr){
         for (int i = 0; i < stringsArr.length; i++)  {
             stringsArr[i]=stringsArr[i].replaceAll(" ","");
        }


        return stringsArr;
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void receive(byte[] message) {
        if(hexEnabled) {
            receiveText.append(TextUtil.toHexString(message) + '\n');
        } else {
            String msg = new String(message);
            if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                String msg_to_save = msg;
                msg_to_save = msg.replace(TextUtil.newline_crlf, TextUtil.emptyString);
                // check message length
                if (msg_to_save.length() > 1) {
                    // split message string by ',' char
                    String[] parts = msg_to_save.split(",");
                    // function to trim blank spaces
                    parts = clean_str(parts);

                    if (workout) {
                        if (lineDataSet.getValues().size() == 0)
                            firstTime = Float.parseFloat(parts[0]);
//                        float v1, v2, v3;
//                        v1 = Float.parseFloat(parts[1]);
//                        v2 = Float.parseFloat(parts[2]);
//                        v3 = Float.parseFloat(parts[3]);
//                        float N = v1 * v1 + v2 * v2 + v3 * v3;
                        // add received values to line dataset for plotting the line-chart
                        data.addEntry(new Entry(Float.parseFloat(parts[0]) - firstTime,
                                Float.parseFloat(parts[1])), 0);
                        lineDataSet.notifyDataSetChanged(); // let the data know a dataSet changed
                        mpLineChart.notifyDataSetChanged(); // let the chart know it's data changed
                        mpLineChart.invalidate(); // refresh


                    }

                    msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                    // send msg to function that saves it to csv
                    // special handling if CR and LF come in separate fragments
                    if (pendingNewline && msg.charAt(0) == '\n') {
                        Editable edt = receiveText.getEditableText();
                        if (edt != null && edt.length() > 1)
                            edt.replace(edt.length() - 2, edt.length(), "");
                    }
                    pendingNewline = msg.charAt(msg.length() - 1) == '\r';
                }
                receiveText.append(TextUtil.toCaretString(msg, newline.length() != 0));
            }
        }
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)),
                0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
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
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

    private ArrayList<Entry> emptyDataValues() {
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        return dataVals;
    }

    private void OpenLoadCSV(){
        Intent intent = new Intent(getContext(),LoadCSV.class);
        startActivity(intent);
    }

    private void reset(){
        workout = false;
//        while(lineDataSet.removeLast()){}
        lineDataSet.clear();
//        mpLineChart.invalidate();
        lineDataSet.notifyDataSetChanged(); // let the data know a dataSet changed
        mpLineChart.notifyDataSetChanged(); // let the chart know it's data changed
        mpLineChart.invalidate(); // refresh
    }

    private void saveData(){
        if (lineDataSet.getValues().size() == 0) {
            Toast.makeText(getContext(), "There are no data", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        // create new csv unless file already exists
        String path = "/sdcard/csv_dir_lab7/";
        File folder = new File(path);
        folder.mkdirs();
        File[] listOfFiles = folder.listFiles();

        final EditText taskEditText = new EditText(getActivity().getApplicationContext());
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Save to CSV")
                .setMessage("Enter the name of the file")
                .setView(taskEditText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fileName = String.valueOf(taskEditText.getText());
                        String fileRealName = fileName + ".csv";
                        for (File file : listOfFiles) {
                            if (fileRealName.equals(file.getName())) {
                                Toast.makeText(getContext(), "This file already exist",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        writeToCsv(path, fileRealName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }
    private void writeToCsv(String path, String name){
        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(path + name, true),
                    ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            List<Entry> vals = lineDataSet.getValues();

            String[] row = new String[]{"NAME:", name, "", ""};
            csvWriter.writeNext(row);

            row = new String[]{"EXPERIMENT TIME:", timeStr, "", ""};
            csvWriter.writeNext(row);

            row = new String[]{"ACTIVITY TYPE:","Walking", "", ""};
            csvWriter.writeNext(row);

            // TODO: change number saved
            row = new String[]{"ESTIMATE STEPS:", String.valueOf(2.5), "", ""};
            csvWriter.writeNext(row);

            row = new String[]{"COUNT OF ACTUAL STEPS:", realSteps.getText().toString(), "", ""};
            csvWriter.writeNext(row);

            row = new String[]{"", "", "", ""};
            csvWriter.writeNext(row);

            row = new String[]{"Time [sec]","N value"};
            csvWriter.writeNext(row);

            for(int i = 0; i < vals.size(); i++) {

                // now [0] is t, [1] is x a, [2] is y a, [3] is z
                row = new String[]{vals.get(i).getX() + "", vals.get(i).getY() + ""};
                csvWriter.writeNext(row);
            }
            csvWriter.close();
            Toast.makeText(getContext(),"This file saved!",Toast.LENGTH_SHORT)
                    .show();
            reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
