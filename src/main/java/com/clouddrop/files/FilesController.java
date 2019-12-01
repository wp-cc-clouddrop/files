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

import org.json.JSONException;
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

    private IFilesAdapter fs;
    private MetadataService service;

    public FilesController() {
        String env = System.getenv("CLUSTER_ENV");
        if(env == "azure"){
            fs = new FilesAzureStorage();
        }else if (env == "gcp") {
            fs = new FilesGoogleStorage();
        }
        else {
            log.error("CLUSTER_ENV not defined/unknown. Possible values are 'gcp' or 'azure'", new IllegalArgumentException());
            System.exit(1);
        }

        service = new MetadataService();
    }

    private String verifyJWT(String token) throws IOException, JSONException {
        String url = "http://clouddrop.xyz/user/auth";
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
            log.debug("EMAIL: " + email);
            return email;
        }
        else {
            HttpStatus status =  HttpStatus.valueOf(responseCode);
            return status.getReasonPhrase();
        }
    }

    @RequestMapping(path = "/files/metadata", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Metadata uploadMetadata(@RequestBody Metadata resource, HttpServletResponse response, @RequestHeader("Authorization") String auth ) {
        Preconditions.checkNotNull(resource);

        String username = null;
        try {
            log.debug("Hallo "+auth);
            username = verifyJWT(auth);
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
        resource.setTags("testtag,");
        HashMap<String,String> map = service.toMap(resource);
        fs.uploadMetadata(map);

        response.addHeader("Location", location);
        return resource;
    }

    @RequestMapping(path = "/files/{filename}", method = RequestMethod.PUT, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateFile(@PathVariable("filename") String filename,
            @RequestParam("file") MultipartFile file, HttpServletResponse response,@RequestHeader("Authorization") String auth) {
        String username = null;
            try {
                username = verifyJWT(auth);
                if(username == null){
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username is invalid");
                }
                if (!fs.updateFile(username, filename, file.getBytes())) {
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
            username = verifyJWT(auth);
            if(username == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username is invalid");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] data = fs.downloadFile(username, filename);
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
            username = verifyJWT(auth);
            if(username == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username is invalid");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            fs.deleteFile(username, filename);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/files/list")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getListFiles(@RequestHeader("Authorization") String auth) {

        String username = null;
        try {
            username = verifyJWT(auth);
            if(username == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username is invalid");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "username is invalid");
        }

        List<String> liste = fs.listFiles(username);
        Map<String,Object> map = new HashMap<>();
        map.put("list",liste);
        map.put("username",username);
        return map;
    }

    //TODO: tag search
    @GetMapping("/files/list/search")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> searchFiles(@RequestHeader("Authorization") String auth,
                            @RequestParam(value = "filename", required = false) String filename,
                            @RequestParam(value = "type", required = false) String type,
                            @RequestParam(value = "dateModified", required = false) String dateModified,
                            @RequestParam(value = "tag", required = false) String tag)
                            {

        String username = null;
        try {
            username = verifyJWT(auth);
            if(username == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "token not authorized");
            }

            if (!username.contains("@")) {
                // got a error response code;
                // username now contains error response code from URLConnection
                throw new ResponseStatusException(HttpStatus.valueOf(username));

            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error sending request to user service");

        } catch (JSONException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is invalid");
        }

        List<String> liste = fs.searchFile(username,filename,type,dateModified, tag);
        if (liste == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No search parameter sent");
        }

        Map<String,Object> map = new HashMap<>();
        Map<String,Object> mapParam = new HashMap<>();
        mapParam.put("filename",filename);
        mapParam.put("type",type);
        mapParam.put("lastModified",dateModified);
        mapParam.put("tag", tag);
        map.put("username",username);
        map.put("searchedParam",mapParam);
        map.put("result",liste);
        return map;
    }
}