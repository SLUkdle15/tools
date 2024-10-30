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
import com.google.api.client.util.Data;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.*;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        try {
            String csvLocation = args[0];
            //get the csv file name at the end of the path
            String parentId = "1leW8tih4ct-axPkhfkW9mh5WiHwOqlB3";
            String clonesName = Path.of(csvLocation).getFileName().toString().replace(".csv", "");

            // Build a new authorized API client service for drive.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Build a new authorized  API client service for sheet.
            Sheets service2 =
                    new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                            .setApplicationName(APPLICATION_NAME)
                            .build();
            //check if the testResult exists
            File testResultFolder = SearchService.getFolder(service, clonesName, parentId);
            if (testResultFolder == null) {
                testResultFolder = SearchService.getFolder(service, "TestReport", parentId);
                List<File> xlsxs = SearchService.listXLSX(service, testResultFolder.getId());

                //create new folder in shared folder
                File aFolder = createFolder(service, clonesName, parentId);
                List<File> clones = duplicateFiles(service, xlsxs, aFolder.getId());
                update(csvLocation, clones, service2);
            } else {
                List<File> xlsxs = SearchService.listXLSX(service, testResultFolder.getId());
                update(csvLocation, xlsxs, service2);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ArrayIndexOutOfBoundsException caught");
        }
    }

    private static void update(String csvLocation, List<File> clones, Sheets service2) throws IOException {
        List<SheetUpdateAction> testResults = SheetUpdateActionFactory.from(Path.of(csvLocation), clones);
        System.out.println("Processing " + testResults.size() + " csv files");

        //update sheets
        for (SheetUpdateAction action : testResults) {
            System.out.println("Updating sheet: " + action.getSheetName());
            updateSheets(service2, action.getTestResults(), action.getSheetId());
        }
    }

    private static File createFolder(Drive service, String folderName, String parentId) throws GoogleJsonResponseException {
        // File's metadata.
        File fileMetadata = new File();
        fileMetadata.setParents(List.of(parentId));
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id, name")
                    .execute();
            System.out.println("new Folder created with ID: " + file.getId() + ", name: " + file.getName());
            return file;
        } catch (GoogleJsonResponseException e) {
            System.err.println("Unable to create folder: " + e.getDetails());
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //duplicate multiple files in a folder and convert them to google sheet
    private static List<File> duplicateFiles(Drive service, List<File> files, String folderId) throws IOException {
        List<File> newIds = new ArrayList<>();
        for (File file : files) {
            System.out.println("Uploading: " + file.getName());
            File fileMetadata = new File();
            String name = file.getName();
            fileMetadata.setParents(List.of(folderId));
            fileMetadata.setName(name);
            fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");
            File upload = service.files().copy(file.getId(), fileMetadata).setFields("id, name").execute();
            newIds.add(upload);
            System.out.println("DOnE: " + upload.getName() + ", id: " + upload.getId());
        }
        return newIds;
    }

    /**
     * @param service2      Sheet Driver
     * @param testResults   processed data extracted from csv file
     * @param spreadsheetId id of sheet want to update/ file id
     * @throws IOException process of updating data from google sheet requires handle this exception
     */
    private static void updateSheets(Sheets service2, Map<String, TestResult> testResults, String spreadsheetId) throws IOException {
        // default first sheet and max
        final String range = "A:ZZZ";
        ValueRange response = service2.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        int[] calculatedIndexes = calculateSelectedRow(values);
        int header = calculatedIndexes[0];
        int resultColumn = calculatedIndexes[1]; //first
        int tsIdColumn = calculatedIndexes[2];
        int noteIdColumn = calculatedIndexes[3]; //end
        //update
        List<List<Object>> updatedValues = calculateChange(header + 1, resultColumn, tsIdColumn, noteIdColumn, testResults, values);

        UpdateValuesResponse result;
        try {
            // Updates the values in the specified range.
            int starting = header + 1;
            String processedRanged = Character.toString(resultColumn + 65) + starting + ":" + Character.toString(noteIdColumn + 65) + values.size();
            System.out.println("Updating range: " + processedRanged);
            ValueRange body = new ValueRange()
                    .setValues(updatedValues);
            result = service2.spreadsheets().values().update(spreadsheetId, processedRanged, body)
                    .setValueInputOption("RAW")
                    .execute();
            System.out.printf("%d cells updated.\n", result.getUpdatedCells());
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

    /**
     * @param values: sheet data
     * @return indexes of header row, result column and tags column
     */
    private static int[] calculateSelectedRow(List<List<Object>> values) {
        int header = 0;
        int resultColumn = 0;
        int tsIdColumn = 0;
        int noteColumn = 0;

        for (int i = 0; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (!row.isEmpty() && row.get(0).equals("TC_ID")) { //assuming Note is the final column
                header = i; //contains TC_ID counting from 0
                for (int j = 0; j < row.size(); j++) {
                    String column = row.get(j).toString();
                    if (column.equals("Result")) {
                        resultColumn = j;
                    } else if (column.equals("TS_ID")) {
                        tsIdColumn = j;
                    } else if (column.equals("Note")) {
                        noteColumn = j;
                    }
                }
                break;
            }
        }
        return new int[]{header, resultColumn, tsIdColumn, noteColumn};
    }

    /**
     * @param testResults
     * @param starting
     * @param values
     * @param tsIdColumn  tags column index (assuming this column always behind result column)
     *                    Dont need result column index because it is always start from this column
     * @return 2d list of updated values
     */
    private static List<List<Object>> calculateChange(int starting, int resultColumn, int tsIdColumn, int noteColumn, Map<String, TestResult> testResults, List<List<Object>> values) {
        List<List<Object>> updatedValues = new ArrayList<>();
        updatedValues.add(new ArrayList<>()); //header

        //go through each row after header
        for (int i = starting; i < values.size(); i++) {
            List<Object> r = values.get(i);

            //check if this row has note column
            if (r.size() > noteColumn) {
                String testId = r.get(tsIdColumn).toString();
                if (testResults.containsKey(testId)) {
                    ExcelResultType resultType = ResultTypeConverter.from(testResults.get(testId).getResult());
                    String notes = testResults.get(testId).getNote();

                    ArrayList<Object> changes = new ArrayList<>();
                    changes.add(resultType.toString());
                    for (int j = resultColumn + 1; j <= noteColumn - 1; j++) {
                        changes.add(Data.NULL_STRING);
                    }
                    changes.add(notes);
                    updatedValues.add(changes);
                } else {
                    updatedValues.add(new ArrayList<>()); //skip this row
                }
            } else {
                updatedValues.add(new ArrayList<>()); //skip this row
            }
        }
        return updatedValues;
    }
}
// [END drive_quickstart]

/**
 * note 1: tss id phai dung
 * note 2: format is messup
 */