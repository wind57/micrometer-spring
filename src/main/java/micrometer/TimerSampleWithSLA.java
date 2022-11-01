package micrometer;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TimerSampleWithSLA {

    public static void main(String[] args) {
        CompositeMeterRegistry registry = new CompositeMeterRegistry();
        registry.add(SampleRegistries.prometheus());
        registry.add(SampleRegistries.jmx());

        // very helpful: https://github.com/micrometer-metrics/micrometer/issues/530
        // https://prometheus.io/docs/practices/histograms/
        // this part : https://prometheus.io/docs/practices/histograms/#errors-of-quantile-estimation
        // actually explained a few things in a very good manner.

        // if you go to http://localhost:8080/prometheus and run this example, you will see
        // that there are only 3 buckets exported.
        Timer timer = Timer.builder("my.timer")
                .sla(Duration.ofMillis(1), Duration.ofMillis(255), Duration.ofMillis(265))
                .register(registry);

        RandomEngine engine = new MersenneTwister64(0);
        Normal duration = new Normal(250, 50, engine);

        // this creates a value that has a mean value of 250 and a standard deviation of 50
        // think about it as the time it takes to process a request in milliseconds.
        // it is a value "around 250 with a standard deviation of 50".
        // this value changes once a second, while we query it every 100 milliseconds.
        // This way we simulate that within 1 seconds we might have requests that take the same
        // amount of time, but some might be different

        // I am also looking at this in grafana, so in grafana I set-up the source as prometheus
        // under URL : "http://host.docker.internal:9090"

        // for this example, I am doing : my_timer_seconds_sum / my_timer_seconds_count

        AtomicInteger latencyForThisSecond = new AtomicInteger(duration.nextInt());
        Flux.interval(Duration.ofSeconds(1))
                .doOnEach(d -> latencyForThisSecond.set(duration.nextInt()))
                .subscribe();

        Flux.interval(Duration.ofMillis(100))
                .doOnEach(d -> timer.record(latencyForThisSecond.get(), TimeUnit.MILLISECONDS))
                .blockLast();

    }

}
