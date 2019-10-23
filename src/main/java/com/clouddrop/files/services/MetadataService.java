package com.clouddrop.files.services;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.clouddrop.files.model.Metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MetadataService {

    private static Logger log = LoggerFactory.getLogger(MetadataService.class);

    ObjectMapper mapper;

    public MetadataService() {
        mapper = new ObjectMapper();
        // create admin user?
    }

    /**
     * Create new file metadata for the user from given metadata.
     *
     * @param user User
     */
    public Metadata create(Metadata resource) {
        return null;
    }

    public Metadata create(String resource) {
        try {
            return mapper.readValue(resource, Metadata.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            System.out.println("HEYYYYY");
        } catch (JsonMappingException e) {
            e.printStackTrace();
            System.out.println("HOOOOO");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toJSON(Metadata m) throws JsonProcessingException {
        return mapper.writeValueAsString(m);
    }

    public HashMap<String, String> toMap(Metadata m) {
        HashMap<String, String> map = new HashMap<>();
        map.put("filename", m.getFilename());
        map.put("type", m.getType());
        map.put("lastModified", m.getLastModified());
        map.put("contentLocation", m.getContentLocation());
        map.put("owner", m.getOwner());
        return map;
    }

}