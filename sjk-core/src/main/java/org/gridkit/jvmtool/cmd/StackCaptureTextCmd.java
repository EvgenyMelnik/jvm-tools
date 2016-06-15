/**
 * Copyright 2014 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.jvmtool.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.gridkit.jvmtool.GlobHelper;
import org.gridkit.jvmtool.JmxConnectionInfo;
import org.gridkit.jvmtool.cli.CommandLauncher;
import org.gridkit.jvmtool.cli.CommandLauncher.CmdRef;
import org.gridkit.jvmtool.cli.TimeIntervalConverter;
import org.gridkit.jvmtool.stacktrace.*;

import javax.management.MBeanServerConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Stack capture command.
 *  
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class StackCaptureTextCmd implements CmdRef {

	@Override
	public String getCommandName() {
		return "stcapt";
	}

	@Override
	public Runnable newCommand(CommandLauncher host) {
		return new StCap(host);
	}

	@Parameters(commandDescription = "[Stack Capture] Dumps stack traces to file for further processing")
    private static class StCap implements Runnable {

		@ParametersDelegate
		private CommandLauncher host;
		
		@Parameter(names = {"-i", "--sampler-interval"}, converter = TimeIntervalConverter.class, description = "Interval between polling MBeans (default = 10000 ms)")
		private long samplerIntervalMS = 10000;
		
		@Parameter(names = {"-f", "--filter"}, description = "Filter threads by name (Java RegEx syntax)")
		private String threadFilter = ".*";

		@Parameter(names = {"-e", "--empty"}, description = "Retain threads without stack trace in dump (ignored by default)")
		private boolean retainEmptyTraces = false;

		@Parameter(names = {"-m", "--match-frame"}, variableArity = true, description = "Frame filter, only traces conatining this string will be included to dump")
		private List<String> frameFilter;

	    @Parameter(names = {"-o", "--output"}, required = true, description = "Name of file to write thread dump to")
	    private String outputFile;

        @Parameter(names = {"-r", "--rotate"}, description = "If specified output file would be rotated every N traces (default = 6)")
        private long rotateCount = 6;

		@ParametersDelegate
		private JmxConnectionInfo connInfo;

		private ThreadDumpSampler sampler;

        //private StackTraceWriter writer;
		
		public StCap(CommandLauncher host) {
			this.host = host;
			this.connInfo = new JmxConnectionInfo(host);
		}
		
		@Override
		public void run() {
			
			try {
				MBeanServerConnection mserver = connInfo.getMServer();
				ThreadMXBean bean = ThreadMXBeanEx.BeanHelper.connectThreadMXBean(mserver);

				sampler = new ThreadDumpSampler();
				sampler.setThreadFilter(threadFilter);
				sampler.connect(bean);

                if (rotateCount == 0) {
                    createOneDump(sampler, 0);
                } else {
                    for (int traceCounter = 1; traceCounter <= rotateCount; traceCounter++) {
                        long nextsample = System.currentTimeMillis() + samplerIntervalMS;
                        // One iteration - one file.
                        createOneDump(sampler, traceCounter);
                        // Delay....
                        if (traceCounter < rotateCount) {
                            checkDelay(nextsample);
                        }
                    }
                }
				System.out.println("Finish!");
				
			} catch (Exception e) {
				host.fail("Unexpected error: " + e.toString(), e);
			}			
		}

        private void createOneDump(ThreadDumpSampler sampler, int dumpFileNumber) throws IOException {
            StackTraceWriter writer = openWriter(dumpFileNumber);
            try {
                sampler.collect(new StackWriterProxy(writer));
                System.out.println("Collected dump");
            } finally {
                writer.close();
            }

        }

        private void checkDelay(long nextsample) throws InterruptedException {
            while (nextsample > System.currentTimeMillis()) {
                long st = nextsample - System.currentTimeMillis();
                if (st > 0) {
                    System.out.println(String.format("Delay %d ms", st));
                    Thread.sleep(st);
                }
            }
        }

        private class StackWriterProxy implements StackTraceWriter {

            private Map<StackFrame, Boolean> elementCache = new HashMap<StackFrame, Boolean>();
            private Matcher[] matchers;
            private StackTraceWriter writer;

            StackWriterProxy(StackTraceWriter writer) {
                this.writer = writer;
                if (frameFilter != null) {
                    matchers = new Matcher[frameFilter.size()];
                    for(int i = 0; i != frameFilter.size(); ++i) {
                        matchers[i] = GlobHelper.translate(frameFilter.get(i), ".").matcher("");
                    }
                }
            }

            @Override
            public void write(ThreadSnapshot snap) throws IOException {
                if (snap.getStackTrace().isEmpty() && !retainEmptyTraces) {
                    return;
                }
                // test filter
                if (frameFilter != null) {
                    boolean match = false;
                    for(StackFrame e: snap.getStackTrace()) {
                        if (match(e)) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        return;
                    }
                }
                writer.write(snap);

            }

            private boolean match(StackFrame e) {
                Boolean cached = elementCache.get(e);
                if (cached == null) {
                    if (elementCache.size() > 4 << 10) {
                        elementCache.clear();
                    }
                    boolean matched = false;
                    for(Matcher m: matchers) {
                        m.reset(e.toString());
                        if (m.lookingAt()) {
                            matched = true;
                            break;
                        }
                    }
                    elementCache.put(e, matched);
                    return matched;
                }
                return cached;
            }

            @Override
            public void close() {
                writer.close();
            }
        }

        private StackTraceWriter openWriter(int fileNumber) throws FileNotFoundException, IOException {
            if (fileNumber < 1) {
                File file = new File(outputFile);
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                System.out.println("Writing to " + file.getAbsolutePath());
                return StackTraceCodec.newWriter(new FileOutputStream(file));
            }
            else {
                int c = outputFile.lastIndexOf('.');
                String pref = c < 0 ? outputFile : outputFile.substring(0, c);
                String suf = c < 0 ? "" : outputFile.substring(c);
                String name = pref + String.format("-%02d", fileNumber) + suf;
                File file = new File(name);
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                System.out.println("Writing to " + file.getAbsolutePath());
                return StackTraceCodec.newWriter(new FileOutputStream(file));
            }
        }
	}
}
