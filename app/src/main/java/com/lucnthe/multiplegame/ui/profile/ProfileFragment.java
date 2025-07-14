package com.lucnthe.multiplegame.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lucnthe.multiplegame.databinding.FragmentProfileBinding;
import com.lucnthe.multiplegame.ui.cloudinary.CloudinaryService;
import com.lucnthe.multiplegame.ui.cloudinary.FileUtils;
import com.lucnthe.multiplegame.ui.login.LoginActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = auth.getCurrentUser().getUid();

        loadUserProfile();
        binding.imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadAvatarToCloudinary(imageUri); // Gọi upload
                    }
                }
        );

        binding.btnSave.setOnClickListener(v -> {
            String newUsername = binding.etUsername.getText().toString().trim();
            if (TextUtils.isEmpty(newUsername)) {
                Toast.makeText(getContext(), "Tên người dùng không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("users").document(uid)
                    .update("username", newUsername)
                    .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        binding.btnLogout.setOnClickListener(v -> {
            auth.signOut();

            // Chuyển người dùng về màn hình đăng nhập
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return root;
    }

    private void loadUserProfile() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && binding != null) {
                        binding.etUsername.setText(snapshot.getString("username"));
                        binding.tvEmail.setText("Email: " + snapshot.getString("email"));
                        binding.tvHighscore.setText("Điểm cao: " + snapshot.getLong("highscore"));
                        binding.tvLevel.setText("Cấp độ: " + snapshot.getLong("level"));

                        String avatarUrl = snapshot.getString("avatar");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this).load(avatarUrl).into(binding.imgAvatar);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding != null) {
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadAvatarToCloudinary(Uri imageUri) {
        String cloudName = "dnh1jlysn";
        String preset = "unsigned_avatar";

        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] imageBytes = buffer.toByteArray();
            inputStream.close();

            RequestBody reqBody = RequestBody.create(MediaType.parse("image/*"), imageBytes);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "avatar.jpg", reqBody);
            RequestBody presetPart = RequestBody.create(MultipartBody.FORM, preset);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.cloudinary.com/v1_1/" + cloudName + "/image/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            CloudinaryService service = retrofit.create(CloudinaryService.class);
            Call<String> call = service.uploadImage(filePart, presetPart);

            call.enqueue(new retrofit2.Callback<String>() {
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(response.body());
                            String imageUrl = json.getString("secure_url");

                            db.collection("users").document(uid)
                                    .update("avatar", imageUrl)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(getContext(), "Avatar đã cập nhật", Toast.LENGTH_SHORT).show();
                                        Glide.with(requireContext()).load(imageUrl).into(binding.imgAvatar);
                                    });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                            Log.e("UPLOAD_FAIL", "Cloudinary response: " + errorBody);
                            Toast.makeText(getContext(), "Upload thất bại:\n" + errorBody, Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
