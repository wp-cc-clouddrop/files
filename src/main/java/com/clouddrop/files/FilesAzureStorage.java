package com.clouddrop.files;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;


import java.io.*;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;

public class FilesAzureStorage implements IFilesAdapter {

    private CloudStorageAccount _storageAccount;
    private CloudBlobClient _blobClient;
    private CloudBlobContainer _blobContainer;

    private String _containerName;
    private byte[] buffer;

    public FilesAzureStorage(){
        //buffer = new byte[0];
    }

    /**
     * Validates the connection string and returns the storage blob client.
     * The connection string must be in the Azure connection string format.
     *
     * @return The newly created CloudBlobClient object
     *
     * @throws RuntimeException "runtime failed"
     * @throws IOException "io failed"
     * @throws URISyntaxException "Uri has invalid syntax"
     * @throws IllegalArgumentException "Illegal Argument"
     * @throws InvalidKeyException "Invalid key"
     */
    public CloudBlobClient getBlobClientReference() throws RuntimeException, IOException, IllegalArgumentException, URISyntaxException, InvalidKeyException {

        // Retrieve the connection string
        Properties prop = new Properties();
        try {
            InputStream propertyStream = FilesAzureStorage.class.getClassLoader().getResourceAsStream("application.properties");
            if (propertyStream != null) {
                prop.load(propertyStream);
            }
            else {
                throw new RuntimeException();
            }
        } catch (RuntimeException|IOException e) {
            System.out.println("\nFailed to load application.properties file.");
            throw e;
        }

        try {
            _storageAccount = CloudStorageAccount.parse(prop.getProperty("StorageConnectionString"));
        }
        catch (IllegalArgumentException|URISyntaxException e) {
            System.out.println("\nConnection string specifies an invalid URI.");
            System.out.println("Please confirm the connection string is in the Azure connection string format.");
            throw e;
        }
        catch (InvalidKeyException e) {
            System.out.println("\nConnection string specifies an invalid key.");
            System.out.println("Please confirm the AccountName and AccountKey in the connection string are valid.");
            throw e;
        }

        return _storageAccount.createCloudBlobClient();
    }

    /**
     * Creates and returns a container.
     *
     * @param blobClient CloudBlobClient object
     * @param containerName Name of the container to create
     * @return The newly created CloudBlobContainer object
     *
     * @throws StorageException "invalid storage"
     * @throws RuntimeException "runtime failed"
     * @throws IOException "io failed"
     * @throws URISyntaxException "uri syntax"
     * @throws IllegalArgumentException "illegal argument"
     * @throws InvalidKeyException "invalid key"
     * @throws IllegalStateException "illegal state"
     */
    private CloudBlobContainer createContainer(CloudBlobClient blobClient, String containerName) throws StorageException, RuntimeException, IOException, InvalidKeyException, IllegalArgumentException, URISyntaxException, IllegalStateException {

        // Create a new container
        CloudBlobContainer container = blobClient.getContainerReference(containerName);
        try {
            if (!container.createIfNotExists()) {
                //throw new IllegalStateException(String.format("Container with name \"%s\" already exists.", containerName));
            }
        }
        catch (StorageException s) {
            if (s.getCause() instanceof java.net.ConnectException) {
                System.out.println("Caught connection exception from the client. If running with the default configuration please make sure you have started the storage emulator.");
            }
            throw s;
        }
        return container;
    }

    public void connect(){
        try {
            _blobClient = getBlobClientReference();
            _blobContainer = createContainer(_blobClient, _containerName);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void uploadMetadata(HashMap<String, String> metadata) {
        try {
            CloudBlockBlob blob = _blobContainer.getBlockBlobReference(metadata.get("owner") + "-" + metadata.get("filename"));
            uploadEmptyBlob(blob);

            blob.setMetadata(metadata);
            blob.uploadMetadata();
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadEmptyBlob(CloudBlockBlob blob) throws StorageException, IOException {
        byte[] empty = new byte[0];
        blob.uploadFromByteArray(empty, 0, 0);
    }

    @Override
    public String uploadFile(String userName,String pathname) {

        try {
            File localFile = new File(pathname);

            //Getting a blob reference
            CloudBlockBlob blob = _blobContainer.getBlockBlobReference(userName+"-"+localFile.getName());
            //Creating blob and uploading file to it
            blob.uploadFromFile(localFile.getAbsolutePath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (StorageException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String updateFile(String userName, String filename) {
        try {
            File localFile = new File(filename);
            if(_blobContainer.getBlockBlobReference(userName+"-"+localFile.getName()).exists()){
                uploadFile(userName,filename);
            }else{
                throw new IllegalArgumentException("Fail! Man kann keine Datei, die noch nicht existiert, updaten!!");
            }
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String downloadFile(String userName, String filePathName) {
        File localFile = new File(filePathName);
        try {
            CloudBlockBlob blob = _blobContainer.getBlockBlobReference(userName+"-"+localFile.getName());
            blob.downloadToFile(userName+"-"+filePathName);
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String deleteFile(String userName, String filePathName) {
        try {
            CloudBlockBlob blob =  _blobContainer.getBlockBlobReference(userName+"-"+filePathName);
            if(!blob.deleteIfExists()){
                throw new IllegalArgumentException("Man kann keine Datei löschen, die nicht existiert!!!");
            }
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> listFiles(String userName) {
        List<String> results = new ArrayList<>();
        for(ListBlobItem blobItem: _blobContainer.listBlobs(userName)){
            if(blobItem instanceof CloudBlob){
                String path = blobItem.getUri().getPath();
                String prefix = "/"+userName+"-";
                int pathIndex = path.indexOf(prefix);
                //String s = String.format("\t\t%s\t: %s", ((CloudBlob) blobItem).getProperties().getBlobType(), path.substring(pathIndex+prefix.length()));
                String s = String.format("%s", (path.substring(pathIndex+prefix.length())));
                results.add(s);
                //System.out.println(s);
            }
        }
        return results;

    }

    @Override
    public List<String> searchFile(String userName,String name, String typ, String date){
        List<String> results = new ArrayList<>();
        if(name == null && typ == null && date == null){
            return listFiles(userName);
        }
        for(ListBlobItem blobItem : _blobContainer.listBlobs(userName)){
            HashMap<String,String> metaData;
            if(blobItem instanceof CloudBlob){
                try {
                    ((CloudBlob) blobItem).downloadAttributes();
                } catch (StorageException e) {
                    e.printStackTrace();
                }
                metaData = ((CloudBlob) blobItem).getMetadata();
            }else{
                continue;
            }
            boolean nameExists = name != null;
            boolean typeExists = typ != null;
            boolean dateExists = date != null;
            boolean matches = true;
            if(nameExists){
                matches &= metaData.containsValue(name);
            }
            if(typeExists){
                matches &= metaData.containsValue(typ);
            }
            if(dateExists){
                matches &= metaData.containsValue(date);
            }
            if(matches){
                results.add(metaData.get("Dateiname"));
            }
        }
        for(String s : results){
            System.out.println(s);
        }
        return results;
    }

    public void setContainerName(String containerName) {
        _containerName = containerName;
    }
}
