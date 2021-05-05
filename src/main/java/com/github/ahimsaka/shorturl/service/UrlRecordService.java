package com.github.ahimsaka.shorturl.service;

import com.github.ahimsaka.shorturl.entity.UrlRecord;

import java.util.Collection;

public interface UrlRecordService {

    UrlRecord saveUrlRecord(UrlRecord urlRecord);

    UrlRecord updateUrlRecord(UrlRecord urlRecord);

    UrlRecord getUrlRecordByExtension(String extension);

    Collection<UrlRecord> getAllUrlRecords();

    void deleteUrlRecord(UrlRecord urlRecord);
}
