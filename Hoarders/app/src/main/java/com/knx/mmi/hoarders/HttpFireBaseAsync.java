package com.knx.mmi.hoarders;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpFireBaseAsync extends AsyncTask <String, Integer, String> {

    private ResultHandler resultHandler;
    private

    HttpFireBaseAsync(ResultHandler handler){
        resultHandler = handler;
    }

    @Override
    protected String doInBackground(String... strings) {

        try{
            URL url = new URL(strings[0]);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            return readStream(httpURLConnection.getInputStream());

        } catch (java.io.IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result){
        resultHandler.handleHttpResult(result);
    }

    private String readStream (InputStream inputStream){
        StringBuilder sb = new StringBuilder();

        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = br.readLine()) != null){
                sb.append(line);
            }
        } catch (java.io.IOException e){
            e.printStackTrace();
        }

        return sb.toString();
    }

    public interface ResultHandler{
        void handleHttpResult(String result);
    }
}
