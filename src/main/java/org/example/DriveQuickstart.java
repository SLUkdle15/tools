package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* class to demonstrate use of Drive files list API */
public class DriveQuickstart {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = List.of(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service for drive.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Build a new authorized API client service for sheet.
        Sheets service2 =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        //read csv file

        File testcaseAccount = getFileByName(service, "Testcase_Account");
        // duplicate file by ID
        String newId = duplicate(service, testcaseAccount.getId());
        List<TestResult> testResults = CSVReader.read(Path.of("C:\\Users\\Admin\\Desktop\\projects\\fpt\\tool\\src\\main\\java\\org\\example\\Report.csv"));
        Map<String, CSVResultType> map = testResults.stream().collect(Collectors.toMap(TestResult::getId, TestResult::getResult));
        updateSheets(service2, map, newId);

    }

    private static String duplicate(Drive service, String fileId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName("abc");
        fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");
        File upload = service.files().copy(fileId, fileMetadata).setFields("id").execute();
        return upload.getId();
    }

    /**
     * @param service2      Sheet Driver
     * @param testResults   processed data extracted from csv file
     * @param spreadsheetId id of sheet want to update
     * @throws IOException process of updating data from google sheet requires handle this exception
     */
    private static void updateSheets(Sheets service2, Map<String, CSVResultType> testResults, String spreadsheetId) throws IOException {
        // default first sheet and max
        final String range = "A:ZZZ";
        ValueRange response = service2.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        int[] calculatedIndexes = calculateSelectedRow(values);
        int header = calculatedIndexes[0];
        int resultColumn = calculatedIndexes[1];
        int tsIdColumn = calculatedIndexes[2];
        //update
        List<List<Object>> updatedValues = new ArrayList<>();
        updatedValues.add(new ArrayList<>()); //header
        for (int i = header; i < values.size(); i++) {
            List<Object> r = values.get(i);
            if (r.size() > tsIdColumn) {
                String testId = r.get(tsIdColumn).toString();
                if (testResults.containsKey(testId)) {
                    ExcelResultType resultType = ResultTypeConverter.from(testResults.get(testId));
                    updatedValues.add(new ArrayList<>() {{
                        add(resultType.toString());
                        add(null);
                        add(null);
                        add(null);
                        add(null);
                        add(null);
                        add(null);
                    }});
                } else {
                    updatedValues.add(new ArrayList<>());
                }
            } else {
                updatedValues.add(new ArrayList<>());
            }
        }

        UpdateValuesResponse result;
        try {
            // Updates the values in the specified range.
            String processedRanged = Character.toString(resultColumn + 65) + calculatedIndexes[0] + ":" + Character.toString(tsIdColumn + 65) + values.size();
            ValueRange body = new ValueRange()
                    .setValues(updatedValues);
            result = service2.spreadsheets().values().update(spreadsheetId, processedRanged, body)
                    .setValueInputOption("RAW")
                    .execute();
            System.out.printf("%d cells updated.", result.getUpdatedCells());
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf("Spreadsheet not found with id '%s'.\n", spreadsheetId);
            } else {
                throw e;
            }
        }
    }

    private static int[] calculateSelectedRow(List<List<Object>> values) {
        int header = 0;
        int resultColumn = 0;
        int tsIdColumn = 0;
        for (int i = 0; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (!row.isEmpty() && row.get(0).equals("TC_ID")) {
                header = i + 1;
                for (int j = 0; j < row.size(); j++) {
                    String column = row.get(j).toString();
                    if (column.equals("Result")) {
                        resultColumn = j;
                    } else if (column.equals("TS_ID")) {
                        tsIdColumn = j;
                    }
                }
                break;
            }
        }
        return new int[]{header, resultColumn, tsIdColumn};
    }

    private static File getFileByName(Drive service, String fileName) throws IOException {
        FileList result = service.files().list()
                .setQ("name='" + fileName + ".xlsx'")
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
            return new File();
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
            return files.get(0);
        }
    }
}
// [END drive_quickstart]
