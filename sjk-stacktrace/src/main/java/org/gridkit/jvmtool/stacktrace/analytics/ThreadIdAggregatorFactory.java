package org.gridkit.jvmtool.stacktrace.analytics;

import org.gridkit.jvmtool.stacktrace.ThreadSnapshot;

class ThreadIdAggregatorFactory implements ThreadDumpAggregator, ThreadDumpAggregatorFactory {

    @Override
    public ThreadDumpAggregator newInstance() {
        return new ThreadIdAggregatorFactory();
    }

    long id = -1;
    
    @Override
    public void aggregate(ThreadSnapshot threadInfo) {
        id = threadInfo.getThreadId();
    }

    @Override
    public Object info() {
        return id;
    }
}
