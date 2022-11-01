package micrometer.spring2019;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;


public class NonSpringMetrics {

    private static final List<Chore> CHORES = List.of(
        new Chore("Mow front lawn", Duration.ofMinutes(20), "yard"),
        new Chore("Mow back lawn", Duration.ofMinutes(10), "yard"),
        new Chore("Gather the laundry", Duration.ofMinutes(7), "laundry"),
        new Chore("Wash the laundry", Duration.ofMinutes(7), "laundry"),
        new Chore("Sort/Fold the laundry", Duration.ofMinutes(50), "laundry"),
        new Chore("Wash dishes", Duration.ofMinutes(10), "laundry")
    );

    public static void main(String[] args) throws Exception {
        //Metrics.globalRegistry
        //CompositeMeterRegistry meterRegistry = new CompositeMeterRegistry();
        var meterRegistry = Metrics.globalRegistry;
        SimpleMeterRegistry simple = new SimpleMeterRegistry();
        meterRegistry.add(simple);
        meterRegistry.add(loggingMeterRegistry());

//        meterRegistry.config().meterFilter(MeterFilter.deny(
//                x -> x.getName().equals("chores.completed")
//        ));

        meterRegistry.config().meterFilter(new MeterFilter() {
            @Override
            public Meter.Id map(Meter.Id id) {
                if (id.getName().equals("chores.duration")) {
                    Iterable<Tag> tags = id.getTagsAsIterable();
                    List<Tag> replacedTags = StreamSupport.stream(tags.spliterator(), false)
                            .map(x -> {
                                if (x.getKey().equals("group") && x.getValue().equals("laundry")) {
                                    return x;
                                } else return Tag.of("group", "other");
                            }).toList();
                    return id.replaceTags(replacedTags);
                }
                return id;
            }
        });


        addGauge(meterRegistry);
        for (Chore chore : CHORES) {
            meterRegistry.counter("chores.completed").increment();
            meterRegistry.timer("chores.duration", Tags.of("group", chore.group())).record(chore.duration());
        }

        simple.getMeters().forEach(meter -> {
            System.out.println(meter.getId() + "  " + meter.measure());
        });

        System.gc();
        for(int i=0;i<100;++i) {
            Thread.sleep(1000);
            System.out.println("Waiting");
        }
    }

    private static void addGauge(CompositeMeterRegistry meterRegistry) {
        var diffChores = CHORES.stream().map(Function.identity()).toList();

        // weak-reference in here
        //meterRegistry.gauge("chores.size", diffChores, x -> ((Integer)x.size()).doubleValue());

        meterRegistry.gauge("chores.size", diffChores, x -> ((Integer)diffChores.size()).doubleValue());
    }

    private static LoggingMeterRegistry loggingMeterRegistry() {
        LoggingRegistryConfig config = new LoggingRegistryConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public boolean logInactive() {
                return true;
            }

            @Override
            public Duration step() {
                return Duration.ofSeconds(5);
            }
        };

        return new LoggingMeterRegistry(config, Clock.SYSTEM);
    }

}
