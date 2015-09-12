package org.klomp.snark.web.rpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.klomp.snark.Peer;
import org.klomp.snark.Snark;
import org.klomp.snark.SnarkManager;
import org.klomp.snark.Storage;

/**
 * Utility functions for Transmission RPC
 * @author jeff
 *
 */
public final class RPCUtil {

    /**
     * Gets all fields from a Peer that satisfy a tr_peer_stat
     * @see https://trac.transmissionbt.com/browser/trunk/extras/rpc-spec.txt
     * 
     * @return a JSONObject containing the extracted fields
     */
    public static JSONObject snarkToPeer(Peer p) {
        JSONObject info = new JSONObject();
        String dest = p.getDestination().toBase32();
        info.put("address", dest);
        
        return info;
    }


    /**
    *
    * @return the list of IDs requested 
    */
    public static List<Integer> getIDs(SnarkManager manager, JSONObject param) {
        // do we have the ids parameter?
        if (param.has("ids")) {
            // yah
            // extract it
            Object obj = param.get("ids");
            List<Integer> ids = new ArrayList<Integer>();
            // is it an array ?
            if (obj instanceof JSONArray ) {
                // ya
                // grab all the values
                JSONArray arr = (JSONArray) obj;
                for (int idx = 0 ; idx < arr.length(); idx ++) {
                    // does not check for type
                    // XXX: is this bad?
                    ids.add(arr.getInt(idx));
                }
            } else if (obj instanceof Integer ) {
                // it's just a long, add it
                ids.add((Integer) obj);
            } else if (obj instanceof String) {
                // it's a string
                String str = obj.toString();
                // is it 'recently-active' ?
                if (str.equalsIgnoreCase("recently-active")) {
                    // yah
                    // get all torrents because we don't have any way of detecting activity right now
                    // TODO: check for active torrents
                    return getAllTorrentIDs(manager);
                } else {
                    // nah, we don't know what this means so throw
                    throw new JSONException(String.format("id value was invalid string '%s'", str));
                }
            } else {
                // this is an invalid type wtf?
                throw new JSONException("ids was invalid type: "+obj.getClass().getCanonicalName());
            }
            // return what we got
            return ids;
        } else {
            // nah we don't have the ids parameter
            // just send everything :^)
            return getAllTorrentIDs(manager);
        }
    }
    
    /**
     * @param manager The SnarkManager to extract every torrent id from
     * @return every torrent's RPCID
     */
    static List<Integer> getAllTorrentIDs(SnarkManager manager) {
        List<Integer> ids = new ArrayList<Integer>();
        
        for (Snark snark : manager.getTorrents()) {
            ids.add(snark.getRPCID());
        }
        // sort them so transmission-remote displays it nice
        Collections.sort(ids);
        return ids;
    }
}
