package food2fork.com.findmyrecipe.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import food2fork.com.findmyrecipe.AsyncTaskCompleteListener;
import food2fork.com.findmyrecipe.exceptions.AppErrorException;
import food2fork.com.findmyrecipe.exceptions.NetworkErrorException;
import food2fork.com.findmyrecipe.exceptions.ServerFaultException;
import food2fork.com.findmyrecipe.json.BaseJson;
import food2fork.com.findmyrecipe.json.JsonRecipeDetails;
import food2fork.com.findmyrecipe.json.JsonRecipeSearchResult;

import static android.content.ContentValues.TAG;

/**
 * @author Alexei Ivanov
 * utilising: http://food2fork.com/about/api
 */
public class ServerUtility {

    private static final int CONNECTION_TIMEOUT = 5000;

    //b549c4c96152e677eb90de4604ca61a2
    public static final String ENCODING = "UTF8";
    public static final String METHOD_GET = "GET";

    private static final String EMPTY_STRING = "";
    private static final String BASE_SEARCH_URL = "http://food2fork.com/api/search?key=b549c4c96152e677eb90de4604ca61a2";
    private static final String BASE_RECIPE_URL = "http://food2fork.com/api/get?key=b549c4c96152e677eb90de4604ca61a2";
    private static final String CONTENT_TYPE = "application/json; charset=utf8";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String MALFORMED_URL_MESSAGE = "Wrong base URL or URI";
    private static final String WRONG_HTTP_METHOD_MESSAGE = "Wrong HTTP-method";
    private static final String HTTP_FORBIDDEN_MESSAGE = "403 forbidden";
    private static final String UNKNOWN_ERROR_MESSAGE = "Unknow application error";
    private static final String INVALID_URL_MESSAGE = "Invalid URL";

    private static final String UNKNOWN_JSON_PROPERTY = "Uknown property";
    private static final String JSON_PARSE_EXCEPTION = "JsonParseException";
    private static final String JSON_MAPPING_EXCEPTION = "JsonMappingException";
    private static final String UNKNOWN_JSON_ERROR = "Uknown error";



    public static JsonRecipeSearchResult searchForRecipes(String query)
            throws ServerFaultException, AppErrorException, NetworkErrorException, MalformedURLException {
        URL url;
        try {
            url = new URL(BASE_SEARCH_URL + query);
        } catch (MalformedURLException e) {
            throw new MalformedURLException(MALFORMED_URL_MESSAGE);
        }
        return (JsonRecipeSearchResult) getJsonFromServer(ServerUtility.METHOD_GET, url, EMPTY_STRING, JsonRecipeSearchResult.class);
    }

    public static JsonRecipeDetails getRecipe(String query)
            throws MalformedURLException, ServerFaultException, AppErrorException, NetworkErrorException {
        URL url;
        try {
            url = new URL(BASE_RECIPE_URL + query);
        } catch (MalformedURLException e) {
            throw new MalformedURLException(MALFORMED_URL_MESSAGE);
        }

        return (JsonRecipeDetails) getJsonFromServer(ServerUtility.METHOD_GET, url, EMPTY_STRING, JsonRecipeDetails.class);
    }

    private static String callServer(String method, URL url, String body)
            throws IOException, AppErrorException, ServerFaultException {


        String input;
        BufferedReader br;
        StringBuilder responseBody;
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(CONNECTION_TIMEOUT);

        if (body != null && !body.isEmpty()) {
            if (method.equals(METHOD_GET)) {
                throw new AppErrorException(WRONG_HTTP_METHOD_MESSAGE);
            }

            connection.setDoOutput(true);
            OutputStream requestBodyStream = connection.getOutputStream();
            try {
                requestBodyStream = connection.getOutputStream();
                requestBodyStream.write(body.getBytes(ENCODING));
            } finally {
                if (requestBodyStream != null) try { requestBodyStream.close(); } catch (IOException ignore) {}
            }
        }

        connection.connect();

        switch (connection.getResponseCode()) {
            case HttpURLConnection.HTTP_OK:
                break;
            case HttpURLConnection.HTTP_FORBIDDEN:
                throw new AppErrorException(HTTP_FORBIDDEN_MESSAGE);
            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new ServerFaultException();
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                throw new ServerFaultException();
            default:
                throw new AppErrorException(UNKNOWN_ERROR_MESSAGE);
        }

        br = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
        responseBody = new StringBuilder(EMPTY_STRING);
        while ((input = br.readLine()) != null){
            responseBody.append(input);
        }
        br.close();

        return responseBody.toString();
    }


   public static <T> Object getJsonFromServer(String method,
                                               URL url, String requestBody, Class<? extends BaseJson> jsonClass)
            throws AppErrorException, ServerFaultException, NetworkErrorException {

        String responseBody = null;
        try {
            responseBody = callServer(method, url, requestBody);
        } catch (MalformedURLException e) {
            throw new AppErrorException(INVALID_URL_MESSAGE);
        } catch (IOException e) {
            throw new NetworkErrorException();
        }

        if (jsonClass == null) {
            return null;
        }

        BaseJson jsonObject = null;
        try {
            jsonObject = BaseJson.stringToJsonObject(responseBody, jsonClass);
        } catch (UnrecognizedPropertyException e) {
//            System.out.println(e.getUnrecognizedPropertyName()); for debugging only
            e.printStackTrace();
            Log.e(TAG, "UnrecognizedPropertyException while reading JSON", e);
            throw new AppErrorException(UNKNOWN_JSON_PROPERTY);
        } catch (JsonParseException e) {
            e.printStackTrace();
            Log.e(TAG, "JsonParseException while reading JSON", e);
            throw new AppErrorException(JSON_PARSE_EXCEPTION);
        } catch (JsonMappingException e) {
            e.printStackTrace();
            Log.e(TAG, "JsonMappingException while reading JSON", e);
            throw new AppErrorException(JSON_MAPPING_EXCEPTION);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException while reading JSON", e);
            throw new AppErrorException(UNKNOWN_JSON_ERROR);
        }
//        System.out.println("jsonObject: " + jsonObject.jsonToString());// for debug purposes only! remove after tests complete
        return jsonObject;
    }


    public static void getImageBitmap(AsyncTaskCompleteListener callback, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            new ImageLoadTask(callback, imageUrl).execute(imageUrl);
        }
    }


    private static Bitmap getImageFromUrlSource(String urlString) {
        URL url;
        Bitmap bm = null;
        InputStream is = null;
        URLConnection connection;
        BufferedInputStream bis = null;
        ByteArrayOutputStream bos = null;

        try {
            url = new URL(urlString);
            connection = url.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);
            connection.connect();

            is = connection.getInputStream();
            bis = new BufferedInputStream(is);
            bos = new ByteArrayOutputStream();
            bm = BitmapFactory.decodeStream(bis);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            Log.e(TAG, "SocketTimeoutException while getting bitmap", e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error getting bitmap", e);
        } finally {
            Utility.closeSilently(bis);
            Utility.closeSilently(bos);
            Utility.closeSilently(is);
        }
        return bm;
    }

    private static class ImageLoadTask extends AsyncTask<String, String, Bitmap> {

        private String url;
        private AsyncTaskCompleteListener callback;

        ImageLoadTask(AsyncTaskCompleteListener callback, String url) {
            this.url = url;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                return ServerUtility.getImageFromUrlSource(url);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            if (image != null) {
                callback.onSuccess(image);
            } else {
                callback.onFailure();
            }
        }
    }

}
