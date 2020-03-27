package io.extremum.mapper.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static io.extremum.test.hamcrest.SameMomentMatcher.atSameMomentAs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BasicJsonObjectMapperTest {
    private static final ZonedDateTime MOMENT_WITH_MICROS = expectedMoment(123_456_000);
    private static final ZonedDateTime MOMENT_WITH_MILLIS = expectedMoment(123_000_000);
    private static final ZonedDateTime MOMENT_WITH_SECONDS = expectedMoment(0);
    private static final ZoneId PLUS_3_HOURS = ZoneId.of("+03:00");

    private final BasicJsonObjectMapper mapper = new BasicJsonObjectMapper();

    private static ZonedDateTime expectedMoment(int nanoOfSecond) {
        return ZonedDateTime.of(2020, 1, 1,
                13, 59, 59,
                nanoOfSecond,
                ZoneId.of("UTC"));
    }

    @Test
    void failsToParseDateTimeWithOffsetInBasicFormatAndMicroseconds() {
        assertParsingFails(() -> mapper.readValue(quoted("2020-01-01T13:59:59.123456+0000"), ZonedDateTime.class));
    }

    @Test
    void failsToParseDateTimeWithOffsetInBasicFormatAndMilliseconds() {
        assertParsingFails(() -> mapper.readValue(quoted("2020-01-01T13:59:59.123+0000"), ZonedDateTime.class));
    }

    private void assertParsingFails(Executable parseAttempt) {
        assertThrows(DateTimeParseException.class, parseAttempt);
    }

    @Test
    void failsToParseDateTimeWithOffsetInBasicFormatAndJustSeconds() {
        assertParsingFails(() -> mapper.readValue(quoted("2020-01-01T13:59:59+0000"), ZonedDateTime.class));
    }

    @Test
    void parsesDateTimeWithOffsetInExtendedFormatAndMicroseconds() throws Exception {
        ZonedDateTime dateTime = mapper.readValue(quoted("2020-01-01T13:59:59.123456+00:00"), ZonedDateTime.class);

        assertThat(dateTime, atSameMomentAs(MOMENT_WITH_MICROS));
    }

    @Test
    void failsToParseDateTimeWithOffsetInExtendedFormatAndMilliseconds() {
        assertParsingFails(() -> mapper.readValue(quoted("2020-01-01T13:59:59.123+00:00"), ZonedDateTime.class));
    }

    @Test
    void failsToParseDateTimeWithOffsetInExtendedFormatAndJustSeconds() {
        assertParsingFails(() -> mapper.readValue(quoted("2020-01-01T13:59:59+00:00"), ZonedDateTime.class));
    }

    @Test
    void parsesDateTimeWithOffsetAsZAndMicroseconds() throws Exception {
        ZonedDateTime dateTime = mapper.readValue(quoted("2020-01-01T13:59:59.123456Z"), ZonedDateTime.class);

        assertThat(dateTime, atSameMomentAs(MOMENT_WITH_MICROS));
    }

    @Test
    void failsToParseDateTimeWithOffsetAsZAndMilliseconds() {
        assertParsingFails(() -> mapper.readValue(quoted("2020-01-01T13:59:59.123Z"), ZonedDateTime.class));
    }

    @Test
    void failsToParseDateTimeWithOffsetAsZAndJustSeconds() {
        assertParsingFails(() -> mapper.readValue(quoted("2020-01-01T13:59:59Z"), ZonedDateTime.class));
    }

    @Test
    void formatsDateTimeWithMicrosecondsInExtendedISO8601Format() throws Exception {
        String formatted = mapper.writeValueAsString(MOMENT_WITH_MICROS);

        assertThat(formatted, equalTo(quoted("2020-01-01T13:59:59.123456Z")));
    }

    @Test
    void formatsDateTimeWithMillisecondsInExtendedISO8601Format() throws Exception {
        String formatted = mapper.writeValueAsString(MOMENT_WITH_MILLIS);

        assertThat(formatted, equalTo(quoted("2020-01-01T13:59:59.123000Z")));
    }

    @Test
    void formatsDateTimeWithJustSecondsInExtendedISO8601Format() throws Exception {
        String formatted = mapper.writeValueAsString(MOMENT_WITH_SECONDS);

        assertThat(formatted, equalTo(quoted("2020-01-01T13:59:59.000000Z")));
    }

    @Test
    void formatsDateTimeWithNonZeroOffsetInExtendedISO8601Format() throws Exception {
        String formatted = mapper.writeValueAsString(MOMENT_WITH_MICROS.withZoneSameLocal(PLUS_3_HOURS));

        assertThat(formatted, equalTo(quoted("2020-01-01T13:59:59.123456+03:00")));
    }

    private String quoted(String str) {
        return "\"" + str + "\"";
    }

    @Test
    void serializesWhenZonedDateTimeIsNull() throws Exception{
        String json = mapper.writeValueAsString(new ZonedDateTimeWrapper());

        assertThat(json, is("{}"));
    }

    @Test
    void parsesWhenZonedDateTimeIsMissing() throws Exception{
        ZonedDateTimeWrapper wrapper = mapper.readValue("{}", ZonedDateTimeWrapper.class);

        assertThat(wrapper.dateTime, is(nullValue()));
    }

    @Test
    void parsesWhenZonedDateTimeIsNull() throws Exception{
        ZonedDateTimeWrapper wrapper = mapper.readValue("{\"dateTime\":null}", ZonedDateTimeWrapper.class);

        assertThat(wrapper.dateTime, is(nullValue()));
    }

    private static class ZonedDateTimeWrapper {
        @JsonProperty
        private ZonedDateTime dateTime;
    }
}