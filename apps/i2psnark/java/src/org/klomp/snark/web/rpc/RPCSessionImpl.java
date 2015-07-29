package org.klomp.snark.web.rpc;

import java.util.Date;

import org.klomp.snark.SnarkManager;

public class RPCSessionImpl implements RPCSession {
    // TODO: should this be shorter?
    private static final long DEFAULT_SESSION_DURATION = 60 * 1000;
    /** when this session expires */;
    private final long expires_at;

    /** underlying SnarkManager that we are operating with */
    private final SnarkManager _manager;

    /**
     * @param sessionDuration how long should this session last?
     */
    public RPCSessionImpl(SnarkManager manager, long sessionDuration) {
        expires_at = new Date().getTime() + sessionDuration;
        _manager = manager;
    }

    public RPCSessionImpl(SnarkManager manager) {
        this(manager, DEFAULT_SESSION_DURATION);
    }

    public boolean expired() {
        return new Date().getTime() >= expires_at;
    }

    public SnarkManager snarkManager() {
        return _manager;
    }
}
