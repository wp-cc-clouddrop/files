package com.clouddrop.files;

import com.clouddrop.files.services.MetadataService;
import com.clouddrop.files.services.PicMetadataExtractor;
import com.clouddrop.files.services.TextMetadataExtractor;
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FilesAzureStorage implements IFilesAdapter {

    private static Logger log = LoggerFactory.getLogger(FilesAzureStorage.class);

    private CloudStorageAccount _storageAccount;
    private CloudBlobClient _blobClient;
    private CloudBlobContainer _blobContainer;
    private TextMetadataExtractor _txtMetadataExtractor;
    private PicMetadataExtractor _picMetadataExtractor;

    private String _containerName;

    public FilesAzureStorage() {
        try {
            _blobClient = getBlobClientReference();
            _blobContainer = createContainer(_blobClient, _containerName);
            _containerName = "guestcontainer";
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (StorageException e) {
            e.printStackTrace();
        }
        _txtMetadataExtractor = new TextMetadataExtractor();
        _picMetadataExtractor = new PicMetadataExtractor();
    }

    /**
     * Validates the connection string and returns the storage blob client. The
     * connection string must be in the Azure connection string format.
     *
     * @return The newly created CloudBlobClient object
     *
     * @throws RuntimeException         "runtime failed"
     * @throws IOException              "io failed"
     * @throws URISyntaxException       "Uri has invalid syntax"
     * @throws IllegalArgumentException "Illegal Argument"
     * @throws InvalidKeyException      "Invalid key"
     */
    public CloudBlobClient getBlobClientReference()
            throws RuntimeException, IOException, IllegalArgumentException, URISyntaxException, InvalidKeyException {

        // Retrieve the connection string
        String connString = System.getenv("AZURE_BLOB_CONN_STRING");
        log.debug("Azure Blob connection string: " + connString);

        try {
            _storageAccount = CloudStorageAccount.parse(connString);
        } catch (IllegalArgumentException | URISyntaxException e) {
            log.debug("\nConnection string specifies an invalid URI.");
            log.debug("Please confirm the connection string is in the Azure connection string format.");
            throw e;
        } catch (InvalidKeyException e) {
            log.debug("\nConnection string specifies an invalid key.");
            log.debug("Please confirm the AccountName and AccountKey in the connection string are valid.");
            throw e;
        }

        return _storageAccount.createCloudBlobClient();
    }

    /**
     * Creates and returns a container.
     *
     * @param blobClient    CloudBlobClient object
     * @param containerName Name of the container to create
     * @return The newly created CloudBlobContainer object
     *
     * @throws StorageException         "invalid storage"
     * @throws RuntimeException         "runtime failed"
     * @throws IOException              "io failed"
     * @throws URISyntaxException       "uri syntax"
     * @throws IllegalArgumentException "illegal argument"
     * @throws InvalidKeyException      "invalid key"
     * @throws IllegalStateException    "illegal state"
     */
    private CloudBlobContainer createContainer(CloudBlobClient blobClient, String containerName)
            throws StorageException, RuntimeException, IOException, InvalidKeyException, IllegalArgumentException,
            URISyntaxException, IllegalStateException {

        // Create a new container
        CloudBlobContainer container = blobClient.getContainerReference(containerName);
        try {
            if (!container.createIfNotExists()) {
                // throw new IllegalStateException(String.format("Container with name \"%s\"
                // already exists.", containerName));
            }
        } catch (StorageException s) {
            if (s.getCause() instanceof java.net.ConnectException) {
                System.out.println(
                        "Caught connection exception from the client. If running with the default configuration please make sure you have started the storage emulator.");
            }
            throw s;
        }
        return container;
    }

    @Override
    public void uploadMetadata(HashMap<String, String> metadata) {
        log.debug("metadata.toString():" + metadata.toString());
        log.debug("username: " + metadata.get("username") + " filename: " + metadata.get("filename"));
        log.debug("blob container: " + _blobContainer.toString());
        try {
            CloudBlockBlob blob = _blobContainer
                    .getBlockBlobReference(metadata.get("username") + "-" + metadata.get("filename"));
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
    public boolean updateFile(String username, String filename, byte[] buffer) {

        try {
            CloudBlockBlob blob = _blobContainer.getBlockBlobReference(getBlobName(username, filename));
            if (!blob.exists()) {
                log.error("File does not exist. username: " + username + " filename: " + filename, new Exception());
                return false;
            }

            blob.downloadAttributes();
            HashMap<String, String> metadata = blob.getMetadata();
            blob.uploadFromByteArray(buffer, 0, buffer.length);

            // extract and set metadata
            String type = metadata.get("type");
            String tags = metadata.get("tags");
            switch (type){
                case ".png":
                case ".jpg":
                    tags += _picMetadataExtractor.getMetadata(buffer);
                    break;
                case  ".txt":
                    String textfileAsString = new String(buffer);
                    log.debug("pre metadataextr with: " + textfileAsString);
                    tags += _txtMetadataExtractor.getMetadata(textfileAsString);
                    log.debug("post metadataextr result: " + tags);
                    break;
                default: log.debug("Type: " + type + " is not supported for AI metadata extraction");
            }
            metadata.put("tags",tags);

            // Update lastModified
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            metadata.put("lastModified", dtf.format(now));

            // log metadata content
            log.debug("metadata content: " + metadata.toString());

            blob.setMetadata(metadata);
            blob.uploadMetadata();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public byte[] downloadFile(String username, String filename) {
        try {
            CloudBlockBlob blob = _blobContainer.getBlockBlobReference(getBlobName(username, filename));
            if (!blob.exists()) {
                log.error("AZURE ERROR: Blob does not exists: username: " + username + "; filename : " + filename);
                return null;
            }

            int expected = Math.toIntExact(blob.getProperties().getLength());
            byte[] data = new byte[expected];

            // check if we received whole file already
            // if not, redownload until whole file is received
            int received = blob.downloadToByteArray(data, 0);
            while (received < expected) {
                expected -= received;
                if (expected < 0) {
                    log.error("AZURE ERROR: expected is smaller than received");
                    return null;
                }

                received = blob.downloadToByteArray(data, received);
            }

            return data;
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        log.error("AZURE ERROR: exception while downloading file");
        return null;
    }

    private String getBlobName(String username, String filename) {
        return username + "-" + filename;
    }

    @Override
    public void deleteFile(String username, String filename) throws IllegalArgumentException {
        try {
            CloudBlockBlob blob =  _blobContainer.getBlockBlobReference(getBlobName(username, filename));
            if(!blob.deleteIfExists()){
                log.error("AZURE ERROR: file to delete does not exists");
                throw new IllegalArgumentException("AZURE ERRROR: Man kann keine Datei löschen, die nicht existiert!!!");
            }
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> listFiles(String username) {
        List<String> results = new ArrayList<>();
        for(ListBlobItem blobItem: _blobContainer.listBlobs(username)){
            if(blobItem instanceof CloudBlob){
                String path = blobItem.getUri().getPath();
                String prefix = "/"+username+"-";
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
    public List<String> searchFile(final String username,final String name, final String typ, final String date, final String tag){
        List<String> results = new ArrayList<>();
        if(name == null && typ == null && date == null && tag == null){
            return null;
        }
        for(ListBlobItem blobItem : _blobContainer.listBlobs(username)){
            HashMap<String,String> metaData;
            if(!(blobItem instanceof CloudBlob)){
                continue;
            }

            try {
                ((CloudBlob) blobItem).downloadAttributes();
            } catch (StorageException e) {
                e.printStackTrace();
                continue;
            }

            metaData = ((CloudBlob) blobItem).getMetadata();

            boolean nameExists = name != null;
            boolean typeExists = typ != null;
            boolean dateExists = date != null;
            boolean tagExists = tag != null;
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
            if(tagExists){
                String[] tagsArray = metaData.get("tags").split(",");
                HashSet<String> tagsSet = new HashSet<String>(Arrays.asList(tagsArray));
                matches &= tagsSet.contains(tag.toLowerCase());
            }
            if(matches){
                results.add(metaData.get("filename"));
            }
        }
        for(String s : results){
            log.debug("SEARCH: found matching: " + s);
        }
        return results;
    }

}
