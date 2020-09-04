package com.stylingandroid.vizualiser;

import android.Manifest;
import android.content.Intent;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.stylingandroid.vizualiser.permissions.PermissionsActivity;
import com.stylingandroid.vizualiser.permissions.PermissionsChecker;

public class MainActivity extends AppCompatActivity implements Visualizer.OnDataCaptureListener {

    private static final int CAPTURE_SIZE = 256;
    private static final int REQUEST_CODE = 0;
    static final String[] PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS};

    private Visualizer visualiser;
    private WaveformView waveformView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        waveformView = (WaveformView) findViewById(R.id.waveform_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PermissionsChecker checker = new PermissionsChecker(this);

        if (checker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        } else {
            startVisualiser();
        }
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        }
    }

    private void startVisualiser() {
        visualiser = new Visualizer(0);
        visualiser.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            /**
             * 返回波形信息
             */
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                long v = 0;
                for (int i = 0; i < waveform.length; i++) {
                    v += Math.pow(waveform[i], 2);
                }

                double volume = 10 * Math.log10(v / (double) waveform.length);

                int currentVolume = (int) volume;
                Log.d("TAG", "onWaveFormDataCapture: " + currentVolume);

            }

            @Override
            /**
             *返回经过fft变换后的信息
             */
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) 					{

                float[] magnitudes = new float[fft.length / 2];
                int max = 0;
                for (int i = 0; i < magnitudes.length; i++) {
                    magnitudes[i] = (float) Math.hypot(fft[2 * i], fft[2 * i + 1]);
                    if (magnitudes[max] < magnitudes[i]) {
                        max = i;
                    }

                }

                int currentFrequency = max * samplingRate / fft.length;
                Log.i("xiaozhu", "currentFrequency=" + currentFrequency);

            }
        }, Visualizer.getMaxCaptureRate(), true, true);
        visualiser.setCaptureSize(CAPTURE_SIZE);
        visualiser.setEnabled(true);
    }

    @Override
    public void onWaveFormDataCapture(Visualizer thisVisualiser, byte[] waveform, int samplingRate) {
        if (waveformView != null) {
            waveformView.setWaveform(waveform);
        }
    }

    @Override
    public void onFftDataCapture(Visualizer thisVisualiser, byte[] fft, int samplingRate) {
        // NO-OP
    }

    @Override
    protected void onPause() {
        if (visualiser != null) {
            visualiser.setEnabled(false);
            visualiser.release();
            visualiser.setDataCaptureListener(null, 0, false, false);
        }
        super.onPause();
    }
}
