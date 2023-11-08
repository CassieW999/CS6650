import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CsvWriter {
    private String csvFilePath = "src/main/resources/metrics.csv";;
    private CSVPrinter csvPrinter;

    public CsvWriter() throws IOException {
        // if the file already exists, delete it
        File file = new File(csvFilePath);
        if (file.exists()) {
            file.delete();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath));
        this.csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("StartTime", "RequestType", "Latency", "ResponseCode"));
    }

    public synchronized void write(ApiRequestMetric metric) throws IOException {
        csvPrinter.printRecord(metric.getStartTime(), metric.getRequestType(), metric.getLatency(), metric.getResponseCode());
    }

    public void close() throws IOException {
        csvPrinter.flush();
        csvPrinter.close();
    }
}
