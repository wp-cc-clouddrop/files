package com.clouddrop.files;

import java.io.File;

public interface IAdapter {

    public String uploadFile(File file);
    public String updateFile(File file);
    public String downloadFile(Long id);
    public String deleteFile(Long id);
    public String listFiles();
    public String searchFile(String attribute);

}
