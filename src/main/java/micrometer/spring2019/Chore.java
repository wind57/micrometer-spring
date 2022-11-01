package micrometer.spring2019;

import java.time.Duration;

public record Chore(String name, Duration duration, String group) {
}
