package com.signly.common.encryption;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EncryptionProperties.class)
@RequiredArgsConstructor
public class EncryptionConfig {
    
    private final EncryptionProperties encryptionProperties;
    
    @Bean
    public AesEncryptionService aesEncryptionService() {
        return new AesEncryptionService(encryptionProperties);
    }
    
    @Bean
    public StringEncryptionConverter stringEncryptionConverter(AesEncryptionService aesEncryptionService) {
        return new StringEncryptionConverter(aesEncryptionService);
    }
}