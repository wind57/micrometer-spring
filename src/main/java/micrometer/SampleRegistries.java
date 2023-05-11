package micrometer;

//import com.netflix.spectator.atlas.AtlasConfig;
import com.sun.net.httpserver.HttpServer;
//import io.micrometer.atlas.AtlasMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;

public final class SampleRegistries {

    private SampleRegistries() {

    }

    // I am not providing any config for this one
//    public static AtlasMeterRegistry atlas() {
//        return new AtlasMeterRegistry(new AtlasConfig() {
//
//            @Override
//            public Duration step() {
//                return Duration.ofSeconds(10);
//            }
//
//            @Override
//            public String get(String k) {
//                return null;
//            }
//        }, Clock.SYSTEM);
//    }

    public static PrometheusMeterRegistry prometheus() {

        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(new PrometheusConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            public String get(String k) {
                return null;
            }
        });

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/prometheus", httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });

            new Thread(server::start).run();
        } catch (IOException ie) {
            throw new RuntimeException(ie);
        }

        return prometheusRegistry;

    }

    public static JmxMeterRegistry jmx() {
        JmxMeterRegistry registry = new JmxMeterRegistry(JmxConfig.DEFAULT, Clock.SYSTEM);
        return registry;
    }

}
