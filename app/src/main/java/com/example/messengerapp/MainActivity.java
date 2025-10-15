package com.example.messengerapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText edtMessage;
    private LinearLayout chatContainer;

    // Launchers for new Activity Result API
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> captureImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtMessage = findViewById(R.id.edtMessage);
        chatContainer = findViewById(R.id.chatContainer);

        ImageButton btnSend = findViewById(R.id.btnSend);
        ImageButton btnPick = findViewById(R.id.btnPickImage);
        ImageButton btnCapture = findViewById(R.id.btnCaptureImage);

        // --- Setup launcher for picking image from gallery ---
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        addImageToChat(imageUri);
                    }
                }
        );

        // --- Setup launcher for capturing image from camera ---
        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null && extras.get("data") != null) {
                            Bitmap photo = (Bitmap) extras.get("data");
                            addImageToChat(photo);
                        }
                    }
                }
        );

        // --- Button click events ---
        btnSend.setOnClickListener(v -> sendTextMessage());
        btnPick.setOnClickListener(v -> pickImage());
        btnCapture.setOnClickListener(v -> captureImage());
    }

    /** Send text message and share via other apps */
    private void sendTextMessage() {
        String msg = edtMessage.getText().toString().trim();
        if (!msg.isEmpty()) {
            // Add message to chat view
            TextView textView = new TextView(this);
            textView.setText(msg);
            textView.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            textView.setPadding(16, 8, 16, 8);
            chatContainer.addView(textView);

            // Share text using Intent
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
            startActivity(Intent.createChooser(sendIntent, "Sharing..."));

            edtMessage.setText("");
        }
    }

    /** Open gallery to select an image */
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    /** Open camera to capture a new image */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageLauncher.launch(intent);
    }

    /** Add selected image (from gallery) to chat container */
    private void addImageToChat(Uri uri) {
        ImageView img = new ImageView(this);
        img.setImageURI(uri);
        img.setAdjustViewBounds(true);
        img.setMaxHeight(500);

        // Click to open image viewer
        img.setOnClickListener(v -> {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(uri, "image/*");
            startActivity(viewIntent);
        });

        chatContainer.addView(img);
    }

    /** Add captured image (from camera) to chat container */
    private void addImageToChat(Bitmap bitmap) {
        ImageView img = new ImageView(this);
        img.setImageBitmap(bitmap);
        img.setAdjustViewBounds(true);
        img.setMaxHeight(500);
        chatContainer.addView(img);
    }
}
