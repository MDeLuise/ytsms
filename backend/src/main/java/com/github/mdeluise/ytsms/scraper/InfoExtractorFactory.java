package com.github.mdeluise.ytsms.scraper;

import com.github.mdeluise.ytsms.scraper.rss.InfoFeedExtractor;
import com.github.mdeluise.ytsms.scraper.ytapi.InfoApiExtractor;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InfoExtractorFactory {
    private final String youTubeApi;
    private final InfoFeedExtractor infoFeedExtractor;
    private final InfoApiExtractor infoApiExtractor;


    public InfoExtractorFactory(@Value("${youtube.key}") String youTubeApi, InfoFeedExtractor infoFeedExtractor,
                                InfoApiExtractor infoApiExtractor) {
        this.youTubeApi = youTubeApi;
        this.infoFeedExtractor = infoFeedExtractor;
        this.infoApiExtractor = infoApiExtractor;
    }


    public InfoExtractor getInfoExtractor() {
        return Strings.isNullOrEmpty(youTubeApi) ? infoFeedExtractor : infoApiExtractor;
    }
}
