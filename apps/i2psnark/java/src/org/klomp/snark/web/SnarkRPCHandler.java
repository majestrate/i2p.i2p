package org.klomp.snark.web;

import java.io.IOException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.i2p.I2PAppContext;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.klomp.snark.SnarkManager;
import org.klomp.snark.web.rpc.RPCMethod;

/**
 * handles transmission-rpc requests
 * @see https://trac.transmissionbt.com/browser/trunk/extras/rpc-spec.txt 
 * @author jeff
 *
 */
class SnarkRPCHandler {

    private static final String TRPC_SESSION_HEADER = "X-Transmission-Session-Id";

    /**
     * contains rpc session information 
     * @author jeff
     *
     */
    private class RPCSession {
        static final long DEFAULT_SESSION_DURATION = 60 * 1000;
        /** when this session expires */;
        final long expires_at;
    
        /**
         * @param sessionDuration how long should this session last?
         */
        RPCSession(long sessionDuration) {
            expires_at = new Date().getTime() + sessionDuration;
        }
    
        RPCSession() {
            this(DEFAULT_SESSION_DURATION);
        }
    
        boolean expired() {
            return new Date().getTime() >= expires_at;
        }
    }
  
    /** 
     * active sessions
     * maps session id -> session info 
     **/
    private final Map<String, RPCSession> _sessions; 
    /** i2p app context from snark */
    private transient final I2PAppContext _context;
    /** transmissionrpc method handlers */
    private final Map<String, RPCMethod> _methods;
  
    /** construct */
    SnarkRPCHandler(I2PAppContext context) {
        _context = context;
        _sessions = new ConcurrentHashMap<String, RPCSession>();
        _methods = new ConcurrentHashMap<String, RPCMethod>();
    }
  
    private void addMethod(RPCMethod method) {
        _methods.put(method.name(), method);
    }
    
    /** initialize _methods map with handlers */
    private void initMethods() {
        
    }
  
    /** generate and return a new session id, does not check for duplicates */
    private String generateSessionID() {
        return String.format("trans-%s", _context.random().nextLong());
    }

    /**
     * create a new session, store it's info
     * @param session_duration how long until this session expires in ms
     * @return the session ID
     */
    private String createSession() {
        String sessionID = generateSessionID();
        _sessions.put(sessionID, new RPCSession());
        return sessionID;
    }

    /** write a json object as a response, convience */
    protected void respondJSON(JSONObject json_resp, HttpServletResponse resp) throws ServletException, IOException {
        // set headers
        resp.setHeader("Content-Type", "text/json");
        // write out object
        Writer http_wr = resp.getWriter();
        json_resp.write(http_wr);
        http_wr.flush();
    }
  
    /**
     * handle an http request that is routed to us
     * @param manager the current snark manager we are going to use, we do not store this
     */
    public void handleRPC(SnarkManager manager, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    
        // check CSRF
        if (!checkCSRF(req)) {
            String sessionID = createSession();
            resp.setHeader(TRPC_SESSION_HEADER, sessionID);
            resp.sendError(409);
            return;
        }
    
        // prepare response
        JSONObject json_resp = new JSONObject();
    
        JSONTokener json = new JSONTokener(req.getInputStream());
        // parse request
        JSONObject json_req;
        try {
            
            json_req = new JSONObject(json);
        } catch (JSONException thrown) {
            // error parsing
            // tell them about it via an error
            StringWriter str = new StringWriter();
            PrintWriter wr = new PrintWriter(str);
            wr.println("error parsing json: "+json);
            thrown.printStackTrace(wr);
            json_resp.put("result", str.toString());
            respondJSON(json_resp, resp);
            return;
        }
    
        // get keys in the json request
        String tag = json_req.optString("tag", null);
        String method = json_req.optString("method", null);
        JSONObject args = json_req.optJSONObject("arguments");

        // do we have the required method paramter?
        if (method == null) {
            json_resp.put("result", "no method specified");
        } else {
            // do we have this method?
            if (_methods.containsKey(method)) {
                RPCMethod handler = _methods.get(method);
                // prepare response
                RPCMethod.Result result = null;
                // call it
                try {
                    result = handler.call(manager, args);
                } catch (Exception thrown) {
                    // if we hit an exception, report it
                    StringWriter str = new StringWriter();
                    PrintWriter wr = new PrintWriter(str);
                    thrown.printStackTrace(wr);
                    result = new RPCMethod.Result(str.toString(), null);
                }
                // put our results in the response
                json_resp.put("result", result.result);
                json_resp.put("arguments", result.arguments);
            } else {
                // we don't have this method implemented
                // maybe it's the future and the spec changed?
                json_resp.put("result", "i2psnark does not implement "+method);
            }
        }
        if (tag != null) {
            // if we have a tag put it
            json_resp.put("tag", tag);
        }
        // send response
        respondJSON(json_resp, resp);
    }

    /**
     * @param sessionID
     * @return do we have this session? returns true if we have it even if it's expired
        */
    protected boolean hasSession(String sessionID) {
        return _sessions.containsKey(sessionID);
    }
  
    /**
     * check if this request contains the right CSRF headers
     * @param req
     * @return false if the X-Transmission-Session-Id header is invalid or expired otherwise true
     */
    protected boolean checkCSRF(HttpServletRequest req) {
        String sessionID = req.getHeader(TRPC_SESSION_HEADER);
        if (sessionID == null) {
            return false;
        }
        if (hasSession(sessionID)) {
            boolean expired = _sessions.get(sessionID).expired();
            // remove this session if it expired
            if(expired) {
                _sessions.remove(sessionID);
                return false;
            }
            return true;
        }
        return false;
    }
}
