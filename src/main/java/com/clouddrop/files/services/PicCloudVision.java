package com.clouddrop.files.services;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PicCloudVision {
    private ImageAnnotatorClient _vision;

    public PicCloudVision(){
        try {
            // Instantiates a client
            _vision = ImageAnnotatorClient.create();
        } catch (IOException e) {
            e.printStackTrace();
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
        Map<Descriptors.FieldDescriptor,Object> map;
        for (AnnotateImageResponse res : responses){
            for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                annotation.getAllFields().forEach((k, v) ->
                        resultString+=v.toString());
                        /*System.out.printf("%s : %s\n", k, v.toString())

                );*/
            }
        }

        return resultString;
    }

}
