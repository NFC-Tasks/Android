package com.tristanwiley.nfctasks;

import android.provider.BaseColumns;

/**
 * Created by adammcneilly on 2/21/16.
 */
public class NFContract {

    public static final class TagEntry implements BaseColumns {
        public static final String TABLE_NAME = "tagTable";
        public static final String COLUMN_NAME = "tagName";
    }

    public static final class NestTaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "nestTaskTable";
        public static final String COLUMN_TAG = "nestTagColumn";
        public static final String COLUMN_TARGET_VALUE = "nestTargetValue";
    }
}
