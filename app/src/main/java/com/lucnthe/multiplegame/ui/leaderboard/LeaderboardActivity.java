package com.lucnthe.multiplegame.ui.leaderboard;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.lucnthe.multiplegame.R;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<LeaderboardEntry> entryList = new ArrayList<>();
    private LeaderboardAdapter adapter;
    private TextView tvCurrentUserRank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        recyclerView = findViewById(R.id.recyclerView);
        tvCurrentUserRank = findViewById(R.id.tvCurrentUserRank);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String game = getIntent().getStringExtra("game");
        loadLeaderboard(game); // 👈 Khởi chạy loadLeaderboard
    }

    //    private void loadLeaderboard(String game) {
//        FirebaseFirestore.getInstance()
//                .collection("leaderboards")
//                .document(game)
//                .collection("scores")
//                .orderBy("score", Query.Direction.DESCENDING)
//                .limit(10)
//                .get()
//                .addOnSuccessListener(snapshot -> {
//                    entryList.clear();
//                    for (DocumentSnapshot doc : snapshot) {
//                        String name = doc.getString("username");
//                        long score = doc.getLong("score");
//                        entryList.add(new LeaderboardEntry(name, score));
//                        Log.d("LEADERBOARD", "Doc: " + doc.getData());
//                    }
//                    adapter.notifyDataSetChanged();
//                    Log.d("LEADERBOARD", "Game = " + game);
//                    Log.d("LEADERBOARD", "Snapshot size = " + snapshot.size());
//
//                });
//
//
//    }
    private void loadLeaderboard(String game) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(currentUid).get().addOnSuccessListener(userDoc -> {
            String currentUsername = userDoc.getString("username");

            adapter = new LeaderboardAdapter(entryList, currentUsername);
            recyclerView.setAdapter(adapter);

            db.collection("leaderboards")
                    .document(game)
                    .collection("scores")
                    .orderBy("score", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        entryList.clear();
                        boolean isInTop10 = false;

                        for (int i = 0; i < snapshot.size(); i++) {
                            DocumentSnapshot doc = snapshot.getDocuments().get(i);
                            String name = doc.getString("username");
                            long score = doc.getLong("score");

                            entryList.add(new LeaderboardEntry(name, score, i + 1));

                            if (doc.getId().equals(currentUid)) {
                                isInTop10 = true;
                                tvCurrentUserRank.setText("Bạn: " + name + " - Hạng: " + (i + 1) + " - Điểm: " + score);
                            }
                        }

                        if (!isInTop10) {
                            db.collection("leaderboards")
                                    .document(game)
                                    .collection("scores")
                                    .orderBy("score", Query.Direction.DESCENDING)
                                    .get()
                                    .addOnSuccessListener(allSnapshot -> {
                                        for (int i = 0; i < allSnapshot.size(); i++) {
                                            DocumentSnapshot doc = allSnapshot.getDocuments().get(i);
                                            if (doc.getId().equals(currentUid)) {
                                                String name = doc.getString("username");
                                                long score = doc.getLong("score");
                                                LeaderboardEntry myEntry = new LeaderboardEntry(name, score, i + 1);
                                                entryList.add(null); // ngăn cách
                                                entryList.add(myEntry);

                                                // Gán đúng vị trí nếu không thuộc top 10
                                                tvCurrentUserRank.setText("Bạn: " + name + " - Hạng: " + (i + 1) + " - Điểm: " + score);
                                                adapter.notifyDataSetChanged();
                                                break;
                                            }
                                        }
                                    });
                        }
                            adapter.notifyDataSetChanged();
                    });
        });
    }

}

