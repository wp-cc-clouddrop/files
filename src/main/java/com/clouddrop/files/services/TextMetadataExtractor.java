package com.clouddrop.files.services;

import com.microsoft.azure.cognitiveservices.textanalytics.*;
import com.microsoft.azure.cognitiveservices.textanalytics.implementation.*;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * only works with english
 */
public class TextMetadataExtractor {

    private TextAnalyticsAPIImpl _apiClient;
    private String _apiEndpoint;
    private String _subscriptionKey;
    private int _idCounter;

    private static Logger log = LoggerFactory.getLogger(TextMetadataExtractor.class);

    public TextMetadataExtractor() {
        _apiEndpoint = System.getenv("AZURE_CS_TEXT_ANALYSIS_ENDPOINT");
        _subscriptionKey = System.getenv("AZURE_CS_TEXT_ANALYSIS_KEY");

        if (_apiClient == null || _subscriptionKey == null) {
            log.error("Env. Variables for credentials are not available for AI Service Text Analysis");
        }
        _idCounter = 0;
        _apiClient = new TextAnalyticsAPIImpl(
                _apiEndpoint,
                new ServiceClientCredentials() {
                    @Override
                    public void applyCredentialsFilter(OkHttpClient.Builder builder) {
                        builder.addNetworkInterceptor(
                                new Interceptor() {
                                    @Override
                                    public Response intercept(Interceptor.Chain chain) throws IOException {
                                        Request request = null;
                                        Request original = chain.request();
                                        // Request customization: add request headers
                                        Request.Builder requestBuilder = original.newBuilder()
                                                .addHeader("Ocp-Apim-Subscription-Key", _subscriptionKey);
                                        request = requestBuilder.build();
                                        return chain.proceed(request);
                                    }
                                });
                    }
                });
        _apiClient.withAzureRegion(AzureRegions.NORTHEUROPE);
    }

    public String getMetadata(String text) {
        List<MultiLanguageInput> inputList = new ArrayList<MultiLanguageInput>();
        inputList.add(makeInput(String.valueOf(_idCounter), text));
        MultiLanguageBatchInputInner batch = new MultiLanguageBatchInputInner();
        batch.withDocuments(inputList);
        KeyPhraseBatchResultInner result = _apiClient.keyPhrases(batch);

        String resultString = "";
        for(KeyPhraseBatchResultItem document : result.documents())
        {
            for(String keyphrase : document.keyPhrases())
            {
                resultString += keyphrase + ",";
            }
        }
        resultString = resultString.substring(0,resultString.length()-1);
        _idCounter ++;
        return resultString;
    }

    private static MultiLanguageInput makeInput(String id, String text) {
        MultiLanguageInput input = new MultiLanguageInput();
        input.withId(id);
        input.withLanguage("en");
        input.withText(text);
        return input;
    }

}
