package com.lucnthe.multiplegame.ui.tetris;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.lucnthe.multiplegame.R;
import com.lucnthe.multiplegame.ui.leaderboard.LeaderboardActivity;

public class TetrisActivity extends AppCompatActivity {
    private TetrisView tetrisView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetris);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tetris 🎮");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // hiện nút back
        }

        tetrisView = findViewById(R.id.tetrisView);
        tetrisView.setScoreText((TextView) findViewById(R.id.scoreText));
        tetrisView.setLevelText((TextView) findViewById(R.id.levelText));
        tetrisView.setHighScoreText((TextView) findViewById(R.id.highScoreText));
        findViewById(R.id.btnLeaderboard).setOnClickListener(v -> {
            Intent intent = new Intent(this, LeaderboardActivity.class);
            intent.putExtra("game", "tetris");
            startActivity(intent);
        });

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
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // quay về activity trước đó
        return true;
    }

}


