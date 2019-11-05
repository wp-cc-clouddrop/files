package com.clouddrop.files.services;

import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ImageTag;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.TagResult;

public class PicMetadataExtractor {

    private ComputerVisionClient _cvc;
    private String _subscriptionKey = System.getenv("AZURE_CS_PICTURE_ANALYSIS_KEY");
    private String _endpoint = System.getenv("AZURE_CS_PICTURE_ANALYSIS_KEY");

    public PicMetadataExtractor(){
        authenticate(_subscriptionKey, _endpoint);
    }

    private void authenticate(String subscriptionKey, String endpoint) {
        _cvc = ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
    }


    public String getMetadata(byte[] picture) {
        String metaData = TagImage(_cvc,picture);
        return metaData;
    }

    /**
     * TAG IMAGE:
     * API call: TagImageInStream & TagImage
     * Displays the image tags and their confidence values.
     */
    public String TagImage(ComputerVisionClient client, byte[] binary) {
        String resultString = "";
        try {
            // Get tags from local image
            TagResult analysisLocal = client.computerVision().tagImageInStream()
                    .withImage(binary)
                    .withLanguage("en")
                    .execute();

            TagResult[] results = { analysisLocal };

            // Print results of local
            for (TagResult result : results){
                String location = null;
                TagResult analysis = null;
                if (result == analysisLocal) { analysis = analysisLocal; location = "local"; }
                //System.out.println("Tags from " + location + " image: ");
                if (analysis.tags().size() == 0) {
                    System.out.println("No tags detected in " + location + " image.");
                } else {

                    for (ImageTag tag : analysis.tags()) {
                        //System.out.printf("\'%s\' with confidence %2.2f%%\n", tag.name(), tag.confidence() * 100);
                        resultString+=tag.name() + ",";
                    }
                }
                //System.out.println();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        resultString = resultString.substring(0,resultString.length()-1);
        return resultString;
    }
    //  END - Tag Image
}
