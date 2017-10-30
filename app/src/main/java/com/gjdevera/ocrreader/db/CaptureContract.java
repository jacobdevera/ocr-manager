package com.gjdevera.ocrreader.db;

import android.provider.BaseColumns;

public class CaptureContract {
    public static final String DB_NAME = "com.gjdevera.ocrreader.db";
    public static final int DB_VERSION = 1;

    public class CaptureEntry implements BaseColumns {
        public static final String TABLE = "captures";
        public static final String COL_TEXT = "text";
    }
}