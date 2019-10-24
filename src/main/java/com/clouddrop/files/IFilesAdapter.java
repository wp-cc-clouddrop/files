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
     * @param userName file owner
     * @param filename name of the file
     * @return downloaded data in bytes
     */
    public byte[] downloadFile(String userName, String filename);

    public String deleteFile(String userName, String filePathName);
    public List<String> listFiles(String userName);
    public List<String> searchFile(final String userName, final String name, final String type, final String date);

}
