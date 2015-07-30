package org.klomp.snark.web.rpc.handlers;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.i2p.I2PAppContext;
import net.i2p.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.klomp.snark.MetaInfo;
import org.klomp.snark.Peer;
import org.klomp.snark.Snark;
import org.klomp.snark.SnarkManager;
import org.klomp.snark.Storage;
import org.klomp.snark.web.rpc.AbstractRPCMethod;
import org.klomp.snark.web.rpc.RPCSession;

/**
 * implements transmission rpc's torrent-get method
 * @author jeff
 *
 */
public class TorrentGet extends AbstractRPCMethod {

    private static final long ACTIVITY_TIMEOUT = 20;
    
    private final Log _log;
    private final Map<String, Field<? extends Object>> _handlers;
    
    public TorrentGet(I2PAppContext ctx) {
        _log = ctx.logManager().getLog(this.getClass());
        _handlers = new ConcurrentHashMap<String, Field<? extends Object>>();
        initFields();
    }
    
    @Override
    public String name() {
        return "torrent-get";
    }
    
    private void addField(Field<? extends Object> f) {
        _handlers.put(f.name(), f);
    }
    
    /**
     * numeric status of a torrent
     * @author jeff
     *
     */
    private static class TorrentStatus {
        final static int STOPPED = 0;
        final static int CHECK_WAIT = 1;
        final static int CHECK = 2;
        final static int DOWNLOAD_WAIT = 3;
        final static int DOWNLOAD = 4;
        final static int SEED_WAIT = 5;
        final static int SEED = 6;
    }
    
