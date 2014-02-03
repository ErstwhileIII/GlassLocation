package com.jbirdvegas.glassLocation;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jbird on 2/1/14.
 */
public class HttpCaller extends AsyncTask<TextView, Void, String> {
    private static final String URL_WEBSITE = "http://google.com";
    private static final String TAG = HttpCaller.class.getSimpleName();
    private TextView mTextView;

    private String getPage() throws IOException {
        HttpGet httpGet = new HttpGet("http://www.google.com");
        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        // The default value is zero, that means the timeout is not used.
        int timeoutConnection = 3000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT)
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        try{
            Log.d(TAG, "Checking network connection...");
            HttpResponse execute = httpClient.execute(httpGet);
            StatusLine statusLine = execute.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            InputStream content = execute.getEntity().getContent();
            StringBuilder builder = new StringBuilder(0);
            byte buffer[] = new byte[1024];
            while (content.read(buffer) != -1) {
                builder.append(new String(buffer));
            }
            Log.d(TAG, "Connection OK");
            return builder.toString();
        }
        catch(ClientProtocolException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return "fail";
    }

    @Override
    protected String doInBackground(TextView... textViews) {
        mTextView = textViews[0];
        try {
            return getPage();
        } catch (IOException e) {
            Log.d(TAG, "Connection unavailable");
            return "fail ioexception: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        mTextView.setText(s);
        super.onPostExecute(s);
    }
}