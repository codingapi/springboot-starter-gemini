package com.codingapi.gemini.properties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GeminiProperties {

    private String apiKey;
    private String proxyHost;
    private int proxyPort;

    private String version = "v1beta";
    private boolean proxyEnable = true;
}
