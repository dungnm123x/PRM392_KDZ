package com.lucnthe.multiplegame.ui.tetris;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class Tetromino {
    private int[][] shape;
    public int row, col;
    private int color;

    public Tetromino(int[][] shape, int colStart) {
        this.shape = shape;
        this.row = 0;
        this.col = colStart;
    }

    public static Tetromino random(int cols) {
        int[][][] shapes = {
                {{1, 1, 1, 1}},             // I
                {{1, 1}, {1, 1}},           // O
                {{0, 1, 0}, {1, 1, 1}},     // T
                {{1, 1, 0}, {0, 1, 1}},     // S
                {{0, 1, 1}, {1, 1, 0}},     // Z
                {{1, 1, 1}, {1, 0, 0}},     // L
                {{1, 1, 1}, {0, 0, 1}}      // J
        };

        int[] colors = {
                Color.parseColor("#00BCD4"), // I – Cyan
                Color.parseColor("#FFEB3B"), // O – Yellow
                Color.parseColor("#9C27B0"), // T – Purple
                Color.parseColor("#4CAF50"), // S – Green
                Color.parseColor("#F44336"), // Z – Red
                Color.parseColor("#FF9800"), // L – Orange
                Color.parseColor("#3F51B5")  // J – Blue
        };

        int idx = new Random().nextInt(shapes.length);
        Tetromino tetromino = new Tetromino(shapes[idx], cols / 2 - 1);
        tetromino.color = colors[idx];
        return tetromino;
    }


    public boolean moveDown(int[][] grid) {
        if (!collides(grid, row + 1, col)) {
            row++;
            return true;
        }
        return false;
    }

    public void lockToGrid(int[][] grid, int[][] gridColor) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int gr = row + r;
                    int gc = col + c;
                    grid[gr][gc] = 1;
                    gridColor[gr][gc] = color;
                }
            }
        }
    }


    public boolean collides(int[][] grid) {
        return collides(grid, row, col);
    }

    public boolean collides(int[][] grid, int testRow, int testCol) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int gr = testRow + r;
                    int gc = testCol + c;
                    if (gr >= grid.length || gc < 0 || gc >= grid[0].length || grid[gr][gc] != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

//    public void draw(Canvas canvas, int blockSize, int offsetX, int offsetY) {
//        Paint paint = new Paint();
//        paint.setColor(color);
//        for (int r = 0; r < shape.length; r++) {
//            for (int c = 0; c < shape[r].length; c++) {
//                if (shape[r][c] != 0) {
//                    int left = offsetX + (col + c) * blockSize;
//                    int top = offsetY + (row + r) * blockSize;
//                    float radius = blockSize * 0.2f;
//                    canvas.drawRoundRect(
//                            left, top,
//                            left + blockSize, top + blockSize,
//                            radius, radius, paint
//                    );
//
//                }
//            }
//        }
//    }
public void draw(Canvas canvas, int blockSize, int offsetX, int offsetY) {
    Paint paint = new Paint();
    float radius = blockSize * 0.06f;

    for (int r = 0; r < shape.length; r++) {
        for (int c = 0; c < shape[r].length; c++) {
            if (shape[r][c] != 0) {
                int left = offsetX + (col + c) * blockSize;
                int top = offsetY + (row + r) * blockSize;

                // Fill
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(color);
                paint.setAlpha(255);
                canvas.drawRoundRect(left, top, left + blockSize, top + blockSize, radius, radius, paint);

                // Border
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2f);
                paint.setColor(Color.DKGRAY);
                canvas.drawRoundRect(left, top, left + blockSize, top + blockSize, radius, radius, paint);

                // Highlight
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                paint.setAlpha(30);
                canvas.drawRoundRect(
                        left + 2, top + 2,
                        left + blockSize * 0.4f, top + blockSize * 0.4f,
                        radius, radius, paint
                );
            }
        }
    }
}


    public void rotate(int[][] grid) {
        int[][] rotated = new int[shape[0].length][shape.length];
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[0].length; c++) {
                rotated[c][shape.length - 1 - r] = shape[r][c];
            }
        }
        int oldCol = col;

        // adjust column if out of bounds
        if (col + rotated[0].length > grid[0].length) {
            col = grid[0].length - rotated[0].length;
        }

        // check collision before rotating
        if (!collides(grid, row, col, rotated)) {
            shape = rotated;
        } else {
            col = oldCol; // revert col if failed
        }
    }

    // Overloaded collision check with custom shape
    private boolean collides(int[][] grid, int testRow, int testCol, int[][] testShape) {
        for (int r = 0; r < testShape.length; r++) {
            for (int c = 0; c < testShape[r].length; c++) {
                if (testShape[r][c] != 0) {
                    int gr = testRow + r;
                    int gc = testCol + c;
                    if (gr >= grid.length || gc < 0 || gc >= grid[0].length || grid[gr][gc] != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

