package org.gridkit.jvmtool.stacktrace.analytics;

import org.gridkit.jvmtool.stacktrace.ThreadSnapshot;

import java.lang.Thread.State;

class ThreadStateAggregatorFactory implements ThreadDumpAggregator, ThreadDumpAggregatorFactory {

    private final State state;
    
    public ThreadStateAggregatorFactory(State state) {
        this.state = state;
    }
    
    @Override
    public ThreadDumpAggregator newInstance() {
        return new ThreadStateAggregatorFactory(state);
    }

    long total;
    long matched;
    
    @Override
    public void aggregate(ThreadSnapshot threadInfo) {
        ++total;
        if (threadInfo.getThreadState() == state) {
            ++matched;
        }
    }

    @Override
    public Object info() {
        return ((double)matched) / total;
    }
}
