package org.gridkit.jvmtool.stacktrace.analytics;

import org.gridkit.jvmtool.stacktrace.ThreadSnapshot;

import java.util.TimeZone;

public class TimeRangeFilter implements ThreadSnapshotFilter {

    private TimeRangeChecker checker;
    
    public TimeRangeFilter(String lower, String upper, TimeZone tz) {
        this.checker = new TimeRangeChecker(lower, upper, tz);
    }

    @Override
    public boolean evaluate(ThreadSnapshot snapshot) {
        return checker.evaluate(snapshot.getTimestamp());
    }
}
