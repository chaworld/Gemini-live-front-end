package com.example.gemini_live;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
public class MainActivity extends AppCompatActivity {

    private static final int RECORD_AUDIO_PERMISSION_CODE = 101;
    private GeminiWebSocketListener wsListener;
    private AudioRecorder audioRecorder;
    private Button toggleButton;
    private Button muteButton;
    private boolean isConnectedAndRecording = false;
    private boolean isMuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleButton = findViewById(R.id.toggle_button);
        muteButton = findViewById(R.id.mute_button);

        // 初始化連線和錄音模組
        wsListener = new GeminiWebSocketListener();
        audioRecorder = new AudioRecorder(wsListener);

        toggleButton.setOnClickListener(v -> {
            if (isConnectedAndRecording) {
                stopStreaming();
            } else {
                startStreaming();
            }
        });
        // --- 設定靜音按鈕的點擊監聽 ---
        muteButton.setOnClickListener(v -> {
            isMuted = !isMuted; // 切換靜音狀態
            audioRecorder.setMuted(isMuted); // 通知 AudioRecorder
            updateMuteButtonText();
        });
    }

    private void startStreaming() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_CODE);
            return;
        }

        // 步驟 1: 建立 WebSocket 連線
        wsListener.connect();

        // 步驟 2: 開始錄音和串流 (在 onOpen 之後啟動更安全，但為簡潔這裡直接呼叫)
        audioRecorder.startRecording();

        isConnectedAndRecording = true;
        toggleButton.setText("停止錄音與串流");
        Toast.makeText(this, "開始語音串流...", Toast.LENGTH_SHORT).show();

        muteButton.setVisibility(View.VISIBLE);
        updateMuteButtonText();
    }

    private void stopStreaming() {
        // 步驟 1: 停止錄音和串流
        audioRecorder.stopRecording();

        // 步驟 2: 關閉 WebSocket 連線
        wsListener.close();

        isConnectedAndRecording = false;
        toggleButton.setText("開始語音串流");
        Toast.makeText(this, "串流已停止", Toast.LENGTH_SHORT).show();
        muteButton.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startStreaming(); // 獲得權限後重新開始
        } else {
            Toast.makeText(this, "需要麥克風權限才能進行語音對話", Toast.LENGTH_LONG).show();
        }
    }
    //更新按鈕文字
    private void updateMuteButtonText() {
        if (isMuted) {
            muteButton.setText("取消靜音 (恢復收音)");
        } else {
            muteButton.setText("暫停收音 (靜音)");
        }
    }

    // 確保 App 關閉時停止所有服務
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopStreaming();
    }
}