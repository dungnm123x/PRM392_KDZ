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

    private int[][] board = new int[4][4];
    private TextView[][] cells = new TextView[4][4];
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
        loadGame();        // ← phải load dữ liệu trước khi hiển thị
        updateScoreUI();
        updateUI();

    }

    private void initBoard() {
        gridLayout.removeAllViews();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                TextView tv = new TextView(this);
                tv.setText("");
                tv.setGravity(Gravity.CENTER);
                tv.setBackgroundColor(Color.LTGRAY);
                tv.setTextSize(28);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                params.width = 0;
                params.height = 0;
                params.setMargins(8, 8, 8, 8);

                tv.setLayoutParams(params);
                gridLayout.addView(tv);
                cells[i][j] = tv;
            }
        }
    }

    private void resetGame() {
        score = 0;
        board = new int[4][4];
        spawnRandom();
        spawnRandom();
        updateUI();
        updateScoreUI();
        saveGame(); // lưu lại reset
    }

    private void saveGame() {
        SharedPreferences prefs = getSharedPreferences("game2048", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                editor.putInt("cell_" + i + "_" + j, board[i][j]);

        editor.putInt("score", score);
        editor.apply();
    }
    private void loadGame() {
        SharedPreferences prefs = getSharedPreferences("game2048", MODE_PRIVATE);

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                board[i][j] = prefs.getInt("cell_" + i + "_" + j, 0);

        score = prefs.getInt("score", 0);
    }

    private void updateUI() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
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
            case 0: return Color.parseColor("#CDC1B4"); // ô trống
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
            default: return Color.parseColor("#3C3A32"); // > 2048
        }
    }

    private void spawnRandom() {
        int x, y;
        if (isBoardFull()) return;

        do {
            x = random.nextInt(4);
            y = random.nextInt(4);
        } while (board[x][y] != 0);

        board[x][y] = random.nextInt(10) < 9 ? 2 : 4; // 90% là 2, 10% là 4
    }
    private boolean isBoardFull() {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (board[i][j] == 0) return false;
        return true;
    }


    private boolean slideLeft() {
        boolean moved = false;

        for (int i = 0; i < 4; i++) {
            int[] newRow = new int[4];
            int index = 0;
            int last = 0;
            for (int j = 0; j < 4; j++) {
                if (board[i][j] != 0) {
                    if (board[i][j] == last) {
                        newRow[index - 1] *= 2;
                        score += newRow[index - 1];
                        if (newRow[index - 1] == 2048) {
                            showWinDialog();
                        }
                        last = 0;
                        moved = true;
                    }
                    else {
                        newRow[index++] = board[i][j];
                        last = board[i][j];
                    }
                }
            }

            for (int j = 0; j < 4; j++) {
                if (board[i][j] != newRow[j]) {
                    moved = true;
                }
                board[i][j] = newRow[j];
            }
        }
        return moved;
    }
    private void showWinDialog() {
        uploadScoreToFirebase(); // Gửi điểm
        new AlertDialog.Builder(this)
                .setTitle("Bạn đã thắng!")
                .setMessage("Chúc mừng, bạn đã đạt 2048!")
                .setPositiveButton("Tiếp tục", null)
                .setNegativeButton("Reset", (d, w) -> resetGame())
                .show();
    }
    private boolean canMove() {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0) return true;
                if (j < 3 && board[i][j] == board[i][j+1]) return true;
                if (i < 3 && board[i][j] == board[i+1][j]) return true;
            }
        return false;
    }

    private void rotateBoardCW() {
        int[][] newBoard = new int[4][4];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                newBoard[j][3 - i] = board[i][j];
        board = newBoard;
    }

    private void move(Direction dir) {
        boolean moved = false;

        for (int i = 0; i < dir.rotations; i++) rotateBoardCW();

        moved = slideLeft();

        for (int i = 0; i < (4 - dir.rotations) % 4; i++) rotateBoardCW();

        if (moved) {
            spawnRandom();
            if (!canMove()) {
                uploadScoreToFirebase(); // Ghi điểm trước khi Dialog hiện
                showGameOverDialog();
            }
            updateUI();
            updateScoreUI();
        }
    }

    private void showGameOverDialog() {
        uploadScoreToFirebase(); // Gửi điểm
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

                    // Truy vấn điểm hiện tại đã lưu trên leaderboard
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

        if (score > 0) {
            uploadScoreToFirebase(); // ghi điểm nếu đã chơi
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (isFirstTime()) {
            resetGame(); // khởi tạo game mới
        } else {
            loadGame();
            updateUI();
            updateScoreUI();
        }
    }

    private boolean isFirstTime() {
        SharedPreferences prefs = getSharedPreferences("game2048", MODE_PRIVATE);
        return prefs.getInt("cell_0_0", -1) == -1; // nếu chưa có dữ liệu lưu
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
