package org.gridkit.jvmtool.stacktrace;

import java.lang.Thread.State;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.Iterator;

public class ThreadCapture implements ThreadSnapshot {

    protected long threadId;
    protected String threadName;
    protected long timestamp;
    protected StackTraceElement[] elements;
    protected CounterArray counters = new CounterArray();
    protected State state;
    
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

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    @Override
    public StackFrameList getStackTrace() {
        return new StackProxy(elements);
    }

    @Override
    public State getThreadState() {
        return state;
    }

    @Override
    public CounterArray getCounters() {
        return counters;
    }

    public StackTraceElement[] getElements() {
        return elements;
    }

    public void setElements(StackTraceElement[] elements) {
        this.elements = elements;
    }

    void copyFrom(ThreadInfo info) {
        threadId = info.getThreadId();
        threadName = info.getThreadName();
        elements = info.getStackTrace();
        state = info.getThreadState();

        if (info.getBlockedCount() > 0) {
            counters.set(ThreadCounters.BLOCKED_COUNTER, info.getBlockedCount());
        }

        if (info.getBlockedTime() > 0) {
            counters.set(ThreadCounters.BLOCKED_TIME_MS, info.getBlockedTime());
        }

        if (info.getWaitedCount() > 0) {
            counters.set(ThreadCounters.WAIT_COUNTER, info.getWaitedCount());
        }

        if (info.getWaitedTime() > 0) {
            counters.set(ThreadCounters.WAIT_TIME_MS, info.getWaitedTime());
        }
    }

    public void reset() {
        threadId = -1;
        threadName = null;
        timestamp = -1;
        elements = null;
        counters.reset();
        state = null;
    }
    
    private class StackProxy implements StackFrameList {

        StackTraceElement[] stack;
        
        StackProxy(StackTraceElement[] stack) {
            this.stack = stack;
        }

        @Override
        public Iterator<StackFrame> iterator() {
            final Iterator<StackTraceElement> it = Arrays.asList(stack).iterator();
            return new Iterator<StackFrame>() {

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public StackFrame next() {
                    return new StackFrameWrapper(it.next());
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }

        @Override
        public StackFrame frameAt(int n) {
            return new StackFrameWrapper(stack[n]);
        }

        @Override
        public int depth() {
            return stack.length;
        }

        @Override
        public StackFrameList fragment(int from, int to) {
            throw new UnsupportedOperationException();
        }

        @Override
        public StackFrame[] toArray() {
            StackFrame[] frames = new StackFrame[stack.length];
            for(int i = 0; i !=  stack.length; ++i) {
                frames[i] = new StackFrameWrapper(stack[i]);
            }
            return frames;
        }

        @Override
        public boolean isEmpty() {
            return stack.length == 0;
        }
    }
    
    private static class StackFrameWrapper extends StackFrame {

        StackTraceElement ste;
        
        StackFrameWrapper(StackTraceElement ste) {
            super(ste);
            this.ste = ste;
        }

        @Override
        public StackTraceElement toStackTraceElement() {
            return ste;
        }
    }
}
