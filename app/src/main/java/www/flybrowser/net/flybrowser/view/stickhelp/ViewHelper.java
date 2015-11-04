/**
 * ViewHelper.java
 * StickyScrollView
 * 
 * Created by likebamboo on 2014-4-21
 * Copyright (c) 1998-2014 https://github.com/likebamboo All rights reserved.
 */

package www.flybrowser.net.flybrowser.view.stickhelp;

import android.annotation.SuppressLint;
import android.view.View;

import static www.flybrowser.net.flybrowser.view.stickhelp.AnimatorProxy.NEEDS_PROXY;
import static www.flybrowser.net.flybrowser.view.stickhelp.AnimatorProxy.wrap;

/**
 * 动画代理，来源于NineOldAndroids
 * 
 * @author ferris
 */
public final class ViewHelper {
    private ViewHelper() {
    }

    public static float getTranslationY(View view) {
        return NEEDS_PROXY ? wrap(view).getTranslationY() : Honeycomb.getTranslationY(view);
    }

    public static void setTranslationY(View view, float translationY) {
        if (NEEDS_PROXY) {
            wrap(view).setTranslationY(translationY);
        } else {
            Honeycomb.setTranslationY(view, translationY);
        }
    }

    @SuppressLint("NewApi")
    private static final class Honeycomb {
        static float getTranslationY(View view) {
            return view.getTranslationY();
        }

        static void setTranslationY(View view, float translationY) {
            view.setTranslationY(translationY);
        }
    }
}
