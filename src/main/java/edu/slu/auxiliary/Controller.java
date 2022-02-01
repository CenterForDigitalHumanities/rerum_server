/**
 *
 * @author bhaberbe
 * @author cubap
 */
package edu.slu.auxiliary;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

public class Controller {
    private static final Method expand = null;
    private static final Method compress = null;
    private static final Method compact = null;
    private static final Method recurse = null;
    private static final Method batch = null;
    private static final Method decscribe = null;
    private final JSONObject document;
    
    public Controller(JSONObject json){
        document = json;
    }
    /**
     * Map of API flags from the query string and the resulting method call.
     * Organized in order of operations?
     */
    private final List<String> flagsList = Array.asList(
            "expand",
            "compact",
            "recurse",
            "batch",
            "decscribe",
            "compress");

    public JSONObject processFlags(JSONObject documObject, String[] params) {
        Set<String> validFlags = params.stream()
        .distinct()
        .filter(flagsList::contains)
        .collect(Collectors.toSet());
        if (validFlags.size()>0)
        {
            if (validFlags.contains("expand"))
            try {
                // JSONObject serviced = new ServiceController().compress(jo);
                // getting ready to if all these for Bryan
            } catch (Exception e) {
                Logger.getLogger(Controller.class.getName()).info("Could not process flag.\n" + e.getMessage());
            }
        }
        return documObject;
    }
    
    /**
     * Remove keys from a JSONObject that should not be kept because of compression.
     * This will remove every key from the provided JSONObject that is not in the array of default primitive keys.
     * @param toCompress A JSONObject to compress (by removing unwanted keys)
     * @param keysToKeep A String[] Array of keys that should not be removed.
     * @return toCompress once the keys are removed, or the original if there was an error.
     */
    public JSONObject compress(String[] keysToKeep){
        //Either you get keys to keep, or you pass in a null/empty which will just use the default
        return new CompressorService().compress(document, keysToKeep);
    }
}
