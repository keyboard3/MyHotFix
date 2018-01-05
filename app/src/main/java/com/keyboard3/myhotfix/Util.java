package com.keyboard3.myhotfix;

import android.content.Context;
import android.widget.Toast;

/**
 * @author keyboard3 on 2018/1/3
 */

public class Util {
    public void Toast(Context context) {
        Toast.makeText(context, "bug", Toast.LENGTH_SHORT).show();
    }

    public void Toast(Context context, String value) {
        Toast.makeText(context, value, Toast.LENGTH_SHORT).show();
    }
}
