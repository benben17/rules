package com.spacex.rule.config;

import org.springframework.beans.factory.annotation.Value;

public class Properties {

    @Value("${yara.server.url}")
    public String yaraUrl;

    @Value("${yara.server.port}")
    public String yaraPort;

    public String getYaraValidateUrl() {
        return "http://" + yaraUrl + ":" + yaraPort + "/yara/verification";
    }

}
