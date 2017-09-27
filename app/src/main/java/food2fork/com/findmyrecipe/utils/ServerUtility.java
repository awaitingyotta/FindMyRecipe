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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

    private static final String BASE_SEARCH_URL = "http://food2fork.com/api/search?key=b549c4c96152e677eb90de4604ca61a2";
    private static final String BASE_RECIPE_URL = "http://food2fork.com/api/get?key=b549c4c96152e677eb90de4604ca61a2";
    private static final String CONTENT_TYPE = "application/json; charset=utf8";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String ENCODING = "UTF8";
    public static final String METHOD_GET = "GET";

    public static JsonRecipeSearchResult searchForRecipes(String query)
            throws ServerFaultException, AppErrorException, NetworkErrorException, MalformedURLException {
        URL url;
        try {
            url = new URL(BASE_SEARCH_URL + query);
        } catch (MalformedURLException e) {
            throw new MalformedURLException("Wrong base URL or URI");
        }
        return (JsonRecipeSearchResult) getJsonFromServer(ServerUtility.METHOD_GET, url, "", JsonRecipeSearchResult.class);
    }

    public static JsonRecipeDetails getRecipe(String query)
            throws MalformedURLException, ServerFaultException, AppErrorException, NetworkErrorException {
        URL url;
        try {
            url = new URL(BASE_RECIPE_URL + query);
        } catch (MalformedURLException e) {
            throw new MalformedURLException("Wrong base URL or URI");
        }

        return (JsonRecipeDetails) getJsonFromServer(ServerUtility.METHOD_GET, url, "", JsonRecipeDetails.class);
    }

    private static String callServer(String method, URL url, String body)
            throws IOException, AppErrorException, ServerFaultException {


        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(CONNECTION_TIMEOUT);

        if (body != null && !body.isEmpty()) {
            if (method.equals(METHOD_GET)) {
                throw new AppErrorException("Wrong HTTP-method");
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

//        Utility.setConnectionTest(new TestSecuredConnection(connection));
//        if (Utility.getConnectionTest().getException() != null) {
//            if (Utility.getConnectionTest().getException().getClass() == SSLPeerUnverifiedException.class) {
//                throw new AppErrorException("Uverifisert serverport. Vennligst ta kontakt med kundesenteret");
//            }
//            else if (Utility.getConnectionTest().getException().getClass() == IllegalStateException.class) {
//                throw new AppErrorException("Det har oppstått en feil");
//            }
//        }
//        else if (!Utility.getConnectionTest().isValid()) {
//            connection.disconnect();
//            throw new AppErrorException("SSL-sertifikatet er ugyldig. Vennligst ta kontakt med kundesenteret");
//        }

        switch (connection.getResponseCode()) {
            case HttpURLConnection.HTTP_OK:
                break;
            case HttpURLConnection.HTTP_FORBIDDEN:
                throw new AppErrorException("403 forbidden");
            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new ServerFaultException();
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                throw new ServerFaultException();
            default:
                throw new AppErrorException("Unknow application error");
        }

        BufferedReader br = new BufferedReader( new InputStreamReader(connection.getInputStream()) );
        String input;
        StringBuilder responseBody = new StringBuilder("");
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
            throw new AppErrorException("Invalid URL");
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
            System.out.println(e.getUnrecognizedPropertyName());
            e.printStackTrace();
            throw new AppErrorException("Uknown property");
        } catch (JsonParseException e) {
            e.printStackTrace();
            throw new AppErrorException("JsonParseException");
        } catch (JsonMappingException e) {
            e.printStackTrace();
            throw new AppErrorException("JsonMappingException");
        } catch (IOException e) {
            e.printStackTrace();
            throw new AppErrorException("Uknown error");
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
        Bitmap bm = null;
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Error getting bitmap", e);
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
            }
        }
    }

}
