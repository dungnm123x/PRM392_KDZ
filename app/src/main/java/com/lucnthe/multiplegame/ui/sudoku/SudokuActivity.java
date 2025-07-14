package com.lucnthe.multiplegame.ui.sudoku;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lucnthe.multiplegame.R;
import com.lucnthe.multiplegame.ui.leaderboard.LeaderboardActivity;

import java.util.HashMap;
import java.util.Map;

public class SudokuActivity extends AppCompatActivity {
    private SudokuBoardView boardView;
    private SudokuGame sudokuGame;
    private FrameLayout container;
    private String currentDifficulty = "medium";
    private GridLayout keyboard;
    private boolean hasCheckedContinueDialog = false;
    private int mistakeCount = 0;
    private TextView tvMistake;
    private int hintLeft = 3;
    private TextView tvHintCount;

    private long startTime;
    private TextView tvTimer;
    private TextView tvCurrentScore;
    private TextView tvBestScore;

    private final Handler timerHandler = new Handler();
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = System.currentTimeMillis() - startTime;
            int seconds = (int) (elapsed / 1000);
            int minutes = seconds / 60;
            seconds %= 60;

            tvTimer.setText(String.format("⏱ %02d:%02d", minutes, seconds));
            long score = calculateScore(elapsed);
            tvCurrentScore.setText("Điểm hiện tại: " + sudokuGame.getCurrentScore());
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sudoku 🧩");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Button btnHint = findViewById(R.id.btnHint);
        Button btnSolve = findViewById(R.id.btnSolve);
        Button btnReset = findViewById(R.id.btnReset);
        Button btnNew = findViewById(R.id.btnNewGame);
        Button btnLeaderboard = findViewById(R.id.btnLeaderboard);

        tvTimer = findViewById(R.id.tvTimer);
        tvCurrentScore = findViewById(R.id.tvCurrentScore);
        tvMistake = findViewById(R.id.tvMistake);
        tvBestScore = findViewById(R.id.tvBestScore);
        container = findViewById(R.id.board_container);
        keyboard = findViewById(R.id.keyboard);
        tvHintCount = findViewById(R.id.tvHintCount);

