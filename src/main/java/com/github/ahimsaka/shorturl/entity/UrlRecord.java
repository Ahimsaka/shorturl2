package com.github.ahimsaka.shorturl.entity;

import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.stereotype.Indexed;

import javax.persistence.*;

@Entity
@Data
public class UrlRecord {

    @Id
    private String extension;

    private String url;

    private Integer hits = 0;

}
