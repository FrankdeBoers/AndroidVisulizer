package com.stylingandroid.vizualiser;

import android.Manifest;
import android.content.Intent;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;


public class MainActivity extends AppCompatActivity {

    private static final int CAPTURE_SIZE = 256;

    private Visualizer visualiser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

    }

    @Override
    protected void onResume() {
        super.onResume();

            startVisualiser();
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
    protected void onPause() {
        if (visualiser != null) {
            visualiser.setEnabled(false);
            visualiser.release();
            visualiser.setDataCaptureListener(null, 0, false, false);
        }
        super.onPause();
    }
}
