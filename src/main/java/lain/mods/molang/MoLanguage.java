package lain.mods.molang;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import lain.mods.molang.io.UnicodeInputStreamReader;
import lain.mods.molang.res.ResIndex;
import lain.mods.molang.res.ResList;
import lain.mods.molang.res.ResObject;
import lain.mods.molang.util.SimpleCachedRemoteFile;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.LineProcessor;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;

@Mod(modid = "MoLanguage", dependencies = "after:*", useMetadata = true)
public class MoLanguage
{

    public boolean verifyRemoteList;

    public ResIndex index;
    public ResList list;

    private void closeSilently(Closeable closeable)
    {
        try
        {
            Closeables.close(closeable, true);
        }
        catch (IOException ignored)
        {
        }
    }

    public void processLocalLangFile(File file)
    {
        if (file != null && file.isFile() && file.getName().toLowerCase().endsWith(".lang"))
        {
            final File f = file;
            ResObject obj = new ResObject(SimpleCachedRemoteFile.hashFilename(f.toString()), 0L)
            {
                @Override
                public InputStream openStream() throws IOException
                {
                    return new FileInputStream(f);
                }
            };
            index.put(obj);
            list.put("lang/local", obj.hash);
        }
    }

    public void processRemoteLangFile(String baseURL, Multimap<String, String> files)
    {
        File langFile;
        for (String modname : files.keySet())
        {
            if (!modname.equals("*") && !Loader.isModLoaded(modname))
                continue;
            for (String p : files.get(modname))
            {
                if ((langFile = SimpleCachedRemoteFile.download(String.format(baseURL, p))) != null)
                {
                    final File f = langFile;
                    ResObject obj = new ResObject(SimpleCachedRemoteFile.hashFilename(f.toString()), 0L)
                    {
                        @Override
                        public InputStream openStream() throws IOException
                        {
                            return new FileInputStream(f);
                        }
                    };
                    index.put(obj);
                    list.put("lang/remote", obj.hash);
                }
            }
        }
    }

    public void processRemoteLangFileList(String baseURL, String listPath)
    {
        if (listPath == null)
            listPath = "langlist.list";
        File fileList = SimpleCachedRemoteFile.download(String.format(baseURL, listPath));
        if (fileList != null)
        {
            Reader reader = null;
            try
            {
                reader = new UnicodeInputStreamReader(new FileInputStream(fileList), "UTF-8");
                processRemoteLangFile(baseURL, CharStreams.readLines(reader, new LineProcessor<Multimap<String, String>>()
                {

                    Multimap<String, String> files = HashMultimap.create();
                    boolean flag = verifyRemoteList;

                    @Override
                    public Multimap<String, String> getResult()
                    {
                        return files;
                    }

                    @Override
                    public boolean processLine(String line) throws IOException
                    {
                        if (!flag)
                            flag = line.equals("#langlist");
                        if (!flag)
                            return false;
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#"))
                            return true;
                        String[] parts = line.split(" ");
                        if (parts.length != 4)
                            return true;
                        files.put(parts[0], String.format("%s/%s", parts[2], parts[1]));
                        return true;
                    }

                }));
            }
            catch (IOException ignored)
            {
            }
            finally
            {
                closeSilently(reader);
            }
        }
    }

}
