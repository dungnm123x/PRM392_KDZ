package com.lucnthe.multiplegame.ui.tetris;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lucnthe.multiplegame.R;
import com.lucnthe.multiplegame.ui.leaderboard.LeaderboardEntry;


public class TetrisView extends View {

    private final int COLS = 10;
    private final int ROWS = 20;
    private int blockSize;
    private int offsetX = 0;
    private int offsetY = 0;// px
    private int[][] grid = new int[ROWS][COLS];
    private Paint paint = new Paint();
    private int score = 0;
    private int level = 1;
    private int linesCleared = 0;
    private long tickDelay = 500;
    private TextView scoreText;
    private TextView levelText;
    private boolean isPaused = false;
    private int highScore = 0;
    private SharedPreferences prefs;
    private TextView highScoreText;
    private Tetromino currentTetromino;
    private Handler handler = new Handler();
    private Runnable gameTick;
    private int[][] gridColor = new int[ROWS][COLS]; // màu tương ứng cho mỗi ô
    private SoundPool soundPool;
    private int soundTick;
    private int soundClear;

    public TetrisView(Context context) {
        super(context);
        init();
    }
    public TetrisView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        spawnNewTetromino();
        prefs = getContext().getSharedPreferences("TetrisPrefs", Context.MODE_PRIVATE);
        highScore = prefs.getInt("HIGH_SCORE", 0);
        soundPool = new SoundPool.Builder().setMaxStreams(2).build();
        soundTick = soundPool.load(getContext(), R.raw.block, 1);
        soundClear = soundPool.load(getContext(), R.raw.clear, 1);

