package lain.mods.molang.res;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class ResList
{

    private final Multimap<String, String> hashes = LinkedHashMultimap.create();

    public void clear()
    {
        hashes.clear();
    }

    public Collection<String> delete(String key)
    {

        return hashes.removeAll(key);
    }

    public Collection<String> get(String key)
    {
        return Collections.unmodifiableCollection(hashes.get(key));
    }

    public Set<String> keys()
    {
        return Collections.unmodifiableSet(hashes.keySet());
    }

    public boolean put(String key, String hash)
    {
        return hashes.put(key, hash);
    }

}
