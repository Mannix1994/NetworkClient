package cn.edu.scu.creator.networkclient;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wang on 2017/3/3.
 * 日志纪录类
 */

public final class Logger {
    private static String fileDir = Environment.getExternalStorageDirectory().getPath()+"/NetworkClient";
    private static String filePath = Environment.getExternalStorageDirectory().getPath()+"/NetworkClient/srun.log";
    private final static  String TAG = "NetworkClient";

    /**
     * 将日志写入SD卡
     * @param tag 标签
     * @param message 日志
     */
    public static void d(String tag,String message){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        String datatime = df.format(new Date());
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            try{
                //Log.d(TAG, "d: "+filePath);
                File dir = new File(fileDir);
                if(!dir.exists()){
                    if(!dir.mkdirs()){
                        Log.d(TAG, "Logger.d(): 创建文件夹失败");
                        return;
                    }
                }
                File file = new File(filePath);
                if(!file.exists())
                    file.createNewFile();
                FileWriter writer = new FileWriter(file,true);
                String data = datatime+tag+" :"+message+"\n";
                //Log.d(TAG, "d: "+data);
                writer.write(data);
                writer.close();
            } catch (IOException e) {
                Log.d(TAG, "Logger.d(): "+e.toString());
            }
        }
    }
}
