package com.rorkien.bucketscraper.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.rorkien.bucketscraper.domain.BucketContent;
import com.rorkien.bucketscraper.domain.BucketResult;

public class ScrapeBucketV1 implements Scraper {
	private String bucketUrl;
	
	public ScrapeBucketV1(String bucketUrl) {
		this.bucketUrl = bucketUrl;
	}

	public List<BucketResult> scrape() {

		List<BucketResult> buckets = new ArrayList<BucketResult>();

		BucketResult currentBucket = null;
		do {
			URL url = null;

			try {
				if (currentBucket != null) {
					BucketContent lastContent = currentBucket.getContents().get(currentBucket.getContents().size() - 1);
					System.out.printf("Last bucket was truncated. Continuing from last key (%s)\n", lastContent.getKey());
					url = new URL(bucketUrl + "?marker=" + lastContent.getKey());
				} else {
					url = new URL(bucketUrl);
				}
				System.out.println("Connecting to: " + url.toString());
			} catch (MalformedURLException e) {
				System.err.println("Bad URL: " + bucketUrl);
				e.printStackTrace();
			}

			URLConnection connection = null;
			try {
				connection = url.openConnection();
			} catch (IOException e) {
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace();
			}

			BufferedReader br;
			StringBuilder builder = new StringBuilder();
			
			try {
				br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
				
				String line = null;
				while ((line = br.readLine()) != null) {
					builder.append(line);
				}
			} catch (UnsupportedEncodingException e) {
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace();
			}

			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(BucketResult.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				currentBucket = (BucketResult) jaxbUnmarshaller.unmarshal(new StringReader(builder.toString()));					
			} catch (JAXBException e) {
				System.err.println("Unmarshalling Error: " + e.getMessage());
				e.printStackTrace();
			}

			buckets.add(currentBucket);

			System.out.printf("Unmarshalled bucket with %d items.\n", currentBucket.getContents().size());
		} while (currentBucket.getIsTruncated());

		System.out.println("Done scraping.");
		return buckets;
	}

}
