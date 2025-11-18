package com.chaean.teamchatsa.infra.Azure;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AzureStorageConfig {

	@Value("${azure.storage.account-name}")
	private String accountName;

	@Value("${azure.storage.account-key}")
	private String accountKey;

	@Value("${azure.storage.blob-endpoint}")
	private String blobEndpoint;

	@Bean
	public BlobServiceClient blobServiceClient() {
		String connectionString = String.format(
				"DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
				accountName,
				accountKey
		);

		BlobServiceClient client = new BlobServiceClientBuilder()
				.connectionString(connectionString)
				.buildClient();

		log.info("Azure Blob Storage Client initialized. Endpoint: {}", blobEndpoint);
		return client;
	}
}
