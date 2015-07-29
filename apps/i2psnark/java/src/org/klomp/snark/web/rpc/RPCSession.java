package org.klomp.snark.web.rpc;

import org.klomp.snark.SnarkManager;

/**
 * interface for rpc sessions 
 * @author jeff
 *
 */
public interface RPCSession {
   
    /** @return true if the session has expired */
    boolean expired();
    
    /** @return current SnarkManager */
    SnarkManager snarkManager();
}
