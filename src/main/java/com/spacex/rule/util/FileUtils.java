package com.spacex.rule.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FileUtils {
    private static Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static String fileToString(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            log.error(fileName + "file is not exists!");
            return "";
        }
        Long fileLength = file.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(fileContent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {

            return new String(fileContent, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("The OS does not support " + "utf-8");
            e.printStackTrace();
            return "";
        }

    }
}
