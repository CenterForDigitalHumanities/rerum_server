/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.slu.auxiliary;

import java.util.Iterator;
import net.sf.json.JSONObject;

/**
 *
 * @author bhaberbe
 */
public class ExpanderService {
    public ExpanderService(){
    
    }
    
    /**
     * Remove keys from a JSONObject that should not be kept because of compression.
     * This will remove every key from the provided JSONObject that is not in the provided array of keys to keep.
     * @param toCompress A JSONObject to compress (by removing unwanted keys)
     * @param keysToKeep A String[] Array of keys that should not be removed.
     * @return toCompress once the keys are removed, or the original if there was an error.
     */
    public JSONObject expand(JSONObject toExpand){
        Iterator keys = toExpand.keys();
        JSONObject orig = JSONObject.fromObject(toExpand);
        try{
            while(keys.hasNext()){
                //toExpand[keys.next()]
            }
            return toExpand;
        }
        catch(Exception e){
            System.out.println("Could not compress object.  See error below.");
            System.out.println(e);
            return orig;
        }
    }
}
