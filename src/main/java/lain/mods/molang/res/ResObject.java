package lain.mods.molang.res;

import java.io.IOException;
import java.io.InputStream;

public abstract class ResObject
{

    public final String hash;
    public final long size;

    public ResObject(String hash, long size)
    {
        this.hash = hash;
        this.size = size;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;

        ResObject that = (ResObject) obj;

        if (size != that.size)
            return false;
        if (!hash.equals(that.hash))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = hash.hashCode();
        result = 31 * result + (int) (size ^ size >>> 32);
        return result;
    }

    public abstract InputStream openStream() throws IOException;

}
