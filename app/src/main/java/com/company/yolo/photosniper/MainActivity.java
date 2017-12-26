package com.company.yolo.photosniper;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button buttonTakeImageButton;
    Button buttonConnectToServer;
    TextView textViewServerResult;
    TextView textViewServerStatus;
    EditText editTextServerAdress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        initUi();
    }

    public void initUi() {
        buttonTakeImageButton = (Button) findViewById(R.id.button_take_image);
        buttonConnectToServer = (Button) findViewById(R.id.button_connect_to_websocket);

        textViewServerResult = (TextView) findViewById(R.id.textview_server_result);
        textViewServerStatus = (TextView) findViewById(R.id.textView_server_status);
        editTextServerAdress = (EditText) findViewById(R.id.edittext_server_adress);


        buttonTakeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TakeImageActivity.class);
                startActivityForResult(intent, TakeImageActivity.REQUEST_IMAGE_CAPTURE_SIMPLE);
            }
        });


        buttonConnectToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                willPickGalleryItem(MainActivity.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "enter onActivityResult  [-] MainActivity [-] onActivityResult ");

        if (requestCode == TakeImageActivity.REQUEST_IMAGE_CAPTURE_SIMPLE) {

            if (resultCode == RESULT_OK) {

                byte[] image = ImageHandler.getInstance().getImage();
                if (image == null) {
                    Log.e("MainActivity", "image form Imagehandler was null [-] MainActivity [-] onActivityResult ");

                    return;
                }

                sendDataToServer(image);

            } else {
                Log.e("MainActivity", "result was not okay  [-] MainActivity [-] onActivityResult ");
            }

        } else if (requestCode == GALLERY_REQUEST) {
            if (data != null) {

                // Get the data from the uri
                Uri selectedUri = data.getData();

                // Read the file from the uri with inputstream - there is no alternative to that.
                byte[] byteArrayUri = (ToolFiles.readFileStreamFromUriToByteArray(this, selectedUri));

                if (byteArrayUri == null) {
                    Log.e("MainActivity", "result of gallery pick had no data[-] MainActivity [-] onActivityResult [-] GALLERY_REQUEST ");
                    return;
                }

                sendDataToServer(byteArrayUri);

            }
        }
    }

    public void sendDataToServer(byte[] data){
        ServerConnectionWebsocket serverConnectionWebsocket = new ServerConnectionWebsocket();
        serverConnectionWebsocket.setWebSocketAdresse(editTextServerAdress.getText().toString());
        serverConnectionWebsocket.send(data, new ServerConnectionWebsocket.ServerResultCallback() {
            @Override
            public void serverConnected(final String status) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewServerStatus.setText(status);
                    }
                });
            }

            @Override
            public void resultCallback(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewServerResult.setText(result);
                    }
                });
            }
        });
    }

    public static String encodeToBase64String(byte[] file) {
        String encodedImage = Base64.encodeToString(file, Base64.DEFAULT);
        return encodedImage;
    }


    public static int READ_EXTERNAL_STORAGE_PERM = 2114;
    public static int GALLERY_REQUEST = 1153;


    private void willPickGalleryItem(Activity activity) {

        String[] requiredPermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

        if (permissionsGranted(requiredPermission, activity)) {
            Log.d("Permissions", "permissions already granted for pickGalleryItem [-] ToolPermissions [-] willPickGalleryItem");
            pickGalleryItem(activity);
        } else {

            Log.d("Permissions", "permissions not granted for pickGalleryItem [-] ToolPermissions [-] willPickGalleryItem");
            requestAndroidPermissoins(activity, requiredPermission, READ_EXTERNAL_STORAGE_PERM);
        }
    }

    private void pickGalleryItem(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        activity.startActivityForResult(Intent.createChooser(intent, "Pick a image"),
                GALLERY_REQUEST);
    }

    public boolean permissionGranted(String permission, Activity activity) {
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public boolean permissionsGranted(String[] permission,Activity activity) {
        StringBuilder permissionString = new StringBuilder();
        for (String perm : permission) {
            permissionString.append(" ").append(perm);
        }

        for (String perm : permission) {
            if (!permissionGranted(perm,activity)) {
                return false;
            }
        }
        return true;
    }


    public void requestAndroidPermissoins(Activity activity, String[] permission, int permissionRequest) {
        ActivityCompat.requestPermissions(activity, permission, permissionRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (requestCode == READ_EXTERNAL_STORAGE_PERM) {

            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                willPickGalleryItem(this);
            }
        }
    }
}
