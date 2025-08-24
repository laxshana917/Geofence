package com.laxshana.project.geofence;

import java.util.ArrayList;
import com.laxshana.project.geofence.ImageItem;


public class ImageItem {
    private String imageUrl;

    public ImageItem() {
        // Default constructor required for Firebase
    }

    public ImageItem(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

