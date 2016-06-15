package org.gridkit.jvmtool.stacktrace.analytics;

import org.gridkit.jvmtool.stacktrace.ThreadSnapshot;

class FrequencyHMAggregatorFactory implements ThreadDumpAggregator, ThreadDumpAggregatorFactory {

    @Override
    public ThreadDumpAggregator newInstance() {
        return new FrequencyHMAggregatorFactory();
    }
    
    
    long count = 0;
    double reciproicalTotal = 0;
    long lastTimestamp = Long.MAX_VALUE;

    @Override
    public void aggregate(ThreadSnapshot threadInfo) {
        if (threadInfo.getTimestamp() > lastTimestamp) {
            long delta = threadInfo.getTimestamp() - lastTimestamp;
            double rcp = 1000d / delta;
            count += 1;
            reciproicalTotal += rcp;
        }
        lastTimestamp = threadInfo.getTimestamp();
    }

    @Override
    public Object info() {
        if (count > 0) {
            return reciproicalTotal / ((double)count);
        }
        else {
            return Double.NaN;
        }
    }    
}
