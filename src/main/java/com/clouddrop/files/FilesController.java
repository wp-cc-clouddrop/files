package com.clouddrop.files;

import com.microsoft.azure.storage.StorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@RestController
public class FilesController {

    private FilesAzureStorage fas;

    public FilesController(){
        fas = new FilesAzureStorage();
        fas.setContainerName("guestcontainer");
        fas.connect();
    }

    @PostMapping("/files")
    public String uploadFile(
            @RequestBody FileMetaData fileMetaData, @RequestParam("file") MultipartFile file
            ) {
        String a = fileMetaData.getFilename()+fileMetaData.getType()+fileMetaData.getModDate();
        return "POST to /files"+a;
    }

    @PutMapping("/files")
    public String updateFile() {
        return "PUT to /files";
    }

    @GetMapping("/files/{fileName}")
    public String getFile(@PathVariable("fileName") String fileName) {
        return "GET to /files/" + fileName;
    }

    @DeleteMapping("/files/{id}")
    public String deleteFile(@PathVariable("id") Long id) {
        return "DELETE to /files/" + id;
    }

    @GetMapping("/files/list")
    public String getListFiles() {
        return "GET to /files/list";
    }

    @GetMapping("/files/list/search")
    public String searchFiles(@RequestParam(value = "filename", required = false) String filename,
                              @RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "dateModified", required = false) String dateModified) {
        String answer = "GET to /files/list/search with params";

        // check if request param set
        if (filename != null) {
            answer += " filename = " + filename;
        }
        if (type != null) {
            answer += " type = " + type;
        }
        if (dateModified != null) {
            answer += " dateModified = " + dateModified;
        }

        return answer;
    }

}