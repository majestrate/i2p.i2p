package org.klomp.snark.web.rpc;

import org.json.JSONObject;
import org.klomp.snark.Peer;

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
    JSONObject snarkToPeer(Peer p) {
        JSONObject info = new JSONObject();
        String dest = p.getDestination().toBase32();
        info.put("address", dest);
        String clientName = p.get
        return info;
    }
    
}
