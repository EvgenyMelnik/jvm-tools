package org.gridkit.jvmtool.stacktrace;

import java.lang.Thread.State;

public class ThreadRecord implements ThreadSnapshot {

    private long threadId = -1;
    private String threadName;
    private long timestamp = -1;
    private StackFrameList stackTrace;
    private State threadState;

    public ThreadRecord() {
    }

    public ThreadRecord(StackFrameList trace) {
        stackTrace = trace;
    }

    public ThreadRecord(StackFrameList trace, State state) {
        stackTrace = trace;
        threadState = state;
    }
    
    public void reset() {
        threadId = -1;
        threadName = null;
        timestamp = -1;
        stackTrace = null;
        threadState = null;             
    }
    
    public void load(StackTraceReader reader) {
        reset();
        threadId = reader.getThreadId();
        threadName = reader.getThreadName();
        stackTrace = reader.getStackTrace();
        threadState = reader.getThreadState();        
    }
    
    @Override
    public long getThreadId() {
        return threadId;
    }

    @Override
    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {

    }

    @Override
    public StackFrameList getStackTrace() {
        return stackTrace;
    }
    
    @Override
    public State getThreadState() {
        return threadState;
    }

    @Override
    public CounterArray getCounters() {
        return CounterArray.EMPTY;
    }
}
