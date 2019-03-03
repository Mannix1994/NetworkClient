package cn.edu.scu.creator.networkclient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import cn.edu.scu.creator.networkclient.ConstValues.*;

/**
 * Created by Creator on 2017/8/15.
 * 本类主要负责网络操作，操作的结果通过handler返回
 */

public final class NetworkService {

    private final static String TAG = MainActivity.TAG;

    private String lastError = STRINGS.NO_ERROR;
    private Context applicationContext;
    private Handler handler;
    private String queryString;

    public NetworkService(Context context, Handler handler) {
        this.applicationContext = context;
        this.handler = handler;
    }
    public String getLastError(){
        String error = lastError;
        lastError = STRINGS.NO_ERROR;
        return error;
    }
    /**
     * 登录
     * @return 返回登录结果
     */
    public boolean login(final String userId, final String password, final String service){
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what =  MSG_TYPES.MsgLogin;
                    if(isHostAvailiable("webportal.scu.edu.cn")) {
                        String result = sendGetRequest("http://www.baidu.com");
                        if(result.isEmpty()){
                            msg.what =  MSG_TYPES.MsgError;
                        }else {
                            queryString = result.substring(result.indexOf("wlanuserip"), result.indexOf("'</script>"));
                            String loginData = makeLoginString(userId, password ,service);
                            if(loginData.isEmpty())
                                msg.what =  MSG_TYPES.MsgError;
                            else {
                                String loginResult = sendPostRequest(URLS.loginUrl, loginData);
                                if(loginResult.isEmpty()){
                                    msg.what = MSG_TYPES.MsgError;
                                }else {
                                    msg.obj = loginResult;
                                    msg.arg1 = NETWORK_REQUEST.SUCCESS;
                                }
                            }
                        }
                    }else{
                        msg.arg1 = NETWORK_REQUEST.FAIL;
                        msg.obj = STRINGS.FAIL_TO_CONNECT_SERVER;
                    }
                    handler.sendMessage(msg);
                }
            });
            t.start();
            return true;
        }catch (Exception e){
            Logger.d(TAG, "NetworkService.E0097: "+e.toString());
            lastError = STRINGS.FAIL_TO_LOGIN + e.toString();
            return false;
        }
    }

    /**
     * 登录并通过handler返回登录结果
     * @param userId 账号
     * @param password 密码
     * @param service 服务类型
     * @param operatorUserId 服务账号
     * @param operatorPwd 服务密码
     * @return 未出错防护true
     */
    public boolean login(final String userId, final String password, final String service,final String operatorUserId,final String operatorPwd){
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = MSG_TYPES.MsgLogin;
                    if(isHostAvailiable("webportal.scu.edu.cn")) {
                        String result = sendGetRequest("http://www.baidu.com");
                        if(result.isEmpty()){
                            msg.what = MSG_TYPES.MsgError;
                        }else {
                            queryString = result.substring(result.indexOf("wlanuserip"), result.indexOf("'</script>"));
                            String loginData = makeLoginString(userId, password ,service,operatorUserId,operatorPwd);
                            if(loginData.isEmpty())
                                msg.what = MSG_TYPES.MsgError;
                            else {
                                String loginResult = sendPostRequest(URLS.loginUrl, loginData);
                                if(loginResult.isEmpty()){
                                    msg.what = MSG_TYPES.MsgError;
                                }else {
                                    msg.obj = loginResult;
                                    msg.arg1 = NETWORK_REQUEST.SUCCESS;
                                }
                            }
                        }
                    }else{
                        msg.arg1 = NETWORK_REQUEST.FAIL;
                        msg.obj = STRINGS.FAIL_TO_CONNECT_SERVER;
                    }
                    handler.sendMessage(msg);
                }
            });
            t.start();
            return true;
        }catch (Exception e){
            Logger.d(TAG, "NetworkService.E0097: "+e.toString());
            lastError = STRINGS.FAIL_TO_LOGIN + e.toString();
            return false;
        }
    }

    /**
     * 获取判读是否已经在线的信息
     * @return 信息
     */
    public boolean isAlreadyOnline(){
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = MSG_TYPES.MsgIsOnline;
                    if(isHostAvailiable("webportal.scu.edu.cn")) {
                        String resultS = sendPostRequest(URLS.userInfoUrl,"userIndex=");
                        if(resultS.isEmpty()){
                            msg.what = MSG_TYPES.MsgError;
                        }else {
                            msg.obj = resultS;
                            msg.arg1 = NETWORK_REQUEST.SUCCESS;
                        }
                    }else{
                        msg.arg1 = NETWORK_REQUEST.FAIL;
                        msg.obj = STRINGS.FAIL_TO_CONNECT_SERVER;
                    }
                    handler.sendMessage(msg);
                }
            });
            t.start();
            return true;
        }catch (Exception e){
            Logger.d(TAG, "NetworkService.E0128: "+e.toString());
            lastError = STRINGS.FAIL_TO_GETUSERINFO + e.toString();
            return false;
        }
    }

    /**
     * 获取用户信息
     * @param userIndex userIndex
     * @return 未出错返回true
     */
    public boolean getUserInfo(final String userIndex){
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = MSG_TYPES.MsgUserinfo;
                    if(isHostAvailiable("webportal.scu.edu.cn")) {
                        String resultS = sendPostRequest(URLS.userInfoUrl,"userIndex="+userIndex);
                        if(resultS.isEmpty())
                            msg.what = MSG_TYPES.MsgError;
                        else {
                            msg.obj = resultS;
                            msg.arg1 = NETWORK_REQUEST.SUCCESS;
                        }
                    }else{
                        msg.arg1 = NETWORK_REQUEST.FAIL;
                        msg.obj = STRINGS.FAIL_TO_CONNECT_SERVER;
                    }
                    handler.sendMessage(msg);
                }
            });
            t.start();
            return true;
        }catch (Exception e){
            Logger.d(TAG, "NetworkService.E0159: "+e.toString());
            lastError = STRINGS.FAIL_TO_GETUSERINFO + e.toString();
            return false;
        }
    }

    /**
     * 通过userIndex注销
     * @param userIndex userIndex
     * @return 未出错返回true
     */
    public boolean logout(final String userIndex){
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = MSG_TYPES.MsgLogout;
                    if(isHostAvailiable("webportal.scu.edu.cn")) {
                        String logoutData = "userIndex=" + userIndex;
                        String logoutResult = sendPostRequest(URLS.logoutUrl, logoutData);
                        if(logoutData.isEmpty()){
                            msg.what = MSG_TYPES.MsgError;
                        }else {
                            msg.obj = logoutResult;
                            msg.arg1 = NETWORK_REQUEST.SUCCESS;
                        }
                    }else {
                        msg.arg1 = NETWORK_REQUEST.FAIL;
                        msg.obj = STRINGS.FAIL_TO_CONNECT_SERVER;
                    }
                    handler.sendMessage(msg);
                }
            });
            t.start();
            return true;
        }catch (Exception e){
            Logger.d(TAG, "NetworkService.E0194: "+e.toString());
            lastError += STRINGS.FAIL_TO_LOGOUT + e.toString();
            return false;
        }
    }

    /**
     * 强制下线所有设备
     * @param userId 用户ID
     * @param pass 用户密码
     * @return 成功返回
     */
    public boolean logoutByUserIdAndPass(final String userId,final String pass){
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = MSG_TYPES.MsgLogoutByUserIdAndPass;
                    if(isHostAvailiable("webportal.scu.edu.cn")) {
                        String logoutString = "userId="+userId+"&pass="+pass;
                        String logoutResult = sendPostRequest(URLS.logoutByUserIdAndPassUrl, logoutString);
                        if(logoutResult.isEmpty()){
                            msg.what = MSG_TYPES.MsgError;
                        }else {
                            msg.obj = logoutResult;
                            msg.arg1 = NETWORK_REQUEST.SUCCESS;
                        }
                    }else {
                        msg.arg1 = NETWORK_REQUEST.FAIL;
                        msg.obj = STRINGS.FAIL_TO_CONNECT_SERVER;
                    }
                    handler.sendMessage(msg);
                }
            });
            t.start();
            return true;
        }catch (Exception e){
            Logger.d(TAG, "NetworkService.E0194: "+e.toString());
            lastError += STRINGS.FAIL_TO_LOGOUT + e.toString();
            return false;
        }
    }

    /**
     * 石佛连接wifi
     * @param context getApplicationContext()的返回值
     * @return 连接防护true
     */
    public boolean isWifiConnected(Context context){
//        return true;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            //Logger.d(TAG, "isWifiAvailable: " + info.getTypeName());
            if (info.getTypeName().equals("WIFI"))
                return info.isConnected();
            else {
                return false;
            }
        } catch (Exception e) {
            Logger.d(TAG, "NetworkService.E0218: " + e.toString());
            lastError = STRINGS.FAIL_TO_CHECK_WIFI+e.toString();
            return false;
        }
    }

    /**
     * 指定host和port的socket否可连接
     * @param host 主机
     * @return 成功返回true
     */
    public static boolean isHostAvailiable(String host){
        boolean status = false;
        try{
            //status = InetAddress.getByName(host).isReachable(timeOut);
            InetAddress add = InetAddress.getByName(host);
            Socket socket = new Socket(add,80);
            status = socket.isConnected();
            socket.close();
            //Logger.d(TAG, "isHostAvailiable: "+status);
        }
        catch(IOException e){
            Logger.d(TAG, "NetworkService.E0241: "+e.toString());
            return  false;
        }
        return status;
    }

    /**
     * 制作登录需要的数据
     * @param userId 账号
     * @param password 密码
     * @param service 服务类型
     * @return 数据
     */
    private String makeLoginString(String userId,String password,String service){
        if(userId.isEmpty()){
            return "";
        }
        if(password.isEmpty()){
            return "";
        }
        if(queryString.isEmpty()){
            return "";
        }
        if(service.isEmpty()){
            return "";
        }
        String loginString = "";
        try {
            loginString = "userId=" + URLEncoder.encode(URLEncoder.encode(userId,"utf-8"),"utf-8")
                    + "&password=" + URLEncoder.encode(URLEncoder.encode(password,"utf-8"),"utf-8")
                    + "&service="+service+"&queryString=" + URLEncoder.encode(queryString, "utf-8")
                    + "&operatorPwd=&operatorUserId=&validcode=";
        }catch (UnsupportedEncodingException e){
            Logger.d(TAG, "NetworkService.E0265: "+e.toString());
            lastError = e.toString();
            return "";
        }
        return loginString;
    }

    /**
     * 制作登陆数据
     * @param userId 账号
     * @param password 密码
     * @param service 服务类型
     * @param operatorUserId 对应服务的账号
     * @param operatorPwd 对应服务的密码
     * @return 返回制作成功的字符串
     */
    private String makeLoginString(final String userId, final String password, final String service,final String operatorUserId,final String operatorPwd){
        if(userId.isEmpty()){
            return "";
        }
        if(password.isEmpty()){
            return "";
        }
        if(queryString.isEmpty()){
            return "";
        }
        if(service.isEmpty()){
            return "";
        }
        if(operatorUserId.isEmpty()){
            return "";
        }
        if(operatorPwd.isEmpty()){
            return "";
        }
        String loginString = "";
        try {
            loginString = "userId=" + URLEncoder.encode(URLEncoder.encode(userId,"utf-8"),"utf-8")
                    + "&password=" +URLEncoder.encode(URLEncoder.encode(password,"utf-8"),"utf-8")
                    + "&service="+service+"&queryString=" + URLEncoder.encode(queryString, "utf-8")
                    + "&operatorPwd="+URLEncoder.encode(URLEncoder.encode(operatorPwd,"utf-8"),"utf-8")
                    + "&operatorUserId="+URLEncoder.encode(URLEncoder.encode(operatorUserId,"utf-8"),"utf-8")
                    + "&validcode=";
        }catch (UnsupportedEncodingException e){
            Logger.d(TAG, "NetworkService.E0265: "+e.toString());
            lastError = e.toString();
            return "";
        }
        return loginString;
    }
    /**
     * 发送get请求
     * @param url 要请求的url
     * @return 返回请求结果
     */
    private String sendGetRequest(String url)
    {
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
                result += "\n";
            }
            //Logger.d(TAG, "sendGetRequest: "+result);
        } catch (Exception e) {
            Logger.d(TAG,"NetworkService.E0299: "+e.toString());
            lastError = STRINGS.FAIL_TO_SENDGETREQUEST + e.toString();
            return "";
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                Logger.d(TAG, "NetworkService.E0313: "+ex.toString());
            }
        }
        return result;
    }

    /**
     * 发送post请求
     * @return
     */
    private String sendPostRequest(String url, String data){
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(data);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            Logger.d(TAG, "NetworkService.E0351: "+e.toString());
            lastError = STRINGS.FAIL_TO_SENDPOSTREQUEST + e.toString();
            return "";
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                Logger.d(TAG, "NetworkService.E0365: "+ex.toString());
            }
        }
        return result;
    }
}
