package com.clouddrop.files.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Entity;

@Entity
public class Metadata {

    private String filename;
    private String type;
    private String lastModified;
    private String contentLocation;
    private String username;

    public Metadata() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Metadata(Metadata resource, String location) {
        this.filename = resource.getFilename();
        this.type = resource.getType();
        this.contentLocation = location;
    }

    public String getContentLocation() {
        return contentLocation;
    }

    public void setContentLocation(String contentLocation) {
        this.contentLocation = contentLocation;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String modDate) {
        this.lastModified = modDate;
    }

    public void updateLastModified() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        this.lastModified = dtf.format(now);
    }
}
