package com.lucnthe.multiplegame.ui.sudoku;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class SudokuGenerator {
    private static final int SIZE = 9;
    private static final int SUBGRID = 3;
    private static final Random rand = new Random();

//    public static int[][] generateBoard() {
//        int[][] board = new int[SIZE][SIZE];
//        fillDiagonal(board);
//        solveSudoku(board);
//        removeCells(board, 40); // 40 ô trống tương đương độ khó trung bình
//        return board;
//    }
public static int[][] generateBoard(String difficulty) {
    int remove = 35; // mặc định trung bình
    switch (difficulty) {
        case "easy": remove = 30; break;
        case "hard": remove = 50; break;
    }
    int[][] board = new int[9][9];
    fillDiagonal(board);
    solveSudoku(board);
    removeCells(board, remove);
    return board;
}


    private static void fillDiagonal(int[][] board) {
        for (int i = 0; i < SIZE; i += SUBGRID) {
            fillBox(board, i, i);
        }
    }

    private static void fillBox(int[][] board, int row, int col) {
        List<Integer> nums = getShuffledNumbers();
        int index = 0;
        for (int i = 0; i < SUBGRID; i++) {
            for (int j = 0; j < SUBGRID; j++) {
                board[row + i][col + j] = nums.get(index++);
            }
        }
    }

    private static List<Integer> getShuffledNumbers() {
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) nums.add(i);
        Collections.shuffle(nums);
        return nums;
    }

    public static boolean solveSudoku(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= SIZE; num++) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num;
                            if (solveSudoku(board)) return true;
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isSafe(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (i != col && board[row][i] == num) return false;
            if (i != row && board[i][col] == num) return false;
        }

        int startRow = row - row % SUBGRID;
        int startCol = col - col % SUBGRID;
        for (int i = 0; i < SUBGRID; i++) {
            for (int j = 0; j < SUBGRID; j++) {
                int r = startRow + i;
                int c = startCol + j;
                if ((r != row || c != col) && board[r][c] == num) return false;
            }
        }
        return true;
    }

    private static void removeCells(int[][] board, int numToRemove) {
    int removed = 0;
    while (removed < numToRemove) {
        int row = rand.nextInt(SIZE);
        int col = rand.nextInt(SIZE);
        if (board[row][col] != 0) {
            int backup = board[row][col];
            board[row][col] = 0;

            // Copy board để test solve
            int[][] copy = new int[SIZE][SIZE];
            for (int i = 0; i < SIZE; i++) {
                System.arraycopy(board[i], 0, copy[i], 0, SIZE);
            }

            // Nếu vẫn còn lời giải thì gỡ thành công
            if (solveSudoku(copy)) {
                removed++;
            } else {
                board[row][col] = backup; // revert nếu không giải được
            }
        }
    }
    }

}
