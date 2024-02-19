package com.codingapi.gemini.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.codingapi.gemini.pojo.Embedding;
import com.codingapi.gemini.pojo.Generate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final String apiKey;

    private final static String baseUrl = "https://generativelanguage.googleapis.com/v1beta/";

    private final HttpHeaders headers;

    public GeminiClient(String apiKey, String proxyHost, int proxyPort) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();

        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        if (StringUtils.hasLength(proxyHost) && proxyPort > 0) {
            requestFactory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }
        restTemplate.setRequestFactory(requestFactory);
    }

    public void stream(Generate.Request request, Consumer<Generate.Response> consumer) throws IOException {
        String url = baseUrl + "models/gemini-pro:streamGenerateContent?key=" + apiKey;
        String json = request.toJSONString();
        log.info("json:{}", json);
        HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);
        ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Resource.class);
        InputStream in = Objects.requireNonNull(response.getBody()).getInputStream();
        byte[] bytes = new byte[1024 * 8];
        int len;
        while ((len = in.read(bytes)) != -1) {
            String body = new String(bytes, 0, len);
            List<Generate.Response> responseList = JSONArray.parseArray(body, Generate.Response.class);
            for (Generate.Response res : responseList) {
                consumer.accept(res);
            }
        }
        in.close();
    }


    public Generate.Response generate(Generate.Request request) {
        String url;
        if (request.isVision()) {
            url = baseUrl + "models/gemini-pro-vision:generateContent?key=" + apiKey;
        } else {
            url = baseUrl + "models/gemini-pro:generateContent?key=" + apiKey;
        }
        String json = request.toJSONString();
        log.info("json:{}", json);
        HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        return JSONObject.parseObject(response.getBody(), Generate.Response.class);
    }

    public Embedding.Response embedding(Embedding.Request request) {
        String url = baseUrl + "models//embedding-001:embedContent?key=" + apiKey;
        String json = request.toJSONString();
        log.info("json:{}", json);
        HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        return JSONObject.parseObject(response.getBody(), Embedding.Response.class);
    }

}
