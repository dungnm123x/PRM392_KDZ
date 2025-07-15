package com.lucnthe.multiplegame.ui.xo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.lucnthe.multiplegame.R;

public class XOActivity extends AppCompatActivity {
    private final int SIZE = 6;
    private Button[][] buttons = new Button[SIZE][SIZE];
    private boolean isXTurn = true;
    private boolean gameEnded = false;
    private GameMode currentMode = GameMode.TWO_PLAYER;
    private DifficultyLevel difficultyLevel = DifficultyLevel.EASY;

    private TextView txtTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        GridLayout grid = findViewById(R.id.gridLayout);
        txtTurn = findViewById(R.id.txtTurn);
        Button btnRestart = findViewById(R.id.btnRestart);

        showGameModeDialog();

        // ✅ Tính kích thước mỗi ô sao cho vuông
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int totalPadding = 32 + 10 * 2 + (SIZE - 1) * 8; // padding layout + grid padding + margin mỗi ô
        int cellSize = (screenWidth - totalPadding) / SIZE;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                final int r = row;
                final int c = col;

                Button button = new Button(this);
                button.setText("");
                button.setTextSize(20);
                button.setBackgroundColor(Color.LTGRAY);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                params.setMargins(4, 4, 4, 4); // viền giữa các ô
                button.setLayoutParams(params);

                button.setOnClickListener(v -> handleClick(r, c));
                grid.addView(button);
                buttons[row][col] = button;
            }
        }

        btnRestart.setOnClickListener(v -> resetGame());
    }

    private void showGameModeDialog() {
        String[] options = {"2 Người", "Chơi với Máy"};

        new AlertDialog.Builder(this)
                .setTitle("Chọn chế độ chơi")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        currentMode = GameMode.TWO_PLAYER;
                        resetGame();
                    } else {
                        currentMode = GameMode.VS_COMPUTER;
                        showDifficultyDialog(); // chọn độ khó
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showDifficultyDialog() {
        String[] difficulties = {"Dễ", "Trung bình", "Khó"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn độ khó")
                .setItems(difficulties, (dialog, which) -> {
                    switch (which) {
                        case 0: difficultyLevel = DifficultyLevel.EASY; break;
                        case 1: difficultyLevel = DifficultyLevel.MEDIUM; break;
                        case 2: difficultyLevel = DifficultyLevel.HARD; break;
                    }
                    resetGame();
                })
                .setCancelable(false)
                .show();
    }

    private void handleClick(int row, int col) {
        if (gameEnded || !buttons[row][col].getText().toString().equals("")) return;

        buttons[row][col].setText(isXTurn ? "X" : "O");
        buttons[row][col].setTextColor(isXTurn ? Color.RED : Color.BLUE);

        if (checkWin(row, col)) {
            gameEnded = true;
            Toast.makeText(this, "Người chơi " + (isXTurn ? "X" : "O") + " thắng!", Toast.LENGTH_LONG).show();
            return;
        }

        if (isBoardFull()) {
            gameEnded = true;
            Toast.makeText(this, "Hòa!", Toast.LENGTH_SHORT).show();
            return;
        }

        isXTurn = !isXTurn;
        txtTurn.setText("Lượt: " + (isXTurn ? "X" : "O"));

        if (currentMode == GameMode.VS_COMPUTER && !isXTurn && !gameEnded) {
            playComputer();
        }
    }

    private void playComputer() {
        String ai = "O";
        String opponent = "X";
        int[] move;

        switch (difficultyLevel) {
            case EASY:
                move = getFirstEmptyCell();
                break;
            case MEDIUM:
                move = getBestMove(ai, opponent);
                break;
            case HARD:
                move = getHardMove(ai, opponent);
                break;
            default:
                move = getFirstEmptyCell();
        }

        if (move != null) {
            handleClick(move[0], move[1]);
        }
    }

    private int[] getFirstEmptyCell() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (buttons[r][c].getText().toString().equals(""))
                    return new int[]{r, c};
        return null;
    }

    private int[] getBestMove(String aiSymbol, String opponentSymbol) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (buttons[r][c].getText().toString().equals("")) {
                    buttons[r][c].setText(aiSymbol);
                    if (checkWin(r, c)) {
                        buttons[r][c].setText("");
                        return new int[]{r, c};
                    }
                    buttons[r][c].setText("");
                }
            }
        }

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (buttons[r][c].getText().toString().equals("")) {
                    buttons[r][c].setText(opponentSymbol);
                    if (checkWin(r, c)) {
                        buttons[r][c].setText("");
                        return new int[]{r, c};
                    }
                    buttons[r][c].setText("");
                }
            }
        }

        int center = SIZE / 2;
        int minDist = Integer.MAX_VALUE;
        int[] best = null;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (buttons[r][c].getText().toString().equals("")) {
                    int dist = Math.abs(r - center) + Math.abs(c - center);
                    if (dist < minDist) {
                        minDist = dist;
                        best = new int[]{r, c};
                    }
                }
            }
        }
        return best;
    }

    private int[] getHardMove(String ai, String opponent) {
        int maxScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (buttons[r][c].getText().toString().equals("")) {
                    int score = evaluateMove(r, c, ai, opponent);
                    if (score > maxScore) {
                        maxScore = score;
                        bestMove = new int[]{r, c};
                    }
                }
            }
        }
        return bestMove;
    }

    private int evaluateMove(int r, int c, String ai, String opponent) {
        int score = 0;
        buttons[r][c].setText(ai);
        if (checkWin(r, c)) score += 1000;
        buttons[r][c].setText("");

        buttons[r][c].setText(opponent);
        if (checkWin(r, c)) score += 500;
        buttons[r][c].setText("");

        int center = SIZE / 2;
        int dist = Math.abs(r - center) + Math.abs(c - center);
        score += (10 - dist);

        return score;
    }

    private boolean checkWin(int row, int col) {
        String current = buttons[row][col].getText().toString();
        return checkLine(row, col, 1, 0, current) ||
                checkLine(row, col, 0, 1, current) ||
                checkLine(row, col, 1, 1, current) ||
                checkLine(row, col, 1, -1, current);
    }

    private boolean checkLine(int row, int col, int dr, int dc, String player) {
        int count = 1;
        int r = row - dr, c = col - dc;
        while (inBounds(r, c) && buttons[r][c].getText().toString().equals(player)) {
            count++; r -= dr; c -= dc;
        }

        r = row + dr;
        c = col + dc;
        while (inBounds(r, c) && buttons[r][c].getText().toString().equals(player)) {
            count++; r += dr; c += dc;
        }

        return count >= 4;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

    private boolean isBoardFull() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (buttons[r][c].getText().toString().equals("")) return false;
        return true;
    }

    private void resetGame() {
        isXTurn = true;
        gameEnded = false;
        txtTurn.setText("Lượt: X");

        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                buttons[r][c].setText("");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
