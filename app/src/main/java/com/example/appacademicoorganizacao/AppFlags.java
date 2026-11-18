package com.example.appacademicoorganizacao;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public final class AppFlags {

    private AppFlags() { }

    public static boolean isDebug(Context ctx) {
        return (ctx.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}
