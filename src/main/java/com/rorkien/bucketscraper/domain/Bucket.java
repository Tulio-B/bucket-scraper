package com.rorkien.bucketscraper.domain;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Bucket {
	
	@JsonProperty("base-url")
	private String baseUrl;
	
	@JsonProperty("timestamp")
	private Date syncTimestamp;
	
	@JsonProperty("contents")
	private List<BucketContent> contents;

	public String getBaseUrl() { return baseUrl; }
	public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
	
	public Date getSyncTimestamp() { return syncTimestamp; }
	public void setSyncTimestamp(Date syncTimestamp) { this.syncTimestamp = syncTimestamp; }
	
	public List<BucketContent> getContents() { return contents; }
	public void setContents(List<BucketContent> contents) { this.contents = contents; }	

}
