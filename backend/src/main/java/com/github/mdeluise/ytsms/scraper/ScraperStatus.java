package com.github.mdeluise.ytsms.scraper;

import java.util.Date;

public record ScraperStatus(Date lastScrape, boolean scraping, boolean isLastFailed) {
}
