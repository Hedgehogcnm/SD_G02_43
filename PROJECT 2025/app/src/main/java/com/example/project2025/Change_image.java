package com.example.project2025;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.File;

/**
 * Activity for changing user profile images
 */
public class Change_image extends AppCompatActivity {

    private static final String TAG = "Change_image";

    // UI Elements
    private MaterialButton saveButton;
    // Return/back button
    private ImageView returnButton, change_image;

    // Firebase components for authentication and database
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Track which image is currently selected
    private String selectedImage = "sad_mouse.jpg"; // Default image filename
    private CardView selectedCard = null; // Reference to the currently selected card for highlighting

    // Custom image handling
    private Uri customImageUri = null;
    private boolean isCustomImage = false;
    private String currentPhotoPath;

    // Permission request codes
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.change_image);

        // Initialize Firebase authentication and database instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize activity result launchers
        initializeActivityResultLaunchers();

        // Set up click listeners for all interactive elements
        setupClickListeners();
        // Load and highlight the user's current profile image selection
        loadCurrentProfileImage();
    }

    /**
     * Initialize activity result launchers for camera and gallery
     */
    private void initializeActivityResultLaunchers() {
        // Camera launcher - handles the result of taking a photo
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Photo was taken successfully
                        handleCameraImage();
                    }
                });

        // Gallery launcher - handles the result of picking an image from gallery
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Image was selected from gallery
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Validate file type
                            if (isValidImageType(selectedImageUri)) {
                                customImageUri = selectedImageUri;
                                isCustomImage = true;

                                // Display the selected image in the ImageView
                                change_image.setImageURI(customImageUri);
                                Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Cannot accept this file type. Please select JPEG, PNG, or JPG only.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    /**
     * Check if the selected image has a valid type (JPEG, PNG, or JPG)
     */
    private boolean isValidImageType(Uri uri) {
        try {
            String mimeType = getContentResolver().getType(uri);
            if (mimeType != null) {
                return mimeType.equals("image/jpeg") ||
                        mimeType.equals("image/png") ||
                        mimeType.equals("image/jpg");
            }

            // If we can't get MIME type, check file extension
            String path = uri.getPath();
            if (path != null) {
                String extension = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
                return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png");
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking image type", e);
            return false;
        }
    }

    /**
     * Set up click listeners for all interactive UI elements
     */
    private void setupClickListeners() {
        // Initialize UI elements
        returnButton = findViewById(R.id.returnButton);
        saveButton = findViewById(R.id.saveButton);
        change_image = findViewById(R.id.change_image);

        // Return button - closes this activity and goes back to previous screen
        returnButton.setOnClickListener(v -> finish());

        // Change image click listener - opens gallery to select an image
        change_image.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });
        // Save button - commits the selected image to Firebase Firestore
        saveButton.setOnClickListener(v -> saveProfileImage());
    }

    /**
     * Load the user's current profile image from Firebase and display it in the UI
     * This runs when the activity starts to show the current profile image
     */
    private void loadCurrentProfileImage() {
        if (currentUser == null) {
            Log.e(TAG, "No current user authenticated");
            return;
        }

        // Get the user's document from the Users collection using their UID
        DocumentReference userRef = db.collection("Users").document(currentUser.getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve the profilepic field value
                String currentProfilePic = documentSnapshot.getString("profilepic");
                if (currentProfilePic != null && !currentProfilePic.isEmpty()) {
                    // Update local state and display the current image
                    selectedImage = currentProfilePic;

                    // Display the current profile image
                    ProfileImageHelper.loadProfileImage(this, change_image, currentProfilePic);
                } else {
                    // If profilepic is empty/null, use default image
                    change_image.setImageResource(R.drawable.sad_mouse);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading current profile image from Firebase", e);
        });
    }

    /**
     * Check if camera permission is granted
     */
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request camera permission
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    /**
     * Check if storage permission is granted
     */
    private boolean checkStoragePermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Request storage permission
     */
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    /**
     * Handle permission request results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Open gallery to select an image
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        galleryLauncher.launch(intent);
    }

    /**
     * Open camera to take a photo
     */
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.project2025.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create a file to store the camera image
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Handle the camera image after it's taken
     */
    private void handleCameraImage() {
        if (currentPhotoPath != null) {
            File f = new File(currentPhotoPath);
            customImageUri = Uri.fromFile(f);
            isCustomImage = true;

            // Show a preview of the captured image
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                change_image.setImageBitmap(bitmap); // Display the captured image

                // Optionally, add the photo to the gallery
                galleryAddPic();

                Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error processing camera image", e);
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Add the photo to the gallery
     */
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    /**
     * Save the selected profile image to Firebase Storage
     * Updates the user's document with the new profilepic value
     */
    private void saveProfileImage() {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state to provide user feedback during save operation
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        if (customImageUri != null) {
            // Upload custom image to Firebase Storage
            uploadImageToStorage();
        } else {
            // No image selected, show error message
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            saveButton.setText("Save");
        }
    }

    /**
     * Upload custom image to Firebase Storage
     */
    private void uploadImageToStorage() {
        try {
            // Create a storage reference
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageName = "profile_" + currentUser.getUid() + "_" + timeStamp + ".jpg";
            StorageReference imageRef = storageRef.child("profile_images/" + imageName);

            // Upload the image
            SharedPreferences sharedPreferences = getSharedPreferences("ROLE", MODE_PRIVATE);
            String role = sharedPreferences.getString("Role", "Users");
            UploadTask uploadTask = imageRef.putFile(customImageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Get the download URL
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Save the download URL to Firestore
                    String imageUrl = uri.toString();
                    DocumentReference userRef = db.collection(role).document(currentUser.getUid());
                    userRef.update("profilepic", imageUrl, "isCustomImage", true)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Custom profile image updated successfully");
                                Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating custom profile image in Firebase", e);
                                Toast.makeText(this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                saveButton.setEnabled(true);
                                saveButton.setText("Save");
                            });
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting download URL", e);
                    Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                    saveButton.setText("Save");
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading image", e);
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
                saveButton.setText("Save");
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in uploadImageToStorage", e);
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            saveButton.setText("Save");
        }
    }
}