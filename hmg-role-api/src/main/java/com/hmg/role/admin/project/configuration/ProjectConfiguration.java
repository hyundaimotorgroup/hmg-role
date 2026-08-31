package com.hmg.role.admin.project.configuration;

import com.hmg.role.admin.project.Project;
import com.hmg.role.util.sqlconverter.MapStringToJsonTextConverter;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;

@Data
@Table(name = "project_configurations")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
public class ProjectConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_configuration_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "configuration_key")
    private String configurationKey;

    @Column(name = "configuration_value")
    @Convert(converter = MapStringToJsonTextConverter.class)
    // maybe denormalize this later into rows
    // since this is a JSON text
    private Map<String, String> configurationValue;

    @Formula(
            """
      STR_TO_DATE(
        JSON_UNQUOTE(JSON_EXTRACT(configuration_value, '$.expiredAfter')),
        '%Y-%m-%dT%H:%i:%sZ'
      )
    """) // only compatible for ISO 8601 seconds format in UTC zone (HH:mm:ssZ)
    private LocalDateTime expiryUtc;

    @Formula(
            """
            JSON_UNQUOTE(JSON_EXTRACT(configuration_value, '$.encryptionKey'))
            """) // for security configurations
    private String encryptionKeyB64;
}
