package com.company.yolo.photosniper;

import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by mathiaskrill on 12/23/17.
 */

public class ServerConnectionWebsocket {
    private OkHttpClient client;
    private static ServerConnectionWebsocket instance;
    private String webSocketAdresse = "ws://192.168.2.102:30000";
    private ByteString dataToSend;

    private ServerConnectionListener serverConnectionListener;

    public ServerConnectionWebsocket() {
        client = new OkHttpClient();
    }

    public static ServerConnectionWebsocket getInstantce() {

        if (instance == null) {
            instance = new ServerConnectionWebsocket();
        }

        return instance;
    }



    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            webSocket.send(dataToSend);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            triggerCallback("Receiving : " + text);
           // webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            triggerCallback("Receiving bytes : " + bytes.hex());
          //  webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            triggerCallback("Closing : " + code + " / " + reason);
           // webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            triggerCallback("Error : " + t.getMessage());
        }
    }


    public void send(byte [] data) {

        Request request = new Request.Builder().url(webSocketAdresse).build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }

    private void triggerCallback(String result){
        if(serverConnectionListener != null){
            serverConnectionListener.serverResultCallback(result);
        }
    }

    public interface ServerConnectionListener {

        void serverResultCallback(String result);
    }

    public ServerConnectionListener getServerConnectionListener() {
        return serverConnectionListener;
    }

    public void setServerConnectionListener(ServerConnectionListener serverConnectionListener) {
        this.serverConnectionListener = serverConnectionListener;
    }
}

