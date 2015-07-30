package org.klomp.snark.web.rpc;

import org.json.JSONObject;

/**
 * interface for transmission-rpc methods like torrent-set, torrent-get, etc 
 * 
 * @author jeff
 *
 */
public interface RPCMethod {

    /** the result of an rpc method call */
    public static class Result {
        public final String result;
        public final JSONObject arguments;
    
        public Result(String res, JSONObject args) {
            result = res;
            arguments = args;
        }
    }
      /** @return the name of our method */
    String name();
  
    /** 
     * call our method
     * 
     * @param param paramters passed in via http
     * @param manager i2psnark instance
     * @return call result
     */
    Result call(RPCSession session, JSONObject param);
}
