package com.clouddrop.files;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Properties;

public class ClouddropFiles implements IAdapter {

    private static final String STORAGE_CONNECTION_STRING ="DefaultEndpointsProtocol=https;"+
        "AccountName=clouddropstorage;"+
            "AccountKey=tX+Q/nhQbKhNw1gTUQrK1sNeNpr96lmTyHl/2Eww/iim5o8KF4Xr1SFqu56DxBoVar0sY53YNZhTHbrbKa14Ug==";


    private CloudStorageAccount _storageAccount;
    private CloudBlobClient _blobClient;
    private CloudBlobContainer _blobContainer;
    private CloudBlob _blob;


    /**
     * Initialisierung von ClouddropFiles.
     */
    public ClouddropFiles() throws InvalidKeyException, IOException, URISyntaxException, StorageException {
        _blobClient = getBlobClientReference();
        _blobContainer = createContainer(_blobClient, "Container");
    }

    /**
     * Validates the connection string and returns the storage blob client.
     * The connection string must be in the Azure connection string format.
     *
     * @return The newly created CloudBlobClient object
     *
     * @throws RuntimeException ""
     * @throws IOException ""
     * @throws URISyntaxException "Uri has invalid syntax"
     * @throws IllegalArgumentException "Illegal Argument"
     * @throws InvalidKeyException "Invalid key"
     */
    public CloudBlobClient getBlobClientReference() throws RuntimeException, IOException, IllegalArgumentException, URISyntaxException, InvalidKeyException {

        // Retrieve the connection string
        Properties prop = new Properties();
        try {
            InputStream propertyStream = ClouddropFiles.class.getClassLoader().getResourceAsStream("application.properties");
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
     * Creates and returns a container for the sample application to use.
     *
     * @param blobClient CloudBlobClient object
     * @param containerName Name of the container to create
     * @return The newly created CloudBlobContainer object
     *
     * @throws StorageException
     * @throws RuntimeException
     * @throws IOException
     * @throws URISyntaxException
     * @throws IllegalArgumentException
     * @throws InvalidKeyException
     * @throws IllegalStateException
     */
    private static CloudBlobContainer createContainer(CloudBlobClient blobClient, String containerName) throws StorageException, RuntimeException, IOException, InvalidKeyException, IllegalArgumentException, URISyntaxException, IllegalStateException {

        // Create a new container
        CloudBlobContainer container = blobClient.getContainerReference(containerName);
        try {
            if (container.createIfNotExists() == false) {
                throw new IllegalStateException(String.format("Container with name \"%s\" already exists.", containerName));
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


    @Override
    public String uploadFile(File file) {

        try {
            _blob = _blobContainer.getBlockBlobReference(file.getName());
            _blob.uploadFromFile(file.getAbsolutePath());
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
    public String updateFile(File file) {
        // im Storage gucken, ob es den File gibt
        // wenn ja, dann aktualisieren
        // wenn nein, uploaden
        //TODO
        return null;
    }

    @Override
    public String downloadFile(Long id) {
        String downloadedBlobPath = String.format("%scopyof-%s", System.getProperty("java.io.tmpdir"), _blob.getName());
        try {
            _blob.downloadToFile(downloadedBlobPath);
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new File(downloadedBlobPath).deleteOnExit();
        return null;
    }

    @Override
    public String deleteFile(Long id) {
        try {
            _blob.delete(DeleteSnapshotsOption.INCLUDE_SNAPSHOTS, null, null, null);
        } catch (StorageException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String listFiles() {
        for(ListBlobItem blobItem: _blobContainer.listBlobs()){
            System.out.println(blobItem.getUri());
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

}
