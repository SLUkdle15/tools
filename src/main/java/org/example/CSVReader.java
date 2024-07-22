package org.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CSVReader {
    public static List<TestResult> read(Path path) {
        try (Stream<String> s = Files.lines(path)) {
            int initialCapacity = (int) s.count();
            List<TestResult> list = new ArrayList<>(initialCapacity);
            BufferedReader reader = Files.newBufferedReader(path);
            final CSVFormat csvFormat = CSVFormat.Builder.create()
                    .setDelimiter(',')
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build();
            Iterable<CSVRecord> records = csvFormat.parse(reader);
            for (CSVRecord record : records) {
                String id = record.get(0);
                String name = record.get(1);
                String result = record.get(2);
                String notes = record.get(3);
                list.add(new TestResult(id, name, result, notes));
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
