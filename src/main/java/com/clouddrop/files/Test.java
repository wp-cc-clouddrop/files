package com.clouddrop.files;

public class Test {
    public static void main(String ... unused){
        String pathname = "C:\\Users\\fazel\\Desktop\\files-service\\src\\main\\java\\com\\clouddrop\\files\\Hello.txt";
        String pathname2 = "C:\\Users\\fazel\\Desktop\\files-service\\src\\main\\java\\com\\clouddrop\\files\\Fisch.jpg";
        String containerName = "mycontainer";
        String userName = "A";
        FilesAzureStorage fas = new FilesAzureStorage();
        fas.setContainerName(containerName);
        fas.connect();
        //fas.deleteFile(userName, "Fisch.jpg");
        fas.uploadFile(userName,pathname);
        //fas.uploadFile(userName,pathname2);
        //fas.updateFile(userName,pathname);
        //fas.listFiles(userName);
    }
}
