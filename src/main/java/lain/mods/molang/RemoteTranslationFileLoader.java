package lain.mods.molang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.minecraftforge.common.Configuration.UnicodeInputStreamReader;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.LineProcessor;
import cpw.mods.fml.common.Loader;

public class RemoteTranslationFileLoader
{

    public static TranslationTable load(File baseDir) throws IOException
    {
        TranslationTable t = new TranslationTable();
        File dir = new File(baseDir, "langOnlineTemp");
        if (dir.exists() || dir.mkdirs())
        {
            for (String provider : Configs.OnlineProviders)
                load0(t, provider, dir);
        }
        return t;
    }

    private static void load0(TranslationTable t, String baseURL, File tempDir) throws IOException
    {
        File fileList = SimpleCachedRemoteFile.download(tempDir, String.format(baseURL, "langlist.list"));
        if (fileList != null)
        {
            FileInputStream data = null;
            try
            {
                data = new FileInputStream(fileList);
                load1(t, baseURL, tempDir, CharStreams.readLines(new UnicodeInputStreamReader(data, "UTF-8"), new LineProcessor<Multimap<String, String>>()
                {

                    Multimap<String, String> files = HashMultimap.create();
                    boolean flag = Configs.OnlineSkipVerification;

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
            finally
            {
                Closeables.close(data, true);
            }
        }
    }

    private static void load1(TranslationTable t, String baseURL, File tempDir, Multimap<String, String> files) throws IOException
    {
        for (String modname : files.keySet())
        {
            if (!modname.equals("*") && !Loader.isModLoaded(modname))
                continue;
            for (String n : files.get(modname))
            {
                File langFile = SimpleCachedRemoteFile.download(tempDir, String.format(baseURL, n));
                if (langFile != null)
                {
                    InputStream data = null;
                    try
                    {
                        data = new FileInputStream(langFile);
                        t.importTranslationFile(data, n.substring(0, n.indexOf("/")));
                    }
                    finally
                    {
                        Closeables.close(data, true);
                    }
                }
            }
        }
    }

}
