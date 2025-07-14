package com.lucnthe.multiplegame.ui.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lucnthe.multiplegame.R;
import com.lucnthe.multiplegame.ui.model.GameItem;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private final List<GameItem> games;
    private final Context context;

    public GameAdapter(Context context, List<GameItem> games) {
        this.context = context;
        this.games = games;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameItem game = games.get(position);
        holder.title.setText(game.title);
        holder.icon.setImageResource(game.iconResId);
        holder.itemView.setOnClickListener(v -> {
            context.startActivity(new Intent(context, game.activityClass));
        });
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.imgGameIcon);
            title = itemView.findViewById(R.id.tvGameTitle);
        }
    }
}

