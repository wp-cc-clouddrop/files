package com.clouddrop.files;

public class Test {
    public static void main(String ... unused){
        String pathname = "C:\\Users\\fazel\\Desktop\\files-service\\src\\main\\java\\com\\clouddrop\\files\\Hello.txt";
        String containerName = "containertest";
        String userName = "A";
        FilesAzureStorage fas = new FilesAzureStorage();
        fas.setContainerName(containerName);
        fas.connect();
        //fas.uploadFile(userName,pathname);
        //fas.updateFile(userName,pathname);
    }
}
