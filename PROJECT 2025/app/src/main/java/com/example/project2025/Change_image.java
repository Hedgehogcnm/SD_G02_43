package com.example.project2025;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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

/**
 * Activity for changing user profile images
 * 
 * Features:
 * - Displays 4 animal images in a 2x2 grid layout (sad_cat, happy_monkey, scared_cat, desperate_dog)
 * - Includes a "Use Default" option for sad_mouse
 * - Highlights currently selected image when opened
 * - Saves selected image to Firebase Firestore
 * - Provides visual feedback during selection and saving
 * 
 * Flow:
 * 1. User opens screen from Settings -> My Account -> Change Profile Image
 * 2. Current profile image is loaded and highlighted
 * 3. User selects a new image (visual feedback provided)
 * 4. User clicks Save to update Firebase
 * 5. Success message shown and returns to previous screen
 */
public class Change_image extends AppCompatActivity {

    private static final String TAG = "Change_image";
    
    // UI Elements - Cards for the 4 animal profile pictures
    private CardView sadCatCard, happyMonkeyCard, scaredCatCard, desperateDogCard;
    // Buttons for default option and saving changes
    private MaterialButton defaultButton, saveButton, cameraButton, galleryButton;
    // Return/back button
    private ImageView returnButton;
    
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

        // Set up the user interface components
        initializeViews();
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
                            customImageUri = selectedImageUri;
                            isCustomImage = true;
                            clearSelection(); // Clear any previous selection
                            
