package com.lucnthe.multiplegame.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import com.lucnthe.multiplegame.ui.tetris.TetrisActivity;
import com.lucnthe.multiplegame.databinding.FragmentHomeBinding;
import com.lucnthe.multiplegame.ui.xo.XOActivity;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Handle play Tetris button
        binding.btnPlayTetris.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TetrisActivity.class);
            startActivity(intent);
        });

        // Handle play Caro button
        binding.btnPlayCaro.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), XOActivity.class);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}