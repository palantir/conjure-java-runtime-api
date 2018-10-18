/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.java.api.config.service;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.SafeArg;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HumanReadableDuration implements Comparable<HumanReadableDuration>, TemporalAmount {
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(\\S+)");

    private static final Map<String, TimeUnit> SUFFIXES = createSuffixes();

    private static Map<String, TimeUnit> createSuffixes() {
        Map<String, TimeUnit> suffixes = new HashMap<>();
        suffixes.put("ns", TimeUnit.NANOSECONDS);
        suffixes.put("nanosecond", TimeUnit.NANOSECONDS);
        suffixes.put("nanoseconds", TimeUnit.NANOSECONDS);
        suffixes.put("us", TimeUnit.MICROSECONDS);
        suffixes.put("microsecond", TimeUnit.MICROSECONDS);
        suffixes.put("microseconds", TimeUnit.MICROSECONDS);
        suffixes.put("ms", TimeUnit.MILLISECONDS);
        suffixes.put("millisecond", TimeUnit.MILLISECONDS);
        suffixes.put("milliseconds", TimeUnit.MILLISECONDS);
        suffixes.put("s", TimeUnit.SECONDS);
        suffixes.put("second", TimeUnit.SECONDS);
        suffixes.put("seconds", TimeUnit.SECONDS);
        suffixes.put("m", TimeUnit.MINUTES);
        suffixes.put("minute", TimeUnit.MINUTES);
        suffixes.put("minutes", TimeUnit.MINUTES);
        suffixes.put("h", TimeUnit.HOURS);
        suffixes.put("hour", TimeUnit.HOURS);
        suffixes.put("hours", TimeUnit.HOURS);
        suffixes.put("d", TimeUnit.DAYS);
        suffixes.put("day", TimeUnit.DAYS);
        suffixes.put("days", TimeUnit.DAYS);
        return suffixes;
    }

    public static HumanReadableDuration nanoseconds(long count) {
        return new HumanReadableDuration(count, TimeUnit.NANOSECONDS);
    }

    public static HumanReadableDuration microseconds(long count) {
        return new HumanReadableDuration(count, TimeUnit.MICROSECONDS);
    }

    public static HumanReadableDuration milliseconds(long count) {
        return new HumanReadableDuration(count, TimeUnit.MILLISECONDS);
    }

    public static HumanReadableDuration seconds(long count) {
        return new HumanReadableDuration(count, TimeUnit.SECONDS);
    }

    public static HumanReadableDuration minutes(long count) {
        return new HumanReadableDuration(count, TimeUnit.MINUTES);
    }

    public static HumanReadableDuration hours(long count) {
        return new HumanReadableDuration(count, TimeUnit.HOURS);
    }

    public static HumanReadableDuration days(long count) {
        return new HumanReadableDuration(count, TimeUnit.DAYS);
    }

    @JsonCreator
    public static HumanReadableDuration valueOf(String duration) {
        final Matcher matcher = DURATION_PATTERN.matcher(duration);
        Preconditions.checkArgument(matcher.matches(), "Invalid duration", SafeArg.of("duration", duration));

        final long count = Long.parseLong(matcher.group(1));
        final TimeUnit unit = SUFFIXES.get(matcher.group(2));
        if (unit == null) {
            throw new IllegalArgumentException("Invalid duration: " + duration + ". Wrong time unit");
        }

        return new HumanReadableDuration(count, unit);
    }

    private final long count;
    private final TimeUnit unit;

    private HumanReadableDuration(long count, TimeUnit unit) {
        this.count = count;
        this.unit = Preconditions.checkNotNull(unit, "unit must not be null");
    }

    public long getQuantity() {
        return count;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public long toNanoseconds() {
        return TimeUnit.NANOSECONDS.convert(count, unit);
    }

    public long toMicroseconds() {
        return TimeUnit.MICROSECONDS.convert(count, unit);
    }

    public long toMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(count, unit);
    }

    public long toSeconds() {
        return TimeUnit.SECONDS.convert(count, unit);
    }

    public long toMinutes() {
        return TimeUnit.MINUTES.convert(count, unit);
    }

    public long toHours() {
        return TimeUnit.HOURS.convert(count, unit);
    }

    public long toDays() {
        return TimeUnit.DAYS.convert(count, unit);
    }

    public Duration toJavaDuration() {
        return Duration.of(count, chronoUnit(unit));
    }

    /**
     * Converts a {@code TimeUnit} to a {@code ChronoUnit}.
     * <p>
     * This handles the seven units declared in {@code TimeUnit}.
     *
     * @param unit the unit to convert, not null
     * @return the converted unit, not null
     * @implNote This method can be removed in JDK9
     * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8141452">JDK-8141452</a>
     */
    private static ChronoUnit chronoUnit(TimeUnit unit) {
        Objects.requireNonNull(unit, "unit");
        switch (unit) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            default:
                throw new IllegalArgumentException("Unknown TimeUnit constant");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final HumanReadableDuration duration = (HumanReadableDuration) obj;
        return (count == duration.count) && (unit == duration.unit);

    }

    @Override
    public int hashCode() {
        return (31 * (int) (count ^ (count >>> 32))) + unit.hashCode();
    }

    @Override
    @JsonValue
    public String toString() {
        String units = unit.toString().toLowerCase(Locale.ENGLISH);
        if (count == 1) {
            units = units.substring(0, units.length() - 1);
        }
        return Long.toString(count) + ' ' + units;
    }

    @Override
    public int compareTo(HumanReadableDuration other) {
        if (unit == other.unit) {
            return Long.compare(count, other.count);
        }

        return Long.compare(toNanoseconds(), other.toNanoseconds());
    }

    @Override
    public long get(TemporalUnit unit) {
        return toJavaDuration().get(unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return toJavaDuration().getUnits();
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return toJavaDuration().addTo(temporal);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return toJavaDuration().subtractFrom(temporal);
    }
}
