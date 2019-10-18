package com.clouddrop.files;


import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;


import java.io.*;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class ClouddropFiles implements IAdapter {

    public static final String STORAGE_CONNECTION_STRING ="DefaultEndpointsProtocol=https;"+
        "AccountName=clouddropstorage;"+
            "AccountKey=mwV05Y6DmcWrxgklGsDbqp1HyOHO4gOiYJMfD4KUKOIOlRiS/sSP4lD3Z4FhDkzFm0RRXDTlczcwXDyfPFPE9A==";

    private CloudStorageAccount _storageAccount;
    private CloudBlobClient _blobClient;
    private CloudBlobContainer _blobContainer;
    private CloudBlockBlob _blockBlob;
    private File _quellDatei;
    private File _heruntergeladeneDatei;

    /**
     * Initialisierung von ClouddropFiles.
     */
    public ClouddropFiles() throws InvalidKeyException, StorageException, URISyntaxException {
        erstelleContainer();;
    }

    private void erstelleContainer()throws URISyntaxException, InvalidKeyException, StorageException{
        try {
            // Parse the connection string and create a blob client to interact with Blob storage
            _storageAccount = CloudStorageAccount.parse(STORAGE_CONNECTION_STRING);
            _blobClient = _storageAccount.createCloudBlobClient();
            _blobContainer = _blobClient.getContainerReference("My Container");

            // Create the container if it does not exist with public access.
            _blobContainer.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());

            // Acquire a lease on a container so that another client cannot write to it or delete it
            //getBlobContainer().acquireLease();
            //getBlobContainer().breakLease(0);
            //_blobContainer.acquireLease();
            //_blobContainer.breakLease(0);

        }
        catch(StorageException storageException){
            System.out.println(String.format("Error returned from the service. Http code: %d and error code: %s", storageException.getHttpStatusCode(), storageException.getErrorCode()));
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    private String erstelleDatei() throws IOException, URISyntaxException, StorageException {

        //Creating a sample file
        _quellDatei = File.createTempFile("sampleFile", ".txt");
        //System.out.println("Creating a sample file at: " + _quellDatei.toString());
        Writer output = new BufferedWriter(new FileWriter(_quellDatei));
        output.write("Hello Azure!");
        output.close();

        //Getting a blob reference
        _blockBlob = _blobContainer.getBlockBlobReference(_quellDatei.getName());

        //Creating blob and uploading file to it
        //System.out.println("Uploading the sample file ");
        _blockBlob.uploadFromFile(_quellDatei.getAbsolutePath());

        return _quellDatei.toString();
    }

    @Override
    public String ladeDateiHoch(){
        String result;
        result = "";
        try {
            result = erstelleDatei();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (StorageException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String aktualisiereDatei() {
        return null;
    }

    @Override
    public String ladeDateiHerunter(Long id) {
        String result = "";
        // Download blob. In most cases, you would have to retrieve the reference
        // to cloudBlockBlob here. However, we created that reference earlier, and
        // haven't changed the blob we're interested in, so we can reuse it.
        // Here we are creating a new file to download to. Alternatively you can also pass in the path as a string into downloadToFile method: blob.downloadToFile("/path/to/new/file").
        _heruntergeladeneDatei = new File(_quellDatei.getParentFile(), "downloadedFile.txt");
        try {
            _blockBlob.downloadToFile(_heruntergeladeneDatei.getAbsolutePath());
            result = _heruntergeladeneDatei.getAbsolutePath();
        } catch (StorageException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String loescheDatei(Long id) {
        return null;
    }

    @Override
    public String gibListeVonDateien() {
        String liste = "";
        for(ListBlobItem blobItem : _blobContainer.listBlobs()){
            liste+=blobItem.getUri();
        }
        return liste;
    }

    @Override
    public String sucheDatei(String dateiName, String typ, String datum) {
        return null;
    }

    public CloudStorageAccount getStorageAccount() {
        return _storageAccount;
    }

    public void setStorageAccount(CloudStorageAccount storageAccount) {
        _storageAccount = storageAccount;
    }

    public CloudBlobClient getBlobClient() {
        return _blobClient;
    }

    public void setBlobClient(CloudBlobClient blobClient) {
        _blobClient = blobClient;
    }

    public CloudBlobContainer getBlobContainer() {
        return _blobContainer;
    }

    public void setBlobContainer(CloudBlobContainer blobContainer) {
        this._blobContainer = blobContainer;
    }

    public File getQuellDatei() {
        return _quellDatei;
    }

    public void setQuellDatei(File quellDatei) {
        _quellDatei = quellDatei;
    }

    public File getHeruntergeladeneDatei() {
        return _heruntergeladeneDatei;
    }

    public void setHeruntergeladeneDatei(File heruntergeladeneDatei) {
        _heruntergeladeneDatei = heruntergeladeneDatei;
    }
}
