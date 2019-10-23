package com.clouddrop.files;

import com.clouddrop.files.model.Metadata;
import com.clouddrop.files.services.MetadataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.microsoft.azure.storage.StorageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
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

    @RequestMapping(path = "/files/{owner}/{filename}", method = RequestMethod.PUT, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    public void updateFile(@PathVariable("owner") String owner, @PathVariable("filename") String filename,
            @RequestParam("file") MultipartFile file, HttpServletResponse response) {
        try {
            if (!fas.updateFile(owner, filename, file.getBytes())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File does not exist");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    @GetMapping("/files/list/{userName}")
    public Map<String, Object> getListFiles(@PathVariable("userName") String userName) {
        ObjectMapper om = new ObjectMapper();
        List<String> liste = fas.listFiles(userName);
        Map<String,Object> map = new HashMap<>();
        map.put("list",liste);
        map.put("username",userName);
        return map;
    }

    @GetMapping("/files/list/search/{userName}")
    public Map<String, Object> searchFiles(@PathVariable("userName") String userName,
                              @RequestParam(value = "filename", required = false) String filename,
                              @RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "dateModified", required = false) String dateModified) {
        //String answer = "GET to /files/list/search with params";
        ObjectMapper om = new ObjectMapper();
        List<String> liste = fas.searchFile(userName,filename,type,dateModified);

        Map<String,Object> map = new HashMap<>();
        Map<String,Object> mapParam = new HashMap<>();
        mapParam.put("filename",filename);
        mapParam.put("type",type);
        mapParam.put("lastModified",dateModified);
        map.put("owner",userName);
        map.put("searchedParam",mapParam);
        map.put("result",liste);
        return map;
    }
}