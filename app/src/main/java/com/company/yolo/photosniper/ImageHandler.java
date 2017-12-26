package com.company.yolo.photosniper;

/**
 * Created by mathiaskrill on 12/24/17.
 */

public class ImageHandler {

    private float imageHeight;
    private float imageWidth;
    private byte[] image;
    private static ImageHandler instance;

    private ImageHandler() {

    }

    public float getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(float imageHeight) {
        this.imageHeight = imageHeight;
    }

    public float getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(float imageWidth) {
        this.imageWidth = imageWidth;
    }

    public static ImageHandler getInstance() {
        if (instance == null) {
            instance = new ImageHandler();
        }
        return instance;
    }


    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
