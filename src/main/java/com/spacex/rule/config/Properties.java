package com.spacex.rule.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
public class Properties {

    @Value("${yara.server.url}")
    public String yaraUrl;

    @Value("${yara.server.port}")
    public String yaraPort;

    public String getYaraValidateUrl() {
        return "http://" + yaraUrl + ":" + yaraPort + "/yara/verification";
    }
    public String getYaraDeleteValidateUrl() {
        return "http://" + yaraUrl + ":" + yaraPort + "/yara/delete";
    }

    public String getYaraFilePath() {
        return  "./rules";
    }
}
