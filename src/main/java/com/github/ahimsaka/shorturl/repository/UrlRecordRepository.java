package com.github.ahimsaka.shorturl.repository;

import com.github.ahimsaka.shorturl.entity.UrlRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRecordRepository extends JpaRepository<UrlRecord, Long> {
    UrlRecord findByExtension(String extension);
    UrlRecord findByUrl(String url);

}
