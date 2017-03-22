package com.rorkien.bucketscraper.worker;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class WorkerStatistics {
	private AtomicInteger totalFilesDownloaded = new AtomicInteger(0);
	private AtomicLong totalBytesDownloaded = new AtomicLong(0);

	public AtomicInteger getTotalFilesDownloaded() { return totalFilesDownloaded; }
	public AtomicLong getTotalBytesDownloaded() { return totalBytesDownloaded; }
}
