package com.rorkien.bucketscraper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rorkien.bucketscraper.config.APIVersion;
import com.rorkien.bucketscraper.domain.Bucket;
import com.rorkien.bucketscraper.domain.BucketContent;
import com.rorkien.bucketscraper.domain.BucketResult;
import com.rorkien.bucketscraper.scraper.ScrapeBucketV1;
import com.rorkien.bucketscraper.scraper.Scraper;
import com.rorkien.bucketscraper.worker.ThreadWorker;
import com.rorkien.bucketscraper.worker.WorkerStatistics;

public class Worker {

	public List<BucketResult> fromUrl(String bucketUrl, APIVersion version) throws MalformedURLException, IOException, JAXBException {
		List<BucketResult> buckets = new ArrayList<BucketResult>();

		Scraper scraper = null;
		
		switch (version) {
		case V1:
		default:
			System.out.println("Scraping on API Version 1, offsetting results using key markers");
			scraper = new ScrapeBucketV1(bucketUrl);
			break;
		case V2:
			System.out.println("Scraping on API Version 2, offsetting results using page tokens");
			break;
		}

		buckets = scraper.scrape();
		return buckets;
	}
	
	public Bucket coalesce(List<BucketResult> results) {
		List<BucketContent> allContents = new ArrayList<BucketContent>();

		System.out.println("Coalescing buckets...");

		Bucket bucket = new Bucket();
		
		for (BucketResult result : results) {
			if (bucket.getBaseUrl() == null) bucket.setBaseUrl(result.getUrl());
			allContents.addAll(result.getContents());
		}

		long size = 0;
		for (BucketContent content : allContents) { size += content.getSize(); }
		System.out.printf("We've got %d files with a total of %dMB\n", allContents.size(), (size / 1024 / 1024));

		bucket.setSyncTimestamp(new Date());
		bucket.setContents(allContents);
		
		return bucket;
	}
	
	public Bucket wrap(List<BucketContent> contents, String baseUrl) {
		Bucket bucket = new Bucket();
		
		bucket.setBaseUrl(baseUrl);
		bucket.setSyncTimestamp(new Date());
		bucket.setContents(contents);
		
		return bucket;
	}

	public Bucket fromJson(String jsonPath) {
		ObjectMapper mapper = new ObjectMapper();

		File jsonFile = new File(jsonPath);
		Bucket bucket = null;
		try {
			System.out.println("Unmarshalling from previously saved JSON...");
			bucket = mapper.readValue(jsonFile, Bucket.class);
			
			long size = 0;
			for (BucketContent content : bucket.getContents()) { size += content.getSize(); }
			System.out.printf("%d items loaded from bucket, with a total of %dMB.\n", bucket.getContents().size(), (size / 1024 / 1024));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bucket;
	}

	public void exportJsonKeys(File outputFolder, Bucket bucket) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		try {
			File jsonFile = new File(outputFolder.getAbsolutePath() + "/files.json");
			System.out.printf("Trying to save contents to file '%s'\n", jsonFile.getName());
			mapper.writeValue(jsonFile, bucket);			
		} catch (IOException e) {
			System.err.println("Error writing to file.");
		}
	}
	
	public void downloadFiles(Bucket bucket, File outputFolder, Integer offsetStart, Integer offsetEnd, Integer workerAmount) {		
		ThreadGroup workerGroup = new ThreadGroup("Workers");
		WorkerStatistics workerStatistics = new WorkerStatistics();
		
		if (offsetEnd == null) offsetEnd = bucket.getContents().size();
		List<BucketContent> workingList = bucket.getContents().subList(offsetStart, offsetEnd);
		ConcurrentLinkedQueue<BucketContent> contentQueue = new ConcurrentLinkedQueue<BucketContent>(workingList);
		
		for (int i = 0; i < workerAmount; i++) {
			ThreadWorker worker = new ThreadWorker(workerGroup, workerStatistics, bucket, offsetStart, offsetEnd, outputFolder, contentQueue);
			worker.setName("Worker #" + (i + 1));
			worker.start();
		}
	}
}
