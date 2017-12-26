package com.company.yolo.photosniper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import deprecated.ImageToServer;

import static android.support.v4.content.FileProvider.getUriForFile;

public class TakeImageActivity extends AppCompatActivity {

    private static boolean imageIsTaken = false;
    private static Uri targetFileUri = null;
    private static File imagePath = null;
    public static final String RESULT_KEY = "RESULT_KEY";
    public static String KEY_IMAGE_CAMERA = "KEY_IMAGE_CAMERA";
    private final int REQUEST_READ_WRITE_CAMERA_PERMISSION = 1145; // just a random number to identify the permission callback.
    public static final int REQUEST_IMAGE_CAPTURE_SIMPLE = 411; // randum number to identify the camera callback.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);

        //Allowing Strict mode policy for Nougat support
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        requestAndroidPermissoins(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_READ_WRITE_CAMERA_PERMISSION);

    }

    /**
     * Requests android permissions to use camera, to read and write stuff to the external storage.
     *
     * @param activity
     * @param permission
     * @param permissionRequest
     */
    public void requestAndroidPermissoins(Activity activity, String[] permission, int permissionRequest) {
        ActivityCompat.requestPermissions(activity, permission, permissionRequest);
    }

    public void takeImage() {

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                createInternalDirectoryAndHull();

                String packagenameFileprovider = getPackageName() + ".fileprovider"; // this name is important to get the right maping to the internal file.
                // Create the uri by mapping our internal file to the fileprovider
                targetFileUri = getUriForFile(this, packagenameFileprovider, imagePath);
            } else {
                createExternalPlaceholder();
                targetFileUri = Uri.fromFile(imagePath);
            }

        } catch (IOException e) {
            Log.e("TakingImage", "failed creating file for taking image: (" + e.getMessage() + ") [-] PhotoActivity [-] takeImage");
            return;
        }


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, targetFileUri);

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_SIMPLE);

    }

    public void createExternalPlaceholder() throws IOException {
        // Create an image file name
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Science");

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("TakingImage", "imagePath does not exist[-] PhotoActivity [-] createExternalPlaceholder");
                failedTakingImage();
            }
        }

        File image = new File(dir.getPath() + File.separator + "ImageToCheck" + ".png");
        imagePath = image;
    }

    public void createInternalDirectoryAndHull() throws IOException {

        // Create packename and filename for the camera image which will be saved in our internal space.
        File imagePath = new File(getFilesDir(), "images");
        TakeImageActivity.imagePath = new File(imagePath, "camera_image.png");

        // Create the temp and empty file which will be used to save the acutal image in.

        imagePath.mkdirs();

        if (imagePath.exists() == false) {
            Log.e("TakingImage", "imagePath does not exist [-] PhotoActivity [-] createInternalDirectoryAndHull");
            failedTakingImage();
            return;
        }

        if (TakeImageActivity.imagePath.exists() == false) {
            Log.d("TakingImage", "imagePath does already exist [-] PhotoActivity [-] createInternalDirectoryAndHull");
            TakeImageActivity.imagePath.createNewFile();
        }

        if (TakeImageActivity.imagePath.exists() == false) {
            Log.e("TakingImage", "image file does still not exist, after retry to create one [-] PhotoActivity [-] createInternalDirectoryAndHull");
            failedTakingImage();
            return;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TakingImage", "enter onActivityResult [-] PhotoActivity [-] onActivityResult");


        if (requestCode == REQUEST_IMAGE_CAPTURE_SIMPLE) {
            Log.d("TakingImage", "enter onActivityResult [-] PhotoActivity [-] onActivityResult [-] requestCode");

            if (resultCode == Activity.RESULT_CANCELED) {
                Log.e("TakingImage", "resultCode was RESULT_CANCELED [-] PhotoActivity [-] onActivityResult [-] requestCode");
                failedTakingImage();
                return;
            }

            byte[] imageBytes = null;

            // Try to get the image from the data
            if (data != null && data.getExtras() != null) {
                Log.d("TakingImage", "data was not null [-] PhotoActivity [-] onActivityResult [-] data");

                Bitmap image = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, byteBuffer);
                imageBytes = byteBuffer.toByteArray();

                if(imageBytes != null){
                    Log.d("TakingImage", "could read image from data extras [-] PhotoActivity [-] onActivityResult [-] data");

                    ImageHandler.getInstance().setImage(imageBytes);
                    closeWithSuccess();
                    return;
                } else {
                    failedTakingImage();
                }
            }


            if (imagePath != null && imageBytes == null) {
                Log.d("TakingImage", "resultCode was Okay, start reading file [-] PhotoActivity [-] onActivityResult [-] imagePath");
                imageBytes = ToolFiles.readImageFilBytesToCompressedByteArray(imagePath.getAbsolutePath());
                Log.d("TakingImage", "reading file was successfull, filesize: (" + imageBytes.length + ")  [-] PhotoActivity [-] onActivityResult [-] imagePath");

                if (imageBytes == null) {
                    failedTakingImage();
                    return;
                } else {
                    Log.d("TakingImage", "could read image from uri successfull [-] PhotoActivity [-] onActivityResult [-] data");
                    ImageHandler.getInstance().setImage(imageBytes);
                    closeWithSuccess();
                }

            } else {
                Log.e("TakingImage", "imagePath was null [-] PhotoActivity [-] onActivityResult [-] imagePath");
                failedTakingImage();
                return;
            }

        } else {
            //onActivity result data is null
            failedTakingImage();
            return;
        }
    }

    /**
     * Calling methodes to upload the image etc.
     *
     * @param byteArray
     * @return
     */
    public String progressImageServer(byte[] byteArray) {
        Log.d("TakingImage", "enter progressImageServer [-] PhotoActivity [-] progressImageServer");

        String result = "";
        result = ImageToServer.communicateWithServer(byteArray);
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public final boolean onKeyDown(final int keyCode, final KeyEvent event) {
        failedTakingImage();
        return super.onKeyDown(keyCode, event);
    }

    //---------------------------------------------------------------------------------------------
    //Permissions
    //---------------------------------------------------------------------------------------------

    public void failedTakingImage() {
        imageIsTaken = false;
        Intent returnIntent = new Intent();
        returnIntent.putExtra(RESULT_KEY, "failed taking image");
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
        return;
    }

    public void failedServer() {
        imageIsTaken = false;
        Intent returnIntent = new Intent();
        returnIntent.putExtra(RESULT_KEY, "failed server");
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
        return;
    }

    public void closeWithSuccess() {
        Log.d("TakingImage", "enter closeWithSuccess [-] PhotoActivity [-] closeWithSuccess");

        imageIsTaken = false;
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        return;
    }


    @Override
    public void onStop() {
        super.onStop();
        imageIsTaken = false;
        targetFileUri = null;
    }

    //---------------------------------------------------------------------------------------------
    //Permissions
    //---------------------------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_WRITE_CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!imageIsTaken) {

                        if (
                                permissionsGranted(new String[]{
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                            takeImage();
                        } else {
                            failedTakingImage();
                        }
                    }
                } else {
                    failedTakingImage();
                }
            }
        }
    }

    public boolean permissionGranted(String permission) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public boolean permissionsGranted(String[] permission) {
        StringBuilder permissionString = new StringBuilder();
        for (String perm : permission) {
            permissionString.append(" ").append(perm);
        }

        for (String perm : permission) {
            if (!permissionGranted(perm)) {
                return false;
            }
        }
        return true;
    }

    public static String convertByteToString(byte[] data) {
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String encodeToBase64String(byte[] file) {
        String encodedImage = Base64.encodeToString(file, Base64.DEFAULT);
        return encodedImage;
    }
}
