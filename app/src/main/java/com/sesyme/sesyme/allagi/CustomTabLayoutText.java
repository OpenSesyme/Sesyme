package com.sesyme.sesyme.allagi;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.material.tabs.TabLayout;
import java.lang.reflect.Field;


public class CustomTabLayoutText extends TabLayout {

    int screenWidth;

    public CustomTabLayoutText(Context context) {
        super(context);
        screenWidth = AllagiUtils.getScreenSize(context)[0];
        initTabMinWidth();
    }

    public CustomTabLayoutText(Context context, AttributeSet attrs) {
        super(context, attrs);
        screenWidth = AllagiUtils.getScreenSize(context)[0];
        initTabMinWidth();
    }

    public CustomTabLayoutText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        screenWidth = AllagiUtils.getScreenSize(context)[0];
        initTabMinWidth();
    }

    private void initTabMinWidth() {

        Field field;
        try {
            field = TabLayout.class.getDeclaredField("mScrollableTabMinWidth");
            field.setAccessible(true);
            field.set(this, screenWidth);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Field field2;
        try {
            field2 = TabLayout.class.getDeclaredField("mRequestedTabMaxWidth");
            field2.setAccessible(true);
            field2.set(this, screenWidth);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
