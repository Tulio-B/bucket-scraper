package com.rorkien.bucketscraper.worker;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.rorkien.bucketscraper.domain.Bucket;
import com.rorkien.bucketscraper.domain.BucketContent;

public class ThreadWorker extends Thread {
	private Integer filesDownloaded = 0;
	private Long bytesDownloaded = 0L;
	
	private Integer offsetStart;
	private Integer offsetEnd;
	
	private Bucket bucket;
	private File outputFolder;

	private ConcurrentLinkedQueue<BucketContent> contents;
	private WorkerStatistics workerStatistics;
	
	public ThreadWorker(ThreadGroup group, WorkerStatistics workerStatistics, Bucket bucket, Integer offsetStart, Integer offsetEnd, File outputFolder, ConcurrentLinkedQueue<BucketContent> contents) {
		super(group, "Worker");
		this.workerStatistics = workerStatistics;
		this.bucket = bucket;
		this.offsetStart = offsetStart;
		this.offsetEnd = offsetEnd;
		this.outputFolder = outputFolder;
		this.contents = contents;
	}

	public void run() {
		System.out.printf("Starting %s\n", getName());
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) { }
		
		BucketContent content = null;
		while ((content = contents.poll()) != null) {
			URL url = null;
			
			try {
				url = new URL(bucket.getBaseUrl() + URLEncoder.encode(content.getKey(), "UTF-8"));
				String fileName = content.getKey().substring(content.getKey().lastIndexOf('/') + 1, content.getKey().length());
				
				filesDownloaded++;
				int totalFilesDownloaded = workerStatistics.getTotalFilesDownloaded().incrementAndGet();
				long totalBytesDownloaded = workerStatistics.getTotalBytesDownloaded().get() / 1024 / 1024;
				
				if (fileName.equals("")) {
					System.out.printf("[%s][%d/%d (%d), %d/%dMB] Downloading file %d out of %d: Unnamed file. Ignoring.\n",
							getName(),
							totalFilesDownloaded,
							contents.size(),
							filesDownloaded,
							bytesDownloaded / 1024 / 1024,
							totalBytesDownloaded,
							offsetStart + totalFilesDownloaded,
							offsetEnd);
					continue;
				} else {
					System.out.printf("[%s][%d/%d (%d), %d/%dMB] Downloading file %d out of %d (%s, %dMB)\n",
							getName(),
							totalFilesDownloaded,
							contents.size(),
							filesDownloaded,
							bytesDownloaded / 1024 / 1024,
							totalBytesDownloaded,
							offsetStart + totalFilesDownloaded,
							offsetEnd,
							content.getKey(),
							content.getSize() / 1024 / 1024);
				}
				
				String sanitizedFileName = fileName.replaceAll("[\\/:*?\"<>|]", "");
				Path targetPath = new File(outputFolder + File.separator + sanitizedFileName).toPath();
				Path temporaryFilePath = new File(outputFolder + File.separator + String.format(".bs-%s.tmp", sanitizedFileName, System.currentTimeMillis())).toPath();
				
				Files.copy(url.openStream(), temporaryFilePath, StandardCopyOption.REPLACE_EXISTING);
				try {
					Files.move(temporaryFilePath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);					
				} catch (AtomicMoveNotSupportedException e) {
					Files.move(temporaryFilePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
				}

				bytesDownloaded += content.getSize();
				workerStatistics.getTotalBytesDownloaded().addAndGet(content.getSize());
				
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
