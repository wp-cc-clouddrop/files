package com.clouddrop.files;

import java.io.File;

public interface IFilesAdapter {

    public String uploadFile(String userName, String filePathname);
    public String updateFile(String userName, String filePathName);
    public String downloadFile(Long id);
    public String deleteFile(Long id);
    public String listFiles();
    public String searchFile(String attribute);

}