        btnHint.setOnClickListener(v -> {
            if (hintLeft <= 0) {
                Toast.makeText(this, "❗Bạn đã dùng hết 3 lần gợi ý!", Toast.LENGTH_SHORT).show();
                return;
            }

            int[][] solvedBoard = gameCopy(sudokuGame.board);
            if (!SudokuGenerator.solveSudoku(solvedBoard)) {
                Toast.makeText(this, "❌ Không thể gợi ý!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean hintPlaced = false;
            for (int row = 0; row < 9 && !hintPlaced; row++) {
                for (int col = 0; col < 9 && !hintPlaced; col++) {
                    if (sudokuGame.getNumber(row, col) == 0) {
                        int correctNumber = solvedBoard[row][col];
                        sudokuGame.setNumber(row, col, correctNumber);
                        boardView.invalidate();
                        hintPlaced = true;
                        hintLeft--;
                        tvHintCount.setText(String.valueOf(hintLeft));
                        Toast.makeText(this, "💡 Gợi ý: hàng " + (row + 1) + ", cột " + (col + 1), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            if (!hintPlaced) {
                Toast.makeText(this, "✅ Bạn đã điền xong!", Toast.LENGTH_SHORT).show();
            }
        });

        btnSolve.setOnClickListener(v -> {
            int[][] currentBoard = gameCopy(sudokuGame.board);
            if (SudokuGenerator.solveSudoku(currentBoard)) {
                sudokuGame.board = currentBoard;

                int finalScore = sudokuGame.getCurrentScore();
                boardView.invalidate();
                updateCurrentScoreView();
                updateKeyboardStatus();
                stopTimer();
                saveScoreToLeaderboard(finalScore);

                new AlertDialog.Builder(this)
                        .setTitle("🎉 Hoàn thành!")
                        .setMessage("Bạn đã giải xong bảng Sudoku.\nĐiểm: " + finalScore)
                        .setPositiveButton("OK", (dialog, which) -> {
                            clearSavedGame();
                            showDifficultyDialog();
                        })
                        .setCancelable(false)
                        .show();
            } else {
                Toast.makeText(this, "❌ Không thể giải được bảng hiện tại!", Toast.LENGTH_SHORT).show();
            }
        });

        btnReset.setOnClickListener(v -> {
            sudokuGame = new SudokuGame(currentDifficulty);
            boardView.setGame(sudokuGame);
            boardView.invalidate();
            resetTimer();
        });

        btnNew.setOnClickListener(v -> {
            clearSavedGame();
            showDifficultyDialog();
        });

        btnLeaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, LeaderboardActivity.class);
            intent.putExtra("game", "sudoku");
            startActivity(intent);
        });

        for (int i = 0; i < keyboard.getChildCount(); i++) {
            View child = keyboard.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                String value = btn.getText().toString();
                if (!value.equals("X")) {
                    btn.setTag(R.id.key_number_tag, Integer.parseInt(value));
                }
                btn.setOnClickListener(v -> {
                    String val = ((Button) v).getText().toString();
                    if (val.equals("X")) {
                        boardView.inputNumber(0);
                    } else {
                        Integer number = (Integer) v.getTag(R.id.key_number_tag);
                        if (number != null) boardView.inputNumber(number);
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasCheckedContinueDialog) {
            checkContinueGame();
            hasCheckedContinueDialog = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveGameToPrefs();
        stopTimer();
    }

    private void checkContinueGame() {
        SharedPreferences prefs = getSharedPreferences("sudoku", MODE_PRIVATE);
        if (prefs.contains("board")) {
            new AlertDialog.Builder(this)
                    .setTitle("Tiếp tục ván cũ?")
                    .setMessage("Bạn có muốn tiếp tục ván Sudoku trước đó?")
                    .setPositiveButton("Tiếp tục", (d, w) -> loadGameFromPrefs())
                    .setNegativeButton("Chơi mới", (d, w) -> {
                        clearSavedGame();
                        showDifficultyDialog();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            showDifficultyDialog();
        }
    }

    private void loadGameFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("sudoku", MODE_PRIVATE);
        String[] parts = prefs.getString("board", "").split(",");
        String[] fixedParts = prefs.getString("fixed", "").split(",");

        if (parts.length != 81 || fixedParts.length != 81) {
            showCorruptedDataFallback();
            return;
        }

        int[][] board = new int[9][9];
        boolean[][] fixedCells = new boolean[9][9];
        int index = 0;
        try {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    board[i][j] = Integer.parseInt(parts[index]);
                    fixedCells[i][j] = fixedParts[index].equals("1");
                    index++;
                }
            }
        } catch (NumberFormatException e) {
            showCorruptedDataFallback();
            return;
        }

        mistakeCount = 0;
        tvMistake.setText("Lỗi: 0/3");

        hintLeft = prefs.getInt("hintLeft", 3); // ✅ load hintLeft
        tvHintCount.setText(String.valueOf(hintLeft));

        currentDifficulty = prefs.getString("difficulty", "medium");
        sudokuGame = new SudokuGame(board, fixedCells);
        sudokuGame.addScore((int) prefs.getLong("score", 0));

        boardView = new SudokuBoardView(this, sudokuGame);
        container.removeAllViews();
        container.addView(boardView);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        boardView.setLayoutParams(params);

        startTime = prefs.getLong("startTime", System.currentTimeMillis());
        timerHandler.postDelayed(timerRunnable, 0);

        updateCurrentScoreView();
        updateKeyboardStatus();
        updateBestScoreFromFirebase();
    }

    private void showCorruptedDataFallback() {
        Toast.makeText(this, "❌ Dữ liệu cũ bị lỗi. Bắt đầu mới.", Toast.LENGTH_SHORT).show();
        clearSavedGame();
        showDifficultyDialog();
    }

    private void saveGameToPrefs() {
        StringBuilder boardBuilder = new StringBuilder();
        StringBuilder fixedBuilder = new StringBuilder();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                boardBuilder.append(sudokuGame.board[i][j]).append(",");
                fixedBuilder.append(sudokuGame.getFixedCells()[i][j] ? "1" : "0").append(",");
            }
        }

        getSharedPreferences("sudoku", MODE_PRIVATE).edit()
                .putString("board", boardBuilder.toString())
                .putString("fixed", fixedBuilder.toString())
                .putString("difficulty", currentDifficulty)
                .putLong("score", sudokuGame.getCurrentScore())
                .putLong("startTime", startTime)
                .putInt("hintLeft", hintLeft) // ✅ save hintLeft
                .apply();
    }

    private void clearSavedGame() {
        getSharedPreferences("sudoku", MODE_PRIVATE).edit().clear().apply();
    }

    public void showDifficultyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Chọn độ khó")
                .setItems(new String[]{"Dễ", "Trung bình", "Khó"}, (dialog, which) -> {
                    switch (which) {
                        case 0: currentDifficulty = "easy"; break;
                        case 2: currentDifficulty = "hard"; break;
                        default: currentDifficulty = "medium"; break;
                    }
                    mistakeCount = 0;
                    tvMistake.setText("Lỗi: 0/3");
                    hintLeft = 3;
                    tvHintCount.setText("3");
                    sudokuGame = new SudokuGame(currentDifficulty);
                    boardView = new SudokuBoardView(this, sudokuGame);
                    container.removeAllViews();
                    container.addView(boardView);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.gravity = Gravity.CENTER;
                    boardView.setLayoutParams(params);

                    resetTimer();
                    tvCurrentScore.setText("Điểm hiện tại: 0");
                    updateKeyboardStatus();
                    updateBestScoreFromFirebase();
                })
                .setCancelable(false)
                .show();
    }

    private int[][] gameCopy(int[][] original) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, 9);
        }
        return copy;
    }

    public void updateKeyboardStatus() {
        for (int i = 0; i < keyboard.getChildCount(); i++) {
            View child = keyboard.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                Object tag = btn.getTag(R.id.key_number_tag);
                if (tag instanceof Integer) {
                    int number = (int) tag;
                    int count = sudokuGame.countNumber(number);

                    if (count == 9) {
                        btn.setText("✓");
                        btn.setEnabled(false);
                    } else {
                        btn.setText(String.valueOf(number));
                        btn.setEnabled(true);
                    }
                }
            }
        }
    }

    public void updateCurrentScoreView() {
        if (tvCurrentScore != null && sudokuGame != null) {
            tvCurrentScore.setText("Điểm hiện tại: " + sudokuGame.getCurrentScore());
        }
    }

    private void updateBestScoreFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("leaderboards")
                .document("sudoku")
                .collection("scores")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Long score = snapshot.getLong("score");
                    if (score != null) {
                        tvBestScore.setText("🏆 Kỷ lục: " + score);
                    }
                });
    }

    public void increaseMistakeCount() {
        mistakeCount++;
        tvMistake.setText("Lỗi: " + mistakeCount + "/3");

        if (mistakeCount >= 3) {
            stopTimer();
            new AlertDialog.Builder(this)
                    .setTitle("💥 Thua cuộc")
                    .setMessage("Bạn đã mắc quá 3 lỗi.\nBắt đầu ván mới?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        clearSavedGame();
                        showDifficultyDialog();
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    public long calculateScore(long elapsedMillis) {
        int baseScore = 100000;
        int difficultyMultiplier;

        switch (currentDifficulty) {
            case "easy":
                difficultyMultiplier = 1;
                break;
            case "medium":
                difficultyMultiplier = 2;
                break;
            case "hard":
                difficultyMultiplier = 3;
                break;
            default:
                difficultyMultiplier = 1;
        }

        return Math.max(100, (baseScore - elapsedMillis / 10) * difficultyMultiplier);
    }

    private void resetTimer() {
        startTime = System.currentTimeMillis();
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 0);
    }

    public void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    public void saveScoreToLeaderboard(long score) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String username = doc.getString("username");

                    if (username == null || username.isEmpty()) {
                        Toast.makeText(this, "Không thể lấy tên người dùng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("leaderboards")
                            .document("sudoku")
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
                                            .document("sudoku")
                                            .collection("scores")
                                            .document(uid)
                                            .set(data);

                                    String key = "best_score_" + currentDifficulty;
                                    getSharedPreferences("profile", MODE_PRIVATE)
                                            .edit()
                                            .putLong(key, score)
                                            .apply();
                                }
                            });
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}