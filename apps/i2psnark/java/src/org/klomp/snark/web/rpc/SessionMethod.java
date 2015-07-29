package org.klomp.snark.web.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.klomp.snark.SnarkManager;
import org.klomp.snark.web.SnarkRPCHandler;

/**
 * Base class for session-* method handlers, provides helpers 
 * @author jeff
 *
 */
public abstract class SessionMethod implements RPCMethod {

    @Override
    public abstract String name();

    @Override
    public abstract Result call(RPCSession session, JSONObject params);


    /**
     * parse out all the parameters from a SnarkManager if params is null or just the ones in params if not null
     * @return Arguments
     */
    protected final Arguments extractArguments(SnarkManager manager, JSONObject params) {
        Arguments args = new Arguments();
        Collection<String> wantedKeys = new ArrayList<String>();
        if (params == null) {
            wantedKeys.addAll(Arguments.keys);
        } else {
            for (String key : params.keySet()) {
                wantedKeys.add(key);
            }
        }
        // TODO: actually implement extracting arguments from snark
        for (String key : wantedKeys) {
            if (key != null && Arguments.defaults.containsKey(key) ) {
                args.put(key, Arguments.defaults.get(key));
            }
        }
        return args;
    }
    
    /**
     * Arguments for session-* methods with defaults
     * @author jeff
     * @see https://trac.transmissionbt.com/browser/trunk/extras/rpc-spec.txt#L446
     *
     */
    protected static class Arguments extends ConcurrentHashMap<String, String> {
        
        static final String [][] bools = {
            {"alt-speed-enabled", "false"},
            {"alt-speed-time-enabled", "false"},
            {"blocklist-enabled", "false"},
            {"download-queue-enabled", "false"},
            {"dht-enabled", "true"},
            {"idle-seeding-limit-enabled", "false"},
            {"incomplete-dir-enabled", "false"},
            {"lpd-enabled", "false"},
            {"pex-enabled", "true"},
            {"peer-port-random-on-start", "false"},
            {"port-forwarding-enabled", "false"},
            {"queue-stalled-enabled", "false"},
            {"rename-partial-files", "false"},
            {"script-torrent-done-enabled", "false"},
            {"seedRatioLimited", "false"},
            {"seed-queue-enabled", "false"},
            {"speed-limit-down-enabled", "false"},
            {"speed-limit-up-enabled", "false" },
            {"start-added-torrents", "false" },
            {"trash-original-torrent-files", "false"},
            {"utp-enabled", "false"},
        };
        
        static final String [][] floats = {
            {"seedRatioLimit", "-1.0"},
        };
        
        static final String [][] strings = {
            {"blocklist-url", "" },
            {"download-dir", "" },
            {"encryption", "required" },
            {"incomplete-dir", "" },
            {"script-torrent-done-filename", ""},
            {"version", String.format("transmissionrpc %d (i2psnark)", SnarkRPCHandler.TRPC_VERSION)},
        };
        
        static final String [][] nums = {
            {"alt-speed-down", "1"},
            {"alt-speed-time-begin", "0"},
            {"alt-speed-time-end", "0" },
            {"alt-speed-time-day", "0" },
            {"alt-speed-up", "1" },
            {"cache-size-mb", "0" },
            {"download-queue-size", "0" },
            {"idle-seeding-limit", "0" },
            {"peer-limit-global", "100" },
            {"peer-limit-per-torrent", "100" },
            { "peer-port", "6886" },
            {"queue-stalled-minutes", "0"},
            {"seed-queue-size", "0"},
            {"speed-limit-down", "100"},
            {"speed-limit-up", "100"},
        };
        
        static final Collection<String> keys = new ArrayList<String>();
        
        // populate keys
        static {
            for (String[] pair : nums) {
                keys.add(pair[0]);
            }
            for (String[] pair : bools) {
                keys.add(pair[0]);
            }
            for (String[] pair : floats) {
                keys.add(pair[0]);
            }
            for (String[] pair : strings) {
                keys.add(pair[0]);
            }
        }
        
        static final Arguments defaults = new Arguments().putDefaults();
        
        
        Arguments putDefaults() {
            for (String[] pair : nums) {
                keys.add(pair[0]);
            }
            for (String[] pair : bools) {
                keys.add(pair[0]);
            }
            for (String[] pair : floats) {
                keys.add(pair[0]);
            }
            for (String[] pair : strings) {
                keys.add(pair[0]);
            }
            return this;
        }
        
        /**
         * convert to JSONObject
         */
        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            for (String[] pair : nums) {
                String key = pair[0];
                if (containsKey(key)) {
                    json.put(key, Long.parseLong(get(key)));
                }
            }
            for (String[] pair : bools) {
                String key = pair[0];
                if (containsKey(key)) {
                    json.put(key, Boolean.parseBoolean(get(key)));
                }
            }
            for (String[] pair : floats) {
                String key = pair[0];
                if (containsKey(key)) {
                    json.put(key, Double.parseDouble(get(key)));
                }
            }
            for (String[] pair : strings) {
                String key = pair[0];
                if (containsKey(key)) {
                    json.put(key, get(key));
                }
            }
            if (containsKey("units")) {
                JSONObject units =  new JSONObject();
                json.put("units", units);
            }
            return json;
        }
        
        String[] speedUnits = {"KiB/s", "MiB/s", "GiB/s", "TiB/s"};
        long speedBytes = 1024;
        String[] sizeUnits = {"KiB", "MiB", "GiB", "TiB"};
        long sizeBytes = 1024;
        String[] memoryUnits = {"kB", "MB", "GB", "TB"};
        long memoryBytes = 1000;
    }
}
