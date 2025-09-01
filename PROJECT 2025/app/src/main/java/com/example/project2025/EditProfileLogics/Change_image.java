package com.example.project2025.EditProfileLogics;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.project2025.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for changing user profile images
 */
public class Change_image extends AppCompatActivity {

    private static final String TAG = "Change_image";

    // UI Elements
    private MaterialButton saveButton;
    private ImageView returnButton, change_image;

    // Firebase components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // State
    private String selectedImage = "sad_mouse.jpg";
    private CardView selectedCard = null;
    private Uri customImageUri = null;
    private boolean isCustomImage = false;
    private String currentPhotoPath;

    // Permission codes
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    // Launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.change_image);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        initializeActivityResultLaunchers();
        setupClickListeners();
        loadCurrentProfileImage(); // 🔥 CHANGED – will now use cache + Firestore
    }

    private void initializeActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        handleCameraImage();
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            customImageUri = selectedImageUri;
                            isCustomImage = true;
                            change_image.setImageURI(customImageUri);
                            Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupClickListeners() {
        returnButton = findViewById(R.id.returnButton);
        saveButton = findViewById(R.id.saveButton);
        change_image = findViewById(R.id.change_image);

        returnButton.setOnClickListener(v -> finish());

        change_image.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });

        saveButton.setOnClickListener(v -> saveProfileImage());
    }

    /**
     * 🔥 Updated to use SharedPreferences cache + Firestore fallback
     */
    private void loadCurrentProfileImage() {
        if (currentUser == null) {
            Log.e(TAG, "No current user authenticated");
            return;
        }

        // First: try cached URL
        SharedPreferences prefs = getSharedPreferences("PROFILE", MODE_PRIVATE);
        String cachedUrl = prefs.getString("profilepic", null);
        if (cachedUrl != null) {
            ProfileImageHelper.loadProfileImage(this, change_image, cachedUrl); // load cached immediately
        }

        // Then: refresh from Firestore
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentProfilePic = documentSnapshot.getString("profilepic");

                if (currentProfilePic != null && !currentProfilePic.isEmpty()) {
                    selectedImage = currentProfilePic;
                    ProfileImageHelper.loadProfileImage(this, change_image, currentProfilePic);

                    // 🔥 Save to cache
                    prefs.edit().putString("profilepic", currentProfilePic).apply();
                } else {
                    ProfileImageHelper.loadProfileImage(this, change_image, null);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading profile image", e);
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    private boolean checkStoragePermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.project2025.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void handleCameraImage() {
        if (currentPhotoPath != null) {
            File f = new File(currentPhotoPath);
            customImageUri = Uri.fromFile(f);
            isCustomImage = true;
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            change_image.setImageBitmap(bitmap);
            Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfileImage() {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        if (customImageUri != null) {
            uploadImageToStorage();
        } else {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            saveButton.setText("Save");
        }
    }

    private void uploadImageToStorage() {
        try {
            String imageName = "profile_" + currentUser.getUid() + ".jpg";
            StorageReference imageRef = storageRef.child("profile_images/" + currentUser.getUid() + "/" + imageName);

            SharedPreferences sharedPreferences = getSharedPreferences("ROLE", MODE_PRIVATE);
            String role = sharedPreferences.getString("Role", "Users");

            UploadTask uploadTask = imageRef.putFile(customImageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    DocumentReference userRef = db.collection(role).document(currentUser.getUid());
                    userRef.update("profilepic", imageUrl, "isCustomImage", true)
                            .addOnSuccessListener(aVoid -> {
                                // 🔥 Save to cache immediately
                                getSharedPreferences("PROFILE", MODE_PRIVATE)
                                        .edit()
                                        .putString("profilepic", imageUrl)
                                        .apply();

                                Toast.makeText(this, "Update Successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                saveButton.setEnabled(true);
                                saveButton.setText("Save");
                            });
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
                saveButton.setText("Save");
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            saveButton.setText("Save");
        }
    }
}