        gameTick = new Runnable() {
            @Override
            public void run() {
                if (isPaused) return;
                update();
                invalidate();
                handler.postDelayed(this, tickDelay);// speed
            }
        };
        handler.postDelayed(gameTick, 500);
    }

    private void update() {
        if (!currentTetromino.moveDown(grid)) {
            currentTetromino.lockToGrid(grid, gridColor);
            int linesClearedThisTurn = clearLines();
            if (soundPool != null && soundTick != 0 && soundClear != 0) {
                if (linesClearedThisTurn > 0) {
                    soundPool.play(soundClear, 1, 1, 0, 0, 1);
                } else {
                    soundPool.play(soundTick, 1, 1, 0, 0, 1);
                }
            }

            spawnNewTetromino();
            if (currentTetromino.collides(grid)) {
                if (score > highScore) {
                    highScore = score;
                    prefs.edit().putInt("HIGH_SCORE", highScore).apply();
                    updateHighScoreDisplay();
                    Toast.makeText(getContext(), "🎉 New High Score!", Toast.LENGTH_SHORT).show();
                }

                // Game Over
                handler.removeCallbacks(gameTick);
                showGameOverDialog();

            }
        }
    }
    private void showGameOverDialog() {
        if (!(getContext() instanceof Activity)) return;
        Activity activity = (Activity) getContext();

        if (activity.isFinishing() || activity.isDestroyed()) return;

        isPaused = true;
        handler.removeCallbacks(gameTick);
        saveScoreToLeaderboard(score);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Game Over")
                .setMessage("Your Score: " + score + "\nHigh Score: " + highScore)
                .setCancelable(false)
                .setPositiveButton("Play Again", null)
                .setNegativeButton("Exit", (d, which) -> activity.finish())
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                dialog.dismiss();
                resetGame();
            });
        });

        dialog.show();
    }
    private void saveScoreToLeaderboard(int score) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    String username = userDoc.getString("username");

                    if (username == null || username.isEmpty()) {
                        Toast.makeText(getContext(), "Không thể lấy tên người dùng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("leaderboards")
                            .document("tetris")
                            .collection("scores")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                Long oldScore = snapshot.getLong("score");

                                if (oldScore == null || score > oldScore) {
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("username", username);
                                    data.put("score", score);
                                    data.put("timestamp", System.currentTimeMillis());

                                    db.collection("leaderboards")
                                            .document("tetris")
                                            .collection("scores")
                                            .document(uid)
                                            .set(data);
                                }
                            });
                });
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Tính blockSize sao cho lưới 10x20 vừa chiều cao
        blockSize = Math.min(w / COLS, h / ROWS);

        // Tính phần lề để canh giữa
        int boardWidth = blockSize * COLS;
        int boardHeight = blockSize * ROWS;

        offsetX = (w - boardWidth) / 2;
        offsetY = (h - boardHeight) / 2;
    }

    private void spawnNewTetromino() {
        currentTetromino = Tetromino.random(COLS);
    }

    private int clearLines() {
        int lines = 0;
        for (int r = ROWS - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                for (int row = r; row > 0; row--) {
                    grid[row] = Arrays.copyOf(grid[row - 1], COLS);
                    gridColor[row] = Arrays.copyOf(gridColor[row - 1], COLS);
                }
                grid[0] = new int[COLS];
                gridColor[0] = new int[COLS];
                r++;
                lines++;
            }
        }

        if (lines > 0) {
            score += lines * 100;
            linesCleared += lines;
            updateScoreDisplay();

            if (linesCleared >= level * 5) {
                level++;
                tickDelay = Math.max(100, tickDelay - 50); // Tăng tốc độ
                updateLevelDisplay();
            }
        }
        return lines;

    }
    private void updateScoreDisplay() {
        if (scoreText != null) {
            scoreText.setText("Score: " + score);
        }
    }

    private void updateLevelDisplay() {
        if (levelText != null) {
            levelText.setText("Level: " + level);
        }
    }
    private void resetGame() {
        // Reset dữ liệu
        grid = new int[ROWS][COLS];
        score = 0;
        level = 1;
        linesCleared = 0;
        tickDelay = 500;
        isPaused = false;
        gridColor = new int[ROWS][COLS];

        updateScoreDisplay();
        updateLevelDisplay();
        updateHighScoreDisplay();

        spawnNewTetromino();
        handler.postDelayed(gameTick, tickDelay);
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Kích thước & canh giữa
        int boardWidth = COLS * blockSize;
        int boardHeight = ROWS * blockSize;

        canvas.drawColor(Color.parseColor("#EEEEEE")); // nền sáng

        drawGridLines(canvas, offsetX, offsetY);       // lưới
        drawGrid(canvas, offsetX, offsetY);            // khối đã rơi
        currentTetromino.draw(canvas, blockSize, offsetX, offsetY); // khối đang rơi
    }

    private void drawBlock(Canvas canvas, int left, int top, int size, int color) {
        float radius = size * 0.06f;

        // Khối chính
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setAlpha(255); // không trong suốt
        canvas.drawRoundRect(left, top, left + size, top + size, radius, radius, paint);

        // Viền
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.DKGRAY);
        canvas.drawRoundRect(left, top, left + size, top + size, radius, radius, paint);

        // Highlight góc trên trái
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setAlpha(30); // nhẹ
        canvas.drawRoundRect(
                left + 2, top + 2,
                left + size * 0.4f, top + size * 0.4f,
                radius, radius, paint
        );
    }
    private void drawGrid(Canvas canvas, int offsetX, int offsetY) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != 0) {
                    int left = offsetX + c * blockSize;
                    int top = offsetY + r * blockSize;
                    int color = gridColor[r][c];
                    drawBlock(canvas, left, top, blockSize, color);
                }
            }
        }
    }

    private void drawGridLines(Canvas canvas, int offsetX, int offsetY) {
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(2f);

        // Dọc
        for (int c = 0; c <= COLS; c++) {
            float x = offsetX + c * blockSize;
            canvas.drawLine(x, offsetY, x, offsetY + ROWS * blockSize, paint);
        }

        // Ngang
        for (int r = 0; r <= ROWS; r++) {
            float y = offsetY + r * blockSize;
            canvas.drawLine(offsetX, y, offsetX + COLS * blockSize, y, paint);
        }
    }

    public void moveLeft() {
        if (isPaused) return; // 🔒 chặn thao tác khi đang pause
        if (!currentTetromino.collides(grid, currentTetromino.row, currentTetromino.col - 1)) {
            currentTetromino.col--;
            invalidate();
        }
    }

    public void moveRight() {
        if (isPaused) return;
        if (!currentTetromino.collides(grid, currentTetromino.row, currentTetromino.col + 1)) {
            currentTetromino.col++;
            invalidate();
        }
    }

    public void rotate() {
        if (isPaused) return;
        currentTetromino.rotate(grid);
        invalidate();
    }

    public void drop() {
        if (isPaused) return;
        while (currentTetromino.moveDown(grid)) {}
        update(); // lock and spawn
        invalidate();
    }

    public void setScoreText(TextView textView) {
        this.scoreText = textView;
        updateScoreDisplay();
    }

    public void setLevelText(TextView textView) {
        this.levelText = textView;
        updateLevelDisplay();
    }
    public boolean isPaused() {
        return isPaused;
    }

    public void pauseGame() {
        isPaused = true;
        handler.removeCallbacks(gameTick);
    }

    public void resumeGame() {
        if (!isPaused) return;
        isPaused = false;
        handler.postDelayed(gameTick, tickDelay);
    }
    public void setHighScoreText(TextView textView) {
        this.highScoreText = textView;
        updateHighScoreDisplay();
    }

    private void updateHighScoreDisplay() {
        if (highScoreText != null) {
            highScoreText.setText("High: " + highScore);
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

}
