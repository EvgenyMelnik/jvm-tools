package org.gridkit.jvmtool.stacktrace.analytics;

import org.gridkit.jvmtool.stacktrace.*;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FilterParserMatchingTest {

    static class Trace {
        
        String name;
        List<StackFrame> frame = new ArrayList<StackFrame>();
        
        public Trace(String name) {
            this.name = name;
        }
        
        public Trace t(String trace) {
            if (trace.indexOf('(') > 0) {
                frame.add(StackFrame.parseTrace(trace));
            }
            else {
                frame.add(StackFrame.parseTrace(trace + "(X.java)"));
            }
            return this;
        }
        
        public StackFrameList frameList() {
            ArrayList<StackFrame> list = new ArrayList<StackFrame>(frame);
            Collections.reverse(list);
            StackFrame[] array = list.toArray(new StackFrame[0]);
            return new StackFrameArray(array);
        }
        
        public String toString() {
            return name;
        }
    }
    
    private static List<Object[]> cases = new ArrayList<Object[]>();

    public static final Trace TRACE_A = new Trace("TRACE_A")
    .t("com.acme.MyClass")
    .t("test.MyBean.init(MyBean.java:100)")
    .t("com.acme.MyClass$1");

    public static final Trace TRACE_B1 = new Trace("TRACE_B1")
    .t("test.server.Handler.run")
    .t("test.server.Handler.process(X.java:123)")
    .t("test.myapp.security.Filter.process")
    .t("test.framework.Rederer.execute")
    .t("test.framework.Bijector.invoke")
    .t("test.framework.Bijector.proceed")
    .t("test.myapp.app.MyBean.doStuff")
    .t("test.framework.Bijector.invoke")
    .t("test.framework.Bijector.doChecks");

    public static final Trace TRACE_B2 = new Trace("TRACE_B2")
    .t("test.server.Handler.run")
    .t("test.server.Handler.process(X.java:123)")
    .t("test.myapp.security.Filter.process")
    .t("test.framework.Rederer.execute")
    .t("test.framework.Bijector.invoke")
    .t("test.framework.Bijector.proceed")
    .t("test.myapp.app.MyBean.doStuff")
    .t("test.framework.Bijector.invoke")
    .t("test.framework.Bijector.proceed")
    .t("test.myapp.app.MyBean.doStuff");

    public static final Trace TRACE_B3 = new Trace("TRACE_B3")
    .t("test.server.Handler.run")
    .t("test.server.Handler.process(X.java:123)")
    .t("test.myapp.security.Filter.process")
    .t("test.framework.Rederer.execute")
    .t("test.framework.Bijector.invoke")
    .t("test.framework.Bijector.proceed")
    .t("test.myapp.app.MyBean.doStuff")
    .t("test.framework.Bijector.invoke")
    .t("test.framework.Bijector.doChecks")
    .t("javax.jdbc.Something");

    public static final Trace TRACE_B4 = new Trace("TRACE_B4")
    .t("test.server.Handler.run")
    .t("test.server.Handler.process(X.java:123)")
    .t("test.myapp.security.Filter.process")
    .t("test.framework.Rederer.execute")
    .t("test.framework.Bijector.invoke")
    .t("test.framework.Bijector.proceed")
    .t("test.myapp.app.MyBean.doStuff")
    .t("test.framework.Syncjector.invoke")
    .t("test.framework.Syncjector.doChecks");

    public static final Trace TRACE_B5 = new Trace("TRACE_B5")
    .t("test.server.Handler.run")
    .t("test.server.Handler.process(X.java:128)") // differnt line number
    .t("test.myapp.security.Filter.process")
    .t("test.framework.Rederer.execute")
    .t("test.framework.Bijector.invoke")
    .t("test.framework.Bijector.proceed")
    .t("test.myapp.app.MyBean.doStuff")
    .t("test.framework.Syncjector.invoke")
    .t("test.framework.Syncjector.doChecks");

    
    @Parameters(name = "\"{0}\" {1} {2}")
    public static List<Object[]> getExpressions() {
        caseMatch   (TRACE_A, "**.acme.**");
        caseMatch   (TRACE_A, "garbage, garbage,**.acme.**");
        caseMatch   (TRACE_A, "**.acme.**,garbage, garbage");
        caseMatch   (TRACE_A, "garbage,**.acme.**,garbage");
        caseMatch   (TRACE_A, "**.acme.*");
        caseMatch   (TRACE_A, "**.MyCla");
        caseMatch   (TRACE_A, "**.MyClass$1");
        caseMatch   (TRACE_A, "**.*$1");
        caseNonMatch(TRACE_A, "*.MyClass");
        caseNonMatch(TRACE_A, "MyClass");
        caseMatch   (TRACE_A, "**.MyBean.init*.java:100");
        caseMatch   (TRACE_A, "**.MyBean.init**:100");
        caseMatch   (TRACE_A, "**.MyBean.init*:100"); // special case
        caseNonMatch(TRACE_A, "**.MyBean*:100"); // special case
        caseMatch   (TRACE_A, "**.MyBean**:100");
        
        String smartMatch = "(**.Handler.process*.java:123)!(javax.jdbc,test.jbbc)+(**.Bijector.invoke,**.Syncjector.invoke/!(**.proceed))";
        caseMatch   (TRACE_B1, smartMatch);
        caseNonMatch(TRACE_B2, smartMatch);
        caseNonMatch(TRACE_B3, smartMatch);
        caseMatch   (TRACE_B4, smartMatch);
        caseNonMatch(TRACE_B5, smartMatch);
        
        return cases;
    }

    private static void caseMatch(Trace trace, String filter) {
        cases.add(new Object[]{filter, trace, true});
    }

    private static void caseNonMatch(Trace trace, String filter) {
        cases.add(new Object[]{filter, trace, false});
    }
    
    
    private String expression;
    private Trace trace;
    private boolean match;
    
    public FilterParserMatchingTest(String expression, Trace trace, boolean match) {
        this.expression = expression;
        this.trace = trace;
        this.match = match;
    }
    
    @Test
    public void match() {
        ThreadSnapshotFilter f = TraceFilterPredicateParser.parseFilter(expression, new BasicFilterFactory());
        if (match) {
            Assert.assertTrue("Should match", f.evaluate(trace()));
        }
        else {
            Assert.assertFalse("Should not match", f.evaluate(trace()));
        }
    }

    private ThreadSnapshot trace() {
        return new ThreadSnapshot() {
            
            @Override
            public long getTimestamp() {
                return 0;
            }

            @Override
            public void setTimestamp(long timestamp) {

            }

            @Override
            public State getThreadState() {
                return null;
            }
            
            @Override
            public String getThreadName() {
                return null;
            }
            
            @Override
            public long getThreadId() {
                return 0;
            }

            @Override
            public void setThreadId(long threadId) {

            }

            @Override
            public StackFrameList getStackTrace() {
                return trace.frameList();
            }
            
            @Override
            public CounterArray getCounters() {
                return null;
            }
        };
    }
    
    
}
