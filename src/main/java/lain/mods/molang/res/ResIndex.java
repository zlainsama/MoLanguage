package lain.mods.molang.res;

import java.util.Map;
import java.util.Set;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ResIndex
{

    private final Map<String, ResObject> objects = Maps.newHashMap();

    public void clear()
    {
        objects.clear();
    }

    public ResObject delete(String hash)
    {
        ResObject obj = get(hash);
        if (obj != null)
            objects.remove(obj);
        return obj;
    }

    public ResObject get(String hash)
    {
        return objects.get(hash);
    }

    public Set<ResObject> objects()
    {
        return Sets.newHashSet(objects.values());
    }

    public ResObject put(ResObject obj)
    {
        return objects.put(obj.hash, obj);
    }

}
