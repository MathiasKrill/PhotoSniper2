package com.company.yolo.photosniper;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static com.company.yolo.photosniper.ServerConnectionWebsocket.EchoWebSocketListener.NORMAL_CLOSURE_STATUS;

/**
 * Created by mathiaskrill on 12/23/17.
 */

public class ServerConnectionWebsocket {
    private OkHttpClient client;
    private static ServerConnectionWebsocket instance;
    private String webSocketAdresse = "ws://demos.kaazing.com/echo"; // default adress
    private WebSocket webSocket;

    public String getWebSocketAdresse() {
        return webSocketAdresse;
    }

    public void setWebSocketAdresse(String webSocketAdresse) {
        this.webSocketAdresse = webSocketAdresse;
    }

    interface ServerResultCallback {
        void serverConnected(String status);
        void resultCallback(String result);
    }

    public ServerConnectionWebsocket() {
        client = new OkHttpClient();
    }


    public void send(byte[] dataToSend, ServerResultCallback serverResultCallback) {
        Log.d("Websocket", "onOpen [-] ServerConnectionWebsocket [-] send");

        SendImageSyncTask sendImageSyncTask = new SendImageSyncTask(dataToSend,serverResultCallback);
        sendImageSyncTask.execute("");
    }

    public final class EchoWebSocketListener extends WebSocketListener {
        public static final int NORMAL_CLOSURE_STATUS = 1000;
        public String dataToSend;
        public ServerResultCallback serverResultCallback;

        public EchoWebSocketListener(byte[] data, ServerResultCallback callback){
            dataToSend = encodeToBase64String(data);
            serverResultCallback = callback;
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d("Websocket", "onOpen [-] ServerConnectionWebsocket [-] EchoWebSocketListener [-] onOpen");
            webSocket.send(dataToSend);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d("Websocket", "onMessage: ("+text
                    +")[-] ServerConnectionWebsocket [-] EchoWebSocketListener [-] onMessage");

            if (serverResultCallback != null) {
                serverResultCallback.resultCallback(text);
                closeConnection();
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            Log.d("Websocket", "onMessage: ("+bytes
                    +")[-] ServerConnectionWebsocket [-] EchoWebSocketListener [-] onMessage");

        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.d("Websocket", "onClosing: ("+reason
                    +")[-] ServerConnectionWebsocket [-] EchoWebSocketListener [-] onClosing");
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.d("Websocket", "onFailure response: ("+response
                    +")[-] ServerConnectionWebsocket [-] EchoWebSocketListener [-] onFailure");
        }
    }


    public void closeConnection() {
        webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        client.dispatcher().executorService().shutdown();
    }


    public class SendImageSyncTask extends AsyncTask<String, Void, String> {

        // The volatile variables can read and shared between threads, while a synchronized would still respect a volatile object.
        volatile byte[] data;
        volatile ServerResultCallback listener;

        public SendImageSyncTask(@NonNull byte[] data, @NonNull ServerResultCallback callback) {
            this.data = data;
            this.listener = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d("SendImageSyncTask", "doInBackground [-] ServerConnectionWebsocket [-] SendImageSyncTask [-] doInBackground");

            Request request = new Request.Builder().url(webSocketAdresse).build();
            EchoWebSocketListener echoWebSocketListener = new EchoWebSocketListener(data,listener);
            webSocket = client.newWebSocket(request, echoWebSocketListener);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("SendImageSyncTask", "onPostExecute [-] ServerConnectionWebsocket [-] SendImageSyncTask [-] onPostExecute");

        }

        @Override
        protected void onPreExecute() {
            Log.d("SendImageSyncTask", "onPreExecute [-] ServerConnectionWebsocket [-] SendImageSyncTask [-] onPreExecute");

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public static String encodeToBase64String(byte[] file) {
        Log.d("Websocket", "encodeToBase64String [-] ServerConnectionWebsocket [-] encodeToBase64String");
        String encodedImage = Base64.encodeToString(file, Base64.DEFAULT);
        return encodedImage;
    }


}

