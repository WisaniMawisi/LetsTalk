package com.example.letstalk;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_view);


        imageView = findViewById(R.id.imageView);
        imageUri = getIntent().getStringExtra("uri");
        Picasso.get().load(imageUri).into(imageView);

    }
}