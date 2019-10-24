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
     * @param owner Username of owner
     * @param data  File to update in bytes array
     * @return true if successfull. False otherwise.
     */
    public boolean updateFile(String owner, String filename, byte[] data);

    /**
     * Downloads file from cloud storage
     *
     * @param userName owner of the file
     * @param filename name of the file
     * @param data     returned data in bytes
     */
    public boolean downloadFile(String userName, String filename, byte[] data);

    public String deleteFile(String userName, String filePathName);
    public List<String> listFiles(String userName);
    public List<String> searchFile(final String userName, final String name, final String type, final String date);

}
