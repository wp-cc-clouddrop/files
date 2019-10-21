package com.clouddrop.files;

import java.io.File;

public interface IFilesAdapter {

    public String uploadFile(String userName, String filePathname);
    public String updateFile(String userName, String filePathName);
    public String downloadFile(String userName, String filePathName);
    public String deleteFile(String userName, String filePathName);
    public String listFiles(String userName);
    public String searchFile(String attribute);

}
