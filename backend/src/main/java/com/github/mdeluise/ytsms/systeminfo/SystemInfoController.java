package com.github.mdeluise.ytsms.systeminfo;

import com.github.mdeluise.ytsms.scraper.ScraperStatus;
import com.github.mdeluise.ytsms.scraper.VideoScraper;
import com.github.mdeluise.ytsms.scraper.VideoScraperFactory;
import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/info")
@Tag(name = "Info", description = "Endpoints for system info")
@SecurityRequirements()
public class SystemInfoController {
    private final String version;
    private final boolean youTubeApiFetchingModeEnabled;
    private final VideoScraper videoScraper;


    public SystemInfoController(@Value("${app.version}") String version, @Value("${youtube.key}") String youtubeApiKey,
                                VideoScraperFactory videoScraperFactory) {
        this.version = version;
        this.youTubeApiFetchingModeEnabled = !Strings.isNullOrEmpty(youtubeApiKey);
        this.videoScraper = videoScraperFactory.getVideoScraper();
    }


    @GetMapping("/ping")
    @Operation(
        summary = "Ping the service",
        description = "Check if the service is running."
    )
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }


    @GetMapping("/version")
    @Operation(
        summary = "System version",
        description = "Get the version of the system."
    )
    public ResponseEntity<String> getVersion() {
        return ResponseEntity.ok(version);
    }


    @GetMapping("/scraping-mode")
    @Operation(
        summary = "Fetching mode",
        description = "Return the used fetching mode."
    )
    public ResponseEntity<String> getFetchingMode() {
        return ResponseEntity.ok(youTubeApiFetchingModeEnabled ? "YouTube_API" : "Scraping");
    }


    @GetMapping("/scraping-status")
    @Operation(
        summary = "Status of the scraper",
        description = "Return info about the scraping."
    )
    public ResponseEntity<ScraperStatus> getLastFetchDate() {
        return ResponseEntity.ok(videoScraper.getStatus());
    }
}
