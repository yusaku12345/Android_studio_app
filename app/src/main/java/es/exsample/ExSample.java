package es.exsample;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class ExSample extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener accelerometerListener;
    private TextView xAxisTextView;
    private TextView yAxisTextView;
    private TextView zAxisTextView;
    private static final double FALL_THRESHOLD = 18.0;  // 落下検知の閾値
    private MediaPlayer mediaPlayer;

    // タイマー関連のビューとオブジェクト
    private TextView timerTextView;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private Handler timerHandler = new Handler();
    private long startTime = 0L;
    private long timeInMillis = 0L;
    private long timeSwapBuff = 0L;
    private long updateTime = 0L;
    private boolean isRunning = false;

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMillis = System.currentTimeMillis() - startTime;
            updateTime = timeSwapBuff + timeInMillis;
            int secs = (int) (updateTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updateTime % 1000);
            timerTextView.setText("" + mins + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            timerHandler.postDelayed(this, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost th = findViewById(android.R.id.tabhost);  // TabHostオブジェクト取得
        th.setup();  // TabHostのセットアップ

        TabHost.TabSpec tab1 = th.newTabSpec("tab1");  // tab1のセットアップ
        tab1.setIndicator("アプリの説明");  // タブ名の設定
        tab1.setContent(R.id.tab1);   // タブに使うリニアレイアウトの指定
        th.addTab(tab1);  // タブホストにタブを追加

        TabHost.TabSpec tab2 = th.newTabSpec("tab2");  // tab2のセットアップ
        tab2.setIndicator("除雪時のアドバイス");
        tab2.setContent(R.id.tab2);
        th.addTab(tab2);

        TabHost.TabSpec tab3 = th.newTabSpec("tab3"); // tab3のセットアップ
        tab3.setIndicator("新潟の天気");
        tab3.setContent(R.id.tab3);
        th.addTab(tab3);

        TabHost.TabSpec tab4 = th.newTabSpec("tab4"); // tab4のセットアップ
        tab4.setIndicator("加速度センサ");
        tab4.setContent(R.id.tab4);
        th.addTab(tab4);

        TabHost.TabSpec tab5 = th.newTabSpec("tab5"); // tab5のセットアップ
        tab5.setIndicator("タイマー");
        tab5.setContent(R.id.tab5);
        th.addTab(tab5);

        th.setCurrentTab(0); // 最初のタブをtab1に設定

        // タイマー関連のビューの初期化
        timerTextView = findViewById(R.id.timer_text_view);
        startButton = findViewById(R.id.start_button);
        pauseButton = findViewById(R.id.pause_button);
        resetButton = findViewById(R.id.reset_button);

        // スタートボタンのクリックリスナー
        startButton.setOnClickListener(v -> {
            if (!isRunning) {
                startTime = System.currentTimeMillis();
                timerHandler.postDelayed(updateTimerThread, 0);
                isRunning = true;
            }
        });

        // ポーズボタンのクリックリスナー
        pauseButton.setOnClickListener(v -> {
            if (isRunning) {
                timeSwapBuff += timeInMillis;
                timerHandler.removeCallbacks(updateTimerThread);
                isRunning = false;
            }
        });

        // リセットボタンのクリックリスナー
        resetButton.setOnClickListener(v -> {
            startTime = 0L;
            timeInMillis = 0L;
            timeSwapBuff = 0L;
            updateTime = 0L;
            timerTextView.setText("00:00:000");
            if (isRunning) {
                timerHandler.removeCallbacks(updateTimerThread);
                isRunning = false;
            }
        });

        // 加速度センサのセットアップ
        xAxisTextView = findViewById(R.id.x_axis);
        yAxisTextView = findViewById(R.id.y_axis);
        zAxisTextView = findViewById(R.id.z_axis);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            accelerometerListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];

                        xAxisTextView.setText("X軸：" + x);
                        yAxisTextView.setText("Y軸：" + y);
                        zAxisTextView.setText("Z軸：" + z);

                        double magnitude = Math.sqrt(x * x + y * y + z * z);
                        if (magnitude > FALL_THRESHOLD) {
                            onFallDetected();
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };
        }

        // タブ3のWebView設定
        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://tenki.jp/forecast/4/18/");

        // MediaPlayerを初期化
        mediaPlayer = MediaPlayer.create(this, R.raw.se_ymd06);
        mediaPlayer.setLooping(true);  // 音をループ再生

        // キャンセルボタンのセットアップ
        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop(); // 音を停止
                try {
                    mediaPlayer.prepare();
                    mediaPlayer.seekTo(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null && accelerometerListener != null) {
            sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometerListener != null) {
            sensorManager.unregisterListener(accelerometerListener);
        }
    }

    private void onFallDetected() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();  // 音を再生
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
