import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CsvReader {
    private String csvFilePath = "src/main/resources/metrics.csv";

    public synchronized void readAndCalculateStatistics() throws IOException {
        List<Long> postLatencies = new ArrayList<>();
        List<Long> getLatencies = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord csvRecord : csvParser) {
                String requestType = csvRecord.get("RequestType");
                long latency = Long.parseLong(csvRecord.get("Latency"));

                if ("POST".equalsIgnoreCase(requestType)) {
                    postLatencies.add(latency);
                } else if ("GET".equalsIgnoreCase(requestType)) {
                    getLatencies.add(latency);
                }
            }
        }

        System.out.println("POST Requests Statistics:");
        calculateStatistics(postLatencies);

        System.out.println("\nGET Requests Statistics:");
        calculateStatistics(getLatencies);
    }

    private void calculateStatistics(List<Long> latencies) {
        if (latencies.isEmpty()) {
            System.out.println("No data available for this request type.");
            return;
        }

        Collections.sort(latencies);
        long sum = latencies.stream().mapToLong(Long::longValue).sum();
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);
        double mean = sum / (double) latencies.size();
        double median = latencies.size() % 2 == 0 ?
                (latencies.get(latencies.size() / 2 - 1) + latencies.get(latencies.size() / 2)) / 2.0 :
                latencies.get(latencies.size() / 2);
        long p99 = latencies.get((int) (latencies.size() * 0.99));

        System.out.println("Mean: " + mean);
        System.out.println("Median: " + median);
        System.out.println("P99: " + p99);
        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
    }
}
