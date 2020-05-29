package com.z.exoplayertest.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.z.exoplayertest.database.Channel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    /**
     * 用户手动添加频道文件地址
     */
    public static String USER_CHANNEL_FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "channel.txt";
    /**
     * 获取文件中的数据
     *
     * @param context
     * @return
     */
    static public List<Channel> getChannelFromTxt(Context context) {
        List<Channel> list = new ArrayList<>();
        AssetManager am = context.getAssets();
        try {
            //读取assets内的文件
            InputStream is = am.open("channel");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "utf-8"));

            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                if (lineTxt.contains("&&")) {
                    String[] str = lineTxt.split("&&");
                    if (str.length > 1) {
                        list.add(new Channel(str[0], str[1], 0));
                    }
                }
            }
            bufferedReader.close();
            is.close();

            File file = new File(USER_CHANNEL_FILE_PATH);
            if (file.exists()) {
                //读取用户手动添加的文件
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
                BufferedReader br = new BufferedReader(reader);
                String s = null;
                while ((s = br.readLine()) != null) {
                    if (s.contains("&&")) {
                        String[] str = s.split("&&");
                        if (str.length > 1) {
                            list.add(new Channel(str[0], str[1], 0));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 将字符串写入到文本文件中
    public static void writeFile(String strFilePath, String strcontent) {
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {

        }
    }
}
