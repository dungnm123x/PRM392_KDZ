package com.lucnthe.multiplegame.ui.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lucnthe.multiplegame.R;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private EditText etUsername;
    private TextView tvEmail, tvHighscore, tvLevel;
    private Button btnSave;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgAvatar = findViewById(R.id.imgAvatar);
        etUsername = findViewById(R.id.etUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvHighscore = findViewById(R.id.tvHighscore);
        tvLevel = findViewById(R.id.tvLevel);
        btnSave = findViewById(R.id.btnSave);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = auth.getCurrentUser().getUid();

        loadUserProfile();

        btnSave.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Tên người dùng không được rỗng", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("users").document(uid)
                    .update("username", newUsername)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loadUserProfile() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        etUsername.setText(snapshot.getString("username"));
                        tvEmail.setText("Email: " + snapshot.getString("email"));
                        tvHighscore.setText("Điểm cao: " + snapshot.getLong("highscore"));
                        tvLevel.setText("Cấp độ: " + snapshot.getLong("level"));
                        // Load avatar nếu có (có thể dùng Glide)
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
