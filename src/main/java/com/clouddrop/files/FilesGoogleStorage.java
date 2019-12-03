package com.clouddrop.files;

import com.clouddrop.files.services.PicCloudVision;
import com.clouddrop.files.services.TextCloudEntity;
import com.google.common.collect.Lists;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List.*;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import com.microsoft.azure.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FilesGoogleStorage implements IFilesAdapter {

    private static Logger log = LoggerFactory.getLogger(FilesGoogleStorage.class);

    private Storage _storage;
    private Bucket _bucket;
    private String _bucketName;
    private PicCloudVision _pcv;
    private TextCloudEntity _tce;


    public FilesGoogleStorage(String jsonPath){
        // Explicitly request service account credentials from the compute engine instance.
        //GoogleCredentials credentials = ComputeEngineCredentials.create();
        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Instantiates a client
        setStorage(StorageOptions.newBuilder().setCredentials(credentials).build().getService());//ToDo maybe this could be a solution

        //The name of the bucket to access
        setBucketName("guestbucket");

        // Creates the new bucket
        _bucket = getStorage().get(getBucketName());
        if(getBucket() == null){
            setBucket(getStorage().create(BucketInfo.of(getBucketName())));
        }

        _pcv = new PicCloudVision(jsonPath);
        _tce = new TextCloudEntity(jsonPath);

    }

    public FilesGoogleStorage(){
        // Instantiates a client
        setStorage(StorageOptions.getDefaultInstance().getService());

        //The name of the bucket to access
        setBucketName("guestbucket");

        // Creates the new bucket
        _bucket = getStorage().get(getBucketName());
        if(getBucket() == null){
            setBucket(getStorage().create(BucketInfo.of(getBucketName())));
        }
    }

    public Storage getStorage() {
        return _storage;
    }

    public void setStorage(Storage storage) {
        _storage = storage;
    }

    public Bucket getBucket() {
        return _bucket;
    }

    public void setBucket(Bucket bucket) {
        _bucket = bucket;
    }

    public String getBucketName() {
        return _bucketName;
    }

    public void setBucketName(String bucketName) {
        _bucketName = bucketName;
    }

    @Override
    public void uploadMetadata(HashMap<String, String> metadata) {
        //https://cloud.google.com/storage/docs/uploading-objects
        BlobId blobId = BlobId.of(getBucketName(), metadata.get("username")+"-"+metadata.get("filename"));
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)/*.setContentType("text/plain")*/.setMetadata(metadata).build();
        Blob blob = getStorage().create(blobInfo, new byte[0]);
    }

    @Override
    public boolean updateFile(String username, String filename, byte[] data) {
        //https://cloud.google.com/storage/docs/uploading-objects
        boolean resu = true;

        BlobId blobId = BlobId.of(getBucketName(), username+"-"+filename);

        Blob blob = getStorage().get(blobId);

        if(!blob.exists()){
            log.error("File does not exist. username: " + username + " filename: " + filename, new Exception());
            return false;
        }

        Map<String,String> metadata = blob.getMetadata();
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)/*.setContentType("text/plain")*/.setMetadata(metadata).build();

        // extract and set metadata
        String type = metadata.get("type");
        String tags = metadata.get("tags");
        switch (type){
            case ".png":
            case ".jpg":
                tags += _pcv.getMetadata(data);
                break;
            case  ".txt":
                String textfileAsString = new String(data);
                log.debug("pre metadataextr with: " + textfileAsString);
                tags += _tce.getMetadata(textfileAsString);
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

        try(WriteChannel wc = getStorage().writer(blobInfo)){
            try{
                wc.write(ByteBuffer.wrap(data));
            } catch (Exception ex){
                ex.printStackTrace();
                resu = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            resu = false;
        }

        return resu;
    }

    @Override
    public byte[] downloadFile(String username, String filename) {
        // The name of the bucket to access
        String bucketName = getBucketName();

        // Get specific file from specified bucket
        Blob blob = getStorage().get(BlobId.of(bucketName, username+"-"+filename));

        // Download file to specified path
        //blob.downloadTo(destFilePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        blob.downloadTo(baos,Blob.BlobSourceOption.generationMatch());
        return baos.toByteArray();
    }

    @Override
    public void deleteFile(String username, String filename) throws IllegalArgumentException {
        //https://cloud.google.com/storage/docs/deleting-objects
        BlobId blobId = BlobId.of(getBucketName(), username+"-"+filename);
        boolean deleted = getStorage().delete(blobId);
        if (deleted) {
            // the blob was deleted
        } else {
            // the blob was not found
            throw new IllegalArgumentException("The blob was not found! Maybe you have deleted it.");
        }
    }

    @Override
    public List<String> listFiles(String username) {
        //https://cloud.google.com/storage/docs/listing-objects
        List<String> results = new ArrayList<>();
        Page<Blob> blobs = getBucket().list();
        for (Blob blob : blobs.iterateAll()) {
            // do something with the blob
            String name = blob.getName();
            int value = name.indexOf(username);
            if(value == -1){
                continue;
            }
            Map<String,String> metaData = blob.getMetadata();
            results.add(metaData.get("filename"));
        }
        return results;
    }

    @Override
    public List<String> searchFile(final String username,final String name, final String typ, final String date, final String tag) {

        List<String> results = new ArrayList<>();
        Page<Blob> blobs = getBucket().list();
        for (Blob blob : blobs.iterateAll()) {

            Map<String,String> metaData = blob.getMetadata();

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
        return results;
    }
}
