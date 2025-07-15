package com.lucnthe.multiplegame.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lucnthe.multiplegame.MainActivity;
import com.lucnthe.multiplegame.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        auth = FirebaseAuth.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("LOGIN_DEBUG", "Đang tìm username: " + username);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("username", username)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        Log.d("LOGIN_DEBUG", "Số kết quả tìm được: " + querySnapshot.size());
                        if (!querySnapshot.isEmpty()) {
                            String email = querySnapshot.getDocuments().get(0).getString("email");
                            Log.d("LOGIN_DEBUG", "Email tìm được: " + email);

                            auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener(authResult -> {
                                        Log.d("LOGIN_DEBUG", "Đăng nhập thành công");
                                        startActivity(new Intent(this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("LOGIN_DEBUG", "Lỗi đăng nhập: " + e.getMessage());
                                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Log.e("LOGIN_DEBUG", "Không tìm thấy người dùng có username đó");
                            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LOGIN_DEBUG", "Lỗi Firestore: " + e.getMessage());
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });


        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
