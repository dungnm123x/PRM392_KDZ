package com.lucnthe.multiplegame.ui.game2048;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lucnthe.multiplegame.R;
import com.lucnthe.multiplegame.ui.leaderboard.LeaderboardActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class game2048activity extends AppCompatActivity {

    private static final int SIZE = 6;
    private int[][] board = new int[SIZE][SIZE];
    private TextView[][] cells = new TextView[SIZE][SIZE];
    private GridLayout gridLayout;
    private Random random = new Random();
    private GestureDetector gestureDetector;
    private int score = 0;
    private TextView tvScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2468);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Game 2048");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvScore = findViewById(R.id.tvScore);
        findViewById(R.id.btnReset).setOnClickListener(v -> resetGame());
        findViewById(R.id.btnExit).setOnClickListener(v -> finish());
        findViewById(R.id.btnLeaderboard).setOnClickListener(v -> {
            Intent intent = new Intent(this, LeaderboardActivity.class);
            intent.putExtra("game", "game2048");
            startActivity(intent);
        });

        gridLayout = findViewById(R.id.gridLayout);
        gestureDetector = new GestureDetector(this, new SwipeGesture());

        initBoard();
        loadGame();
        updateScoreUI();
        updateUI();
    }

    private void initBoard() {
        gridLayout.removeAllViews();
        gridLayout.setRowCount(SIZE);
        gridLayout.setColumnCount(SIZE);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                TextView tv = new TextView(this);
                tv.setText("");
                tv.setGravity(Gravity.CENTER);
                tv.setBackgroundColor(Color.LTGRAY);
                tv.setTextSize(18);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                params.width = 0;
                params.height = 0;
                params.setMargins(6, 6, 6, 6);

                tv.setLayoutParams(params);
                gridLayout.addView(tv);
                cells[i][j] = tv;
            }
        }
    }

    private void resetGame() {
        score = 0;
        board = new int[SIZE][SIZE];
        spawnRandom();
        spawnRandom();
        updateUI();
        updateScoreUI();
        saveGame();
    }

    private void saveGame() {
        SharedPreferences prefs = getSharedPreferences("game2048", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                editor.putInt("cell_" + i + "_" + j, board[i][j]);

        editor.putInt("score", score);
        editor.apply();
    }

    private void loadGame() {
        SharedPreferences prefs = getSharedPreferences("game2048", MODE_PRIVATE);

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                board[i][j] = prefs.getInt("cell_" + i + "_" + j, 0);

        score = prefs.getInt("score", 0);
    }

    private void updateUI() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int val = board[i][j];
                cells[i][j].setText(val == 0 ? "" : String.valueOf(val));
                cells[i][j].setBackgroundColor(getColorForValue(val));
                cells[i][j].setTextColor(val <= 4 ? Color.BLACK : Color.WHITE);
            }
        }
    }

    private void updateScoreUI() {
        tvScore.setText("Score: " + score);
    }

    private int getColorForValue(int value) {
        switch (value) {
            case 0: return Color.parseColor("#CDC1B4");
            case 2: return Color.parseColor("#EEE4DA");
            case 4: return Color.parseColor("#EDE0C8");
            case 8: return Color.parseColor("#F2B179");
            case 16: return Color.parseColor("#F59563");
            case 32: return Color.parseColor("#F67C5F");
            case 64: return Color.parseColor("#F65E3B");
            case 128: return Color.parseColor("#EDCF72");
            case 256: return Color.parseColor("#EDCC61");
            case 512: return Color.parseColor("#EDC850");
            case 1024: return Color.parseColor("#EDC53F");
            case 2048: return Color.parseColor("#EDC22E");
            default: return Color.parseColor("#3C3A32");
        }
    }

    private void spawnRandom() {
        if (isBoardFull()) return;
        int x, y;
        do {
            x = random.nextInt(SIZE);
            y = random.nextInt(SIZE);
        } while (board[x][y] != 0);

        board[x][y] = random.nextInt(10) < 9 ? 2 : 4;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (board[i][j] == 0) return false;
        return true;
    }

    private boolean slideLeft() {
        boolean moved = false;

        for (int i = 0; i < SIZE; i++) {
            int[] newRow = new int[SIZE];
            int index = 0;
            int last = 0;
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != 0) {
                    if (board[i][j] == last) {
                        newRow[index - 1] *= 2;
                        score += newRow[index - 1];
                        if (newRow[index - 1] == 2048) showWinDialog();
                        last = 0;
                        moved = true;
                    } else {
                        newRow[index++] = board[i][j];
                        last = board[i][j];
                    }
                }
            }
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != newRow[j]) moved = true;
                board[i][j] = newRow[j];
            }
        }
        return moved;
    }

    private void rotateBoardCW() {
        int[][] newBoard = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                newBoard[j][SIZE - 1 - i] = board[i][j];
        board = newBoard;
    }

    private boolean canMove() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) return true;
                if (j < SIZE - 1 && board[i][j] == board[i][j + 1]) return true;
                if (i < SIZE - 1 && board[i][j] == board[i + 1][j]) return true;
            }
        return false;
    }

    private void move(Direction dir) {
        for (int i = 0; i < dir.rotations; i++) rotateBoardCW();

        boolean moved = slideLeft();

        for (int i = 0; i < (4 - dir.rotations) % 4; i++) rotateBoardCW();

        if (moved) {
            spawnRandom();
            if (!canMove()) {
                uploadScoreToFirebase();
                showGameOverDialog();
            }
            updateUI();
            updateScoreUI();
        }
    }

    private void showWinDialog() {
        uploadScoreToFirebase();
        new AlertDialog.Builder(this)
                .setTitle("Bạn đã thắng!")
                .setMessage("Chúc mừng, bạn đã đạt 2048!")
                .setPositiveButton("Tiếp tục", null)
                .setNegativeButton("Reset", (d, w) -> resetGame())
                .show();
    }

    private void showGameOverDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("Không còn nước đi nào hợp lệ!")
                .setPositiveButton("Thử lại", (d, w) -> resetGame())
                .setNegativeButton("Thoát", (d, w) -> finish())
                .show();
    }

    enum Direction {
        UP(3), DOWN(1), LEFT(0), RIGHT(2);
        int rotations;
        Direction(int r) { this.rotations = r; }
    }

    class SwipeGesture extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
            float dx = e2.getX() - e1.getX();
            float dy = e2.getY() - e1.getY();
            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > SWIPE_THRESHOLD) move(Direction.RIGHT);
                else if (dx < -SWIPE_THRESHOLD) move(Direction.LEFT);
            } else {
                if (dy > SWIPE_THRESHOLD) move(Direction.DOWN);
                else if (dy < -SWIPE_THRESHOLD) move(Direction.UP);
            }
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private void uploadScoreToFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String username = doc.getString("username");

                    db.collection("leaderboards")
                            .document("game2048")
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
                                            .document("game2048")
                                            .collection("scores")
                                            .document(uid)
                                            .set(data);
                                }
                            });
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveGame();
        if (score > 0) uploadScoreToFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstTime()) resetGame();
        else {
            loadGame();
            updateUI();
            updateScoreUI();
        }
    }

    private boolean isFirstTime() {
        SharedPreferences prefs = getSharedPreferences("game2048", MODE_PRIVATE);
        return prefs.getInt("cell_0_0", -1) == -1;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
