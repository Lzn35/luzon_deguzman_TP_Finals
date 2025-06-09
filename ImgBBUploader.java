package com.example.luzonfinalproject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class ImgBBUploader {
    private static final String TAG = "ImgBBUploader";
    private static final String API_KEY = "66e56519c495ab397bc5cf7078413c13";
    private static final String API_URL = "https://api.imgbb.com/1/upload";

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                File imageFile = createTempFileFromUri(context, imageUri);
                if (imageFile == null) {
                    callback.onFailure("Failed to process image");
                    return;
                }

                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("key", API_KEY)
                        .addFormDataPart("image", imageFile.getName(),
                                RequestBody.create(MediaType.parse("image/*"), imageFile))
                        .build();

                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                String responseData = response.body() != null ? response.body().string() : "";


                imageFile.delete();

                if (response.isSuccessful()) {
                    String imageUrl = parseImageUrl(responseData);
                    if (imageUrl != null) {
                        callback.onSuccess(imageUrl);
                    } else {
                        callback.onFailure("Failed to parse image URL");
                    }
                } else {
                    callback.onFailure("Upload failed: " + extractErrorMessage(responseData));
                }
            } catch (Exception e) {
                Log.e(TAG, "Upload error", e);
                callback.onFailure("Error: " + e.getMessage());
            }
        }).start();
    }

    private static File createTempFileFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile("upload_", ".jpg", context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "File creation failed", e);
            return null;
        }
    }

    private static String parseImageUrl(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONObject data = json.getJSONObject("data");
            return data.getString("url");
        } catch (Exception e) {
            Log.e(TAG, "JSON parsing error", e);
            return null;
        }
    }

    private static String extractErrorMessage(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            return json.has("error") ? json.getJSONObject("error").getString("message") : "Unknown error";
        } catch (Exception e) {
            return "Failed to parse error response";
        }
    }
}