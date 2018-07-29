package com.test.compl.net;

import android.net.Uri;
import android.util.Log;

import com.test.compl.galley.GalleyItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class FlickrFetchr {

    public static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "8fd229bb0ed0d94486895ef1706dfc9a";

    public static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    public static final String FETCH_SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            while ((bytesRead=in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public boolean downloadUrlToStream(String urlSpec, OutputStream outstream) throws IOException {
        URL url = new URL(urlSpec);
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        boolean ret = false;
        try {
            in = new BufferedInputStream(connection.getInputStream());
            out = new BufferedOutputStream(outstream);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            while ((bytesRead=in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            ret = true;
        } finally {
            if (connection != null)
                connection.disconnect() ;

            if (in != null)
                in.close();

            if (out != null)
                out.close();
        }

        return ret;
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    private String buildUrl(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);

        if (method.equals(FETCH_SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }

        return uriBuilder.build().toString();
    }

    public List<GalleyItem> downloaGalleyItems(String url) {
        List<GalleyItem> items = new ArrayList<>();
        try {
            Log.d(TAG, "fetchRecentItems: url " + url);
            String jsonStr = getUrlString(url);
            Log.d(TAG, "fetchRecentItems: jsonStr " + jsonStr);
            parseItems(items, new JSONObject(jsonStr));
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return items;
    }

    public List<GalleyItem> fetchRecentItems() {
        String url = buildUrl(FETCH_RECENT_METHOD, null);
        return downloaGalleyItems(url);
    }

    public List<GalleyItem> fetchSearchItems(String query) {
        String url = buildUrl(FETCH_SEARCH_METHOD, query);
        return downloaGalleyItems(url);
    }

    public void parseItems(List<GalleyItem> items, JSONObject jsonBody) throws IOException, JSONException {
        JSONObject jsonObject = jsonBody.getJSONObject("photos");
        JSONArray galleys = jsonObject.getJSONArray("photo");

        for (int i = 0; i < galleys.length(); i++) {
            JSONObject photo = galleys.getJSONObject(i);
            if (!photo.has("url_s")) {
                continue;
            }
            GalleyItem item = new GalleyItem();
            item.setId(photo.getString("id"));
            item.setTitle(photo.getString("title"));
            item.setUrl(photo.getString("url_s"));
            items.add(item);
        }
    }
}
