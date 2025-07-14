package com.lucnthe.multiplegame.ui.leaderboard;

public class LeaderboardEntry {
    public String username;
    public long score;
    public int rank; // ➕ THÊM THUỘC TÍNH HẠNG

    public LeaderboardEntry(String username, long score) {
        this.username = username;
        this.score = score;
    }

    public LeaderboardEntry(String username, long score, int rank) {
        this.username = username;
        this.score = score;
        this.rank = rank;
    }
}

