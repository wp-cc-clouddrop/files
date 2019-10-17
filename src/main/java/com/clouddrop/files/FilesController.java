package com.clouddrop.files;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FilesController {

    //ClouddropFiles clouddropFiles = new ClouddropFiles(BlobClientProvider.getBlobClientReference());

    @PostMapping("/files")
    public String uploadFile() {
        return "POST to /files";
    }

    @PutMapping("/files")
    public String updateFile() {
        return "PUT to /files";
    }

    @GetMapping("/files/{id}")
    public String getFile(@PathVariable("id") Long id) {
        return "GET to /files/" + id;
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