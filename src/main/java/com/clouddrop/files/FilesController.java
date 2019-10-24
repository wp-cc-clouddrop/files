package com.clouddrop.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.clouddrop.files.model.Metadata;
import com.clouddrop.files.services.MetadataService;
import com.google.common.base.Preconditions;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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

    private String call_me(String token) throws Exception {
        String url = "http://clouddrop.xyz/user/auth";  // TODO: try change this to https???
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");
        //add request header
        con.setRequestProperty("Authorization", token);
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //Read JSON response and print
        JSONObject myResponse = new JSONObject(response.toString());
        String email = null;
        if(responseCode == 200 ){
            email = myResponse.getString("email");
            log.debug(email);
        }
        return email;
    }

    @RequestMapping(path = "/files/metadata", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Metadata uploadMetadata(@RequestBody Metadata resource, HttpServletResponse response, @RequestHeader("Authorization") String auth ) {
        Preconditions.checkNotNull(resource);

        String username = null;
        try {
            log.debug("Hallo "+auth);
            username = call_me(auth);
            if(username == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username is invalid");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed");
        }
        resource.setUsername(username);

        String location = "/files/" + resource.getFilename();
        resource.updateLastModified();
        resource.setContentLocation(location);
        fas.uploadMetadata(service.toMap(resource));

        response.addHeader("Location", location);
        return resource;
    }

    @RequestMapping(path = "/files/{filename}", method = RequestMethod.PUT, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateFile(@PathVariable("filename") String filename,
            @RequestParam("file") MultipartFile file, HttpServletResponse response,@RequestHeader("Authorization") String auth) {
        String username = null;
            try {
                username = call_me(auth);
                if(username == null){
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username is invalid");
                }
                if (!fas.updateFile(username, filename, file.getBytes())) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File does not exist");
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username is invalid");
            }
    }

    @GetMapping(path = "/files/{filename}", produces = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    public byte[] getFile(@PathVariable("filename") String filename, HttpServletResponse response,@RequestHeader("Authorization") String auth) {
        String username = null;
        try {
            username = call_me(auth);
            if(username == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username is invalid");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] data = fas.downloadFile(username, filename);
        if (data == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return data;
    }

    @DeleteMapping(path = "/files/{filename}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(@PathVariable("filename") String filename,@RequestHeader("Authorization") String auth) {
        String username = null;
        try {
            username = call_me(auth);
            if(username == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username is invalid");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            fas.deleteFile(username, filename);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/files/list")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getListFiles(@RequestHeader("Authorization") String auth) {

        String username = null;
        try {
            username = call_me(auth);
            if(username == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username is invalid");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> liste = fas.listFiles(username);
        Map<String,Object> map = new HashMap<>();
        map.put("list",liste);
        map.put("username",username);
        return map;
    }

    @GetMapping("/files/list/search")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> searchFiles(@RequestHeader("Authorization") String auth,
                            @RequestParam(value = "filename", required = false) String filename,
                            @RequestParam(value = "type", required = false) String type,
                            @RequestParam(value = "dateModified", required = false) String dateModified) {

        String username = null;
        try {
            username = call_me(auth);
            if(username == null){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "username is invalid");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> liste = fas.searchFile(username,filename,type,dateModified);

        Map<String,Object> map = new HashMap<>();
        Map<String,Object> mapParam = new HashMap<>();
        mapParam.put("filename",filename);
        mapParam.put("type",type);
        mapParam.put("lastModified",dateModified);
        map.put("username",username);
        map.put("searchedParam",mapParam);
        map.put("result",liste);
        return map;
    }
}