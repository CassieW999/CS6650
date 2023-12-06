import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumsProfile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTestClient {
    private final DefaultApi api;
    private CsvWriter csvWriter;
    private static String IPAddr;

    public LoadTestClient() throws IOException {
        api = new DefaultApi();
        csvWriter = new CsvWriter();
    }

    private static String sendPostRequest(String targetUrl) {
        byte[] imageBytes = "123".getBytes(StandardCharsets.UTF_8);
        String jsonProfile = "{\"artist\": \"ximing1\", \"title\": \"dora\",\"year\": \"2014\"}";

        String boundary = Long.toHexString(System.currentTimeMillis());
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
//                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
//                StringBuilder response = new StringBuilder();
//                String line;
//
//                while ((line = in.readLine()) != null) {
//                    response.append(line);
//                }
//                in.close();
//
//                ObjectMapper objectMapper = new ObjectMapper();
//                DefaultApiExample.AlbumResponse albumResponse = objectMapper.readValue(response.toString(), DefaultApiExample.AlbumResponse.class);
//                System.out.println(Thread.currentThread().getName() + " - POST request to " + targetUrl + " succeeded! AlbumId: " + albumResponse.getAlbumId());
                System.out.println(Thread.currentThread().getName() + " - POST request to " + targetUrl + " succeeded!");
                return "";
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


    public void testApi(int threadGroupSize, int numThreadGroups, int delay) {
        api.getApiClient().setBasePath(IPAddr);

        // preheat the server
        executeThreads(10, 100, null);

        // formal testing starts
        long startTime = System.currentTimeMillis();
        AtomicInteger successCounter = new AtomicInteger();
        // More complex phase with multiple thread groups, each making 1000 POST and GET API calls
        for (int i = 0; i < numThreadGroups; i++) {
            executeThreads(threadGroupSize, 100, successCounter);
            try {
                TimeUnit.SECONDS.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        long wallTime = endTime - startTime;
        double throughput = (double) successCounter.get() / (wallTime / 1000.0);

        System.out.println("Wall Time: " + wallTime + "ms");
        System.out.println("Throughput: " + throughput + " requests per second");
    }

    private void executeThreads(int numThreads, int callsPerThread, AtomicInteger successCounter) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < callsPerThread; j++) {
                    try {
                        long postStartTime = System.currentTimeMillis();
//                        api.newAlbum(new File("src/main/resources/image_example"), new AlbumsProfile());
                        sendPostRequest(IPAddr + "albums");
                        sendAsyncPostRequest(IPAddr + "review/dislike/2");
                        sendAsyncPostRequest(IPAddr + "review/like/1");
                        sendAsyncPostRequest(IPAddr + "review/like/1");
                        long postEndTime = System.currentTimeMillis();
                        ApiRequestMetric postMetric = new ApiRequestMetric(postStartTime, "POST",
                                postEndTime - postStartTime, 200);

//                        long getStartTime = System.currentTimeMillis();
//                        api.getAlbumByKey("albumID_example");
//                        long getEndTime = System.currentTimeMillis();
//                        ApiRequestMetric getMetric = new ApiRequestMetric(getStartTime, "GET",
//                                getEndTime - getStartTime, 200);

                        if (successCounter != null) {
                            successCounter.addAndGet(2);  // Since you're making two API calls
                            csvWriter.write(postMetric);
//                            csvWriter.write(getMetric);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.out.println("Usage: java MusicAlbumsApiTest <threadGroupSize> <numThreadGroups> <delay> <IPAddr>");
            return;
        }

        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]);
        IPAddr = args[3];

        LoadTestClient tester = new LoadTestClient();
        tester.testApi(threadGroupSize, numThreadGroups, delay);

        tester.csvWriter.close();

        // calculate metrics from csv file after test completion
        CsvReader csvReader = new CsvReader();
        csvReader.readAndCalculateStatistics();

    }
}
