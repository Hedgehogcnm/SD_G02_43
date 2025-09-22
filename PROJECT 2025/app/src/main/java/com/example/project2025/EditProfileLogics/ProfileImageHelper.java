package com.example.project2025.EditProfileLogics;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.project2025.R;

/**
 * Utility class to help load and display profile images
 */
public class ProfileImageHelper {

    /**
     * Load the profile image into the provided ImageView
     *
     * @param context             Application/Activity context
     * @param imageView           ImageView where the profile picture will be displayed
     * @param profilePicFilename  Either a URL (from Firebase Storage) or the name of a predefined drawable
     */
    public static void loadProfileImage(Context context, ImageView imageView, String profilePicFilename) {
        if (profilePicFilename == null || profilePicFilename.isEmpty()) {
            // If no profile picture set, use default loading circle with rotation animation
            imageView.setImageResource(R.drawable.ic_loading);
            
            // Create and apply rotation animation
            RotateAnimation rotate = new RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            );
            rotate.setDuration(1000);
            rotate.setRepeatCount(Animation.INFINITE);
            rotate.setInterpolator(new LinearInterpolator());
            
            imageView.startAnimation(rotate);
            return;
        }

        if (profilePicFilename.startsWith("http://") || profilePicFilename.startsWith("https://")) {
            // Profile picture is a Firebase Storage URL

            // 🔥 Fix flicker:
            // Step 1: Try loading from Glide cache ONLY (no placeholder shown)
            Glide.with(context)
                    .load(profilePicFilename)
                    .onlyRetrieveFromCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            // Cached image available → show instantly (no flicker)
                            imageView.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            // Not used
                        }

                        @Override
                        public void onLoadFailed(Drawable errorDrawable) {
                            // Step 2: If not cached → fallback to full load with placeholder
                            imageView.setImageResource(R.drawable.ic_loading);
                            
                            // Create and apply rotation animation
                            RotateAnimation rotate = new RotateAnimation(
                                0f, 360f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f
                            );
                            rotate.setDuration(1000);
                            rotate.setRepeatCount(Animation.INFINITE);
                            rotate.setInterpolator(new LinearInterpolator());
                            
                            imageView.startAnimation(rotate);
                            
                            // Load the actual image
                            Glide.with(context)
                                    .load(profilePicFilename)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(imageView);
                        }
                    });

            return;
        }

        // Profile picture is a predefined drawable filename
        String resourceName = profilePicFilename.replace(".jpg", "");
        int drawableResource = getDrawableResource(context, resourceName);

        // Load the drawable resource directly with Glide (caches resource internally)
        Glide.with(context)
                .load(drawableResource)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView);
    }

    /**
     * Map a string filename (without extension) to the actual drawable resource ID
     *
     * @param context   Application/Activity context
     * @param imageName The filename without extension (e.g., "sad_cat")
     * @return          The drawable resource ID
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
            case "predefine_profile_image":
                return R.drawable.predefine_profile_image;
            case "loading_circle":
                return R.drawable.ic_loading;
            case "sad_mouse":
            default:
                return R.drawable.ic_loading;
        }
    }
}
