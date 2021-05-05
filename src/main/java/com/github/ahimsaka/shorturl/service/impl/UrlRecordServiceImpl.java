package com.github.ahimsaka.shorturl.service.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.github.ahimsaka.shorturl.entity.UrlRecord;
import com.github.ahimsaka.shorturl.repository.UrlRecordRepository;
import com.github.ahimsaka.shorturl.service.UrlRecordService;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import static java.util.Objects.isNull;

@Service
public class UrlRecordServiceImpl implements UrlRecordService {
    final private Logger log = LoggerFactory.getLogger(UrlRecordService.class);
    final private UrlRecordRepository urlRecordRepository;

    final private WebClient webClient = WebClient.create();

    UrlRecordServiceImpl (UrlRecordRepository urlRecordRepository){
        this.urlRecordRepository = urlRecordRepository;
    }

    @Override
    public UrlRecord saveUrlRecord(UrlRecord urlRecord){

        standardize(urlRecord);
        resolveFinalUrl(urlRecord);

        // is url already in Db?
        if (urlRecordRepository.findByUrl(urlRecord.getUrl()) != null){
            return urlRecordRepository.findByUrl(urlRecord.getUrl());
        }

        do {
            urlRecord.setExtension(NanoIdUtils.randomNanoId(
                    NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                    "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray(),
                    8
            ));
            /*
             * if random extension already exists, set a new one.
             * there is a VERY low chance of this happening.
             */
            } while (urlRecordRepository.findByExtension(urlRecord.getExtension()) != null);
        return urlRecordRepository.save(urlRecord);
    }

    @Override
    public UrlRecord updateUrlRecord(UrlRecord urlRecord) {
        return urlRecordRepository.save(urlRecord);
    }

    @Override
    public UrlRecord getUrlRecordByExtension(String extension) {
        UrlRecord urlRecord = urlRecordRepository.findByExtension(extension);
        urlRecord.setHits(urlRecord.getHits() + 1);
        return urlRecordRepository.save(urlRecord);
    }

    @Override
    public Collection<UrlRecord> getAllUrlRecords(){
        return urlRecordRepository.findAll();
    }

    @Override
    public void deleteUrlRecord(UrlRecord urlRecord){
        urlRecordRepository.delete(urlRecord);
    }

    public UrlRecord standardize(UrlRecord urlRecord) {

        String url = urlRecord.getUrl();

        /*
         If temporary redirect, store requested URL. if permanent, store final Location.
         */
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);

        String standardURL;
        try {
            URI checkUri = new URI(url).normalize();

            if (!checkUri.isAbsolute()) checkUri = new URI("https://" + url);

            standardURL = checkUri.toURL().toString();
        } catch (MalformedURLException | URISyntaxException e) {
            return null;
        }

        urlRecord.setUrl(standardURL);
        return urlRecord;

    }

    public UrlRecord resolveFinalUrl(UrlRecord urlRecord){

        return webClient.get()
                .uri(urlRecord.getUrl())
                .retrieve()
                .toBodilessEntity()
                .map((entity) -> {
                    if (entity.getStatusCode().is3xxRedirection()
                            && entity.getHeaders().getLocation().toString() != urlRecord.getUrl()){
                        urlRecord.setUrl(entity.getHeaders().getLocation().toString());
                        return urlRecord;
                    }

                    return urlRecord;

                }).block();
    }
}
