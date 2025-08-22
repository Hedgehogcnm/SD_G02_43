package com.example.project2025;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Utility class to help load and display profile images throughout the app
 * 
 * This helper provides consistent profile image loading across all activities and fragments.
 * It maps the filename stored in Firebase Firestore to the appropriate drawable resource.
 * 
 * Supported profile images:
 * - sad_cat.jpg -> R.drawable.sad_cat
 * - happy_monkey.jpg -> R.drawable.happy_monkey  
 * - scared_cat.jpg -> R.drawable.scared_cat
 * - desperate_dog.jpg -> R.drawable.desperate_dog
 * - sad_mouse.jpg -> R.drawable.sad_mouse (default)
 * 
 * Usage:
 * ProfileImageHelper.loadProfileImage(context, imageView, "sad_cat.jpg");
 */
public class ProfileImageHelper {

    /**
     * Loads the appropriate profile image into an ImageView based on the filename stored in Firebase
     * 
     * This method handles the mapping between Firebase storage (filename) and Android resources (drawable).
     * If the filename is null, empty, or doesn't match any known images, it defaults to sad_mouse.
     * 
     * This method now also supports loading custom images from URLs (for images uploaded to Firebase Storage)
     * 
     * @param context The Android context (required for resource access)
     * @param imageView The ImageView widget to display the profile image in
     * @param profilePicFilename The filename stored in Firebase profilepic field (e.g., "sad_cat.jpg") or a URL
     */
    public static void loadProfileImage(Context context, ImageView imageView, String profilePicFilename) {
        // Handle null or empty filenames by showing default image
        if (profilePicFilename == null || profilePicFilename.isEmpty()) {
            imageView.setImageResource(R.drawable.sad_mouse); // Default profile image
            return;
        }
        
        // Check if the profilePicFilename is a URL (starts with http:// or https://)
        if (profilePicFilename.startsWith("http://") || profilePicFilename.startsWith("https://")) {
            // Load image from URL using Glide
            Glide.with(context)
                .load(profilePicFilename)
                .placeholder(R.drawable.sad_mouse) // Show default while loading
                .error(R.drawable.sad_mouse) // Show default on error
                .into(imageView);
            return;
        }

        // For predefined images, use the drawable resources
        // Remove .jpg extension for resource name lookup
        String resourceName = profilePicFilename.replace(".jpg", "");
        
        // Get the corresponding drawable resource ID and set it to the ImageView
        int drawableResource = getDrawableResource(context, resourceName);
        imageView.setImageResource(drawableResource);
    }

    /**
     * Maps an image name to its corresponding drawable resource ID
     * 
     * This private method contains the mapping logic between image names (without extension)
     * and their drawable resource IDs. It ensures all profile image references go through
     * a single point of control for easy maintenance.
     * 
     * @param context The Android context (currently unused but kept for future extensibility)
     * @param imageName The image name without file extension (e.g., "sad_cat")
     * @return The drawable resource ID (e.g., R.drawable.sad_cat)
     */
    private static int getDrawableResource(Context context, String imageName) {
        switch (imageName) {
            case "sad_cat":
                return R.drawable.sad_cat;
            case "happy_monkey":
                return R.drawable.happy_monkey;
            case "scared_cat":
                return R.drawable.scared_cat;
            case "desperate_dog":
                return R.drawable.desperate_dog;
            case "sad_mouse":
            default:
                // Default to sad_mouse for any unrecognized image names
                return R.drawable.sad_mouse;
        }
    }
}
