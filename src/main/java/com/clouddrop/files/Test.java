package com.clouddrop.files;

public class Test {
    public static void main(String ... unused){
        String pathname = "C:\\Users\\fazel\\Desktop\\files-service\\src\\main\\java\\com\\clouddrop\\files\\Hello.txt";
        String pathname2 = "C:\\Users\\fazel\\Desktop\\files-service\\src\\main\\java\\com\\clouddrop\\files\\Fisch.jpg";
        String containerName = "mycontainer";
        String userName = "Fazel";
        FilesAzureStorage fas = new FilesAzureStorage();
        fas.setContainerName(containerName);
        fas.connect();
        //fas.uploadFile(userName,pathname);
        //fas.updateFile(userName,pathname); //vorher die txt Datei modifizieren
        //fas.deleteFile(userName, "Hello.txt");
        //fas.uploadFile(userName,pathname2);
        //fas.deleteFile(userName, "Fisch.jpg");
        //fas.listFiles(userName);
        //fas.searchFile(userName,null,null,null);
        //fas.searchFile(userName,"Hello.txt",null,null);
        //fas.searchFile(userName,null,".jpg",null);
    }
}
