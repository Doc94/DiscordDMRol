package me.mrdoc.discord.dmrol;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.Getter;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

public class Core {

    public static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Core.class);

    public static boolean isDebug() {
        return LOGGER.getLevel().equals(Level.DEBUG);
    }

    @Getter
    private static BOT bot;

    /**
     * Inicia la aplicacion
     * <br><br>
     * El primer parametro debe ser siempre el token del bot
     * <br>
     * Para que la aplicacion este en modo debug debe añadir -debug como parametro de inicio
     * @param args Argumentos de inicio
     */
    public static void main(String... args) {
        Thread.currentThread().setName("FortniteHispano");

        if(args.length == 0) {
            LOGGER.error("No hay parametros de inicio, necesitas almenos colocar el token de discord.");
            System.exit(0);
        }

        String token = args[0];

        if(token.equalsIgnoreCase("$TOKEN")) {
            try {
                token = System.getenv("TOKEN");
            } catch (NullPointerException ex) {
                LOGGER.error("El token detectado en .env no existe.",ex);
                System.exit(0);
            }
        }

        ArrayList<String> params = new ArrayList<>(Arrays.asList(args));

        if(params.contains("-debug")) {
            Core.LOGGER.info("Detectado parametro de debug en arranque, cambiando a nivel de DEBUG");
            LOGGER.setLevel(Level.DEBUG);
        }

        if(params.contains("-sslsocket")) {
            Core.LOGGER.info("Detectado parametro de sslsocket en arranque, iniciando socket para conexiones SSL");
            try {
                enableSSLSocket();
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                Core.LOGGER.error("Ocurrio un error al habilitar el Socket SSL. Detalles: " + e.getMessage(),e);
            }
        }

        Core.LOGGER.info("Intentando cargar BOT con token ".concat(token));

        bot = new BOT(token);
        bot.login();
    }

    /**
     * Fuerza inicio de Socket SSL
     * @throws KeyManagementException Problemas con las claves de acceso
     * @throws NoSuchAlgorithmException Problemas de algoritmos
     */
    private static void enableSSLSocket() throws KeyManagementException, NoSuchAlgorithmException {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new X509TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }

}
