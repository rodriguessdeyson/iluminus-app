package com.rad.iluminus.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class HttpService {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }

    public static void sendCommand(String ip, String command, Callback callback) {
        executor.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                String urlString = "http://" + ip + "/set?c=" + command;
                URL url = new URL(urlString);
                
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(2000);
                urlConnection.setReadTimeout(2000);

                int responseCode = urlConnection.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    handler.post(() -> {
                        if (callback != null) callback.onSuccess();
                    });
                } else {
                    throw new IOException("HTTP Error: " + responseCode);
                }

            } catch (Exception e) {
                handler.post(() -> {
                    if (callback != null) callback.onError(e);
                });
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }

    public static void sendColorCommand(String ip, int r, int g, int b, Callback callback) {
        String command = "L&r=" + r + "&g=" + g + "&b=" + b;
        sendCommand(ip, command, callback);
    }
}
