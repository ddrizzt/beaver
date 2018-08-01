package beaver;

import java.text.SimpleDateFormat;

public final class DICT {

    public static final int ACTIVE = 1;

    public static final int PAUSE = 2;

    public static final int PENDING = 0;

    public static final String CF_PENDING = "PENDING";

    public static final String CF_CREATING = "CREATING";

    public static final String CF_UPDATING = "UPDATING";

    public static final String CF_CREATE_COMPLETE = "CREATE_COMPLETE";

    public static final String CF_ROLLBACK = "ROLLBACK";

    public static final String CF_DELETEING = "DELETE_IN_PROGRESS";

    public static final String CF_DELETE_COMPLETE = "DELETE_COMPLETE";

    public static final String CF_ROLLBACK_COMPLETE = "ROLLBACK_COMPLETE";

    public static final SimpleDateFormat DF_YMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final SimpleDateFormat DF_YMD = new SimpleDateFormat("yyyy-MM-dd");
}
