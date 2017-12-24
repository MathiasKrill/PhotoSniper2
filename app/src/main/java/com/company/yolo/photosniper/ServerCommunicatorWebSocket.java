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
        void onAnswer(String answer);
    }

    WebSocketClient mWebSocketClient;

    public void connectToServer(final Listener listener){
        URI uri;
        try {
            uri = new URI("ws://ws://192.168.2.102:30000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened websocket [-] ServerCommunicatorWebSocket [-] connectToServer [-] WebSocketClient");
                listener.onSuccess();
            }

            @Override
            public void onMessage(String s) {
                Log.d(TAG, "message received: " + s);
                listener.onAnswer(s);

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

    public void sendImageAsString(String jsonAsString){
        if(mWebSocketClient == null){
            return;
        }

            mWebSocketClient.send(jsonAsString);

    }

}