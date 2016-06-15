package org.gridkit.jvmtool.stacktrace;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.Iterator;

public class ThreadExCapture extends ThreadCapture {

    private LockInfo lock;
    private boolean isNative;
    private MonitorInfo[] lockedMonitors;
    private LockInfo[] lockedSynchronizers;

    public LockInfo getLock() {
        return lock;
    }

    public LockInfo[] getLockedSynchronizers() {
        return lockedSynchronizers;
    }

    public MonitorInfo[] getLockedMonitors() {
        return lockedMonitors;
    }

    public boolean isNative() {
        return isNative;
    }

    public void copyFrom(ThreadInfo info) {
        threadId = info.getThreadId();
        threadName = info.getThreadName();
        elements = info.getStackTrace();
        state = info.getThreadState();
        lock = info.getLockInfo();
        isNative = info.isInNative();
        lockedMonitors = info.getLockedMonitors();
        lockedSynchronizers = info.getLockedSynchronizers();

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
        lock = null;
        isNative = false;
        lockedMonitors = null;
        lockedSynchronizers = null;
    }
    
    class StackProxy implements StackFrameList {

        StackTraceElement[] stack;
        
        public StackProxy(StackTraceElement[] stack) {
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
    
    static class StackFrameWrapper extends StackFrame {

        StackTraceElement ste;
        
        public StackFrameWrapper(StackTraceElement ste) {
            super(ste);
            this.ste = ste;
        }

        @Override
        public StackTraceElement toStackTraceElement() {
            return ste;
        }
    }
}
