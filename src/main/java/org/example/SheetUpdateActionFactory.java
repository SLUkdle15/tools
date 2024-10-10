package org.example;

import com.google.api.services.drive.model.File;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SheetUpdateActionFactory {
    /**
     * construct a list of Test Result from a csv file
     *
     * @param path path of the csv
     * @return list of Test Result objects
     */
    private static List<TestResult> readFile(Path path) {
        try (Stream<String> s = Files.lines(path)) {
            int initialCapacity = (int) s.count();
            List<TestResult> list = new ArrayList<>(initialCapacity);
            BufferedReader reader = Files.newBufferedReader(path);
            final CSVFormat csvFormat = CSVFormat.Builder.create()
                    .setDelimiter(',')
                    .setHeader(
                            "Tags", "Testname", "Result", "Note"
                    )
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

    //create a function to read multiple csv file from directory
    public static List<SheetUpdateAction> from(Path directory, List<File> excelFiles) {
        Map<String, File> excelFileMap = excelFiles.stream().collect(Collectors.toMap(File::getName, file -> file));

        try {
            List<SheetUpdateAction> list = new ArrayList<>();
            Files.list(directory)
                    .filter(path -> path.toString().endsWith(".csv"))
                    .forEach(path -> {
                        File excelFile = excelFileMap.getOrDefault(FileNameMapping.fileCSVToExcel(path.getFileName().toString()), null);
                       if(excelFile != null) {
                           SheetUpdateAction action = new SheetUpdateAction(excelFile.getId(), excelFile.getName(), readFile(path));
                           list.add(action);
                       }
                    });
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
