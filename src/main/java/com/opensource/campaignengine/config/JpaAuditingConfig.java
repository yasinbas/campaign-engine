package com.opensource.campaignengine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // JPA denetimini aktif et
public class JpaAuditingConfig {
}