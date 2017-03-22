package com.rorkien.bucketscraper.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.rorkien.bucketscraper.config.StorageClass;

@XmlAccessorType(XmlAccessType.FIELD)
public class BucketContent {
	@XmlElement(name = "Key")
	private String key;
	
	@XmlElement(name = "LastModified")
	private Date lastModified;
	
	@XmlElement(name = "ETag")
	private String eTag;
	
	@XmlElement(name = "Size")
	private Long size;
	
	@XmlElement(name = "StorageClass")
	private StorageClass storageClass;
	
	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	
	public Date getLastModified() { return lastModified; }
	public void setLastModified(Date lastModified) { this.lastModified = lastModified; }
	
	public String geteTag() { return eTag; }
	public void seteTag(String eTag) { this.eTag = eTag; }
	
	public Long getSize() { return size; }
	public void setSize(Long size) { this.size = size; }
	
	public StorageClass getStorageClass() { return storageClass; }
	public void setStorageClass(StorageClass storageClass) { this.storageClass = storageClass; }
}
