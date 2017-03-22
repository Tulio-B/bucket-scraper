package com.rorkien.bucketscraper.worker;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.rorkien.bucketscraper.domain.Bucket;
import com.rorkien.bucketscraper.domain.BucketContent;

public class ThreadWorker extends Thread {
	private Integer filesDownloaded = 0;
	private Long bytesDownloaded = 0L;	
	
	private Bucket bucket;
	private File outputFolder;

	private List<BucketContent> contents;
	private WorkerStatistics workerStatistics;
	
	public ThreadWorker(ThreadGroup group, WorkerStatistics workerStatistics, Bucket bucket, File outputFolder, List<BucketContent> contents) {
		super(group, "Worker");
		this.contents = contents;
		this.workerStatistics = workerStatistics;
		this.bucket = bucket;
		this.outputFolder = outputFolder;
	}

	public void run() {
		System.out.printf("Starting %s\n", getName());
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		
		for (int i = 0; i < contents.size(); i++) {
			URL url = null;
			
			try {
				url = new URL(bucket.getBaseUrl() + URLEncoder.encode(contents.get(i).getKey(), "UTF-8"));
				String fileName = contents.get(i).getKey().substring(contents.get(i).getKey().lastIndexOf('/') + 1, contents.get(i).getKey().length());
				
				filesDownloaded++;
				int totalFilesDownloaded = workerStatistics.getTotalFilesDownloaded().incrementAndGet();
				long totalBytesDownloaded = workerStatistics.getTotalBytesDownloaded().get() / 1024 / 1024;
				
				if (fileName.equals("")) {
					System.out.printf("[%s][%d/%d, %d/%dMB] Downloading file %d out of %d : Unnamed file. Ignoring.\n",
							getName(),
							filesDownloaded,
							contents.size(),
							bytesDownloaded / 1024 / 1024,
							totalBytesDownloaded,
							totalFilesDownloaded,
							bucket.getContents().size());
					continue;
				} else {
					System.out.printf("[%s][%d/%d, %d/%dMB] Downloading file %d out of %d (%s, %dMB)\n",
							getName(),
							filesDownloaded,
							contents.size(),
							bytesDownloaded / 1024 / 1024,
							totalBytesDownloaded,
							totalFilesDownloaded,
							bucket.getContents().size(),
							contents.get(i).getKey(),
							contents.get(i).getSize() / 1024 / 1024);
				}
				
				
				Path targetPath = new File(outputFolder + File.separator + fileName.replaceAll("[\\/:*?\"<>|]", "")).toPath();
				Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

				bytesDownloaded += contents.get(i).getSize();
				workerStatistics.getTotalBytesDownloaded().addAndGet(contents.get(i).getSize());
				
			} catch (MalformedURLException e) {
				System.err.println("Bad URL: " + url);
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		System.out.printf("%s is done.\n", Thread.currentThread().getName());
	}

}
