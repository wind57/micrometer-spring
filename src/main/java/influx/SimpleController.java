package influx;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/sample")
public class SimpleController {

    private String color = "initial";

    @RequestMapping(value = "/create/host1", method = RequestMethod.GET)
    public void test1() {
        if ("initial".equals(color)) {
            color = "a";
        }
        else if ("a".equals(color)) {
            color = "b";
        }
        else if ("b".equals(color)) {
            color = "c";
        }
        else if ("c".equals(color)) {
            color = "initial";
        }

        long l = ThreadLocalRandom.current().nextLong(10, 100);

        System.out.println("host : host1, color : " + color);
        Metrics.timer("my.timer", List.of(
                Tag.of("host", "host1"),
                Tag.of("color", color)
        )).record(l, TimeUnit.MILLISECONDS);

        if ("c".equals(color)) {
            color = "initial";
        }
    }

    @RequestMapping(value = "/create/host2", method = RequestMethod.GET)
    public void test2() {
        if ("initial".equals(color)) {
            color = "a";
        }
        else if ("a".equals(color)) {
            color = "b";
        }
        else if ("b".equals(color)) {
            color = "c";
        }
        else if ("c".equals(color)) {
            color = "initial";
        }

        long l = ThreadLocalRandom.current().nextLong(10, 100);

        System.out.println("host : host2, color : " + color);
        Metrics.timer("my.timer", List.of(
                Tag.of("host", "host2"),
                Tag.of("color", color)
        )).record(l, TimeUnit.MILLISECONDS);

        if ("c".equals(color)) {
            color = "initial";
        }
    }

}
