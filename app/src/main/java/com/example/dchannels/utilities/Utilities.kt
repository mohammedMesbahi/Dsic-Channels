package com.example.dchannels.utilities

import android.app.Activity
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.dchannels.Constants
import com.example.dchannels.Models.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class Utilities {
    companion object {
        fun toggleLoading(isLoading:Boolean,button: Button,progressBar: ProgressBar) {
            if (isLoading) {
                button.visibility = View. INVISIBLE
                progressBar.visibility = View. VISIBLE
            } else {
                button.visibility = View. VISIBLE
                progressBar.visibility = View. INVISIBLE
            }
        }
        fun setProfilePic(activity: Activity, selectedImageUri: Any, profilePic: ImageView) {
            Glide.with(activity).load(selectedImageUri).apply(RequestOptions.circleCropTransform()).into(profilePic)
        }

        fun showToast(context: AppCompatActivity, s: String) {
            Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
        }
        fun uploadImageToCloudStorage(
            user: User,
            imageUri: Uri
        ): UploadTask {
            val storage = FirebaseStorage.getInstance()
            // Create a storage reference
            val storageRef = storage.reference
            // Create a reference to 'images/specificId.jpg'
            val imageRef = storageRef.child("${Constants.PROFILE_IMAGE_PATH}${user.id}")
            // Upload the file to Firebase Storage

            return imageRef.putFile(imageUri)
        }
        
        fun loadImageIntoView(imageView: ImageView, id: String) {
            FirebaseStorage.getInstance().getReference().child("profile_pics")
                .child(id)
            if (id != null && id.isNotEmpty()){
                // Get Firebase storage instance
                val storage = FirebaseStorage.getInstance()
                // Create a reference to the image
                val imageRef = storage.reference.child("profile_pics").child(id)

                // Download and display the image using Glide
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(imageView.context)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageView)
                }.addOnFailureListener {
                    // Handle any errors
                    showToast(imageView.context as AppCompatActivity,"Image load failed ${it.message}")
                }
            }

        }


    }
}