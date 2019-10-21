package com.clouddrop.files;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;


import java.io.*;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

public class FilesAzureStorage implements IFilesAdapter {

    private CloudStorageAccount _storageAccount;
    private CloudBlobClient _blobClient;
    private CloudBlobContainer _blobContainer;
    private CloudBlockBlob _blockBlob;
    private File _sourceFile;
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
    public String uploadFile(String userName,String pathname) {

        try {
            HashMap<String,String> metaDaten = new HashMap<String,String>();

            String prefix = ".";
            int pathIndex = pathname.indexOf(prefix);
            metaDaten.put("Type",pathname.substring(pathIndex,pathname.length()-1));

            metaDaten.put("Username",userName);
            metaDaten.put("Pfadname",pathname);
            //TODO Metadaten hinzufügen
            _sourceFile = new File(pathname);
            //Getting a blob reference
            _blockBlob = _blobContainer.getBlockBlobReference(userName+"-"+_sourceFile.getName());
            //Creating blob and uploading file to it
            _blockBlob.uploadFromFile(_sourceFile.getAbsolutePath());
            for(String s : _blockBlob.getMetadata().keySet()){
                System.out.println(s);
            }
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
        uploadFile(userName,filename);
        return null;
    }

    @Override
    public String downloadFile(String userName, String filePathName) {
       //String downloadedBlobPath = String.format("%scopyof-%s", System.getProperty("java.io.tmpdir"), _blockBlob.getName());
        try {
            //_blockBlob = _blobContainer.getBlockBlobReference(userName+"-"+filePathName);
            _blockBlob.downloadToFile(userName+"-"+filePathName);
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String deleteFile(String userName, String filePathName) {
        try {

            _blockBlob = _blobContainer.getBlockBlobReference(userName+"-"+filePathName);
            _blockBlob.delete();
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String listFiles(String userName) {
        for(ListBlobItem blobItem: _blobContainer.listBlobs(userName)){
            if(blobItem instanceof CloudBlob){
                String path = blobItem.getUri().getPath();
                String prefix = "/"+userName+"-";
                int pathIndex = path.indexOf(prefix);
                System.out.println(String.format("\t\t%s\t: %s", ((CloudBlob) blobItem).getProperties().getBlobType(), path.substring(pathIndex+prefix.length())));
            }
        }
        return null;
    }

    @Override
    public String searchFile(String attribute) {
        switch (attribute){
            case "Name": System.out.println("TODO!");
            case "Type": System.out.println("TODO!");
            case "Datum": System.out.println("TODO!");
            default:
                System.out.println("Fehler");
        }
        return null;
    }

    public void setContainerName(String containerName) {
        _containerName = containerName;
    }
}
