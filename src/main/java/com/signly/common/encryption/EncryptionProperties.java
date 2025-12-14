package com.signly.common.encryption;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.encryption")
public class EncryptionProperties {
    
    private boolean enabled = true;
    
    private String secretKey;
    
    private String algorithm = "AES/GCM/NoPadding";
    
    private int ivLength = 12;
    
    private int tagLength = 128;
    
    private String charset = "UTF-8";

    public String getSecretKey() {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalStateException("Encryption secret key is not configured. Please set app.encryption.secret-key");
        }
        return secretKey;
    }

}