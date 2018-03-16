package Model.ReferencableInterface;

import java.util.HashMap;
import java.util.Map;

public class ReferencableManager {
    private static ReferencableManager ourInstance = new ReferencableManager();

    public static ReferencableManager getInstance() {
        return ourInstance;
    }

    public HashMap<String, Object> referencableMap;

    //avoid calling constructor
    private ReferencableManager() {

        referencableMap = new HashMap<String, Object>();

    }

    public void add(IReferencable iReferencable)
    {
        referencableMap.put(iReferencable.getID(), iReferencable.getController());
    }

    public Object get(String id)
    {
        if(referencableMap.containsKey(id)) {
            return referencableMap.get(id);
        }

        return null;
    }

    public void delete(String id)
    {
        if(referencableMap.containsKey(id)) {
            referencableMap.remove(id);
        }
    }




}
