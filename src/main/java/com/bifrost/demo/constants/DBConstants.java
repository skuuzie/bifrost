package com.bifrost.demo.constants;

public final class DBConstants {
    public static final class ParameterRegistryTable {
        public static final String NAME = "parameters";
        public static final String COL_ID = "id";
        public static final String COL_KEY = "key";
        public static final String COL_VALUE = "value";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_CREATED = "created_at";
        public static final String COL_UPDATED = "updated_at";
    }

    public static final class ResumeRoasteryTable {
        public static final String NAME = "resume_roast";
        public static final String COL_ID = "id";
        public static final String COL_STATUS = "status";
        public static final String COL_RESULT = "raw_result";
        public static final String COL_CREATED = "created_at";
        public static final String COL_UPDATED = "updated_at";
    }
}
