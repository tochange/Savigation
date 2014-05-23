package com.tochange.yang.lib;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tochange.yang.lib.Utils.FileInfos;

import android.content.Context;
import android.os.AsyncTask;

public class SimpleLogFile
{
    final static String LOGPATH = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/mydebug";

    final static String LOGCONFIGPATH = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + "/mydebug/config";

    private static boolean getTagListFromFile(String tagFilePath,
            List<String> tagList)
    {
        boolean res = false;
        tagList.clear();
        try
        {
            FileReader in = new FileReader(tagFilePath);
            BufferedReader bufferedReader = new BufferedReader(in);
            String line;
            while (bufferedReader.ready())
            {
                line = bufferedReader.readLine();
                if (line.length() > 0)
                {
                    tagList.add(line);
                }
            }
            in.close();
            if (tagList.size() > 0)
            {
                res = true;
            }
        }
        catch (IOException e)
        {
            log.e(e.toString());
        }

        return res;
    }

    public static void captureLogToFile(Context context, String packageName)
    {
        killLogcat();
        // printBeforPid();
        String appName = packageName
                .substring(packageName.lastIndexOf(".") + 1);
        File tagFile = new File(LOGCONFIGPATH + "/" + appName);

        deduceLogFile(appName);

        if (!tagFile.exists())
        {
            log.e("tag config file:" + tagFile.getAbsolutePath()
                    + "  not found!");
            return;
        }
        List<String> tagList = new ArrayList<String>();
        if (!getTagListFromFile(LOGCONFIGPATH + "/" + appName, tagList))
        {
            log.w("get tag config file failed !");
            return;
        }
        String[] LOGCAT_PREFIX = new String[] { "logcat", "-v", "time", "-s" };
        String[] cmdArray = new String[LOGCAT_PREFIX.length + tagList.size()];
        int length = LOGCAT_PREFIX.length;
        for (int i = 0; i < length; i++)
        {
            cmdArray[i] = LOGCAT_PREFIX[i];
        }
        length = tagList.size();
        for (int j = 0; j < length; j++)
        {
            cmdArray[LOGCAT_PREFIX.length + j] = tagList.get(j);
        }
        String logCmd = "";
        length = cmdArray.length;
        for (int k = 0; k < cmdArray.length; k++)
        {
            logCmd = logCmd + cmdArray[k] + " ";
        }
        // create log file

        String logFileName = appName + Utils.getCurTimeToString(1, 0) + ".log";
        File flog = new File(LOGPATH + "/" + logFileName);
        // start write log file
        String param = logCmd + " -p > " + flog.toString();
        String[] comdline = { "/system/bin/sh", "-c", param };
        String cmd = "pkill logcat";
        log.e("log cmd : " + param);
        try
        {
            Process p = Runtime.getRuntime().exec("sh");// in my company's pad
                                                        // sh is ok
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.write(cmd.getBytes());
            os.flush();
            os.close();
            // clear the logcat first
            Runtime.getRuntime().exec("logcat -c");
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                log.e(e.toString());
            }
            Runtime.getRuntime().exec(comdline);
        }
        catch (IOException e)
        {
            log.e(e.toString());
        }

    }

    private static void printBeforPid()
    {
        try
        {
            Process pp;
            pp = Runtime.getRuntime().exec("sh pidof logcat");
            DataInputStream oss = new DataInputStream(pp.getInputStream());
            String s = oss.readLine();
            oss.close();
            log.e("befor pids:" + s);
            pp.destroy();
        }
        catch (IOException e)
        {
            log.e(e.toString());
        }

    }

    private static void deduceLogFile(String appName)
    {
        List<FileInfos> list = Utils.getDeletedFiles(android.os.Environment
                .getExternalStorageDirectory().getAbsolutePath() + "/mydebug/",
                appName);
        if (list != null)
            for (FileInfos f : list)
                f.file.delete();

    }

    /**
     * when other apps start logcat,it seems this wonn't kill it(them)
     */
    private static void killLogcat()
    {
        try
        {
            Process p = Runtime.getRuntime().exec("sh pidof logcat");
            DataInputStream os = new DataInputStream(p.getInputStream());
            String s = os.readLine();// only one line message
            os.close();
            if (s != null && !s.equals(""))
            {
                String[] pids = s.split(" ");
                for (String pid : pids)
                {
                    log.e("kill logcat process pid:" + pid);
                    Runtime.getRuntime().exec("kill " + pid);
                }
            }
        }
        catch (IOException e)
        {
            log.e(e.toString());
        }
        log.e("");
    }
}
