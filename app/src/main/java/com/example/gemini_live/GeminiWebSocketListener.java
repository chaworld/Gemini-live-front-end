package com.example.gemini_live;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.util.concurrent.TimeUnit;

public class GeminiWebSocketListener extends WebSocketListener {

    private static final String TAG = "GeminiWS";

    // 請替換成你的 FastAPI 代理服務的 IP 地址和埠號
    // 注意：使用 ws 協議，並且 IP 不要是 localhost (使用你的電腦或伺服器 IP)
    private static final String WS_URL = "ws://192.168.2.13:8080/ws/gemini_live";

    // 音訊配置 - 必須與你的 Python 服務 (AudioConfig) 嚴格一致
    private static final int SAMPLE_RATE = 24000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private WebSocket webSocket;
    private AudioTrack audioTrack;
    private final OkHttpClient client;

    public GeminiWebSocketListener() {
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // WebSocket 不應有讀取超時
                .build();

        // 初始化 AudioTrack 用於播放接收到的 TTS 語音
        int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_ENCODING,
                bufferSize,
                AudioTrack.MODE_STREAM
        );
        audioTrack.play();
    }

    public void connect() {
        Request request = new Request.Builder().url(WS_URL).build();
        client.newWebSocket(request, this);
    }

    // 發送音訊數據塊（從麥克風錄音模組呼叫）
    public void sendAudioChunk(byte[] audioData) {
        if (webSocket != null) {
            // 將 Java byte array 轉換為 Okio 的 ByteString 後發送
            webSocket.send(ByteString.of(audioData));
            // Log.d(TAG, "Sent audio chunk: " + audioData.length + " bytes");
        }
    }

    // 發送初始文字訊息，用於在錄音前向 Gemini 提問
    public void sendInitialText(String text) {
        if (webSocket != null) {
            // FastAPI 代理服務在收到第一個 JSON 時，會將其視為初始文本
            // 注意：這裡只是一個模擬，你需要根據你的 FastAPI 邏輯調整 JSON 結構
            String jsonMessage = "{\"initial_text\":\"" + text + "\"}";
            webSocket.send(jsonMessage);
            Log.d(TAG, "Sent initial text: " + text);
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnect");
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
    }

    // --- WebSocketListener 實作 ---

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        this.webSocket = webSocket;
        Log.i(TAG, "WebSocket connection opened successfully.");
        // 連線成功後，可以選擇在這裡發送初始文字訊息
        // sendInitialText("請問今天的天氣如何？");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        // 接收到文字訊息 (通常是狀態或錯誤訊息，例如 turn_complete)
        Log.d(TAG, "Received text message: " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        // 接收到二進制訊息 (TTS 語音數據)
        if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.play();
        }
        // 將 TTS 數據寫入 AudioTrack 進行即時播放
        audioTrack.write(bytes.toByteArray(), 0, bytes.size());
        // Log.d(TAG, "Received and played TTS chunk: " + bytes.size() + " bytes");
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Log.w(TAG, "WebSocket closing: " + code + " / " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(TAG, "WebSocket failure: " + t.getMessage(), t);
        if (response != null) {
            Log.e(TAG, "Failure response code: " + response.code());
        }
    }
}