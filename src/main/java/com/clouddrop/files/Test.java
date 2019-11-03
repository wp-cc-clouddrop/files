package com.clouddrop.files;

import com.clouddrop.files.model.Metadata;
import com.clouddrop.files.services.MetadataService;
import com.clouddrop.files.services.TextMetadataExtractor;

public class Test {
    public static void main(String ... unused){
        /*
        String pathname = "C:\\Users\\fazel\\Desktop\\files-service\\src\\main\\java\\com\\clouddrop\\files\\Hello.txt";
        String pathname2 = "C:\\Users\\fazel\\Desktop\\files-service\\src\\main\\java\\com\\clouddrop\\files\\Fisch.jpg";
        String containerName = "mycontainer";
        String userName = "Fazel";
        // FilesAzureStorage fas = new FilesAzureStorage();
        // fas.setContainerName(containerName);
        // fas.connect();
        //fas.uploadFile(userName,pathname);
        //fas.updateFile(userName,pathname); //vorher die txt Datei modifizieren
        //fas.deleteFile(userName, "Hello.txt");
        //fas.uploadFile(userName,pathname2);
        //fas.deleteFile(userName, "Fisch.jpg");
        //fas.listFiles(userName);
        //fas.searchFile(userName,null,null,null);
        //fas.searchFile(userName,"Hello.txt",null,null);
        //fas.searchFile(userName,null,".jpg",null);

        MetadataService ms = new MetadataService();
        Metadata m = ms.create("{\"filename\": \"testuser\", \"type\": \"testType\"}");
        Metadata mnew = new Metadata(m, "heri");
        System.out.println(mnew.getLastModified());
         */
        TextMetadataExtractor extr = new TextMetadataExtractor();
        System.out.println(extr.getMetadata("Unfortunately, it rained during my entire trip to Seattle. I didn't even get to visit the Space Needle"));

    }
}
