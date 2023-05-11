package influx;

import io.micrometer.core.instrument.Metrics;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CounterController {

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public void counter() {
        Metrics.counter("my.counter").increment();
    }

}
