package com.clouddrop.files;

import com.clouddrop.files.model.Metadata;
import com.clouddrop.files.services.MetadataService;
import com.google.common.base.Preconditions;
import com.microsoft.azure.storage.StorageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

@RestController
public class FilesController {

    private static Logger log = LoggerFactory.getLogger(FilesController.class);

    private FilesAzureStorage fas;
    private MetadataService service;

    public FilesController() {
        fas = new FilesAzureStorage();
        fas.setContainerName("guestcontainer");
        fas.connect();

        service = new MetadataService();
    }

    @RequestMapping(path = "/files/metadata", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Metadata uploadMetadata(@RequestBody Metadata resource, HttpServletResponse response) {
        Preconditions.checkNotNull(resource);

        // TODO: get this from auth header?
        String owner = "testOwner";
        resource.setOwner(owner);

        String location = "/files/" + owner + "/" + resource.getFilename();
        resource.updateLastModified();
        resource.setContentLocation(location);
        fas.uploadMetadata(service.toMap(resource));

        response.addHeader("Location", location);
        return resource;
    }

    @PostMapping("/files/{location}")
    public String uploadFile(@PathVariable("location") String location) {
        return "POST to /files/" + location;
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