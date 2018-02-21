package com.gelostech.dankmemes.commoners;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by root on 9/8/17.
 */

public class FontManager {

    public static final String brooke ="BrookeS8.ttf";
    public static final String kb = "kb.ttf";
    public static final String safrin = "safir.ttf";
    public static final String naughty = "naughty.ttf";

    public static Typeface getTypeFace(Context c, String font){
        return Typeface.createFromAsset(c.getAssets(), font);
    }
}
