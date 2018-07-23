package com.crystaltowerdesigns.newsapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

class HttpHandler {

    public HttpHandler() {
    }

    public String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        //noinspection finally
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();

            // if we did not receive an OK response from the urlConnection,
            // store it as an error contained in the jsonResponse and return it
            int serverResponse = urlConnection.getResponseCode();
            if (serverResponse != 200) {
                jsonResponse = "ERROR|CONNECTION ERROR|" + serverResponse + "|" + urlConnection.getResponseMessage();
                return jsonResponse;
            }

            inputStream = urlConnection.getInputStream();
            jsonResponse = convertStreamToString(inputStream);
        } catch (UnknownHostException e) {
            Log.e("HttpHandler", "makeHttpRequest IO error: Host Unreachable" + e.toString());
        } catch (IOException e) {
            Log.e("HttpHandler", "makeHttpRequest IO error: " + e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("HttpHandler", "makeHttpRequest error: " + e.toString());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private String convertStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String stringIn;
        try {
            while ((stringIn = reader.readLine()) != null) {
                stringBuilder.append(stringIn).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("HttpHandler", "convertStreamToString error: " + e.toString());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("HttpHandler", "convertStreamToString error: " + e.toString());
            }
        }
        return stringBuilder.toString();
    }
}
