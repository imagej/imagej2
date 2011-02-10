/*
 * u.java
 *
 * Created on March 28, 2006, 5:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagej.util;

import java.io.*;
import java.util.List;

/**
 *
 * @author joshy
 */
public class u {
    private static PrintStream wrt;
    private static boolean useWrt = false;
    
    public static void p(String str) {
        if(useWrt && wrt == null) {
            try {
                wrt = new PrintStream(new FileOutputStream(System.getProperty("user.home")+
                        File.separator+"testlog.log",true));
            } catch (Throwable thr) {
                thr.printStackTrace();
                wrt = null;
                useWrt = false;
            }
        }
        
        System.out.println(str);
        System.out.flush();
        if(wrt != null) {
            wrt.println(str);
            wrt.flush();
        }
    }
    
    public static void p(Object[] arr) {
        p("array: " + arr);
        if(arr == null) return;
        for(Object o : arr) {
            p(o);
        }
    }
    public static void p(double[] arr) {
        p("double array: " + arr);
        if(arr == null) return;
        for(double d : arr) {
            p(d);
        }
    }
    public static void p(List headerPainters) {
        p("list: " + headerPainters);
        for(Object o : headerPainters) {
            p(o);
        }
    }
    
    public static void p(Object o) {
        p(""+o);
    }
    public static void p(Exception ex) {
        p(ex.getClass().getName() + ": " + ex.getMessage());
        ex.printStackTrace();
    }
    
    public static void pr(String string) {
        System.out.print(string);
    }

    
    public static String fileToString(InputStream in) throws IOException {
        Reader reader = new InputStreamReader(in);
        StringWriter writer = new StringWriter();
        char[] buf = new char[1024];
        while(true) {
            int n = reader.read(buf);
            if(n == -1) {
                break;
            }
            writer.write(buf,0,n);
        }
        return writer.toString();
    }
    
    
    public static void stringToFile(String text, File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        StringReader reader = new StringReader(text);
        char[] buf = new char[1000];
        while(true) {
            int n = reader.read(buf,0,1000);
            if(n == -1) {
                break;
            }
            writer.write(buf,0,n);
        }
        writer.flush();
        writer.close();
    }
    
    public static void streamToFile(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        while(true) {
            int n = in.read(buf);
            if(n == -1) {
                break;
            }
            out.write(buf,0,n);
        }
        out.close();
    }
    
    private static long time;
    public static void startTimer() {
        time = System.currentTimeMillis();
    }
    
    public static void stopTimer() {
        long stoptime = System.currentTimeMillis();
        p("stopped: " + (stoptime - time));
    }
    
    public static void sleep(long msec) {
        try {
            Thread.currentThread().sleep(msec);
        } catch (InterruptedException ex) {
            p(stack_to_string(ex));
        }
    }
    
    public static String stack_to_string(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
    
    public static void dumpStack() {
        p(stack_to_string(new Exception()));
    }
    
    public static boolean betweenInclusive(int lower, int testValue, int upper) {
        if(testValue >= lower && testValue <= upper) {
            return true;
        }
        return false;
    }

    public static void printFullStackTrace(Throwable th) {
        p("exception: " + th.getMessage());
        for(StackTraceElement e :  th.getStackTrace()) {
            p("   "+e);
        }
        if(th.getCause() != null) {
            printFullStackTrace(th.getCause());
        }
    }
}
