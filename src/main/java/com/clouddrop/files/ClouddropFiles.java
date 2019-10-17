package com.clouddrop.files;


import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class ClouddropFiles {

    /*Konstanten*/
    public static final String STORAGE_CONNECTION_STRING ="DefaultEndpointsProtocol=https;AccountName=clouddropstorage;AccountKey=mwV05Y6DmcWrxgklGsDbqp1HyOHO4gOiYJMfD4KUKOIOlRiS/sSP4lD3Z4FhDkzFm0RRXDTlczcwXDyfPFPE9A==";
    /*Exemplarvariablen*/
    private CloudStorageAccount _storageAccount;
    private CloudBlobClient _blobClient;
    private CloudBlobContainer _blobContainer;

    /*Konstruktor*/

    /**
     * Initialisierung von ClouddropFiles.
     * Es wird der StorageAccount, der BlobClient und der BlobContainer konfiguriert.
     * @throws URISyntaxException ""
     * @throws InvalidKeyException ""
     * @throws StorageException ""
     */
    public ClouddropFiles() throws URISyntaxException, InvalidKeyException, StorageException {
        try {
            _storageAccount = CloudStorageAccount.parse(STORAGE_CONNECTION_STRING);
            _blobClient = _storageAccount.createCloudBlobClient();
            _blobContainer = _blobClient.getContainerReference("My Container");
            _blobContainer.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());
            // Acquire a lease on a container so that another client cannot write to it or delete it
            _blobContainer.acquireLease();
            _blobContainer.breakLease(0);
        }
        catch(StorageException storageException){
            System.out.println(String.format("Error returned from the service. Http code: %d and error code: %s", storageException.getHttpStatusCode(), storageException.getErrorCode()));
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    /*Methoden*/
    /**
     *
     * @return
     */
    public String ladeDateiHoch(){
        return "";
    }

    /**
     *
     * @return
     */
    public String aktualisiereDatei(){
        return "";
    }

    /**
     *
     * @param id
     * @return
     */
    public String gibDatei(Long id){
        return "";
    }

    /**
     *
     * @param id
     * @return
     */
    public String loescheDatei(Long id){
        return "";
    }

    /**
     *
     * @return
     */
    public String gibListeVonDateien(){
        return "";
    }

    /**
     *
     * @param dateiName
     * @param typ
     * @param datum
     * @return
     */
    public String sucheDatei(String dateiName, String typ, String datum){
        return "";
    }

}
