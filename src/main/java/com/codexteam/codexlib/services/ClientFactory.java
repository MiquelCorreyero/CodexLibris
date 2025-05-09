
package com.codexteam.codexlib.services;

import javax.net.ssl.*;
import java.net.http.HttpClient;
import java.security.cert.X509Certificate;

/**
 * Fàbrica de clients {@link HttpClient} per gestionar connexions HTTP en entorns de desenvolupament i producció.
 * <p>
 * Proporciona un client insegur que accepta tots els certificats per a ús local (ex. localhost amb certificats autofirmats),
 * i un client segur per a entorns de producció.
 * </p>
 */
public class ClientFactory {

    // Canviar a false en fase de producció
    private static final boolean DESENVOLUPAMENT = true;

    /**
     * Retorna un {@link HttpClient} segons l'entorn actual.
     *
     * @return Un client insegur si {@code DESENVOLUPAMENT} és {@code true}, o un client segur per a producció si és {@code false}.
     */
    public static HttpClient getClient() {
        if (DESENVOLUPAMENT) {
            return crearClientInsegur();
        } else {
            return ClientFactory.getClient();
        }
    }

    /**
     * Crea un {@link HttpClient} que accepta tots els certificats SSL i qualsevol hostname.
     * <p><strong>Només per a ús en desenvolupament</strong>.</p>
     *
     * @return Un {@code HttpClient} configurat amb un {@link SSLContext} que no valida certificats.
     */
    private static HttpClient crearClientInsegur() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] xcs, String string) {}
                    public void checkServerTrusted(X509Certificate[] xcs, String string) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return ClientFactory.getClient();
        }
    }
}
