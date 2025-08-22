package com.example.project2025;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private MaterialButton defaultButton, saveButton;
    // Return/back button
    private ImageView returnButton;
    
    // Firebase components for authentication and database
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    
    // Track which image is currently selected
    private String selectedImage = "sad_mouse.jpg"; // Default image filename
    private CardView selectedCard = null; // Reference to the currently selected card for highlighting

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.change_image);

        // Initialize Firebase authentication and database instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Set up the user interface components
        initializeViews();
        // Set up click listeners for all interactive elements
        setupClickListeners();
        // Load and highlight the user's current profile image selection
        loadCurrentProfileImage();
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
        sadCatCard.setOnClickListener(v -> selectImage("sad_cat.jpg", sadCatCard));
        happyMonkeyCard.setOnClickListener(v -> selectImage("happy_monkey.jpg", happyMonkeyCard));
        scaredCatCard.setOnClickListener(v -> selectImage("scared_cat.jpg", scaredCatCard));
        desperateDogCard.setOnClickListener(v -> selectImage("desperate_dog.jpg", desperateDogCard));

        // Default button - selects the default sad_mouse image (no card to highlight)
        defaultButton.setOnClickListener(v -> selectImage("sad_mouse.jpg", null));

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
     * Save the selected profile image to Firebase Firestore
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

        // Update the profilepic field in the user's document
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