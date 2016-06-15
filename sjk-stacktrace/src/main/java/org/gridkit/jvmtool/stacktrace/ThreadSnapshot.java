package org.gridkit.jvmtool.stacktrace;

import java.lang.Thread.State;

public interface ThreadSnapshot {

    long getThreadId();
    void setThreadId(long threadId);

    /** may be <code>null</code> */
    String getThreadName();
    
    long getTimestamp();
    void setTimestamp(long timestamp);

    public StackFrameList getStackTrace();
    
    /** may be <code>null</code> */
    public State getThreadState();
    
    public CounterArray getCounters();
        
}
