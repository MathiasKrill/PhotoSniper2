package com.company.yolo.photosniper;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


/**
 * Created by thao on 10/09/16.
 */
public class ServerCommunicatorWebSocket {
    final String TAG = "ServerCommunicator";
    public static Context mContext;

    public ServerCommunicatorWebSocket(Context context){
        mContext = context;
    }

    public interface Listener {
        void onSuccess();
    }

    WebSocketClient mWebSocketClient;

    public void connectToServer(final Listener listener){
        URI uri;
        try {
            uri = new URI("ws://echo.websocket.org");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened websocket [-] ServerCommunicatorWebSocket [-] connectToServer [-] WebSocketClient");

//                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);

                listener.onSuccess();
            }

            @Override
            public void onMessage(String s) {
                Log.d(TAG, "message received: " + s);

                // TODO: read server response
                /*

                {
                  "user_is_ok": "yes",
                  "need_image": "no"
                }

                * */

                // if need_image == yes >> send image
                // sendImage();
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void sendJson(String jsonAsString){
        if(mWebSocketClient == null){
            return;
        }

            mWebSocketClient.send(jsonAsString);

    }

}