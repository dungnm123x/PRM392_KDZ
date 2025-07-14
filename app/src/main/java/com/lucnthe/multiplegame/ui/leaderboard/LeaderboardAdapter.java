package com.lucnthe.multiplegame.ui.leaderboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lucnthe.multiplegame.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private final List<LeaderboardEntry> list;
    private final String currentUsername;

    public LeaderboardAdapter(List<LeaderboardEntry> list, String currentUsername) {
        this.list = list;
        this.currentUsername = currentUsername;
    }

    @Override
    public int getItemViewType(int position) {
        return (list.get(position) == null) ? 0 : 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            // View khoảng trắng
            View space = new View(parent.getContext());
            space.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 24)); // cao 24dp
            return new ViewHolder(space);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_leaderboard, parent, false);
            return new ViewHolder(v);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        LeaderboardEntry e = list.get(pos);

        // Chỉ set nếu không phải khoảng trắng
        if (e != null) {
            h.tvRank.setText(String.valueOf(e.rank));
            h.tvName.setText(e.username);
            h.tvScore.setText(String.valueOf(e.score));

            // 👉 Nếu là người chơi hiện tại thì tô đậm và đổi màu
            if (e.username != null && e.username.equals(currentUsername)) {
                h.tvName.setTextColor(Color.parseColor("#673AB7")); // Tím
                h.tvName.setTypeface(null, Typeface.BOLD);
                h.tvScore.setTextColor(Color.parseColor("#673AB7"));
                h.tvScore.setTypeface(null, Typeface.BOLD);
                h.tvRank.setTextColor(Color.parseColor("#673AB7"));
                h.tvRank.setTypeface(null, Typeface.BOLD);
            } else {
                // Reset lại nếu không phải
                h.tvName.setTextColor(Color.BLACK);
                h.tvScore.setTextColor(Color.BLACK);
                h.tvRank.setTextColor(Color.BLACK);
                h.tvName.setTypeface(null, Typeface.NORMAL);
                h.tvScore.setTypeface(null, Typeface.NORMAL);
                h.tvRank.setTypeface(null, Typeface.NORMAL);
            }
        }
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvScore;

        ViewHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvName = itemView.findViewById(R.id.tvName);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }

}

