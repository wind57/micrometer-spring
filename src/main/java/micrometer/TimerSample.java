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

/**
 *
 */
public class TimerSample {

    public static void main(String[] args) {
        CompositeMeterRegistry registry = new CompositeMeterRegistry();
        registry.add(SampleRegistries.prometheus());
        registry.add(SampleRegistries.jmx());

        Timer timer = Timer.builder("my.timer")
                .publishPercentileHistogram()
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

        // for this example, I am doing two things : my_timer_seconds_sum / my_timer_seconds_count
        // and "rate(my_timer_seconds_sum[$__rate_interval]) / rate(my_timer_seconds_count[$__rate_interval])"

        // the difference is that rate only computes for the current interval, a good example is here:
        // https://stackoverflow.com/questions/63172240/using-rate-instead-of-sum-count-in-micrometer

        AtomicInteger latencyForThisSecond = new AtomicInteger(duration.nextInt());
        Flux.interval(Duration.ofSeconds(1))
                .doOnEach(d -> latencyForThisSecond.set(duration.nextInt()))
                .subscribe();

        // once in 100 milliseconds we query the "current latency" (remember that it's a value of 250 with a standard deviation of 50)
        // we record this value with the timer.
        Flux.interval(Duration.ofMillis(100))
                .doOnEach(d -> timer.record(latencyForThisSecond.get(), TimeUnit.MILLISECONDS))
                .blockLast();

    }

    // I'm leaving this example here because I had to refresh some concepts about statistics
    // this loop will create for example a series of numbers, where the mean is 0 and standard deviation
    // is 1.
    // https://www.youtube.com/watch?v=esskJJF8pCc

//    public static void main(String[] args) {
//        RandomEngine engine = new MersenneTwister64(0);
//        Normal incomingRequests = new Normal(0, 1, engine);
//
//        for(int i=0;i<10;++i) {
//            System.out.println(incomingRequests.nextInt());
//        }
//    }

}
