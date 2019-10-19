package com.clouddrop.files;

import java.io.File;

public interface IFilesAdapter {

    public String uploadFile();
    public String updateFile(String filename);
    public String downloadFile(Long id);
    public String deleteFile(Long id);
    public String listFiles();
    public String searchFile(String attribute);

}
