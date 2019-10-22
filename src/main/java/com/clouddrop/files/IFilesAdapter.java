package com.clouddrop.files;

import java.io.File;
import java.util.List;

public interface IFilesAdapter {

    public String uploadFile(String userName, String filePathname);
    public String updateFile(String userName, String filePathName);
    public String downloadFile(String userName, String filePathName);
    public String deleteFile(String userName, String filePathName);
    public List<String> listFiles(String userName);
    public List<String> searchFile(String userName, String name, String type, String date);

}
