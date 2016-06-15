package org.gridkit.jvmtool.stacktrace;

import java.lang.Thread.State;

public class ReaderProxy implements ThreadSnapshot {
    
    protected StackTraceReader reader;

    public ReaderProxy(StackTraceReader reader) {
        super();
        this.reader = reader;
    }

    protected StackTraceReader getReader() {
        return reader;
    }
    
    @Override
    public long getThreadId() {
        return getReader().getThreadId();
    }

    @Override
    public void setThreadId(long threadId) {
        //getReader().
    }

    @Override
    public String getThreadName() {
        return getReader().getThreadName();
    }

    @Override
    public long getTimestamp() {
        return getReader().getTimestamp();
    }

    @Override
    public void setTimestamp(long timestamp) {

    }

    @Override
    public StackFrameList getStackTrace() {
        return getReader().getStackTrace();
    }

    @Override
    public State getThreadState() {
        return getReader().getThreadState();
    }

    @Override
    public CounterArray getCounters() {
        return getReader().getCounters();
    }
}
