package com.opensource.campaignengine.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Kampanya adı boş bırakılamaz")
    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignType campaignType;

    @NotBlank(message = "Detaylar alanı boş bırakılamaz ve geçerli bir JSON içermelidir")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String details;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private boolean active = true;

    private int priority;

    @Column(length = 50)
    private String externalCode1;

    @Column(length = 50)
    private String externalCode2;
}