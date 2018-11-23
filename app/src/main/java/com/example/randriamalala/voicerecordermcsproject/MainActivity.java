package com.example.randriamalala.voicerecordermcsproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.icu.lang.UProperty;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // Layout elements
    ToggleButton btnRecStp, btnPlayStp;
    Button btnSave;
    Spinner commandPicker;
    TextView print;

    // Needed elements
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;

    // OutputFile path
    String outputFile = "";

    // list of commands
    final String commands[] = {"Avance", "Recule", "Droite", "Gauche", "Etat d urgence", "Tourne droite",
            "Tourne gauche", "Fais un flip", "Arrete toi", "Enregistrer bruit ambiant"};

    // Constants for permission codes
    final int REQUEST_PERMISSION_CODE = 1000;

    private enum State {
        INIT, READY, RECORD, RECPLAYSAVE, PLAY
    }

    private State state;

    // release data when application stops
    @Override
    protected void onStop() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        // request runtime permission
        if (!gotNecessaryPermissions()) {
            askPermissions();
        }

        btnRecStp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (gotNecessaryPermissions()) {
                    if (isChecked) { // RECORDING...
                        switch (state) {
                            case INIT:
                                // INTERDIT
                                break;
                            case READY:
                                state = State.RECORD;
                                updateDisplay();
                                Toast.makeText(MainActivity.this, "Parlez...", Toast.LENGTH_SHORT).show();
                                try {
                                    mediaRecorder.prepare();
                                    mediaRecorder.start();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case RECORD:
                                // INTERDIT
                                break;
                            case RECPLAYSAVE:
                                state = State.RECORD;
                                updateDisplay();
                                Toast.makeText(MainActivity.this, "Parlez...", Toast.LENGTH_SHORT).show();
                                try {
                                    mediaRecorder.prepare();
                                    mediaRecorder.start();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case PLAY:
                                // INTERDIT
                                break;
                        }
                    } else { // STOP RECORDING...
                        switch (state) {
                            case INIT:
                                // INTERDIT
                                break;
                            case READY:
                                // INTERDIT
                                break;
                            case RECORD:
                                state = State.RECPLAYSAVE;
                                updateDisplay();
                                mediaRecorder.stop();
                                mediaRecorder.reset();
                                break;
                            case RECPLAYSAVE:
                                // INTERDIT
                                break;
                            case PLAY:
                                // INTERDIT
                                break;
                        }
                    }
                } else {
                    askPermissions();
                }
            }
        });

        btnPlayStp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switch (state) {
                        case INIT:
                            // INTERDIT
                            break;
                        case READY:
                            // INTERDIT
                            break;
                        case RECORD:
                            // INTERDIT
                            break;
                        case RECPLAYSAVE:
                            state = State.PLAY;
                            updateDisplay();
                            mediaPlayer.start();
                            Toast.makeText(MainActivity.this, "Playing...", Toast.LENGTH_SHORT).show();

                            break;
                        case PLAY:
                            // INTERDIT
                            break;
                    }
                } else {
                    switch (state) {
                        case INIT:
                            // INTERDIT
                            break;
                        case READY:
                            // INTERDIT
                            break;
                        case RECORD:
                            // INTERDIT
                            break;
                        case RECPLAYSAVE:
                            // INTERDIT
                            break;
                        case PLAY:
                            state = State.RECPLAYSAVE;
                            updateDisplay();
                            if (mediaPlayer != null) {
                                mediaPlayer.stop();
                            }
                            break;
                    }
                }
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = State.INIT;
                updateDisplay();
                mediaPlayer.reset();
            }
        });
    }

    // initialize variables, state of  the system and display
    private void init() {
        initVoiceRecorder();
        initSpinnerMenu();
        state = State.INIT;
        updateDisplay();

    }

    // initialize variables
    private void initVoiceRecorder() {
        btnRecStp = (ToggleButton) findViewById(R.id.btnRec);
        btnPlayStp = (ToggleButton) findViewById(R.id.btnPlay);
        btnSave = (Button) findViewById(R.id.btnSave);
        commandPicker = (Spinner) findViewById(R.id.spinner);

        mediaRecorder = new MediaRecorder();
        mediaPlayer = new MediaPlayer();
        // set up output file path, we'll add the file name later
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    }

    // initialize spinner menu
    private void initSpinnerMenu(){
        final ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1);
        adapter.addAll(commands);
        adapter.add("Choisir la commande à enregistrer");
        final int nbrCmd = adapter.getCount()-1;
        commandPicker.setAdapter(adapter);
        commandPicker.setSelection(nbrCmd);

        commandPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (state) {
                    case INIT:
                        if (position>= 0 && position < nbrCmd){// if selected item is not "Choisir la commande à enregistrer"
                            state = State.READY;
                            updateDisplay();
                            setOutputFileName(commandPicker.getSelectedItem().toString());
                            try {
                                mediaPlayer.setDataSource(outputFile);
                                mediaPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            prepareRecordVar();
                            Toast.makeText(MainActivity.this, "Vous pouvez enregistrer " +
                                    commandPicker.getSelectedItem().toString()+ ", ce sera stocké dans "+outputFile, Toast.LENGTH_LONG).show();
                        }
                        break;
                    case READY:
                        // INTERDIT
                        break;
                    case RECORD:
                        // INTERDIT
                        break;
                    case RECPLAYSAVE:
                        // INTERDIT
                        break;
                    case PLAY:
                        // INTERDIT
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // prepare media recorder
    private void prepareRecordVar() {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(outputFile);
    }

    // update dispay of the application's elements
    private void updateDisplay() {
        switch (state) {
            case INIT:
                btnRecStp.setEnabled(false);
                btnPlayStp.setEnabled(false);
                btnSave.setEnabled(false);
                commandPicker.setEnabled(true);
                break;
            case READY:
                btnRecStp.setEnabled(true);
                btnPlayStp.setEnabled(false);
                btnSave.setEnabled(false);
                commandPicker.setEnabled(false);
                break;
            case RECORD:
                btnRecStp.setEnabled(true);
                btnPlayStp.setEnabled(false);
                btnSave.setEnabled(false);
                commandPicker.setEnabled(false);
                break;
            case RECPLAYSAVE:
                btnRecStp.setEnabled(true);
                btnPlayStp.setEnabled(true);
                btnSave.setEnabled(true);
                commandPicker.setEnabled(false);
                break;
            case PLAY:
                btnRecStp.setEnabled(false);
                btnPlayStp.setEnabled(true);
                btnSave.setEnabled(false);
                commandPicker.setEnabled(false);
                break;
        }
    }

    // file naming convention : filename(i).3gp
    private void setOutputFileName(String newName) {
        // replace white space by _
        String temp = outputFile + newName.replaceAll("\\s+", "_") + ".3gp";
        File f = new File(temp);
        int i = 1;
        while (f.exists()) {

        }
    }

    // checks if necessary permissions are granted to the application
    private boolean gotNecessaryPermissions() {
        int canStore = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int canUseMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        return canStore == PackageManager.PERMISSION_GRANTED &&
                canUseMic == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE :
            {
                if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();

                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // ask for user permission
    private void askPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }

}
