package com.github.mdeluise.ytsms.scraper.rss;

import com.github.mdeluise.ytsms.exception.FetchingModeException;
import com.github.mdeluise.ytsms.scraper.InfoExtractor;
import org.springframework.stereotype.Component;

@Component
public class InfoFeedExtractor implements InfoExtractor {
    @Override
    public String getChannelIDFromUsername(String username) {
        throw new FetchingModeException();
    }
}
