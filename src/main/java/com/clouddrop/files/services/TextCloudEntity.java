package com.clouddrop.files.services;

import java.io.IOException;

import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;

public class TextCloudEntity {

	private LanguageServiceClient _client;

	public TextCloudEntity()
	{
		try
		{
			_client = LanguageServiceClient.create();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public TextCloudEntity(String endpoint) 
	{
		try {
			LanguageServiceSettings settings = LanguageServiceSettings.newBuilder()
					.setEndpoint(endpoint)
					.build();
			_client = LanguageServiceClient.create(settings);

		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}


	public String getMetadata(String text){
		String metaData = tagText(text);
		return metaData;
	}

	// Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
	private String tagText(String text) {
		String resultString = null;

		Document doc = Document.newBuilder()
				.setContent(text)  
				.setType(Type.PLAIN_TEXT)
				.build();
		AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder()
				.setDocument(doc)
				.setEncodingType(EncodingType.UTF8)
				.build();

		AnalyzeEntitiesResponse response = _client.analyzeEntities(request);

		for (Entity entity : response.getEntitiesList()) {
			resultString+=entity.getName()+",";
		}

		return resultString;
	}

}