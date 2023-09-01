package com.github.mdeluise.ytsms;

import com.github.mdeluise.ytsms.scraper.InfoExtractor;
import com.github.mdeluise.ytsms.scraper.InfoExtractorFactory;
import com.github.mdeluise.ytsms.scraper.VideoScraper;
import com.github.mdeluise.ytsms.scraper.VideoScraperFactory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.ServerVariable;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "YTSMS REST API", version = "1.0",
        description = "<h1>Introduction</h1>" + "<p>YTSMS is a self-hosted, open " + "source, ...</p>",
        license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"),
        contact = @Contact(name = "GitHub page", url = "https://github.com/MDeLuise/ytsms")
    ), security = {@SecurityRequirement(name = "bearerAuth")}, servers = {
    @Server(description = "Production", url = "/api"),
    @Server(
        description = "Custom",
        url = "{protocol}://{host}:{port}/{basePath}",
        variables = {
            @ServerVariable(name = "protocol", defaultValue = "http", allowableValues = {"http", "https"}),
            @ServerVariable(name = "host", defaultValue = "localhost"),
            @ServerVariable(name = "port", defaultValue = "8085"),
            @ServerVariable(name = "basePath", defaultValue = "api")
        })
}
)
@SecurityScheme(
    name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer",
    in = SecuritySchemeIn.HEADER
)
@EnableScheduling
@EnableMethodSecurity
public class ApplicationConfig {
    @Autowired
    VideoScraperFactory videoScraperFactory;
    @Autowired
    InfoExtractorFactory infoExtractorFactory;


    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }


    @Bean
    public VideoScraper videoScraper() {
        return videoScraperFactory.getVideoScraper();
    }


    @Bean
    public InfoExtractor infoExtractor() {
        return infoExtractorFactory.getInfoExtractor();
    }


    @Scheduled(cron = "${video.refresh.cron}")
    public void saveNewVideo() {
        videoScraper().saveNewVideo();
    }
}
