package com.github.ahimsaka.shorturl.entity;

import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.stereotype.Indexed;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class UrlRecord {

    @Id
    private String extension;

    private String url;

    private Integer hits = 0;

}
