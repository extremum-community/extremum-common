package io.extremum.sharedmodels.converter;

import io.extremum.sharedmodels.spacetime.TimeFrame;
import io.extremum.sharedmodels.spacetime.TimeFrameDocument;

public class TimeFrameConverter {
    public TimeFrame documentToDto(TimeFrameDocument document) {
        TimeFrame timeFrame = new TimeFrame();

        timeFrame.setStart(document.getStart());
        timeFrame.setEnd(document.getEnd());
        timeFrame.setDuration(document.getDuration());

        return timeFrame;
    }

    public TimeFrameDocument dtoToDocument(TimeFrame timeFrame) {
        TimeFrameDocument document = new TimeFrameDocument();

        document.setStart(timeFrame.getStart());
        document.setEnd(timeFrame.getEnd());
        document.setDuration(timeFrame.getDuration());
        if (timeFrame.getDuration() != null) {
            if (timeFrame.getDuration().getIntegerValue() != null) {
                document.setDurationMillis(timeFrame.getDuration().getIntegerValue());
            } else if (timeFrame.getDuration().getStringValue() != null) {
                document.setDurationMillis((int) timeFrame.javaDuration().toMillis());
            }
        }

        return document;
    }
}
