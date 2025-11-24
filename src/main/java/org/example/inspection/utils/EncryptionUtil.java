package org.example.inspection.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.asymmetric.AsymmetricCrypto;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.SM2;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

@Component
public class EncryptionUtil {
    @Resource
    private InspectionEntityUtil inspectionEntityUtil;
    private RSA rsa;

    @PostConstruct
    public void init() throws Exception {
        Map<String, Object> defaults = inspectionEntityUtil.getDefaultConfig();
        rsa = new RSA(defaults.get("priv-key").toString(), defaults.get("pub-key").toString());
    }

    public String encrypt(String plainText) {
        byte[] encryptBytes = rsa.encrypt(plainText.getBytes(), KeyType.PublicKey);
        return Base64.encode(encryptBytes);
    }

    public String decrypt(String cipherText) {
        byte[] decryptBytes = rsa.decrypt(Base64.decode(cipherText), KeyType.PrivateKey);
        return new String(decryptBytes);
    }
}
