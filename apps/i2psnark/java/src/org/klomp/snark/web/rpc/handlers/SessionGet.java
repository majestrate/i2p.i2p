package org.klomp.snark.web.rpc.handlers;

import net.i2p.I2PAppContext;
import net.i2p.util.Log;

import org.json.JSONObject;
import org.klomp.snark.web.rpc.RPCSession;
import org.klomp.snark.web.rpc.SessionMethod;

public class SessionGet extends SessionMethod {

    private final Log _log;
    
    public SessionGet(I2PAppContext ctx) {
        _log = ctx.logManager().getLog(getClass());
    }
    
    @Override
    public String name() {
        return "session-get";
    }

    @Override
    public Result call(RPCSession session, JSONObject param) {
        Arguments args = extractArguments(session.snarkManager(), null);
        return new Result("success", args.toJSON());
    }

}
