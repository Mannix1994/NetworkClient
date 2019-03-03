package cn.edu.scu.creator.networkclient;

/**
 * Created by Creator on 2017/9/5.
 * 定义一些常量
 */

class ConstValues {
    public static class MSG_TYPES{
        public final static int MsgLogin = 0;
        public final static int MsgLogout = 1;
        public final static int MsgUserinfo = 2;
        public final static int MsgIsOnline = 3;
        public final static int MsgError = 4;
        public final static int MsgLogoutByUserIdAndPass = 5;
    }

    public static class REQUEST_CODES{
        //请求Activity
        public static final int RESULT = 0;
        public static final int REQUEST_CHOOSE_SERVICE = 1;
        public final static int REQUEST_CROP = 2;
        //请求权限
        public final static int REQUEST_WRITE_PERMISSION = 3;
        public final static int REQUEST_READ_PERMISSION = 4;
    }

    public static class NETWORK_REQUEST{
        public final static int FAIL = 0;
        public final static int SUCCESS = 1;
    }

    public static class STRINGS{
        public final static String TAG = "NetworkClient";
        //MainAtivity中使用
        public final static  String StrLogin = "登录";
        public final static  String StrLogout = "注销";
        public final static String USER_ID_IS_EMPTY = "用户名为空";
        public final static String PASSWORD_IS_EMPTY = "密码为空";

        //NetworkService中使用
        public final static String FAIL_TO_CONNECT_SERVER = "连接服务器失败";
        public final static String FAIL_TO_LOGIN = "登录失败:";
        public final static String FAIL_TO_LOGOUT = "注销失败:";
        public final static String FAIL_TO_GETUSERINFO = "获取用户信息失败:";
        public final static String FAIL_TO_CHECK_WIFI = "检查WIFI是否连接失败:";
        public final static String FAIL_TO_SENDGETREQUEST = "发送GET请求失败:";
        public final static String FAIL_TO_SENDPOSTREQUEST = "发送Post请求失败:";
        public final static String NO_ERROR = "NoError";
    }

    public static class URLS{
        public final static String loginUrl = "http://webportal.scu.edu.cn/eportal/InterFace.do?method=login";
        public final static String logoutUrl = "http://webportal.scu.edu.cn/eportal/InterFace.do?method=logout";
        public final static String userInfoUrl = "http://webportal.scu.edu.cn/eportal/InterFace.do?method=getOnlineUserInfo";
        public final static String logoutByUserIdAndPassUrl = "http://webportal.scu.edu.cn/eportal/InterFace.do?method=logoutByUserIdAndPass";
//        public final static String loginUrl = "http://192.168.2.135/eportal/InterFace.do?method=login";
//        public final static String logoutUrl = "http://192.168.2.135/eportal/InterFace.do?method=logout";
//        public final static String userInfoUrl = "http://192.168.2.135/eportal/InterFace.do?method=getOnlineUserInfo";
//        public final static String logoutByUserIdAndPassUrl = "http://192.168.2.135/eportal/InterFace.do?method=logoutByUserIdAndPass";
    }
}
