package com.aircraftcarrier.framework.web;

import com.aircraftcarrier.framework.exception.SysException;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;

/**
 * @author liuzhipeng
 * @since 2020-01-15 10:13
 */
public class SSLUtil {

    private SSLUtil() {
    }

    /**
     * getSSLContext
     * eg: "env/dev/jd-mtf-mastercard-keystore.jks", "jks", "123456"
     *
     * @param keyStoreClassPath keyStoreClassPath
     * @param keyStoreType      keyStoreType
     * @param keyStorePassword  keyStorePassword
     * @return SSLContext
     */
    public static SSLContext getSSLContext(String keyStoreClassPath, String keyStoreType, String keyStorePassword) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        InputStream keyStoreInputStream = SSLUtil.class.getClassLoader().getResourceAsStream(keyStoreClassPath);
        if (keyStoreInputStream == null) {
            throw new SysException(keyStoreClassPath + " file not found");
        }
        KeyStore keyStore = getKeyStore(keyStoreInputStream, keyStoreType, keyStorePassword);
        // Trust own CA and all self-signed certs
        return SSLContexts.custom().loadKeyMaterial(keyStore, keyStorePassword.toCharArray()).build();
    }

    /**
     * getKeyStore
     *
     * @param keyStorePath     keyStorePath
     * @param keyStoreType     keyStoreType
     * @param keyStorePassword keyStorePassword
     * @return KeyStore
     * @throws Exception Exception
     */
    public static KeyStore getKeyStore(String keyStorePath, String keyStoreType, String keyStorePassword) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        File file = new File(keyStorePath);
        if (!file.exists() || !file.isFile()) {
            throw new SysException(keyStorePath + "文件未找到");
        }
        InputStream keyStoreInputStream = Files.newInputStream(file.toPath());
        return getKeyStore(keyStoreInputStream, keyStoreType, keyStorePassword);
    }


    /**
     * getKeyStore
     *
     * @param sslCaCertPath sslCaCertPath
     * @param keyStoreType  keyStoreType
     * @return KeyStore
     * @throws Exception Exception
     */
    public static KeyStore getKeyStore(String sslCaCertPath, String keyStoreType) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        File file = new File(sslCaCertPath);
        if (!file.exists() || !file.isFile()) {
            throw new SysException(sslCaCertPath + "文件未找到");
        }
        InputStream keyStoreInputStream = Files.newInputStream(file.toPath());
        return getKeyStore(keyStoreInputStream, keyStoreType, null);
    }

    /**
     * getKeyStore
     *
     * @param keyStore         keyStore
     * @param keyStoreType     keyStoreType
     * @param keyStorePassword keyStorePassword
     * @return KeyStore
     * @throws Exception Exception
     */
    public static KeyStore getKeyStore(InputStream keyStore, String keyStoreType, String keyStorePassword) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keystore = KeyStore.getInstance(keyStoreType);
        if (keyStorePassword == null || "".equals(keyStorePassword.trim())) {
            keystore.load(keyStore, null);
            return keystore;
        }
        keystore.load(keyStore, keyStorePassword.toCharArray());
        return keystore;
    }

    public static KeyManager[] getKeyManagers(KeyStore keyStore, String keyStorePass) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyFactory.init(keyStore, keyStorePass.toCharArray());
        return keyFactory.getKeyManagers();
    }

    public static TrustManager[] getTrustManagers(KeyStore trustKeyStore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustKeyStore);
        return trustFactory.getTrustManagers();
    }


    public static TrustManager[] getTrustManagers(InputStream sslCaCert) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        // Any password will work.
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(sslCaCert);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }
        KeyStore caKeyStore = getKeyStore((InputStream) null, null, "password");
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = "ca" + index++;
            caKeyStore.setCertificateEntry(certificateAlias, certificate);
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(caKeyStore);
        return trustManagerFactory.getTrustManagers();
    }


}
