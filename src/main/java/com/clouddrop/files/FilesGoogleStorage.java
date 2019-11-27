package com.clouddrop.files;

import com.google.api.gax.paging.Page;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FilesGoogleStorage implements IFilesAdapter {

    private Storage _storage;
    private Bucket _bucket;
    private String _bucketName;
    //private Blob _blob;

    public FilesGoogleStorage(){
        // Instantiates a client
        setStorage(StorageOptions.getDefaultInstance().getService());//ToDo we have to find a way to set here the credentials for gcp

        //The name of the bucket to access
        setBucketName("guestbucket");

        // Creates the new bucket
        setBucket(getStorage().create(BucketInfo.of(getBucketName())));
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
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").setMetadata(metadata).build();
        Blob blob = getStorage().create(blobInfo, new byte[0]);
    }

    @Override
    public boolean updateFile(String username, String filename, byte[] data) {
        //https://cloud.google.com/storage/docs/uploading-objects
        boolean resu = true;

        BlobId blobId = BlobId.of(getBucketName(), username+"-"+filename);

        Blob blob = getStorage().get(blobId);

        Map<String,String> metadata = blob.getMetadata();
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").setMetadata(metadata).build();
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
            //String s = String.format("\t\t%s\t: %s", ((CloudBlob) blobItem).getProperties().getBlobType(), path.substring(pathIndex+prefix.length()));
            String s = String.format("%s", (name.substring(value+username.length())));
            results.add(s);
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
