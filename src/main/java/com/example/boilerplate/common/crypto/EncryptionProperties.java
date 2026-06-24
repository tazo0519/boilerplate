package com.example.boilerplate.common.crypto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "boilerplate.security.crypto")
public class EncryptionProperties {

    @NotBlank
    private String keyBase64;
}
