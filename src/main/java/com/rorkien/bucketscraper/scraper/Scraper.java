package com.rorkien.bucketscraper.scraper;

import java.util.List;

import com.rorkien.bucketscraper.domain.BucketResult;

public interface Scraper {
	public List<BucketResult> scrape();
}
