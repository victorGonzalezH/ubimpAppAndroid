package com.metricsfab.utils.http;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.tls.HandshakeCertificates;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestApiClientGenerator {


    /**
     *
     * @param baseUrlParam
     * @param serviceClass
     * @param <S>
     * @return
     */
    public static <S> S createService(String baseUrlParam, Class<S> serviceClass) {
        OkHttpClient.Builder httpClient  = new OkHttpClient.Builder();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrlParam).addConverterFactory(GsonConverterFactory.create()).client(httpClient.build());
        Retrofit retrofit = builder.build();
        return retrofit.create(serviceClass);
    }


    /**
     *
     * @param baseUrlParam
     * @param serviceClass
     * @param cert
     * @param <S>
     * @return
     */
    public static <S> S createService(String baseUrlParam, Class<S> serviceClass, InputStream cert) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {

        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        X509Certificate ca;

        try {
            ca = (X509Certificate) cf.generateCertificate(cert);

            HandshakeCertificates certificates = new HandshakeCertificates.Builder().addTrustedCertificate(ca).build();

            // creating a KeyStore containing our trusted CAs
            //String keyStoreType = KeyStore.getDefaultType();
            //KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            //keyStore.load(null, null);
            //keyStore.setCertificateEntry("ca", ca);

            // creating a TrustManager that trusts the CAs in our KeyStore
            //String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            //TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            //tmf.init(keyStore);

            // creating an SSLSocketFactory that uses our TrustManager
            //SSLContext sslContext = SSLContext.getInstance("TLS");
            //slContext.init(null, tmf.getTrustManagers(), null);

            OkHttpClient.Builder httpClient  = new OkHttpClient.Builder().sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager());
            httpClient.setHostnameVerifier$okhttp(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    //TODO: Make this more restrictive
                    return true;
                }
            });

            Retrofit.Builder builder = new Retrofit.Builder().baseUrl(baseUrlParam)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build());
            Retrofit retrofit = builder.build();
            return retrofit.create(serviceClass);

        }
        catch (Exception exception)
        {
            throw  exception;
        }
        finally {
            try {
                cert.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }

    }

}
