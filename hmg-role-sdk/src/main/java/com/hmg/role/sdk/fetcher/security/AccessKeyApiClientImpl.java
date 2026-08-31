package com.hmg.role.sdk.fetcher.security;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmg.role.sdk.fetcher.dto.ProjectEncryptionKeyDto;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AccessKeyApiClientImpl implements AccessKeyApiClient {

    private static final String ACCESS_KEYS_PATH = "/api/admin/v1/access-keys";

    private final String baseUrl;
    private final String apiKey;
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public AccessKeyApiClientImpl(String baseUrl, String apiKey) {

        this.baseUrl =
                (baseUrl != null && baseUrl.endsWith("/"))
                        ? baseUrl.substring(0, baseUrl.length() - 1)
                        : baseUrl;
        this.apiKey = apiKey;
        this.client =
                new OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)
                        .writeTimeout(5, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build();
        this.mapper =
                new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ProjectEncryptionKeyDto fetchDecryptionKey() throws IOException {
        String url = baseUrl + ACCESS_KEYS_PATH;

        Request request =
                new Request.Builder()
                        .url(url)
                        .get()
                        .header("Accept", "application/json")
                        .header("X-HMG-ROLE-API-KEY", apiKey)
                        .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch secrets: HTTP " + response.code());
            }

            ResponseBody body = response.body();
            String json = Objects.requireNonNull(body, "Empty response body").string();

            ProjectEncryptionKeyDto dto = mapper.readValue(json, ProjectEncryptionKeyDto.class);
            return dto;
        }
    }
}
