package com.company.yolo.photosniper;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import info.guardianproject.netcipher.client.TlsOnlySocketFactory;

public class ImageToServer {

    public static String communicateWithServer(byte[] imagByteArray) {
        Log.e("TakingImage", "enter communicateWithServer [-] ImageToServer [-] communicateWithServer");

        String resultString = "";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("plattformVersion", Build.VERSION.RELEASE);
            jsonObject.put("image",encodeToBase64String(imagByteArray));
        } catch (JSONException e) {
            Log.e("TakingImage", "JSONException: " + e.getMessage() + " [-] ImageToServer [-] communicateWithServer");
            return null;
        }

        JSONObject resultJson = null;

        try {
            resultJson = sendRequestToServer("https://adressOfYourService", jsonObject.toString());

        } catch (Exception e) {
            return "error while sendRequestToServer";
        }


        try {

            // parsing json from server

        }
        /*
        catch (JSONException e) {
            Log.e("TakingImage", "parsing json from server failed: ("+e.getMessage()+") [-] ImageToServer [-] communicateWithServer ");
            return null;
        }
        */ catch (Exception e) {
            Log.e("TakingImage", "error happend: (" + e.getMessage() + ") [-] ImageToServer [-] communicateWithServer ");
            return null;
        }
        return resultString;
    }

    public static String encodeToBase64String(byte[] file) {
        String encodedImage = Base64.encodeToString(file, Base64.DEFAULT);
        return encodedImage;
    }


    public static JSONObject sendRequestToServer(String uri, String json) {
        Log.d("TakingImage", "enter sendRequestToServer  [-] ImageToServer [-] sendRequestToServer ");

        javax.net.ssl.HttpsURLConnection urlConnection = null;
        String data = json;
        JSONObject jsonTransportObject = null;
        try {
            //Connect
            useTLSv1();
            urlConnection = (HttpsURLConnection) ((new URL(uri).openConnection()));
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            // Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(data);
            writer.close();
            outputStream.close();

            //Read
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            bufferedReader.close();

            // This can happen without a exception
            if (urlConnection.getResponseCode() != 200) {
                Log.e("TakingImage", "http-response was: " + sb.toString() + " with: (" + urlConnection + ") enter sendRequestToServer  [-] ImageToServer [-] sendRequestToServer ");
            }

            String transportString = sb.toString();
            jsonTransportObject = new JSONObject(transportString);


            return jsonTransportObject;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.e("TakingImage", "UnsupportedEncodingException: " + e.getMessage() + "  [-] ImageToServer [-] sendRequestToServer ");
        } catch (ProtocolException e) {
            Log.e("TakingImage", "ProtocolException: " + e.getMessage() + "  [-] ImageToServer [-] sendRequestToServer ");
        } catch (MalformedURLException e) {
            Log.e("TakingImage", "MalformedURLException: " + e.getMessage() + "  [-] ImageToServer [-] sendRequestToServer ");
        } catch (IOException e) {
            Log.e("TakingImage", "IOException: " + e.getMessage() + "  [-] ImageToServer [-] sendRequestToServer ");
        }
        return jsonTransportObject;
    }


    public static void useTLSv1() {
        SSLContext sslcontext = null;
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            sslcontext = SSLContext.getInstance("TLSv1");
            sslcontext.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory noSSLv3Factory = new TlsOnlySocketFactory(sslcontext.getSocketFactory());
            HttpsURLConnection.setDefaultSSLSocketFactory(noSSLv3Factory);


            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e("TakingImage", "useTLSv1 error: " + e.getMessage() + "  [-] ImageToServer [-] useTLSv1 ");
        } catch (KeyManagementException e) {
            Log.e("TakingImage", "useTLSv1 error: " + e.getMessage() + "  [-] ImageToServer [-] useTLSv1 ");
        }
    }

    public static void useTLSv1Null() {
        SSLContext sslcontext = null;
        try {

            sslcontext = SSLContext.getInstance("TLSv1");
            sslcontext.init(null, null, null);
            SSLSocketFactory noSSLv3Factory = new TlsOnlySocketFactory(sslcontext.getSocketFactory());
            HttpsURLConnection.setDefaultSSLSocketFactory(noSSLv3Factory);

        } catch (NoSuchAlgorithmException e) {
            Log.e("TakingImage", "useTLSv1Null error: " + e.getMessage() + "  [-] ImageToServer [-] useTLSv1 ");
        } catch (KeyManagementException e) {
            Log.e("TakingImage", "useTLSv1Null error: " + e.getMessage() + "  [-] ImageToServer [-] useTLSv1 ");
        }
    }

}
