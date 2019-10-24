package com.clouddrop.files;

import java.util.HashMap;
import java.util.List;

public interface IFilesAdapter {

    /**
     * Upload blob/object metadata to container/bucket.
     *
     * @param metadata
     */
    public void uploadMetadata(HashMap<String, String> metadata);

    /**
     * Update file in cloud storage.
     *
     * @param username file owner
     * @param data  File to update in bytes array
     * @return true if successfull. False otherwise.
     */
    public boolean updateFile(String username, String filename, byte[] data);

    /**
     * Downloads file from cloud storage
     *
     * @param username file owner
     * @param filename name of the file
     * @return downloaded data in bytes
     */
    public byte[] downloadFile(String username, String filename);

    /**
     * Checks if file exists and deletes it from the cloud storage
     *
     * @param username
     * @param filePathName
     */
    public void deleteFile(String username, String filename) throws IllegalArgumentException;

    /**
     * List all files that belongs to the user in cloud storage
     *
     * @param username
     * @return List of files
     */
    public List<String> listFiles(String username);

    /**
     * Search files of user in cloud storage with parameters
     *
     * @param username
     * @param name
     * @param type
     * @param date
     * @return List of files
     */
    public List<String> searchFile(final String username, final String name, final String type, final String date);

}
