package com.rorkien.bucketscraper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.rorkien.bucketscraper.config.APIVersion;
import com.rorkien.bucketscraper.domain.Bucket;
import com.rorkien.bucketscraper.domain.BucketResult;

public class Application {
	
	public static void main(String[] args) throws MalformedURLException, IOException, JAXBException {
		String url = null;
		APIVersion version = APIVersion.V1;

		String jsonPath = null;
		File outputFolder = null;
		Boolean download = false;
		
		Integer offsetStart = 0;
		Integer offsetEnd = null;
		Integer workers = 1;
		
		if (args.length == 0) {
			System.out.println("Usage: bucket-scraper -u <url path of the bucket> -v <api version (default is v1)> -i <path of the JSON bucket contents> [-o <path to output files>] [-j] [-d] [-fS <start offset> [-fE <end offset>]] [-w <parallel workers amount>]");
		} else {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-u")) {
					url = args[i + 1];
					i++;
				} else if (args[i].equals("-v")) {
					version = APIVersion.valueOf(args[i + 1]);
				} else if (args[i].equals("-i")) {
					System.out.println("Option '-i' is set. Forcing JSON input.");
					jsonPath = args[i + 1];
					i++;
				} else if (args[i].equals("-o")) {
					outputFolder = new File(args[i + 1]);
					System.out.printf("Outputting to '%s'\n", outputFolder.getAbsolutePath());
					i++;
				} else if (args[i].equals("-j")) {
				} else if (args[i].equals("-d")) {
					download = true;
				} else if (args[i].equals("-fS")) {
					download = true;
					offsetStart = Integer.valueOf(args[i + 1]);
				} else if (args[i].equals("-fE")) {
					download = true;
					offsetEnd = Integer.valueOf(args[i + 1]);
				} else if (args[i].equals("-w")) {
					download = true;
					workers = Integer.valueOf(args[i + 1]);
					System.out.printf("The downloader will run with %d parallel workers.\n", workers);
				}
			}
			
			if (offsetEnd != null) {
				System.out.printf("Offsetting download from %d to %d.\n", offsetStart, offsetEnd);
			}
			if (outputFolder == null) {
				System.out.println("Trying to create output folder.");
				
				try {
					outputFolder = Files.createTempDirectory("bucket-scraper-" + System.nanoTime()).toFile();
				} catch (IOException e) {
					System.err.println("Could not create temporary folder.");
					System.exit(0);
				}
			} else if (!outputFolder.exists()) {
				System.out.println("Output directory does not exist. Trying to create...");
				outputFolder.mkdirs();
				
				if (!outputFolder.exists()) {
					System.err.println("Could not create output folder. Attempting fallback to temporary folder.");
					
					try {
						outputFolder = Files.createTempDirectory("bucket-scraper-" + System.nanoTime()).toFile();
					} catch (IOException e) {
						System.err.println("Could not create temporary folder.");
						System.exit(0);
					}
				}
			}
			
			Worker worker = new Worker();
			Bucket bucket = null;
			if (jsonPath != null) {
				bucket = worker.fromJson(jsonPath);
			} else {
				List<BucketResult> results = worker.fromUrl(url, version);
				bucket = worker.coalesce(results);
				worker.exportJsonKeys(outputFolder, bucket);
			}
			if (download) worker.downloadFiles(bucket, outputFolder, offsetStart, offsetEnd, workers);
		}
	}

}
