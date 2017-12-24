package com.company.yolo.photosniper;

/**
 * Created by mathiaskrill on 12/24/17.
 */

public class ImageHandler {

    private byte[] image;
    private static ImageHandler instance;

    private ImageHandler() {

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
