package com.fst.rsi.resto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String secret  ;
    private long expiration  ; // 24 hours in milliseconds
    private long refreshExpiration ; // 7 days in milliseconds
}