    /**
     * initialize the field handlers for this handler 
     */
    private void initFields() {
        // unimplemented fields
        addField(new LongField("activityDate"));
        addField(new LongField("addedDate"));
        addField(new LongField("bandwidthPriority"));
        addField(new LongField("corruptEver"));
        addField(new LongField("doneDate"));
        addField(new LongField("downloadLimit"));
        addField(new BooleanField("downloadLimited"));
        addField(new StringField("error", ""));
        addField(new StringField("errorString", ""));
        addField(new LongField("etaIdle"));
        addField(new BooleanField("isFinished"));
        
        // implemented fields
        addField(new StringField("comment"){
            String extractValue(Snark s, MetaInfo meta) {
                String comment = null;
                if (meta != null) { 
                    comment =  meta.getComment();
                }
                return comment;
            }
        });
        addField(new StringField("creator"){
            String extractValue(Snark s, MetaInfo meta) {
                String creator = null;
                if (meta != null) {
                    creator = meta.getCreatedBy();
                }
                return creator;
            }
        });
        addField(new LongField("dateCreated"){
            Long extractValue(Snark s, MetaInfo meta) {
                long created = 0;
                if (meta != null) {
                    created = meta.getCreationDate();
                }
                return created;
            }
        });
        addField(new LongField("desiredAvailable"){
           Long extractValue(Snark s, MetaInfo meta) {
               long avail = s.getRemainingLength();
               if (avail < 0) {   
                   return 0L;
               } else {
                   return s.getTotalLength() - avail;
               }
           }
        });
        addField(new StringField("downloadDir"){
           String extractValue(Snark s, MetaInfo meta) {
               Storage st = s.getStorage();
               String dir = "";
               // FIXME what if storage is null like in magnet mode? It will fall back to empty string is that okay?
               if (st != null) {
                   dir = st.getBase().getAbsolutePath();
               }
               return dir;
           }
        });
        addField(new LongField("downloadedEver"){
            Long extractValue(Snark s, MetaInfo meta){
                long dl = s.getDownloaded();
                if (dl < 0) {
                    dl = 0;
                }
                return dl;
            }
        });
        addField(new LongField("eta"){
            Long extractValue(Snark s, MetaInfo meta) {
                return s.getEstimatedTimeLeft();
            }
        });
        addField(new JSONArrayField("files"){
            JSONArray extractValue(Snark s, MetaInfo meta) {
                Storage st = s.getStorage();
                JSONArray json_fileStats = new JSONArray();
                if (st == null) {
                    _log.warn("storage is null for torrent with id="+s.getRPCID());
                } else {
                    // for each file in this torrent
                    for ( File file : st.getFiles() ) {
                        // prepare file record
                        JSONObject json_file = new JSONObject();
                        // get file's index
                        int idx = st.indexOf(file);
                        // get our values
                        long filesize = file.length();
                        long completed = filesize - st.remaining(idx);
                        String name = file.getName();
                        // put file record we made into the records
                        json_file.put("bytesCompleted", completed);
                        json_file.put("length", filesize);
                        json_file.put("name", name);
                        json_fileStats.put(json_file);
                    }
                }
                return json_fileStats;
            }
       });
        
       addField(new JSONArrayField("filesStats"){
            JSONArray extractValue(Snark s, MetaInfo meta) {
                Storage st = s.getStorage();
                JSONArray json_fileStats = new JSONArray();
                if (st == null) {
                    _log.warn("storage is null for torrent with id="+s.getRPCID());
                } else {
                 // for each file in this torrent
                    for ( File file : st.getFiles() ) {
                        // prepare file record
                        JSONObject json_file = new JSONObject();
                        // get file's index
                        int idx = st.indexOf(file);
                        long completed = file.length() - st.remaining(idx);
                        // compute desired info
                        int priority = st.getPriority(idx);
                        // this torrent is always enabled
                        boolean wanted = true;
                        // put file record we made into the records
                        json_file.put("bytesCompleted", completed);
                        json_file.put("priority", priority);
                        json_file.put("wanted", wanted);
                        json_fileStats.put(json_file);
                    }
                }
                return json_fileStats;
            }
        });
        addField(new DoubleField("leftUntilDone"){
            Double extractValue(Snark s, MetaInfo meta) {
                Storage st = s.getStorage();
                double left = -1;
                if (st != null && meta != null) {
                    long total = meta.getTotalLength();
                    long remaining = st.totalRemaining();
                    long have = total - remaining;
                    if (total > 0 ) {
                        left = have / total;
                    }
                }
                return left;
            }
        });
        addField(new StringField("name"){
            String extractValue(Snark s, MetaInfo meta) {
                String name = null;
                if (meta != null) {
                    name = meta.getName();
                }
                return name;
            }
        });
        addField(new LongField("peersGettingFromUs"){
            Long extractValue(Snark s, MetaInfo meta) {
                long num = 0;
                for (Peer p : s.getPeerList()) {
                    if (p.isConnected() && p.getUploadRate() > 0) {
                        num ++;
                    }
                }
                return num;
            }
        });
        addField(new LongField("peersSendingToUs"){
            Long extractValue(Snark s, MetaInfo meta) {
                long num = 0;
                for (Peer p : s.getPeerList()) {
                    if (p.isConnected() && p.getDownloadRate() > 0) {
                        num ++;
                    }
                }
                return num;
            }
        });
        addField(new LongField("rateDownload"){
            Long extractValue(Snark s, MetaInfo meta) {
                return s.getDownloadRate();
            }
        });
        addField(new LongField("rateUpload"){
            Long extractValue(Snark s, MetaInfo meta) {
                return s.getUploadRate();
            }
        });
        addField(new LongField("sizeWhenDone"){
            Long extractValue(Snark s, MetaInfo meta) {
                long size = 0;
                if (meta != null) {
                    size = meta.getTotalLength();
                }
                return size;
            }
        });
        addField(new LongField("status"){
           Long extractValue(Snark s, MetaInfo meta) {
               if (s.isStopped()) {
                   return (long) TorrentStatus.STOPPED;
               } else if (s.isChecking() || s.isAllocating()) {
                   return (long) TorrentStatus.CHECK;
               } else if (s.getRemainingLength() == 0) {
                   return (long) TorrentStatus.SEED;
               } else {
                   return (long) TorrentStatus.DOWNLOAD;
               }
           }
        });
        addField(new DoubleField("uploadRatio"){
           Double extractValue(Snark s, MetaInfo meta) {
               long uploaded = s.getUploaded();
               long downloaded = s.getDownloaded();
               if (downloaded == 0) {
                   return 0.0;
               } else {
                   return new Double(uploaded) / new Double(downloaded);
               }
           }
        });
    }
    
