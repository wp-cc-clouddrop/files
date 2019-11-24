package com.clouddrop.files;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FilesGoogleStorage implements IFilesAdapter {

    private Storage _storage;
    private Bucket _bucket;
    private String _bucketName;
    private Blob _blob;

    public FilesGoogleStorage(){
        // Instantiates a client
        setStorage(StorageOptions.getDefaultInstance().getService());//ToDo we have to find a way to set here the credentials for gcp

        //The name of the bucket to access
        setBucketName("guestcontainer");

        // Creates the new bucket
        setBucket(_storage.create(BucketInfo.of(_bucketName)));
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

    public Blob getBlob() {
        return _blob;
    }

    public void setBlob(Blob blob) {
        _blob = blob;
    }

    @Override
    public void uploadMetadata(HashMap<String, String> metadata) {

    }

    public void upload(){//ToDo alles hier muss noch angepasst werden
        //https://cloud.google.com/storage/docs/uploading-objects
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of("bucket", "blob_name");
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        Blob blob = storage.create(blobInfo, "Hello, Cloud Storage!".getBytes());
    }

    @Override
    public boolean updateFile(String username, String filename, byte[] data) {
        return false;
    }

    @Override
    public byte[] downloadFile(String username, String filename) {//ToDo username ist hier nicht relevant,weil der bucketName als Param übergeben wird
        // The name of the bucket to access
        String bucketName = getBucketName();

        // The path to which the file should be downloaded
        Path destFilePath = Paths.get("/local/path/to/file.txt");//ToDo hier muss das Ziel angepasst werden

        // Instantiate a Google Cloud Storage client
        Storage storage = StorageOptions.getDefaultInstance().getService();//ToDo we have to find a way to set here the credentials for gcp

        // Get specific file from specified bucket
        Blob blob = storage.get(BlobId.of(bucketName, filename));

        // Download file to specified path
        blob.downloadTo(destFilePath);
        return new byte[0];//ToDo das hier ist nicht das richtige Ergebnis
    }

    @Override
    public void deleteFile(String username, String filename) throws IllegalArgumentException {//ToDo username ist hier nicht relevant,weil der bucketName als Param übergeben wird
        //https://cloud.google.com/storage/docs/deleting-objects
        BlobId blobId = BlobId.of(_bucketName, filename);
        boolean deleted = _storage.delete(blobId);
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

        }
        return results;
    }

    @Override
    public List<String> searchFile(String username, String name, String type, String date, String tag) {
        return null;
    }
}
