package com.example.gemini_live;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.Arrays;
import android.Manifest;
public class AudioRecorder {

    private static final String TAG = "AudioRecorder";

    // 音訊配置 - 必須與你的 Python 服務 (AudioConfig) 嚴格一致
    private static final int SAMPLE_RATE = 24000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    // 緩衝區大小：至少需要滿足 AudioRecord 的最低要求
    // 建議使用你的 Python 代理服務中的 block_size (2400 samples * 2 bytes/sample = 4800 bytes)
    private static final int BLOCK_SIZE_BYTES = 4800;

    private AudioRecord audioRecord;
    private Thread recordingThread;
    private volatile boolean isRecording = false;
    private final GeminiWebSocketListener wsListener;
    //收音狀態
    private volatile boolean isMuted = false;
    public AudioRecorder(GeminiWebSocketListener listener) {
        this.wsListener = listener;

        // 計算最小緩衝區大小
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING);


        // 確保實際緩衝區大小足夠且是 BLOCK_SIZE_BYTES 的倍數
        int bufferSize = Math.max(minBufferSize, BLOCK_SIZE_BYTES * 2);

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_ENCODING,
                bufferSize
        );
    }
    //控制麥克風開關
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        Log.i(TAG, "Mute state set to: " + this.isMuted);
    }
    public void startRecording() {
        if (isRecording) return;

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord initialization failed.");
            return;
        }

        audioRecord.startRecording();
        isRecording = true;
        Log.i(TAG, "Recording started at " + SAMPLE_RATE + "Hz.");

        recordingThread = new Thread(() -> {
            // 使用固定的數據塊大小進行讀取和發送
            byte[] buffer = new byte[BLOCK_SIZE_BYTES];

            while (isRecording) {
                // 從麥克風讀取 BLOCK_SIZE_BYTES 數據
                int read = audioRecord.read(buffer, 0, BLOCK_SIZE_BYTES);

                if (!isMuted && read > 0) {
                    if (read == BLOCK_SIZE_BYTES) {
                        wsListener.sendAudioChunk(buffer);
                    } else {
                        Log.w(TAG, "Partial read: " + read + " bytes.");
                        wsListener.sendAudioChunk(Arrays.copyOf(buffer, read));
                    }
                } else if (read < 0) { // 處理讀取錯誤
                    Log.e(TAG, "AudioRecord read error: " + read);
                }
            }
        });
        recordingThread.start();
    }

    public void stopRecording() {
        if (!isRecording) return;

        isRecording = false;
        try {
            // 等待錄音執行緒結束
            if (recordingThread != null) {
                recordingThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        Log.i(TAG, "Recording stopped.");
    }
}