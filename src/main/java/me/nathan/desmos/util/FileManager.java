package me.nathan.desmos.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

public class FileManager {

    public FileManager() {
        createDirectoryInAbsence("Desmos");
        createDirectoryInAbsence("Desmos\\Cache");
    }

    public void createDirectoryInAbsence(String name) {
        File file = new File(System.getProperty("user.home") + "\\" + name);

        if(!file.exists()) {
            file.mkdir();
        }
    }

    public void cacheImageFromURL(String search, String name) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            URL url = new URL(search);

            // This user agent is for if the server wants real humans to visit
            String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" +
                    " (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", USER_AGENT);

            inputStream = con.getInputStream();
            outputStream = new FileOutputStream(System.getProperty("user.home") + "\\Desmos\\Cache\\"+ name + ".png");

            byte[] buffer = new byte[2048];

            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getAvatarFromCache(String username) {
        File file = new File(System.getProperty("user.home") + "\\Desmos\\Cache\\"+ username + ".png");

        if (!file.exists()) {
            cacheImageFromURL("https://mc-heads.net/avatar/" + username, username);
        }
        return file;
    }
}
