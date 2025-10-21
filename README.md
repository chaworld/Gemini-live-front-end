# Gemini Live Front-End

這是一個 Android 應用程式，用於與 Gemini Live API 進行即時語音互動。該應用透過 WebSocket 連接到後端服務，支援語音輸入和 TTS 語音輸出功能。

> ⚠️ **重要提醒**：此應用需要搭配後端服務使用，請參考 [Gemini-live-backend](https://github.com/chaworld/Gemini-live-backend) 專案

## 功能特色

- 🎤 **即時語音對話**：透過 WebSocket 與 Gemini API 後端服務即時通訊
- 🔊 **音訊串流傳輸**：支援雙向音訊資料流處理
- 🗣️ **語音互動**：即時語音轉文字（STT）和文字轉語音（TTS）功能
- 📱 **原生 Android 體驗**：使用原生 Android 平台與 Gemini 進行對話互動

## 系統需求

- Android SDK 33 (Android 13) 或以上版本
- 已啟動的後端服務（[Gemini-live-backend](https://github.com/chaworld/Gemini-live-backend)）
- 麥克風權限

## 技術架構

```
Android 應用 (本專案)
    ↕ WebSocket
FastAPI 後端服務
    ↕
Google Gemini API
```

## 技術棧

- **開發語言**：Java
- **最低 SDK**：API 33 (Android 13)
- **目標 SDK**：API 36
- **核心函式庫**：
  - `OkHttp 4.12.0` - WebSocket 客戶端
  - `AudioTrack` - Android 音訊系統
  - `AudioRecord` - 音訊錄製
  - `AndroidX` - 核心元件

## 安裝步驟

### 1. 克隆專案

```bash
git clone https://github.com/chaworld/Gemini-live-front-end.git
cd Gemini-live-front-end
```

### 2. 開啟專案

使用 Android Studio 開啟專案：
1. 開啟 Android Studio
2. 選擇「Open」
3. 選擇專案目錄

### 3. 同步 Gradle

Android Studio 會自動提示同步 Gradle，點擊「Sync Now」等待依賴下載完成。

### 4. 設定後端服務地址

在應用程式碼中找到 WebSocket 連線設定，修改為你的後端服務地址：

```java
// 預設為 localhost，若使用實體裝置請改為電腦的區網 IP
String WS_URL = "ws://localhost:8080/ws/gemini_live";
// 例如：String WS_URL = "ws://192.168.1.100:8080/ws/gemini_live";
```

### 5. 執行應用

1. 連接 Android 裝置或啟動模擬器
2. 點擊「Run」按鈕或使用快捷鍵 `Shift + F10`
3. 選擇目標裝置

## 使用方法

### 前置準備

1. **啟動後端服務**：確保 [Gemini-live-backend](https://github.com/chaworld/Gemini-live-backend) 已經執行
   ```bash
   uvicorn api_server:app --host 0.0.0.0 --port 8080
   ```

2. **確認網路連線**：
   - 模擬器使用 `localhost` 或 `10.0.2.2`
   - 實體裝置使用電腦的區網 IP 地址

### 操作步驟

1. 開啟應用程式
2. 授予麥克風權限
3. 點擊「開始對話」按鈕
4. 對著麥克風說話
5. 等待 Gemini 的語音回應
6. 點擊「結束對話」停止錄音

## 專案結構

```
Gemini-live-front-end/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/gemini_live/
│   │   │   │   └── MainActivity.java        # 主要活動
│   │   │   ├── res/                          # 資源檔案
│   │   │   └── AndroidManifest.xml          # 應用配置
│   │   └── test/                             # 測試檔案
│   └── build.gradle                          # 應用層級 Gradle 配置
├── gradle/                                   # Gradle 包裝器
├── build.gradle                              # 專案層級 Gradle 配置
├── settings.gradle                           # Gradle 設定
└── README.md                                 # 專案說明文件
```

## 音訊規格

- **取樣率**：24000 Hz
- **聲道**：單聲道 (Mono)
- **編碼格式**：PCM 16-bit
- **緩衝大小**：4800 bytes per chunk

## 權限說明

應用需要以下權限：

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

- `RECORD_AUDIO`：用於錄製麥克風音訊
- `INTERNET`：用於與後端服務通訊

## 故障排除

### 連線失敗

1. **檢查後端服務**：確認後端服務是否正常運行
   ```bash
   curl http://localhost:8080
   ```

2. **檢查網路設定**：
   - 模擬器：使用 `10.0.2.2` 代替 `localhost`
   - 實體裝置：確認手機與電腦在同一區網

3. **防火牆設定**：確認防火牆允許 8080 端口連線

### 音訊問題

1. **權限檢查**：確認已授予麥克風權限
2. **裝置兼容性**：確認裝置支援 24000 Hz 取樣率
3. **音量設定**：調整裝置音量設定

### 編譯錯誤

1. **Gradle 同步**：執行 `File > Sync Project with Gradle Files`
2. **清理專案**：執行 `Build > Clean Project`
3. **重建專案**：執行 `Build > Rebuild Project`

## 開發注意事項

1. **API 層級**：最低支援 Android 13 (API 33)
2. **網路安全**：開發階段可使用 HTTP，生產環境建議使用 HTTPS
3. **音訊緩衝**：根據裝置效能調整緩衝區大小
4. **生命週期管理**：注意處理 Activity 生命週期中的 WebSocket 連線

## 相關專案

- [Gemini-live-backend](https://github.com/chaworld/Gemini-live-backend) - 後端 WebSocket 代理服務

## 授權條款

MIT License

## 貢獻

歡迎提交 Issue 或 Pull Request！

## 聯絡方式

如有任何問題，歡迎開啟 Issue 討論。

---

**Made with ❤️ for Gemini Live API**
