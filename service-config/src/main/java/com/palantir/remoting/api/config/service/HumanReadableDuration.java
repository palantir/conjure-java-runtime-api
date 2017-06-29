/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
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

package com.palantir.remoting.api.config.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HumanReadableDuration implements Comparable<HumanReadableDuration> {
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(\\S+)");

    private static final ImmutableMap<String, TimeUnit> SUFFIXES = new ImmutableMap.Builder<String, TimeUnit>()
            .put("ns", TimeUnit.NANOSECONDS)
            .put("nanosecond", TimeUnit.NANOSECONDS)
            .put("nanoseconds", TimeUnit.NANOSECONDS)
            .put("us", TimeUnit.MICROSECONDS)
            .put("microsecond", TimeUnit.MICROSECONDS)
            .put("microseconds", TimeUnit.MICROSECONDS)
            .put("ms", TimeUnit.MILLISECONDS)
            .put("millisecond", TimeUnit.MILLISECONDS)
            .put("milliseconds", TimeUnit.MILLISECONDS)
            .put("s", TimeUnit.SECONDS)
            .put("second", TimeUnit.SECONDS)
            .put("seconds", TimeUnit.SECONDS)
            .put("m", TimeUnit.MINUTES)
            .put("minute", TimeUnit.MINUTES)
            .put("minutes", TimeUnit.MINUTES)
            .put("h", TimeUnit.HOURS)
            .put("hour", TimeUnit.HOURS)
            .put("hours", TimeUnit.HOURS)
            .put("d", TimeUnit.DAYS)
            .put("day", TimeUnit.DAYS)
            .put("days", TimeUnit.DAYS)
            .build();

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
        checkArgument(matcher.matches(), "Invalid duration: %s", duration);

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
        this.unit = checkNotNull(unit);
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
}
