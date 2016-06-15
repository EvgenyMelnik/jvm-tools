package org.gridkit.jvmtool.stacktrace.analytics;

import org.gridkit.jvmtool.stacktrace.ThreadCounters;
import org.gridkit.jvmtool.stacktrace.ThreadSnapshot;

import java.util.HashMap;
import java.util.Map;

class AllocAggregatorFactory implements ThreadDumpAggregator, ThreadDumpAggregatorFactory {

    @Override
    public ThreadDumpAggregator newInstance() {
        return new AllocAggregatorFactory();
    }

    private Map<Long, ThreadTrack> info = new HashMap<Long, AllocAggregatorFactory.ThreadTrack>(); 
    
    @Override
    public void aggregate(ThreadSnapshot threadInfo) {
        if (threadInfo.getCounters().getValue(ThreadCounters.ALLOCATED_BYTES) != Long.MIN_VALUE) {
            ThreadTrack tt = info.get(threadInfo.getThreadId());
            if (tt == null) {
                tt = new ThreadTrack();
                tt.firstTimestamp = threadInfo.getTimestamp();
                tt.lastTimestamp = threadInfo.getTimestamp();
                tt.fisrtAlloc = threadInfo.getCounters().getValue(ThreadCounters.ALLOCATED_BYTES);
                tt.lastAlloc = threadInfo.getCounters().getValue(ThreadCounters.ALLOCATED_BYTES);
                info.put(threadInfo.getThreadId(), tt);
            }
            else {
                if (tt.firstTimestamp > threadInfo.getTimestamp()) {
                    tt.firstTimestamp = threadInfo.getTimestamp();
                    tt.fisrtAlloc = threadInfo.getCounters().getValue(ThreadCounters.ALLOCATED_BYTES);
                }
                if (tt.lastTimestamp < threadInfo.getTimestamp()) {
                    tt.lastTimestamp = threadInfo.getTimestamp();
                    tt.lastAlloc = threadInfo.getCounters().getValue(ThreadCounters.ALLOCATED_BYTES);
                }
            }
        }
    }

    @Override
    public Object info() {
        long totalAlloc = 0;
        long minTs = Long.MAX_VALUE;
        long maxTs = Long.MIN_VALUE;
        for(ThreadTrack tt: info.values()) {
            totalAlloc += tt.lastAlloc - tt.fisrtAlloc;
            minTs = Math.min(minTs, tt.firstTimestamp);
            maxTs = Math.max(maxTs, tt.lastTimestamp);
        }
        if (minTs == Long.MAX_VALUE || minTs == maxTs) {
            return Double.NaN;
        }
        else {
            return (((double)totalAlloc) / (maxTs - minTs)) * 1000d;
        }
    }
    
    private static class ThreadTrack {
        
        long firstTimestamp;
        long fisrtAlloc;

        long lastTimestamp;
        long lastAlloc;
    }
}
