package com.lucnthe.multiplegame.ui.puzzle;
import com.lucnthe.multiplegame.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Mainpuzzle extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private RadioGroup sizeRadioGroup;
    private ImageView imagePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_puzzle);
// Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // dùng toolbar làm ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chọn ảnh & kích thước"); // Optional
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // hiện nút back
        }

        // Xử lý sự kiện back
        toolbar.setNavigationOnClickListener(v -> finish());
        Button selectImageButton = findViewById(R.id.selectImageButton);
        sizeRadioGroup = findViewById(R.id.sizeRadioGroup);
        Button startButton = findViewById(R.id.startButton);
        imagePreview = findViewById(R.id.imagePreview);

        // Handle image selection
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Start the puzzle game
        startButton.setOnClickListener(v -> {
            if (imageUri != null) {
                int selectedId = sizeRadioGroup.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(this, "Please select a size", Toast.LENGTH_SHORT).show();
                    return;
                }

                RadioButton selectedSizeButton = findViewById(selectedId);
                String selectedSize = selectedSizeButton.getText().toString();

                int N;
                switch (selectedSize) {
                    case "3x3":
                        N = 3;
                        break;
                    case "4x4":
                        N = 4;
                        break;
                    case "5x5":
                        N = 5;
                        break;
                    default:
                        N = 3;
                }

                Intent intent = new Intent(Mainpuzzle.this, PuzzleActivity.class);
                intent.putExtra("imageUri", imageUri.toString());
                intent.putExtra("N", N);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
            imagePreview.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}

