package com.lucnthe.multiplegame.ui.puzzle;

import android.content.ContentResolver;
import com.lucnthe.multiplegame.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PuzzleActivity extends AppCompatActivity implements PuzzleAdapter.PuzzleCompletionListener {

    private int N;
    private List<Bitmap> pieces;
    private int[] positions;
    private int[] solvedPositions;
    private PuzzleAdapter adapter;

    // Timer components
    private TextView timerTextView;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;
    private int secondsElapsed = 0;
    private boolean timerRunning = false;

    // Move counter
    private TextView movesTextView;
    private int moveCount = 0;

    // Star rating
    private ImageView star1, star2, star3;

    // Auto-solve components
    private Button autoSolveButton;
    private Handler autoSolveHandler = new Handler(Looper.getMainLooper());
    private Stack<int[]> solutionSteps;
    private boolean autoSolving = false;

    // Hint system
    private Button hintButton;
    private int[] originalPositions;
    private boolean hintActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Giải Puzzle");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // Get data from Intent
        String imageUriString = getIntent().getStringExtra("imageUri");
        N = getIntent().getIntExtra("N", 3);
        Uri imageUri = Uri.parse(imageUriString);

        // Load and process the image
        Bitmap scaledBitmap = loadScaledBitmap(imageUri);
        createPuzzlePieces(scaledBitmap);

        // Initialize and shuffle positions
        initializeAndShufflePositions();

        // Initialize UI elements
        initializeUI();

        // Set up GridView
        GridView gridView = findViewById(R.id.puzzleGrid);
        gridView.setNumColumns(N);
        adapter = new PuzzleAdapter(this, pieces, positions, N, originalPositions);
        gridView.setAdapter(adapter);

        // Set up the item click listener to handle piece movement
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (!autoSolving && !hintActive) {
                boolean moved = adapter.movePiece(position);
                if (moved) {
                    moveCount++;
                    movesTextView.setText("Moves: " + moveCount);
                    if (adapter.isSolved()) {
                        onPuzzleCompleted();
                    }
                }
            }
        });

        // Start the timer
        startTimer();
    }

    private void initializeUI() {
        timerTextView = findViewById(R.id.timerTextView);
        movesTextView = findViewById(R.id.movesTextView);
        movesTextView.setText("Moves: 0");

        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);

        hintButton = findViewById(R.id.hintButton);
        hintButton.setOnClickListener(v -> showHint());

        autoSolveButton = findViewById(R.id.autoSolveButton);
        autoSolveButton.setOnClickListener(v -> startAutoSolve());

        Button quitButton = findViewById(R.id.quitButton);
        quitButton.setVisibility(View.GONE);
        quitButton.setOnClickListener(v -> finish());
    }

    private Bitmap loadScaledBitmap(Uri imageUri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(imageUri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;

            int cropSize = Math.min(originalWidth, originalHeight);
            int cropLeft = (originalWidth - cropSize) / 2;
            int cropTop = (originalHeight - cropSize) / 2;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int pieceSize = screenWidth / N;
            int targetSize = pieceSize * N;

            options.inSampleSize = Math.max(1, (int) Math.ceil(cropSize / (double) targetSize));
            options.inJustDecodeBounds = false;

            inputStream = contentResolver.openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            int sampledCropSize = cropSize / options.inSampleSize;
            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap,
                    cropLeft / options.inSampleSize,
                    cropTop / options.inSampleSize,
                    sampledCropSize,
                    sampledCropSize);

            return Bitmap.createScaledBitmap(croppedBitmap, targetSize, targetSize, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void createPuzzlePieces(Bitmap scaledBitmap) {
        pieces = new ArrayList<>();
        int pieceWidth = scaledBitmap.getWidth() / N;

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i == N - 1 && j == N - 1) {
                    Bitmap blank = Bitmap.createBitmap(pieceWidth, pieceWidth, Bitmap.Config.ARGB_8888);
                    blank.eraseColor(Color.TRANSPARENT);
                    pieces.add(blank);
                } else {
                    Bitmap piece = Bitmap.createBitmap(scaledBitmap, j * pieceWidth, i * pieceWidth, pieceWidth, pieceWidth);
                    pieces.add(piece);
                }
            }
        }
    }

    private void initializeAndShufflePositions() {
        int totalPieces = N * N;
        positions = new int[totalPieces];
        solvedPositions = new int[totalPieces];

        for (int i = 0; i < totalPieces; i++) {
            positions[i] = i;
            solvedPositions[i] = i;
        }

        originalPositions = Arrays.copyOf(positions, positions.length);

        int emptyIndex = totalPieces - 1;
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            List<Integer> adjacent = getAdjacentIndices(emptyIndex);
            int randomAdj = adjacent.get(random.nextInt(adjacent.size()));
            int temp = positions[randomAdj];
            positions[randomAdj] = positions[emptyIndex];
            positions[emptyIndex] = temp;
            emptyIndex = randomAdj;
        }
    }

    private List<Integer> getAdjacentIndices(int index) {
        int row = index / N;
        int col = index % N;
        List<Integer> adjacent = new ArrayList<>();
        if (row > 0) adjacent.add(index - N); // Up
        if (row < N - 1) adjacent.add(index + N); // Down
        if (col > 0) adjacent.add(index - 1); // Left
        if (col < N - 1) adjacent.add(index + 1); // Right
        return adjacent;
    }

    // Timer methods
    private void startTimer() {
        if (!timerRunning) {
            startTime = System.currentTimeMillis() - (secondsElapsed * 1000);
            timerHandler.postDelayed(timerRunnable, 0);
            timerRunning = true;
        }
    }

    private void stopTimer() {
        if (timerRunning) {
            timerHandler.removeCallbacks(timerRunnable);
            timerRunning = false;
        }
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            secondsElapsed = (int) (millis / 1000);
            int minutes = secondsElapsed / 60;
            int seconds = secondsElapsed % 60;
            timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    // Hint system
    private void showHint() {
        if (hintActive) return;

        hintActive = true;
        hintButton.setEnabled(false);

        int emptyPos = adapter.findEmptyPosition();
        int emptyRow = emptyPos / N;
        int emptyCol = emptyPos % N;

        int targetPos = -1;
        List<Integer> adjacent = getAdjacentIndices(emptyPos);

        for (int adj : adjacent) {
            int pieceNum = positions[adj];
            int correctPos = originalPositions[pieceNum];
            int correctRow = correctPos / N;
            int correctCol = correctPos % N;

            if ((Math.abs(correctRow - emptyRow) + Math.abs(correctCol - emptyCol)) <
                    (Math.abs(correctRow - (adj / N)) + Math.abs(correctCol - (adj % N)))) {
                targetPos = adj;
                break;
            }
        }

        if (targetPos != -1) {
            GridView gridView = findViewById(R.id.puzzleGrid);
            View pieceView = gridView.getChildAt(targetPos);
            if (pieceView instanceof ImageView) {
                ImageView imageView = (ImageView) pieceView;

                // Create a semi-transparent yellow overlay
                ColorDrawable yellowOverlay = new ColorDrawable(Color.argb(128, 255, 255, 0));

                // Apply the overlay as a foreground (API 23+), or fallback to background with original image
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    imageView.setForeground(yellowOverlay);
                } else {
                    Drawable originalDrawable = imageView.getDrawable();
                    Drawable[] layers = {originalDrawable, yellowOverlay};
                    LayerDrawable layerDrawable = new LayerDrawable(layers);
                    imageView.setImageDrawable(layerDrawable);
                }

                final int finalTargetPos = targetPos; // Capture targetPos instead
                // Remove the overlay after 2 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    View updatedPieceView = gridView.getChildAt(finalTargetPos);
                    if (updatedPieceView instanceof ImageView) {
                        ImageView updatedImageView = (ImageView) updatedPieceView;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            updatedImageView.setForeground(null);
                        } else {
                            updatedImageView.setImageBitmap(pieces.get(positions[finalTargetPos]));
                        }
                    }
                    hintActive = false;
                    hintButton.setEnabled(true);
                }, 2000);
            }
        } else {
            hintActive = false;
            hintButton.setEnabled(true);
            Toast.makeText(this, "No helpful move found", Toast.LENGTH_SHORT).show();
        }
    }

    // Auto-solve functionality
    private void startAutoSolve() {
        if (autoSolving) return;

        if (Arrays.equals(positions, originalPositions)) {
            Toast.makeText(this, "Already at original state!", Toast.LENGTH_SHORT).show();
            return;
        }

        solutionSteps = null; // Reset solution steps
        generateSolutionSteps();

        if (solutionSteps == null || solutionSteps.isEmpty()) {
            Toast.makeText(this, "No solution found or already solved!", Toast.LENGTH_SHORT).show();
            return;
        }

        autoSolving = true;
        hintButton.setEnabled(false);
        autoSolveButton.setEnabled(false);

        executeNextSolutionStep();
    }

    private void generateSolutionSteps() {
        solutionSteps = new Stack<>();
        int[] currentState = Arrays.copyOf(positions, positions.length);
        int[] targetState = Arrays.copyOf(originalPositions, originalPositions.length);

        // State class to hold puzzle state, cost, and parent
        class PuzzleState {
            int[] state;
            int g; // Cost from start
            int h; // Heuristic cost to goal
            int f; // Total cost (g + h)
            PuzzleState parent;

            PuzzleState(int[] state, int g, PuzzleState parent) {
                this.state = state;
                this.g = g;
                this.h = calculateManhattanDistance(state);
                this.f = this.g + this.h;
                this.parent = parent;
            }
        }

        // Priority queue for A* search
        Comparator<PuzzleState> comparator = Comparator.comparingInt(s -> s.f);
        PriorityQueue<PuzzleState> queue = new PriorityQueue<>(comparator);
        Map<String, Integer> visited = new HashMap<>(); // Store best g-cost for each state

        String initialStateStr = arrayToString(currentState);
        PuzzleState initialState = new PuzzleState(currentState, 0, null);
        queue.add(initialState);
        visited.put(initialStateStr, 0);

        while (!queue.isEmpty()) {
            PuzzleState current = queue.poll();

            if (Arrays.equals(current.state, targetState)) {
                // Reconstruct the solution path
                PuzzleState step = current;
                while (step.parent != null) {
                    solutionSteps.push(step.state);
                    step = step.parent;
                }
                return;
            }

            int emptyPos = findEmptyPosition(current.state);
            List<Integer> adjacent = getAdjacentIndices(emptyPos);

            for (int adj : adjacent) {
                int[] newState = current.state.clone();
                swap(newState, emptyPos, adj);
                String newStateStr = arrayToString(newState);

                int newG = current.g + 1;
                Integer bestG = visited.get(newStateStr);

                if (bestG == null || newG < bestG) {
                    visited.put(newStateStr, newG);
                    PuzzleState nextState = new PuzzleState(newState, newG, current);
                    queue.add(nextState);
                }
            }
        }

        solutionSteps = null; // No solution found
    }

    // Calculate Manhattan Distance heuristic
    private int calculateManhattanDistance(int[] state) {
        int distance = 0;
        for (int i = 0; i < state.length; i++) {
            if (state[i] == N * N - 1) continue; // Skip empty tile
            int currentRow = i / N;
            int currentCol = i % N;
            int targetPos = originalPositions[state[i]];
            int targetRow = targetPos / N;
            int targetCol = targetPos % N;
            distance += Math.abs(currentRow - targetRow) + Math.abs(currentCol - targetCol);
        }
        return distance;
    }

    private String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int value : array) {
            sb.append(value).append(",");
        }
        return sb.toString();
    }

    private int findEmptyPosition(int[] state) {
        for (int i = 0; i < state.length; i++) {
            if (state[i] == N * N - 1) {
                return i;
            }
        }
        return -1;
    }

    private void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    private void executeNextSolutionStep() {
        if (solutionSteps == null || solutionSteps.isEmpty()) {
            autoSolving = false;
            hintButton.setEnabled(true);
            autoSolveButton.setEnabled(true);
            if (Arrays.equals(positions, originalPositions)) {
                onPuzzleCompleted();
            } else {
                Toast.makeText(this, "Auto-solve failed to reach original state!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        int[] nextPositions = solutionSteps.pop();
        System.arraycopy(nextPositions, 0, positions, 0, positions.length);
        adapter.notifyDataSetChanged();

        moveCount++;
        movesTextView.setText("Moves: " + moveCount);

        // Check if we've reached the target state early
        if (Arrays.equals(positions, originalPositions)) {
            solutionSteps.clear(); // Stop further execution
            autoSolveHandler.removeCallbacksAndMessages(null); // Cancel any pending steps
            autoSolving = false;
            hintButton.setEnabled(true);
            autoSolveButton.setEnabled(true);
            onPuzzleCompleted();
            return;
        }

        autoSolveHandler.postDelayed(this::executeNextSolutionStep, 500);
    }

    @Override
    public void onPuzzleCompleted() {
        stopTimer();

        int stars = calculateStarRating();
        updateStarDisplay(stars);

        Button quitButton = findViewById(R.id.quitButton);
        quitButton.setVisibility(View.VISIBLE);

        hintButton.setEnabled(false);
        autoSolveButton.setEnabled(false);

        showCompletionDialog(stars);
    }

    private int calculateStarRating() {
        int minMoves = N * N * 2;
        int timeThreshold = N * N * 5;

        if (moveCount <= minMoves * 1.5 && secondsElapsed <= timeThreshold) {
            return 3;
        } else if (moveCount <= minMoves * 2.5 && secondsElapsed <= timeThreshold * 1.5) {
            return 2;
        } else {
            return 1;
        }
    }

    private void updateStarDisplay(int stars) {
        star1.setImageResource(R.drawable.star_filled);

        if (stars >= 2) {
            star2.setImageResource(R.drawable.star_filled);
        } else {
            star2.setImageResource(R.drawable.star_empty);
        }

        if (stars >= 3) {
            star3.setImageResource(R.drawable.star_filled);
        } else {
            star3.setImageResource(R.drawable.star_empty);
        }

        star1.setVisibility(View.VISIBLE);
        star2.setVisibility(View.VISIBLE);
        star3.setVisibility(View.VISIBLE);
    }

    private void showCompletionDialog(int stars) {
        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Puzzle Complete!");
        builder.setMessage(String.format(
                "Stars: %d/3\nTime: %02d:%02d\nMoves: %d",
                stars, minutes, seconds, moveCount));
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!adapter.isSolved()) {
            startTimer();
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}