                            // Show a preview of the selected image
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), customImageUri);
                                // You could display this bitmap in an ImageView if you want to show a preview
                                Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e(TAG, "Error loading gallery image", e);
                                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Initialize all UI components by finding them in the layout
     * This includes the 4 image selection cards, buttons, and return button
     */
    private void initializeViews() {
        // Initialize the 4 animal image selection cards from the 2x2 grid
        sadCatCard = findViewById(R.id.sadCatCard);
        happyMonkeyCard = findViewById(R.id.happyMonkeyCard);
        scaredCatCard = findViewById(R.id.scaredCatCard);
        desperateDogCard = findViewById(R.id.desperateDogCard);
        
        // Initialize action buttons
        defaultButton = findViewById(R.id.defaultButton); // "Use Default (Sad Mouse)" button
        saveButton = findViewById(R.id.saveButton); // "Save" button to commit changes to Firebase
        returnButton = findViewById(R.id.returnButton); // Back/return navigation button
        cameraButton = findViewById(R.id.cameraButton); // Camera button for taking photos
        galleryButton = findViewById(R.id.galleryButton); // Gallery button for selecting images
    }

    /**
     * Set up click listeners for all interactive UI elements
     * Each click handler calls the appropriate method with the selected image filename
     */
    private void setupClickListeners() {
        // Return button - closes this activity and goes back to previous screen
        returnButton.setOnClickListener(v -> finish());

        // Image selection cards - each card selects a different animal image
        // Parameters: (imageFilename, cardReference for highlighting)
        sadCatCard.setOnClickListener(v -> {
            selectImage("sad_cat.jpg", sadCatCard);
            isCustomImage = false;
            customImageUri = null;
        });
        happyMonkeyCard.setOnClickListener(v -> {
            selectImage("happy_monkey.jpg", happyMonkeyCard);
            isCustomImage = false;
            customImageUri = null;
        });
        scaredCatCard.setOnClickListener(v -> {
            selectImage("scared_cat.jpg", scaredCatCard);
            isCustomImage = false;
            customImageUri = null;
        });
        desperateDogCard.setOnClickListener(v -> {
            selectImage("desperate_dog.jpg", desperateDogCard);
            isCustomImage = false;
            customImageUri = null;
        });

        // Default button - selects the default sad_mouse image (no card to highlight)
        defaultButton.setOnClickListener(v -> {
            selectImage("sad_mouse.jpg", null);
            isCustomImage = false;
            customImageUri = null;
        });

        // Camera button - opens camera to take a photo
        cameraButton.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        // Gallery button - opens gallery to select an image
        galleryButton.setOnClickListener(v -> {
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
     * Handle image selection and visual feedback
     * @param imageName The filename of the selected image (e.g., "sad_cat.jpg")
     * @param card The CardView to highlight (null for default button)
     */
    private void selectImage(String imageName, CardView card) {
        // Clear any previous selection highlighting
        clearSelection();
        
        // Update internal state with new selection
        selectedImage = imageName;
        selectedCard = card;
        
        // Provide visual feedback for the selected card
        if (card != null) {
            // Highlight selected card with light blue background and higher elevation
            card.setCardBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue background
            card.setCardElevation(8f); // Higher elevation creates shadow effect
        }
        
        Log.d(TAG, "Selected image: " + imageName);
    }

    /**
     * Reset all image selection cards to their default unselected appearance
     * This removes highlighting from all cards before applying it to the newly selected one
     */
    private void clearSelection() {
        // Reset all cards to default white background and normal elevation
        if (sadCatCard != null) {
            sadCatCard.setCardBackgroundColor(Color.WHITE);
            sadCatCard.setCardElevation(2f); // Normal elevation
        }
        if (happyMonkeyCard != null) {
            happyMonkeyCard.setCardBackgroundColor(Color.WHITE);
            happyMonkeyCard.setCardElevation(2f);
        }
        if (scaredCatCard != null) {
            scaredCatCard.setCardBackgroundColor(Color.WHITE);
            scaredCatCard.setCardElevation(2f);
        }
        if (desperateDogCard != null) {
            desperateDogCard.setCardBackgroundColor(Color.WHITE);
            desperateDogCard.setCardElevation(2f);
        }
    }

    /**
     * Load the user's current profile image from Firebase and highlight it in the UI
     * This runs when the activity starts to show which image is currently selected
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
                    // Update local state and highlight the current selection
                    selectedImage = currentProfilePic;
                    highlightCurrentSelection();
                }
                // If profilepic is empty/null, default selection (sad_mouse) remains
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading current profile image from Firebase", e);
        });
    }

    /**
     * Highlight the currently selected image in the UI
     * Maps the selectedImage filename to the appropriate card and highlights it
     */
    private void highlightCurrentSelection() {
        clearSelection(); // First clear any existing highlights
        
        // Match the stored filename to the appropriate UI card and highlight it
        switch (selectedImage) {
            case "sad_cat.jpg":
                selectImage("sad_cat.jpg", sadCatCard);
                break;
            case "happy_monkey.jpg":
                selectImage("happy_monkey.jpg", happyMonkeyCard);
                break;
            case "scared_cat.jpg":
                selectImage("scared_cat.jpg", scaredCatCard);
                break;
            case "desperate_dog.jpg":
                selectImage("desperate_dog.jpg", desperateDogCard);
                break;
            case "sad_mouse.jpg":
            default:
                // Default image doesn't have a card to highlight (it's a button)
                selectImage("sad_mouse.jpg", null);
                break;
        }
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
            clearSelection(); // Clear any previous selection
            
            // Show a preview of the captured image
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                // You could display this bitmap in an ImageView if you want to show a preview
                
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
     * Open gallery to select an image
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    /**
     * Save the selected profile image to Firebase Firestore or Storage
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

        if (isCustomImage && customImageUri != null) {
            // Upload custom image to Firebase Storage
            uploadImageToStorage();
        } else {
            // Update the profilepic field in the user's document with predefined image
            DocumentReference userRef = db.collection("Users").document(currentUser.getUid());
            userRef.update("profilepic", selectedImage)
                    .addOnSuccessListener(aVoid -> {
                        // Success: Log the update and show success message
                        Log.d(TAG, "Profile image updated successfully to: " + selectedImage);
                        Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show();
                        finish(); // Close this activity and return to previous screen
                    })
                    .addOnFailureListener(e -> {
                        // Failure: Log error and show error message
                        Log.e(TAG, "Error updating profile image in Firebase", e);
                        Toast.makeText(this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                        
                        // Reset button to normal state so user can try again
                        saveButton.setEnabled(true);
                        saveButton.setText("Save");
            });
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
            UploadTask uploadTask = imageRef.putFile(customImageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Get the download URL
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Save the download URL to Firestore
                    String imageUrl = uri.toString();
                    DocumentReference userRef = db.collection("Users").document(currentUser.getUid());
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