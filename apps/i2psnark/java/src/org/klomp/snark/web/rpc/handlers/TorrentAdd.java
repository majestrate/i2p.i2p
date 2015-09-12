package org.klomp.snark.web.rpc.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import net.i2p.I2PAppContext;
import net.i2p.data.Base64;
import net.i2p.data.ByteArray;
import net.i2p.util.ByteCache;
import net.i2p.util.Log;


import org.json.JSONObject;
import org.klomp.snark.MetaInfo;
import org.klomp.snark.SnarkManager;
import org.klomp.snark.web.rpc.RPCMethod;
import org.klomp.snark.web.rpc.RPCSession;

/**
 * 
 * @author jeff
 * Implements the torrent-add function, allows transmission-rpc to add torrents
 */
public class TorrentAdd implements RPCMethod {

    private final Log _log;
    
    public TorrentAdd(I2PAppContext ctx) {
        _log = ctx.logManager().getLog(getClass());
    }
    
    @Override
    public String name() {
        return "torrent-add";
    }

    @Override
    public Result call(RPCSession session, JSONObject param) {
        // TODO: incomplete
        String result = null;
        JSONObject json_result = new JSONObject();
        String downloadDir = param.optString("download-dir");
        if (result == null) {
            
        }
        
        return null;
    }
    
    /**
     * get meta info from base64
     * @param metainfo base64'd .torrent file
     * @return MetaInfo or null on error
     */
    private MetaInfo getMetaInfoBase64(String metainfo) {
        // transform it to use i2p base64 encoding so we can use our decoder
        byte [] decoded = Base64.decode(metainfo.replace("/", "~").replace("+", "-"));
        if(decoded == null) {
            // we failed to decode
            _log.warn("failed to decode base64'd meta info");
            return null;
        }
        InputStream i = new ByteArrayInputStream(decoded);
        try {
            MetaInfo meta = new MetaInfo(i);
            i.close();
            return meta;
        } catch (IOException thrown) {
            _log.warn("failed to decode MetaInfo", thrown);
            return null;
        }
    }
}
