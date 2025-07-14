package com.lucnthe.multiplegame.ui.puzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.List;

public class PuzzleAdapter extends BaseAdapter {
    private Context context;
    private List<Bitmap> pieces;
    private int[] positions;
    private int[] originalPositions; // Original state before shuffle
    private int N;
    private PuzzleCompletionListener completionListener;

    public interface PuzzleCompletionListener {
        void onPuzzleCompleted();
    }

    public PuzzleAdapter(Context context, List<Bitmap> pieces, int[] positions, int N, int[] originalPositions) {
        this.context = context;
        this.pieces = pieces;
        this.positions = positions;
        this.originalPositions = originalPositions.clone();
        this.N = N;

        if (context instanceof PuzzleCompletionListener) {
            this.completionListener = (PuzzleCompletionListener) context;
        }
    }

    @Override
    public int getCount() {
        return positions.length;
    }

    @Override
    public Object getItem(int position) {
        return pieces.get(positions[position]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            int screenWidth = parent.getWidth() != 0 ? parent.getWidth() : context.getResources().getDisplayMetrics().widthPixels;
            int pieceSize = screenWidth / N;
            imageView.setLayoutParams(new GridView.LayoutParams(pieceSize, pieceSize));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setPadding(2, 2, 2, 2);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageBitmap(pieces.get(positions[position]));
        return imageView;
    }

    public boolean movePiece(int clickedPosition) {
        int emptyPosition = findEmptyPosition();
        if (isAdjacent(clickedPosition, emptyPosition)) {
            int temp = positions[clickedPosition];
            positions[clickedPosition] = positions[emptyPosition];
            positions[emptyPosition] = temp;
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    public int findEmptyPosition() {
        for (int i = 0; i < positions.length; i++) {
            if (positions[i] == N * N - 1) {
                return i;
            }
        }
        return -1;
    }

    private boolean isAdjacent(int pos1, int pos2) {
        int row1 = pos1 / N;
        int col1 = pos1 % N;
        int row2 = pos2 / N;
        int col2 = pos2 % N;
        return (row1 == row2 && Math.abs(col1 - col2) == 1) ||
                (col1 == col2 && Math.abs(row1 - row2) == 1);
    }

    public boolean isSolved() {
        return Arrays.equals(positions, originalPositions); // Check against original state
    }

    public int[] getPositions() {
        return positions;
    }
}