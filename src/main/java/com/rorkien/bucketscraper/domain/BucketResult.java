package com.rorkien.bucketscraper.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ListBucketResult", namespace = "http://s3.amazonaws.com/doc/2006-03-01/")
public class BucketResult {
	
	private String url;
	
	@XmlElement(name = "Name")
	private String name;
	
	@XmlElement(name = "Prefix")
	private String prefix;
	
	@XmlElement(name = "NextContinuationToken")
	private String nextContinuationToken;
	
	@XmlElement(name = "KeyCount")
	private Integer keyCount;
	
	@XmlElement(name = "MaxKeys")
	private Integer maxKeys;
	
	@XmlElement(name = "IsTruncated")
	private Boolean isTruncated;
	
	@XmlElement(name = "Contents")
	private List<BucketContent> contents = new ArrayList<BucketContent>();
	
	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getPrefix() { return prefix; }
	public void setPrefix(String prefix) { this.prefix = prefix; }
	
	public String getNextContinuationToken() { return nextContinuationToken; }
	public void setNextContinuationToken(String nextContinuationToken) { this.nextContinuationToken = nextContinuationToken; }
	
	public Integer getKeyCount() { return keyCount; }
	public void setKeyCount(Integer keyCount) { this.keyCount = keyCount; }
	
	public Integer getMaxKeys() { return maxKeys; }
	public void setMaxKeys(Integer maxKeys) { this.maxKeys = maxKeys; }
	
	public Boolean getIsTruncated() { return isTruncated; }
	public void setIsTruncated(Boolean isTruncated) { this.isTruncated = isTruncated; }
	
	public List<BucketContent> getContents() { return contents; }
	public void setContents(List<BucketContent> contents) { this.contents = contents; }
}
