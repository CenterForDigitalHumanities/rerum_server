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
    /**
     * Map of API flags from the query string and the resulting method call.
     * Organized in order of operations?
     */
    private final Map<String, Method> flagMap = Map.of(
        "expand", expand, // awaiting methods
        "compress", compress        
    ); 

public JSONObject processFlags(JSONObject documObject, String[] params) {
    try {
        for( String param: params) {
            if(flagMap.containsKey(param)) {
                documObject = (JSONObject) flagMap.get(param).invoke(documObject);
            }
        }
    } catch (Exception e) {
        Logger.getLogger(Controller.class.getName()).info("Could not process flag.\n"+e.getMessage());
    }
    return documObject;
}

}
