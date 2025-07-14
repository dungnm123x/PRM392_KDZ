package com.lucnthe.multiplegame.ui.tetris;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lucnthe.multiplegame.R;

public class TetrisActivity extends AppCompatActivity {
    private TetrisView tetrisView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetris);

        tetrisView = findViewById(R.id.tetrisView);
        tetrisView.setScoreText((TextView) findViewById(R.id.scoreText));
        tetrisView.setLevelText((TextView) findViewById(R.id.levelText));
        tetrisView.setHighScoreText((TextView) findViewById(R.id.highScoreText));

        findViewById(R.id.btnLeft).setOnClickListener(v -> tetrisView.moveLeft());
        findViewById(R.id.btnRight).setOnClickListener(v -> tetrisView.moveRight());
        findViewById(R.id.btnRotate).setOnClickListener(v -> tetrisView.rotate());
        findViewById(R.id.btnDrop).setOnClickListener(v -> tetrisView.drop());
        Button pauseButton = findViewById(R.id.btnPause);
        pauseButton.setOnClickListener(v -> {
            if (tetrisView.isPaused()) {
                tetrisView.resumeGame();
                pauseButton.setText("Pause");
            } else {
                tetrisView.pauseGame();
                pauseButton.setText("Resume");
            }
        });

    }
}


