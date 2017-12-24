package com.company.yolo.photosniper;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


//        buttonConnectToServer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ServerConnectionWebsocket.getInstantce().start();
//            }
//        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "enter onActivityResult  [-] MainActivity [-] onActivityResult ");

        if(requestCode == TakeImageActivity.REQUEST_IMAGE_CAPTURE_SIMPLE){

            if(resultCode == RESULT_OK){

                sendImageToServer();
//                Bundle bundle = data.getExtras();
//
//                if(bundle == null){
//                    Log.e("MainActivity", "bundle was null [-] MainActivity [-] onActivityResult ");
//                    return;
//                }
//
//                final String resultString = bundle.getString(TakeImageActivity.RESULT_KEY);
//
//                // Display on ui in the ui-thread
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        textViewServerResult.setText(resultString);
//                    }
//                });

            } else {
                Log.e("MainActivity", "result was not okay  [-] MainActivity [-] onActivityResult ");
            }

        }
    }


    public void sendImageToServer(){
        byte[] image = ImageHandler.getInstance().getImage();
    }




}
