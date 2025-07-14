package com.lucnthe.multiplegame.ui.sudoku;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class SudokuBoardView extends View {
    private SudokuGame game;
    private final Paint linePaint = new Paint();
    private final Paint textPaint = new Paint();
    private int cellSize;
    private int selectedRow = -1, selectedCol = -1;

    public SudokuBoardView(Context context, SudokuGame game) {
        super(context);
        this.game = game;
        linePaint.setColor(Color.BLACK);
        textPaint.setColor(Color.BLUE);
        textPaint.setTextSize(60);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }
    public void setGame(SudokuGame game) {
        this.game = game;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = Math.min(getWidth(), getHeight());
        cellSize = size / 9;
        int boardSize = cellSize * 9;

        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, boardSize, boardSize, bgPaint);

        // 1. Highlight subgrid (ưu tiên)
        if (selectedRow != -1 && selectedCol != -1) {
            int startRow = (selectedRow / 3) * 3;
            int startCol = (selectedCol / 3) * 3;
            Paint subgridPaint = new Paint();
            subgridPaint.setColor(Color.parseColor("#BBDEFB"));
            subgridPaint.setAlpha(180);
            canvas.drawRect(startCol * cellSize, startRow * cellSize,
                    (startCol + 3) * cellSize, (startRow + 3) * cellSize, subgridPaint);
        }

        // 2. Highlight hàng/cột
        if (selectedRow != -1 && selectedCol != -1) {
            Paint highlightPaint = new Paint();
            highlightPaint.setColor(Color.parseColor("#D0E7FF"));
            highlightPaint.setAlpha(120);
            canvas.drawRect(0, selectedRow * cellSize, boardSize, (selectedRow + 1) * cellSize, highlightPaint);
            canvas.drawRect(selectedCol * cellSize, 0, (selectedCol + 1) * cellSize, boardSize, highlightPaint);
        }

        // 3. Highlight ô đang chọn
        if (selectedRow != -1 && selectedCol != -1) {
            Paint selectedPaint = new Paint();
            selectedPaint.setColor(Color.parseColor("#FFF176"));
            selectedPaint.setAlpha(200);
            canvas.drawRect(selectedCol * cellSize, selectedRow * cellSize,
                    (selectedCol + 1) * cellSize, (selectedRow + 1) * cellSize, selectedPaint);
        }

        // 4. Highlight các ô có cùng số với số đang chọn
        if (selectedRow != -1 && selectedCol != -1) {
            int selectedNumber = game.getNumber(selectedRow, selectedCol);
            if (selectedNumber != 0) {
                Paint sameNumberPaint = new Paint();
                sameNumberPaint.setColor(Color.parseColor("#90CAF9")); // xanh nhạt hơn subgrid
                sameNumberPaint.setAlpha(180);

                for (int row = 0; row < 9; row++) {
                    for (int col = 0; col < 9; col++) {
                        if (game.getNumber(row, col) == selectedNumber) {
                            canvas.drawRect(col * cellSize, row * cellSize,
                                    (col + 1) * cellSize, (row + 1) * cellSize, sameNumberPaint);
                        }
                    }
                }
            }
        }

        // 5. Vẽ số lên
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int num = game.getNumber(row, col);
                if (num != 0) {
                    if (game.isFixed(row, col)) {
                        textPaint.setColor(Color.parseColor("#2E3A59")); // màu số mặc định
                    } else if (game.isCellInError(row, col)) {
                        textPaint.setColor(Color.RED);
                    } else {
                        textPaint.setColor(Color.parseColor("#1565C0")); // màu xanh cho nhập đúng
                    }
                    float x = col * cellSize + cellSize / 2;
                    float y = row * cellSize + cellSize / 2 + textPaint.getTextSize() / 3;
                    canvas.drawText(String.valueOf(num), x, y, textPaint);
                }
            }
        }

        // 6. Vẽ lưới
        for (int i = 0; i <= 9; i++) {
            linePaint.setStrokeWidth(i % 3 == 0 ? 5 : 2);
            canvas.drawLine(0, i * cellSize, boardSize, i * cellSize, linePaint);
            canvas.drawLine(i * cellSize, 0, i * cellSize, boardSize, linePaint);
        }
    }

    public void inputNumber(int num) {
        if (selectedRow >= 0 && selectedRow < 9 &&
                selectedCol >= 0 && selectedCol < 9 &&
                game.isCellEditable(selectedRow, selectedCol)) {

            game.setNumber(selectedRow, selectedCol, num);
            if (getContext() instanceof SudokuActivity) {
                SudokuActivity activity = (SudokuActivity) getContext();
                activity.updateKeyboardStatus();
                activity.updateCurrentScoreView();

                if (game.isCellInError(selectedRow, selectedCol)) {
                    activity.increaseMistakeCount(); // ✅ gọi khi sai
                }
            }

            invalidate();

            // 🔁 Cập nhật trạng thái bàn phím nếu đang dùng SudokuActivity
            if (getContext() instanceof SudokuActivity) {
                SudokuActivity activity = (SudokuActivity) getContext();
                activity.updateKeyboardStatus();
                activity.updateCurrentScoreView(); // ✅ gọi thay vì truy cập trực tiếp tvCurrentScore
            }

            if (game.isCompleted()) {
                int score = game.getCurrentScore();
                ((SudokuActivity) getContext()).saveScoreToLeaderboard(score);
                ((SudokuActivity) getContext()).stopTimer();
                new AlertDialog.Builder(getContext())
                        .setTitle("🎉 Hoàn thành!")
                        .setMessage("Bạn đạt được " + score + " điểm.\nChơi ván mới?")
                        .setPositiveButton("Chơi mới", (dialog, which) -> {
                            ((SudokuActivity) getContext()).showDifficultyDialog();
                        })
                        .setNegativeButton("Đóng", null)
                        .setCancelable(false)
                        .show();
            }


        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            int col = (int) (x / cellSize);
            int row = (int) (y / cellSize);

            // ✅ Giới hạn trong vùng hợp lệ 9x9
            if (row >= 0 && row < 9 && col >= 0 && col < 9) {
                selectedRow = row;
                selectedCol = col;
                invalidate(); // redraw
            }

            return true;
        }
        return false;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }


}
