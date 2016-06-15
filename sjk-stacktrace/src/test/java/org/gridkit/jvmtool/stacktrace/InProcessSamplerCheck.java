package org.gridkit.jvmtool.stacktrace;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public class InProcessSamplerCheck {

    @Test
    public void testLocalAccess() throws IOException {
        ThreadDumpSampler sampler = new ThreadDumpSampler();
        sampler.connect(ManagementFactory.getThreadMXBean());
        sampler.collect(new StackTraceWriter() {
            
            @Override
            public void write(ThreadSnapshot snap) throws IOException {
                Assert.assertTrue(snap.getCounters().getValue(ThreadCounters.ALLOCATED_BYTES) >= 0);
            }
            
            @Override
            public void close() {
            }
        });
    }
    
}
