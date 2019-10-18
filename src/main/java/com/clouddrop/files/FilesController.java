package com.clouddrop.files;

import com.microsoft.azure.storage.StorageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@RestController
public class FilesController {

    ClouddropFiles clouddropFiles = new ClouddropFiles();

    public FilesController() throws InvalidKeyException, StorageException, URISyntaxException {
    }

    @PostMapping("/files")
    public String uploadFile() {
        //return clouddropFiles.ladeDateiHoch();
        return "POST to /files";
    }

    @PutMapping("/files")
    public String updateFile() {
        //return clouddropFiles.aktualisiereDatei();
        return "PUT to /files";
    }

    @GetMapping("/files/{id}")
    public String getFile(@PathVariable("id") Long id) {
        //return clouddropFiles.gibDatei(id);
        return "GET to /files/" + id;
    }

    @DeleteMapping("/files/{id}")
    public String deleteFile(@PathVariable("id") Long id) {
        //return clouddropFiles.loescheDatei(id);
        return "DELETE to /files/" + id;
    }

    @GetMapping("/files/list")
    public String getListFiles() {
        //return clouddropFiles.gibListeVonDateien();
        return "GET to /files/list";
    }

    @GetMapping("/files/list/search")
    public String searchFiles(@RequestParam(value = "filename", required = false) String filename,
                              @RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "dateModified", required = false) String dateModified) {
        String answer = "GET to /files/list/search with params";
        //String answer = clouddropFiles.sucheDatei(filename,type,dateModified);
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