    /**
     * a field in a torrent-get request
     * @author jeff
     *
     */
    private abstract class Field<T> {
        
        final String _name;
        
        Field(String name) {
            _name = name;
        }
        
        /** @return the name of this field */
        String name() {
            return _name;
        }
        /** extract the value of a field from a Snark */
        T extractValue(Snark s, MetaInfo meta) {
            return null;
        }
        
        final T get(Snark s, MetaInfo meta) {
            T val = extractValue(s, meta);
            if ( val == null) {
                val = getDefault();
            }
            return val;
        }
        
        /** get the default value for this field */
        abstract T getDefault();
            
    }
    
    private class BooleanField extends Field<Boolean> {
        
        private final boolean _default;
        
        BooleanField(String name) {
            this(name, false);
        }
        
        BooleanField(String name, Boolean defaultValue) {
            super(name);
            _default = defaultValue;
        }

        Boolean getDefault() {
            return _default;
        }
    }
    private class DoubleField extends Field<Double> {
        
        private final double _default;
        
        DoubleField(String name) {
            this(name, 0.0);
        }
        
        DoubleField(String name, double defaultValue) {
            super(name);
            _default = defaultValue;
        }

        Double getDefault() {
            return _default;
        }
    }
    
    private class LongField extends Field<Long> {

        private final long _default;
        
        LongField(String name) {
            this(name, 0L);
        }
        
        LongField(String name, long defaultValue) {
            super(name);
            _default = defaultValue;
        }

        @Override
        Long getDefault() {
            return _default;
        }
        
    }
    
    private class StringField extends Field<String> {

        private final String _default;
        
        StringField(String name, String defaultValue) {
            super(name);
            _default = defaultValue;
        }
        
        StringField(String name) {
            this(name, "??? ‾\\(._.)/‾ ???");
        }

        @Override
        String getDefault() {
            return _default;
        }
        
    }
    
    private class JSONArrayField extends Field<JSONArray> {

        JSONArrayField(String name) {
            super(name);
        }

        @Override
        JSONArray getDefault() {
            return new JSONArray();
        }
        
    }
    
    @Override
    public Result call(RPCSession session, JSONObject param) {
        SnarkManager manager = session.snarkManager();
        JSONObject result_json = new JSONObject();
        // prepare result
        String result = null;
        JSONArray torrents = new JSONArray();
        // get the ids we are operating on
        List<Integer> ids = getIDs(manager, param);
        // get the fields that were requested
        JSONArray fields = param.getJSONArray("fields");
        // for each torrent desired
        for ( Integer id : ids ) {
            Snark sn = manager.getTorrentById(id);
            if (sn == null) {
                result = "no torrent with id "+id;
                break;
            }
            // get the torrent info
            JSONObject info = new JSONObject();
            for (Object o : fields) {
                // assume that field values are always string
                String field = o.toString();
                if (field.equals("id")) {
                 // put this snark's rpcid
                 info.put("id", id);
                } else if (_handlers.containsKey(field)) {
                    // yah
                    Field<? extends Object> handler = _handlers.get(field);
                    // get the value for this field from the current Snark
                    Object val = handler.get(sn, sn.getMetaInfo());
                    // put the gotten value into the result for this Snark
                    
                    info.put(field, val);
                } else {
                    result = "i2psnark does not know how to handle field '"+field+"' in torrent-get method";
                    break; 
                }
            }
            
            // put the info into the response
            torrents.put(info);
        }
        // we check for an error
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
        } else {
            // we got an error, log it and make sure the response is sent
            _log.warn("transmission-rpc torrent-get failed: "+result);
        }
        // send response
        return new Result(result, result_json);
    }
    
}
