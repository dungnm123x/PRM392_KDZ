package com.lucnthe.multiplegame.ui.sudoku;

import static com.lucnthe.multiplegame.ui.sudoku.SudokuGenerator.isSafe;

public class SudokuGame {
    public int[][] board;
    private boolean[][] errorCells = new boolean[9][9];
    private boolean[][] fixedCells = new boolean[9][9];
    private boolean[][] scoredCells = new boolean[9][9];
    private int currentScore = 0;
    public SudokuGame(String difficulty) {
        board = SudokuGenerator.generateBoard(difficulty);
        fixedCells = new boolean[9][9];
        scoredCells = new boolean[9][9];
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                fixedCells[row][col] = board[row][col] != 0;
            }
        }
    }
    public SudokuGame(int[][] board, boolean[][] fixedCells) {
        this.board = board;
        this.fixedCells = fixedCells;
        this.scoredCells = new boolean[9][9];
    }

    public boolean isFixed(int row, int col) {
        return fixedCells[row][col];
    }

    public boolean isCellInError(int row, int col) {
        return errorCells[row][col];
    }

    public int getNumber(int row, int col) {
        return board[row][col];
    }

    public void setNumber(int row, int col, int number) {
        if (row >= 0 && row < 9 && col >= 0 && col < 9) {
            if (fixedCells[row][col]) return;

            int prev = board[row][col];

            if (prev == number) return;

            board[row][col] = number;

            if (number == 0) {
                errorCells[row][col] = false;
            } else {
                boolean valid = isSafe(board, row, col, number);
                errorCells[row][col] = !valid;
                // - là số hợp lệ
                // - trước đó là 0 (ô trống)
                // - và ô này chưa được cộng điểm (scoredCells = false)
                if (valid && prev == 0 && !scoredCells[row][col]) {
                    addScore(10);
                    scoredCells[row][col] = true;

                    if (isRowComplete(row)) addScore(50);
                    if (isColComplete(col)) addScore(50);
                    if (isBoxComplete(row, col)) addScore(50);
                }
            }
        }
    }

    private boolean isRowComplete(int row) {
        boolean[] seen = new boolean[10];
        for (int col = 0; col < 9; col++) {
            int num = board[row][col];
            if (num == 0 || seen[num]) return false;
            seen[num] = true;
        }
        return true;
    }

    private boolean isColComplete(int col) {
        boolean[] seen = new boolean[10];
        for (int row = 0; row < 9; row++) {
            int num = board[row][col];
            if (num == 0 || seen[num]) return false;
            seen[num] = true;
        }
        return true;
    }

    private boolean isBoxComplete(int row, int col) {
        boolean[] seen = new boolean[10];
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;

        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                int num = board[r][c];
                if (num == 0 || seen[num]) return false;
                seen[num] = true;
            }
        }
        return true;
    }

    public boolean isCellEditable(int row, int col) {
        return !fixedCells[row][col];
    }

    public boolean isCompleted() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int num = board[row][col];
                if (num == 0 || !isSafe(board, row, col, num))
                    return false;
            }
        }
        return true;
    }
    public boolean[][] getErrorCells() {
        return errorCells;
    }
    public boolean[][] getFixedCells() {
        return fixedCells;
    }
    public int countNumber(int number) {
        int count = 0;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == number && !errorCells[row][col]) {
                    count++;
                }
            }
        }
        return count;
    }
    public int getCurrentScore() {
        return currentScore;
    }
    public void addScore(int points) {
        currentScore += points;
    }

}
