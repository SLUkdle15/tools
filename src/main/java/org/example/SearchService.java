package org.example;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class SearchService {
    //get Folder by its parent id
    static File getFolder(Drive service, String folderName, String parentId) throws IOException {
        FileList result = service.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and name='" + folderName + "' and '" + parentId + "' in parents and trashed=false")
                .setSupportsAllDrives(true)
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            return null;
        } else {
            for (File file : files) {
                System.out.println("Folder processing: " + file.getName() + " (" + file.getId() + ")" + " contains: ");
            }
            return files.get(0);
        }
    }

    //create a function list all files xlsx and google sheet in a folder
    static List<File> listXLSX(Drive service, String folderId) throws IOException {
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String mimeType2 = "application/vnd.google-apps.spreadsheet";
        FileList result = service.files().list()
                .setQ("'" + folderId + "' in parents and (mimeType='" + mimeType + "' or mimeType='" + mimeType2 + "')")
                .setSpaces("drive")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            throw new FileNotFoundException("No files found.");
        } else {
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
            return files;
        }
    }

}
