package org.klomp.snark.web.rpc;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.klomp.snark.Snark;
import org.klomp.snark.SnarkManager;

/**
 * Base class for Transmission RPC method
 * Implements some snark specific helpers
 * @author jeff
 *
 */
public abstract class AbstractRPCMethod implements RPCMethod {

    @Override
    public abstract String name();

    @Override
    public abstract Result call(RPCSession session, JSONObject param);

    /**
     *
     * @return the list of IDs requested from the
     */
    protected final List<Long> getIDs(SnarkManager manager, JSONObject param) {
        // do we have the ids parameter?
        if (param.has("ids")) {
            // yah
            // extract it
            Object obj = param.get("ids");
            List<Long> ids = new ArrayList<Long>();
            // is it an array ?
            if (obj instanceof JSONArray ) {
                // ya
                // grab all the values
                JSONArray arr = (JSONArray) obj;
                for (int idx = 0 ; idx < arr.length(); idx ++) {
                    // does not check for type
                    // XXX: is this bad?
                    ids.add(arr.getLong(idx));
                }
            } else if (obj instanceof Long ) {
                // it's just a long, add it
                ids.add((Long) obj);
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
     * @return every torrent's ID
     */
    private List<Long> getAllTorrentIDs(SnarkManager manager) {
        List<Long> ids = new ArrayList<Long>();
        // synchronize access to manager
        // XXX: is this needed? it probably won't screw things up, but you never know...
        synchronized(manager) {
            for (Snark snark : manager.getTorrents()) {
                ids.add(snark.getLongID());
            }
        }
        return ids;
    }
}
