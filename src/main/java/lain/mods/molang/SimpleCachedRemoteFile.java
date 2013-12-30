package lain.mods.molang;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import com.google.common.hash.Hashing;

public class SimpleCachedRemoteFile
{

    public static File TEMPDIR;

    static
    {
        try
        {
            TEMPDIR = File.createTempFile("scrf", "");
            TEMPDIR.delete();
            TEMPDIR = new File(TEMPDIR.getParentFile(), ".scrf-temp");
        }
        catch (IOException e)
        {
            TEMPDIR = new File(".scrf-temp");
        }
        finally
        {
            if (!TEMPDIR.exists())
                TEMPDIR.mkdirs();
        }
    }

    public static File download(File tempDir, String url)
    {
        try
        {
            return download(new File(tempDir, hashFilename(url)), new URL(url), Proxy.NO_PROXY, false, 5, false);
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    public static File download(File localFile, URL remoteFile, Proxy proxy, boolean forceDownload, int maxAttempts, boolean silent)
    {
        if (proxy == null)
            proxy = Proxy.NO_PROXY;
        int n = 0;
        while (n++ < maxAttempts)
        {
            try
            {
                if (!silent)
                {
                    System.out.println(String.format("[SCRF] downloading (attempt %d) \'%s\' to \'%s\'", n, remoteFile, localFile));
                    System.out.println("[SCRF] " + download0(localFile, remoteFile, proxy, forceDownload));
                }
                else
                {
                    download0(localFile, remoteFile, proxy, forceDownload);
                }
                return localFile;
            }
            catch (Exception e)
            {
                if (!silent)
                    System.err.println("[SCRF] " + e.toString());
            }
        }
        return null;
    }

    public static File download(String url)
    {
        return download(TEMPDIR, url);
    }

    private static String download0(File localFile, URL remoteFile, Proxy proxy, boolean forceDownload) throws IOException
    {
        if (localFile.getParentFile() != null && !localFile.getParentFile().isDirectory())
            localFile.getParentFile().mkdirs();
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        try
        {
            URLConnection conn = remoteFile.openConnection();
            conn.setUseCaches(false);
            conn.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
            conn.setRequestProperty("Expires", "0");
            conn.setRequestProperty("Pragma", "no-cache");
            if (!forceDownload && localFile.isFile())
                conn.setIfModifiedSince(localFile.lastModified());
            conn.connect();

            if (conn instanceof HttpURLConnection)
            {
                int status = ((HttpURLConnection) conn).getResponseCode();
                if (status == 304)
                    return "Server responded with " + status + ", using cached localFile";
                if (status / 100 == 2)
                {
                    rbc = Channels.newChannel(conn.getInputStream());
                    fos = new FileOutputStream(localFile);
                    fos.getChannel().transferFrom(rbc, 0, 16777216L);
                    localFile.setLastModified(conn.getLastModified());
                }
                if (localFile.isFile())
                    return "Downloaded successfully, using localFile";
                throw new RuntimeException("Server responded with " + status);
            }
            rbc = Channels.newChannel(conn.getInputStream());
            fos = new FileOutputStream(localFile);
            fos.getChannel().transferFrom(rbc, 0, 16777216L);
            localFile.setLastModified(conn.getLastModified());
            if (localFile.isFile())
                return "Downloaded successfully, using localFile";
            throw new RuntimeException("Failed to download remoteFile");
        }
        catch (IOException e)
        {
            if (localFile.isFile())
                return "Failed to download remoteFile (" + e.getClass().getSimpleName() + ": '" + e.getMessage() + "') but localFile exists, using cached localFile";
            throw e;
        }
        finally
        {
            if (rbc != null)
                rbc.close();
            if (fos != null)
                fos.close();
        }
    }

    public static String hashFilename(String filename)
    {
        if (filename == null)
            return null;
        return Hashing.sha1().hashUnencodedChars(filename).toString();
    }

}
