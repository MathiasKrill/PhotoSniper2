package com.company.yolo.photosniper;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
                final ServerCommunicatorWebSocket serverCommunicatorWebSocket = new ServerCommunicatorWebSocket(MainActivity.this);
                serverCommunicatorWebSocket.setUri(editTextServerAdress.getText().toString());
                serverCommunicatorWebSocket.connectToServer(new ServerCommunicatorWebSocket.Listener() {
                    @Override
                    public void onSuccess() {
                        serverCommunicatorWebSocket.sendImageAsString("teststring");
                    }

                    @Override
                    public void onAnswer(final String answer) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewServerResult.setText(answer);
                            }
                        });
                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "enter onActivityResult  [-] MainActivity [-] onActivityResult ");

        if (requestCode == TakeImageActivity.REQUEST_IMAGE_CAPTURE_SIMPLE) {

            if (resultCode == RESULT_OK) {

                final ServerCommunicatorWebSocket serverCommunicatorWebSocket = new ServerCommunicatorWebSocket(this);
                serverCommunicatorWebSocket.connectToServer(new ServerCommunicatorWebSocket.Listener() {
                    @Override
                    public void onSuccess() {
                        serverCommunicatorWebSocket.sendImageAsString(encodeToBase64String(ImageHandler.getInstance().getImage()));
                    }

                    @Override
                    public void onAnswer(final String answer) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewServerResult.setText(answer);
                            }
                        });
                    }
                });


            } else {
                Log.e("MainActivity", "result was not okay  [-] MainActivity [-] onActivityResult ");
            }

        }
    }

    public static String encodeToBase64String(byte[] file) {
        String encodedImage = Base64.encodeToString(file, Base64.DEFAULT);
        return encodedImage;
    }

}
