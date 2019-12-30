package io.extremum.sharedmodels.converter;

import io.extremum.sharedmodels.basic.IntegerOrString;
import io.extremum.sharedmodels.spacetime.TimeFrame;
import io.extremum.sharedmodels.spacetime.TimeFrameDocument;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TimeFrameConverterTest {
    private final TimeFrameConverter converter = new TimeFrameConverter();

    private final ZonedDateTime start = ZonedDateTime.now();
    private final ZonedDateTime end = start.plusSeconds(1);
    private final IntegerOrString duration = new IntegerOrString(1000);

    @Test
    void copiesWriteThroughFieldsFromTimeFrameDocumentToTimeFrame() {
        TimeFrameDocument document = new TimeFrameDocument();
        document.setStart(start);
        document.setEnd(end);
        document.setDuration(duration);

        TimeFrame timeFrame = converter.documentToDto(document);

        assertThat(timeFrame.getStart(), is(start));
        assertThat(timeFrame.getEnd(), is(end));
        assertThat(timeFrame.getDuration(), is(duration));
    }

    @Test
    void copiesWriteThroughFieldsFromTimeFrameToTimeFrameDocument() {
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setStart(start);
        timeFrame.setEnd(end);
        timeFrame.setDuration(duration);

        TimeFrameDocument document = converter.dtoToDocument(timeFrame);

        assertThat(document.getStart(), is(start));
        assertThat(document.getEnd(), is(end));
        assertThat(document.getDuration(), is(duration));
    }

    @Test
    void copiesDurationMillisFromTimeFrameToTimeFrameDocumentIfMillisArePresent() {
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setDuration(new IntegerOrString(500));

        TimeFrameDocument document = converter.dtoToDocument(timeFrame);

        assertThat(document.getDurationMillis(), is(500));
    }

    @Test
    void convertsDurationMillisFromTimeFrameToTimeFrameDocumentIfDurationStringIsPresent() {
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setDuration(new IntegerOrString("5h"));

        TimeFrameDocument document = converter.dtoToDocument(timeFrame);

        int fiveHoursInMillis = (int) Duration.ofHours(5).toMillis();
        assertThat(document.getDurationMillis(), equalTo(fiveHoursInMillis));
    }

}