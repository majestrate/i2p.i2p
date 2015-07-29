package org.klomp.snark.web.rpc.handlers;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.klomp.snark.MetaInfo;
import org.klomp.snark.Snark;
import org.klomp.snark.SnarkManager;
import org.klomp.snark.Storage;
import org.klomp.snark.web.rpc.AbstractRPCMethod;
import org.klomp.snark.web.rpc.RPCMethod;
import org.klomp.snark.web.rpc.RPCMethod.Result;
import org.klomp.snark.web.rpc.RPCSession;

/**
 * implements transmission rpc's torrent-get method
 * @author jeff
 *
 */
public class TorrentGet extends AbstractRPCMethod {

    @Override
    public String name() {
        return "torrent-get";
    }

    /**
     * extract info from a Snark and return it as a JSONObject
     */
    private JSONObject getTorrentInfo(Snark s, JSONArray fields) {
        JSONObject info = new JSONObject();
        MetaInfo meta = s.getMetaInfo();
        if (meta == null) {
            return null;
        }
        for (Object o : fields) {
            String val = o.toString();
            if(val.equals("activityDate")) {
                // TODO: activityDate
                info.put(val, 0);
            } else if (val.equals("addedDate")) {
                // TODO: addedDate
                info.put(val, 0);
            } else if (val.equals("bandwidthPriority")) {
                // TODO: bandwidthPriority
                info.put(val, 0);
            } else if (val.equals("comment")) {
                String comment = meta.getComment();
                if (comment == null) {
                    comment = "";
                }
                info.put(val, comment);
            } else if (val.equals("corruptEver")) {
                // TODO: corruptEver
                info.put(val, 0);
            } else if (val.equals("creator")) {
                String creator = meta.getCreatedBy();
                if (creator == null) {
                    creator = "????";
                }
                info.put(val, creator);
            } else if (val.equals("dateCreated")) {
                long created = meta.getCreationDate();
                info.put(val, created);
            } else if (val.equals("desiredAvailable")) {
                long avail = s.getRemainingLength();
                if (avail < 0) {
                    // if no storage yet, fall back to what metainfo says is the total length
                    info.put(val, meta.getTotalLength());
                } else {
                    info.put(val, avail);
                }
            } else if (val.equals("doneDate")) {
                // TODO: doneDate
                info.put(val, 0);
            } else if (val.equals("downloadDir")) {
                Storage st = s.getStorage();
                String dir = "";
                // FIXME what if storage is null like in magnet mode? It will fall back to empty string is that okay?
                if (st != null) {
                    dir = st.getBase().getAbsolutePath();
                }
                info.put(val, dir);
            } else if (val.equals("downloadedEver")) {
                long dl = s.getDownloaded();
                if (dl < 0) {
                    dl = 0;
                }
                info.put(val, dl);
            } else if (val.equals("downloadLimit")) {
                // TODO: downloadLimit
                info.put(val, 0);
            }
        }
        return info;
    }
    
    @Override
    public Result call(RPCSession session, JSONObject param) {
        SnarkManager manager = session.snarkManager();
        JSONObject result_json = new JSONObject();
        // prepare result
        String result = null;
        JSONArray torrents = new JSONArray();
        // get the ids we are operating on
        List<Long> ids = getIDs(manager, param);
        // get the fields that were requested
        JSONArray fields = param.getJSONArray("fields");
        // for each torrent desired
        for ( Long id : ids ) {
            Snark sn = manager.getTorrentByLongID(id);
            if (sn == null) {
                result = "no torrent with id "+id;
                break;
            }
            // get the torrent info
            JSONObject info = getTorrentInfo(sn, fields);
            // we failed D:
            if (info == null) {
                result = "failed to get info for torrent with id: "+id;
                break;
            }
            // put it into the response
            torrents.put(info);
        }

        if (result == null) {
            // nothing bad happened
            // we gud, it succeeded
            result = "success";
            result_json.put("torrents", torrents);
            // check for "recently-active" key
            if (param.has("ids") && param.get("ids").toString().equalsIgnoreCase("recently-active")) {
                // TODO: get recently remove torrents, right now we send all torrents
                JSONArray removed = new JSONArray();
                result_json.put("removed", removed);
            }
        }
        return new Result(result, result_json);
    }
    
}
