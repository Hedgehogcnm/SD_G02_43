package com.example.project2025.ManageUser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.project2025.R;
import com.example.project2025.EditProfileLogics.ProfileImageHelper;
import com.example.project2025.SignIn_Login_Onboarding.SignInActivity;
import com.example.project2025.Specific_Admin.AdminActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

public class ManageUserEditUser extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView usernameTextView, emailTextView, feederIPTextView;
    private Button deleteUserButton;
    private LinearLayout changeUsername, changeIP;
    private ImageView profileImageView, returnButton, editPenIcon;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String uid;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manage_user_user_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferences = getSharedPreferences("ADMINISTRATION", MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        returnButton = findViewById(R.id.returnButton);
        changeUsername = findViewById(R.id.change_name);
        changeIP = findViewById(R.id.feeder_ip);
        usernameTextView = findViewById(R.id.current_name);
        emailTextView = findViewById(R.id.email_text);
        feederIPTextView = findViewById(R.id.feeder_ip_text);
        profileImageView = findViewById(R.id.profile_image);
        editPenIcon = findViewById(R.id.edit_pen_icon);
        deleteUserButton = findViewById(R.id.delete_user);

        changeUsername.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ManageUserChangeName.class);
            startActivity(intent);
        });

        changeIP.setOnClickListener(v -> {
            showChangeIPDialog();
        });

        editPenIcon.setOnClickListener(v -> {
            Log.d("ManageUserEditUser", "Edit pen icon clicked");
            showChangeProfilePictureDialog();
        });

        profileImageView.setOnClickListener(v -> {
            Log.d("ManageUserEditUser", "Profile image clicked");
            showChangeProfilePictureDialog();
        });

        returnButton.setOnClickListener(v -> {
            finish();
        });

        deleteUserButton.setOnClickListener(v->{
            showDeleteDialog();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeText();
        initializeProfileImage();
    }

    private void initializeText(){
        usernameTextView.setText(sharedPreferences.getString("name", ""));
        emailTextView.setText(sharedPreferences.getString("email", ""));
        feederIPTextView.setText(sharedPreferences.getString("feeder_ip", ""));
    }

    private void initializeProfileImage(){
        DocumentReference userRef = db.collection("Users").document(sharedPreferences.getString("uid", ""));
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String currentProfilePic = documentSnapshot.getString("profilepic");

                if (currentProfilePic != null && !currentProfilePic.isEmpty()) {
                    ProfileImageHelper.loadProfileImage(this, profileImageView, currentProfilePic);
                } else {
                    ProfileImageHelper.loadProfileImage(this, profileImageView, null);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("ManageUserEditUser", "Error loading profile image", e);
        });
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + sharedPreferences.getString("name", "") + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteUser();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // API for delete User from Firestore and Firebase auth
    private void deleteUser(){
        String uid = sharedPreferences.getString("uid", "");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "No UID found", Toast.LENGTH_SHORT).show();
            return;
        }
        String APIurl = "https://us-central1-divine-course-467504-m2.cloudfunctions.net/deleteUser";
        JSONObject payload = new JSONObject();
        try {
            payload.put("uid", uid);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Genuinely no idea whats going on with this API call, but it works
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, APIurl, payload,
                response -> {
                    Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                    startActivity(intent);
                },
                error -> {
                    if(error.getMessage() != null){
                        int statusCode = error.networkResponse.statusCode;
                        String responseBody = new String(error.networkResponse.data);
                        Toast.makeText(this, "Error: " + statusCode + " " + responseBody, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                        startActivity(intent);
                    }
                });

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private void showChangeIPDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CHANGE FEEDER IP");
        builder.setMessage("\nPlease enter your pet feeder's IP address");

        // Create the dialog layout with proper margins
        final EditText ipInput = new EditText(this);
        ipInput.setHint("Enter your new IP");
        ipInput.setInputType(InputType.TYPE_CLASS_TEXT);

        // Add margin to the EditText
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10); // Left, Top, Right, Bottom padding
        layout.addView(ipInput);

        builder.setView(layout);

        // Set positive and negative buttons
        builder.setPositiveButton("Set", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipInput.getText().toString();

                if (TextUtils.isEmpty(ip)) {
                    Log.d("ChangeIP", "IP = " + ip);
                    Toast.makeText(getApplicationContext(), "Please enter your IP", Toast.LENGTH_SHORT).show();
                    return; // Don't dismiss dialog
                }

                DocumentReference userRef = db.collection("Users").document(sharedPreferences.getString("uid", ""));
                userRef.update("feeder_ip", ip).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("ChangeIP", "Update successful for UID: " + sharedPreferences.getString("uid", ""));
                            dialog.dismiss();
                            feederIPTextView.setText(ip);
                            sharedPreferences = getSharedPreferences("ADMINISTRATION", MODE_PRIVATE);
                            sharedPreferences.edit().putString("feeder_ip", ip).apply();
                            Toast.makeText(getApplicationContext(), "Update Successfully", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Log.e("ChangeIP", "Update failed", task.getException());
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void showChangeProfilePictureDialog() {
        Log.d("ManageUserEditUser", "Creating profile picture dialog");
        
        // Create and show dialog first
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CHANGE PROFILE PICTURE");
        builder.setMessage("Select a new profile picture for " + sharedPreferences.getString("name", ""));
        
        // Create a custom layout for the dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        // Create button for gallery option
        Button galleryButton = new Button(this);
        galleryButton.setText("Choose from Gallery");
        galleryButton.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Add button to layout
        layout.addView(galleryButton);

        builder.setView(layout);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("ManageUserEditUser", "Dialog cancelled");
                dialog.dismiss();
            }
        });
        
        AlertDialog dialog = builder.create();
        
        // Set the click listener after dialog is created
        galleryButton.setOnClickListener(v -> {
            Log.d("ManageUserEditUser", "Gallery button clicked");
            dialog.dismiss(); // Close the dialog first
            openGallery();
        });
        
        Log.d("ManageUserEditUser", "Showing dialog");
        dialog.show();
    }

    private void showDefaultImagesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SELECT DEFAULT IMAGE");

        // Create a grid layout for profile picture options
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 10);

        // Create horizontal layouts for the profile pictures
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(android.view.Gravity.CENTER);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setGravity(android.view.Gravity.CENTER);

        // Profile picture options
        String[] profilePics = {"sad_cat", "happy_monkey", "scared_cat", "desperate_dog", "predefine_profile_image", "sad_mouse"};
        String[] displayNames = {"Sad Cat", "Happy Monkey", "Scared Cat", "Desperate Dog", "Default Profile", "Sad Mouse"};

        for (int i = 0; i < profilePics.length; i++) {
            final String profilePic = profilePics[i];
            final String displayName = displayNames[i];

            // Create a vertical layout for each profile picture option
            LinearLayout optionLayout = new LinearLayout(this);
            optionLayout.setOrientation(LinearLayout.VERTICAL);
            optionLayout.setGravity(android.view.Gravity.CENTER);
            optionLayout.setPadding(10, 10, 10, 10);

            // Create ImageView for the profile picture
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            
            // Load the profile picture
            int drawableResource = getDrawableResource(profilePic);
            imageView.setImageResource(drawableResource);

            // Create TextView for the display name
            TextView textView = new TextView(this);
            textView.setText(displayName);
            textView.setTextSize(10);
            textView.setGravity(android.view.Gravity.CENTER);
            textView.setPadding(0, 5, 0, 0);

            // Add click listener to the option
            optionLayout.setOnClickListener(v -> {
                updateUserProfilePicture(profilePic);
            });

            optionLayout.addView(imageView);
            optionLayout.addView(textView);

            // Add to appropriate row
            if (i < 3) {
                row1.addView(optionLayout);
            } else {
                row2.addView(optionLayout);
            }
        }

        layout.addView(row1);
        layout.addView(row2);

        builder.setView(layout);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private int getDrawableResource(String imageName) {
        switch (imageName) {
            case "sad_cat":
                return R.drawable.sad_cat;
            case "happy_monkey":
                return R.drawable.happy_monkey;
            case "scared_cat":
                return R.drawable.scared_cat;
            case "desperate_dog":
                return R.drawable.desperate_dog;
            case "predefine_profile_image":
                return R.drawable.predefine_profile_image;
            case "sad_mouse":
            default:
                return R.drawable.sad_mouse;
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String uid = sharedPreferences.getString("uid", "");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "No UID found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading message
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        // Create a reference to the image in Firebase Storage
        StorageReference imageRef = storage.getReference().child("profile_images").child(uid + "_" + System.currentTimeMillis() + ".jpg");

        // Upload the image
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Update the user's profile picture URL in Firestore
                                updateUserProfilePicture(downloadUri.toString());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("UploadImage", "Failed to get download URL", e);
                                Toast.makeText(getApplicationContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("UploadImage", "Failed to upload image", e);
                        Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserProfilePicture(String newProfilePic) {
        String uid = sharedPreferences.getString("uid", "");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "No UID found", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("Users").document(uid);
        userRef.update("profilepic", newProfilePic).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("ChangeProfilePic", "Update successful for UID: " + uid);
                    // Update the profile image in the UI
                    ProfileImageHelper.loadProfileImage(ManageUserEditUser.this, profileImageView, newProfilePic);
                    Toast.makeText(getApplicationContext(), "Update Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ChangeProfilePic", "Update failed", task.getException());
                    Toast.makeText(getApplicationContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}