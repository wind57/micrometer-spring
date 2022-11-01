package micrometer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class CounterAndGaugeSample {

    /*
     * 1. start jconsole and look at the counter, for the running process and MBeans
     *
     * 2. For prometheus it's a little more involved:
     *
     *    - inside "docker-compose.yaml" change the volumes path to your local path
     *    - "docker-compose up" (if this fails: "docker rm grafana/prometheus")
     *    - inside "SampleRegistries::prometheus" there is a http server that is scrapping whatever
     *      our counter has already persisted locally. We need to tell prometheus where this end-point is.
     *      This is done under "our-scrape-endpoint" in prometheus.yaml.
     *      Notice that it uses "host.docker.internal", so that we can scrape the host from docker.
     *
     *    - run Sample1.
     *    -
     */
    public static void main(String[] args) {
        CompositeMeterRegistry registry = new CompositeMeterRegistry();

        //registry.add(SampleRegistries.atlas());
        registry.add(SampleRegistries.prometheus());
        registry.add(SampleRegistries.jmx());

        AtomicInteger x = new AtomicInteger(0);

        registry.gauge("my.gauge", Collections.emptyList(), x, AtomicInteger::get);

        Counter ping = registry.counter("my.counter.ping", "type", "ping");

        Counter pong =  Counter.builder("my.counter.ping")
                .tag("type", "pong")
                .register(registry);

        Flux.interval(Duration.ofMillis(10))
                .doOnEach(i -> ping.increment())
                .subscribe();

        Flux.interval(Duration.ofMillis(5))
                .doOnEach(i -> pong.increment())
                .subscribe();

        Flux.interval(Duration.ofMillis(5))
                .doOnEach(i -> x.incrementAndGet())
                .blockLast();
    }

}
