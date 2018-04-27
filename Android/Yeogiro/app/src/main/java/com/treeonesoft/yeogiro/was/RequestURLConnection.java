package com.treeonesoft.yeogiro.was;

import android.content.ContentValues;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class RequestURLConnection {

    private static final String TAG = "SESIN_RUC";

    public String request(String _url, ContentValues _params) {
        HttpsURLConnection urlConns = null;
        HttpURLConnection urlConn = null;

        StringBuffer sbParams = new StringBuffer();

        if (_params == null) {
            sbParams.append("");
        } else {
            boolean isAnd = false;

            String key;
            String value;

            for (Map.Entry<String, Object> parameter : _params.valueSet()) {
                key = parameter.getKey();
                value = parameter.getValue().toString();

                if (isAnd)
                    sbParams.append("&");

                sbParams.append(key).append("=").append(value);

                if (!isAnd)
                    if (_params.size() >= 2)
                        isAnd = true;
            }
        }

        try {
            URL url = new URL(_url);

            if (_url.toLowerCase().startsWith("https")) {
                trustAllHosts();

                urlConns = (HttpsURLConnection) url.openConnection();

                urlConns.setRequestMethod("POST");
                urlConns.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConns.setDoInput(true);
                urlConns.setDoOutput(true);
                urlConns.connect();

                String strParams = sbParams.toString();

                Log.d(TAG, "params = " + strParams);

                OutputStream os = urlConns.getOutputStream();
                os.write(strParams.getBytes("UTF-8"));
                os.flush();
                os.close();

                if (urlConns.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "responseCode is no ok! " + urlConns.getResponseCode() + " " + urlConns.getResponseMessage());
                    if (urlConns.getResponseCode() == 403) {
                        Thread.sleep(1000);
                        urlConns = (HttpsURLConnection) url.openConnection();

                        urlConns.setRequestMethod("POST");
                        urlConns.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        urlConns.setDoInput(true);
                        urlConns.setDoOutput(true);
                        urlConns.connect();

                        strParams = sbParams.toString();

                        Log.d(TAG, "params = " + strParams);

                        os = urlConns.getOutputStream();
                        os.write(strParams.getBytes("UTF-8"));
                        os.flush();
                        os.close();
                    } else
                        return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConns.getInputStream(), "UTF-8"));

                String line;
                String page = "";

                while ((line = reader.readLine()) != null) {
                    page += line;
                }

                return page;
            } else {
                urlConn = (HttpURLConnection) url.openConnection();

                urlConn.setRequestMethod("POST");
                urlConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);
                urlConn.connect();

                String strParams = sbParams.toString();

                Log.d(TAG, "params = " + strParams);

                OutputStream os = urlConn.getOutputStream();
                os.write(strParams.getBytes("UTF-8"));
                os.flush();
                os.close();

                if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "responseCode is no ok! " + urlConn.getResponseCode() + " " + urlConn.getResponseMessage());
                    if (urlConn.getResponseCode() == 403) {
                        Thread.sleep(1000);
                        urlConn = (HttpsURLConnection) url.openConnection();

                        urlConn.setRequestMethod("POST");
                        urlConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        urlConn.setDoInput(true);
                        urlConn.setDoOutput(true);
                        urlConn.connect();

                        strParams = sbParams.toString();

                        Log.d(TAG, "params = " + strParams);

                        os = urlConn.getOutputStream();
                        os.write(strParams.getBytes("UTF-8"));
                        os.flush();
                        os.close();
                    } else
                        return null;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

                String line;
                String page = "";

                while ((line = reader.readLine()) != null) {
                    page += line;
                }
                return page;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "exception = " + e.getMessage());
        } finally {
            if (urlConns != null)
                urlConns.disconnect();
            if (urlConn != null)
                urlConn.disconnect();
        }
        return null;
    }

    private void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws java.security.cert.CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws java.security.cert.CertificateException {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext ssl = SSLContext.getInstance("SSL");
            ssl.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(ssl.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}
