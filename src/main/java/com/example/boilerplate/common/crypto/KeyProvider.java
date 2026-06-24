package com.example.boilerplate.common.crypto;

import javax.crypto.SecretKey;

public interface KeyProvider {

    SecretKey getActiveKey();
}
