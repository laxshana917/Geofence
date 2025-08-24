package com.laxshana.project.geofence;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import androidx.appcompat.widget.Toolbar;

public class ImageListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> imageUrls = new ArrayList<>();
    ImageAdapter adapter;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ImageAdapter(this, imageUrls);
        recyclerView.setAdapter(adapter);

        storageRef = FirebaseStorage.getInstance().getReference("photos"); // Your folder

        fetchImages();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back arrow
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Go back to previous activity
        return true;
    }


    private void fetchImages() {
        storageRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }
}
