package org.klomp.snark;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class RunStandalone {

    private final File warfile;
    private final Server _server;
    private RunStandalone(String args[]) {
        int port = 18009;
        String host = "127.0.0.1";
        if (args.length > 2) {
            port = Integer.parseInt(args[2]);
        } 
        if (args.length > 1) {
            host = args[1];
        }
        if (args.length > 0) {
            warfile = new File(args[0]);
        } else {
            warfile = new File("webapps","i2psnark.war");
        }
        _server = new Server(new InetSocketAddress(host, port));
    }
    
    public static void main(String args[]) {
        RunStandalone runner = new RunStandalone(args);
        runner.start();
    }
    
    public void start() {

        
            
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/snark");
        webapp.setWar(warfile.getAbsolutePath());
        _server.setHandler(webapp);
     
        try {
            _server.start();
        } catch(Exception thrown) {
            thrown.printStackTrace();
        }
    }
    
    public void stop() {
        try {
            _server.stop();
        } catch (Exception ie) {
            ie.printStackTrace();
        }
    }
}
