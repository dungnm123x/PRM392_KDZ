package com.lucnthe.multiplegame.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.lucnthe.multiplegame.R;
import com.lucnthe.multiplegame.ui.game2048.game2048activity;
import com.lucnthe.multiplegame.ui.model.GameItem;
import com.lucnthe.multiplegame.ui.sudoku.SudokuActivity;
import com.lucnthe.multiplegame.ui.tetris.TetrisActivity;
import com.lucnthe.multiplegame.databinding.FragmentHomeBinding;
import com.lucnthe.multiplegame.ui.xo.XoOptionsActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private List<GameItem> gameList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (gameList.isEmpty()) {
            gameList.add(new GameItem("Tetris", R.drawable.ic_tetris, TetrisActivity.class));
            gameList.add(new GameItem("2468", R.drawable.ic_2468, game2048activity.class));
            gameList.add(new GameItem("Sudoku", R.drawable.ic_sudoku, SudokuActivity.class));
        }

        GameAdapter adapter = new GameAdapter(requireContext(), gameList);
        binding.rvGames.setLayoutManager(new GridLayoutManager(requireContext(), 2)); // 2 cột
        binding.rvGames.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
