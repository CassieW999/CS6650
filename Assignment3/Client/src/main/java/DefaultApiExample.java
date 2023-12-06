//import io.swagger.client.*;
//import io.swagger.client.auth.*;
//import io.swagger.client.model.*;
//import io.swagger.client.api.DefaultApi;
//
//import java.io.File;
//import java.util.*;
//
//public class DefaultApiExample {
//
//    public static void main(String[] args) {
//
//        DefaultApi apiInstance = new DefaultApi();
//        apiInstance.getApiClient().setBasePath("http://localhost:8080/IGORTON/AlbumStore/1.0.0/");
//        String albumID = "albumID_example"; // String | path  parameter is album key to retrieve
//        try {
//            AlbumInfo result = apiInstance.getAlbumByKey(albumID);
//            System.out.println(result);
//        } catch (ApiException e) {
//            System.err.println("Exception when calling DefaultApi#getAlbumByKey");
//            e.printStackTrace();
//        }
//    }
//}
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.DefaultApi;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DefaultApiExample {
    public static class AlbumResponse {
        private String albumID;
        private int imageSize;

        // getters and setters
        public String getAlbumId() {
            return albumID;
        }

        public void setAlbumId(String albumId) {
            this.albumID = albumId;
        }

        public int getImageSize() {
            return imageSize;
        }

        public void setImageSize(int imageSize) {
            this.imageSize = imageSize;
        }
    }

//    public static void main(String[] args) {
//
//        DefaultApi apiInstance = new DefaultApi();
//        apiInstance.getApiClient().setBasePath("http://localhost:8080/IGORTON/AlbumStore/1.0.0/");
////        apiInstance.getApiClient().setBasePath("http://localhost:8081/MusicAlbums_war_exploded/");
//        File image = new File("src/main/resources/image_example"); // File |
//        AlbumsProfile profile = new AlbumsProfile(); // AlbumsProfile |
//        try {
//            ImageMetaData result = apiInstance.newAlbum(image, profile);
//            System.out.println(result);
//        } catch (ApiException e) {
//            System.err.println("Exception when calling DefaultApi#newAlbum");
//            e.printStackTrace();
//        }
//    }
    public static void main(String[] args) {
//        System.out.println(sendPostRequest("http://localhost:8080/IGORTON/AlbumStore/1.0.0/albums"));
        System.out.println(sendPostRequest("http://localhost:8081/MusicAlbums_war_exploded/albums"));
        System.out.println(sendAsyncPostRequest("http://localhost:8081/MusicAlbums_war_exploded/review/dislike/2"));
        System.out.println(sendAsyncPostRequest("http://localhost:8081/MusicAlbums_war_exploded/review/like/1"));
    }

    private static String sendAsyncPostRequest(String targetUrl) {
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try(OutputStream os = httpURLConnection.getOutputStream()) {
                byte[] input = "yourRequestBody".getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Check the response code (200 means OK)
            int responseCode = httpURLConnection.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    private static String sendPostRequest(String targetUrl) {
        byte[] imageBytes = "123".getBytes(StandardCharsets.UTF_8);
        String jsonProfile = "{\"artist\": \"ximing1\", \"title\": \"dora\",\"year\": \"2014\"}";

        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        try {
            URL url = new URL(targetUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream output = httpURLConnection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

                // Send image data.
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"").append(CRLF);
                writer.append("Content-Type: image/jpeg").append(CRLF);  // Adjust the content type if your image isn't a JPEG
                writer.append(CRLF).flush();
                output.write(imageBytes);
                output.flush();
                writer.append(CRLF).flush();

                // Send JSON profile data.
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"profile\"").append(CRLF);
                writer.append("Content-Type: application/json; charset=UTF-8").append(CRLF);
                writer.append(CRLF).append(jsonProfile).append(CRLF).flush();

                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append(CRLF).flush();
            }

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println(Thread.currentThread().getName() + " - POST request to " + targetUrl + " succeeded!");
                return "";
//                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
//                StringBuilder response = new StringBuilder();
//                String line;
//
//                while ((line = in.readLine()) != null) {
//                    response.append(line);
//                }
//                in.close();
//
//                System.out.println(Thread.currentThread().getName() + " - POST request to " + targetUrl + " succeeded!");
//                ObjectMapper objectMapper = new ObjectMapper();
//                AlbumResponse albumResponse = objectMapper.readValue(response.toString(), AlbumResponse.class);
//                System.out.println(Thread.currentThread().getName() + " - POST request to " + targetUrl + " succeeded! AlbumId: " + albumResponse.getAlbumId());
//                return albumResponse.getAlbumId();
            } else {
                System.out.println(Thread.currentThread().getName() + " - POST request to " + targetUrl + " failed! Response code: " + responseCode);
                return "";
            }
        } catch (Exception e) {
            System.out.println(Thread.currentThread().getName() + " - POST request to " + targetUrl + " Error: " + e.getMessage());
            return "";
        }
    }
}