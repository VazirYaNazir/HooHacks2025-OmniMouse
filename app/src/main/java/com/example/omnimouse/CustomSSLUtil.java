package com.example.omnimouse;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class CustomSSLUtil {
    private static final String TAG = "CustomSSLUtil";

    public static SSLContext getSSLContext(Context context) {
        try {
            // Load the certificate from res/raw/cert.pem
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(context.getResources().openRawResource(R.raw.cert));
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                Log.d(TAG, "Certificate loaded: " + ((java.security.cert.X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType(); // usually "BKS" or "PKCS12"
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e) {
            Log.e(TAG, "Error setting up custom SSL context: " + e.getMessage(), e);
            return null;
        }
    }
}
