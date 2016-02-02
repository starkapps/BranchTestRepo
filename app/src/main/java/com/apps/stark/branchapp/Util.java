package com.apps.stark.branchapp;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by lisa on 2/2/16.
 */
public class Util {

    public static void readAssetFile(Context context, String filename, HashMap<String, String> countryMap) {
        InputStream is = null;
        String line;
        String[] values;
        try {
            is = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            while ((line = reader.readLine()) != null) {
                values = line.split("\t");
                countryMap.put(values[0], values[1]);
            }
            is.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
        }
    }

}
