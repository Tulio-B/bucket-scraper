package com.rorkien.bucketscraper.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Arguments {
	@JsonProperty("bucket-url")
	private String bucketUrl;
	
	@JsonProperty("bucket-api-version")
	private APIVersion bucketAPIVersion;
	
	@JsonProperty("input-json")
	private String[] jsonInputFiles;
	
	@JsonProperty("output-path")
	private String outputPath;
	
	@JsonProperty("output-json")
	private String outputJsonPath;
	
	@JsonProperty("journalled")
	private Boolean journalled;
	
	@JsonProperty("offset")
	private Integer offset;
	
	@JsonProperty("workers")
	private Integer workerAmount;
}
