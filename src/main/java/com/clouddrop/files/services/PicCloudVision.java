package com.clouddrop.files.services;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PicCloudVision {

    private static Logger log = LoggerFactory.getLogger(PicCloudVision.class);

    private ImageAnnotatorClient _vision;

    public PicCloudVision(){
        try {
            // Instantiates a client
            _vision = ImageAnnotatorClient.create();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PicCloudVision(String jsonPath){

        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

            ImageAnnotatorSettings imageAnnotatorSettings =
                    ImageAnnotatorSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                            .build();
            _vision = ImageAnnotatorClient.create(imageAnnotatorSettings);
            // _vision = ImageAnnotatorClient.create();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    public String getMetadata(byte[] picture){
        String metaData = TagImage(_vision,picture);
        return metaData;
    }

    public String TagImage(ImageAnnotatorClient vision, byte[] picture) {
        String resultString = "";

        ByteString imgBytes = ByteString.copyFrom(picture);

        // Builds the image annotation request
        List<AnnotateImageRequest> requests = new ArrayList<>();
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();
        requests.add(request);

        // Performs label detection on the image file
        BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();
        for (AnnotateImageResponse res : responses) {
            // For full list of available annotations, see http://g.co/cloud/vision/docs
            log.debug("VISION API response: " + res.getLabelAnnotationsList());
            for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                String description = annotation.getDescription().toLowerCase();
                log.debug(description);
                resultString += description + ",";
            }
        }

        // remove last comma
        return resultString.substring(0,resultString.length());
    }
}
