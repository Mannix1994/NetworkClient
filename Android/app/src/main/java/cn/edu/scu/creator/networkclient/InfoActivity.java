package cn.edu.scu.creator.networkclient;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cn.edu.scu.creator.networkclient.ConstValues.*;

public class InfoActivity extends AppCompatActivity {

    public final static String TAG = ConstValues.STRINGS.TAG;

    private Handler handler;
    private NetworkService networkService;
    private Timer timer;
    private UserInfo userInfo;

    private ProgressBar progressBar;
    private EditText eUserName;
    private EditText eUserID;
    private EditText eUserIp;
    private EditText ePortalIP;
    private EditText etUserGroup;
    private EditText eAccountFee;
    private EditText eMaxLeavingTime;
    private EditText eUserPackage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        this.initialize();
    }

    private void initialize(){
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        eUserName = (EditText)findViewById(R.id.eUserName);
        eUserID = (EditText)findViewById(R.id.eUserID);
        eUserIp = (EditText)findViewById(R.id.eUserIp);
        ePortalIP = (EditText)findViewById(R.id.ePortalIP);
        etUserGroup = (EditText)findViewById(R.id.etUserGroup);
        eAccountFee = (EditText)findViewById(R.id.eAccountFee);
        eMaxLeavingTime = (EditText)findViewById(R.id.eMaxLeavingTime);
        eUserPackage = (EditText)findViewById(R.id.eUserPackage);
        setControlsVisible(View.INVISIBLE);
        setControlsFocusable(false);

        userInfo = new UserInfo();
        userInfo.userIndex = getIntent().getExtras().getString("userIndex","");
        timer = new Timer();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case ConstValues.MSG_TYPES.MsgUserinfo:
                        processUserInfo(msg);
                        break;
                    case ConstValues.MSG_TYPES.MsgError:
                        processErrorMessage(msg);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        networkService = new NetworkService(getApplicationContext(),handler);
        networkService.getUserInfo(userInfo.userIndex);
    }

    /**
     * 处理用户信息
     * @param msg 消息
     */
    private void processUserInfo(Message msg){
        try {
            if(msg.arg1 == NETWORK_REQUEST.FAIL){
                showMessage(msg.obj.toString());
                return;
            }
            JSONObject jsonObj = new JSONObject(msg.obj.toString());
            if(jsonObj.has("result")){
                if(jsonObj.getString("result").equals("fail")){
                    showMessage("用户已经不在线了");
                    //onBackPressed();
                }
                else if(jsonObj.getString("result").equals("wait")){
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            networkService.getUserInfo(userInfo.userIndex);
                        }
                    },300);//300ms后重试
                }else if(jsonObj.getString("result").equals("success")){
                    showUserInfo(jsonObj);
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "InfoActivity.E0106: "+e.toString());
        }
    }

    /**
     * 显示用户信息
     * @param jsonObj 带有用户信息的json
     */
    private void showUserInfo(JSONObject jsonObj){
        setControlsVisible(View.VISIBLE);
        try {
            if(jsonObj.has("userIndex")){
                userInfo.userIndex = jsonObj.getString("userIndex");
            }
            if(jsonObj.has("userName")){
                userInfo.userName = jsonObj.getString("userName");
                CharSequence cs = "BOSS:"+userInfo.userName;
                eUserName.setText(cs);
            }
            if(jsonObj.has("userId")){
                userInfo.userId = jsonObj.getString("userId");
                CharSequence cs = "BOSSID:"+userInfo.userId;
                eUserID.setText(cs);
            }
            if(jsonObj.has("userIp")){
                userInfo.userIp = jsonObj.getString("userIp");
                CharSequence cs = "BOSSIP:"+userInfo.userIp;
                eUserIp.setText(cs);
            }
            if(jsonObj.has("portalIp")){
                userInfo.portalIp = jsonObj.getString("portalIp");
                CharSequence cs = "服务器IP:"+userInfo.portalIp;
                ePortalIP.setText(cs);
            }
            if(jsonObj.has("userGroup")){
                userInfo.userGroup = jsonObj.getString("userGroup");
                CharSequence cs = "用户组别:"+userInfo.userGroup;
                etUserGroup.setText(cs);
            }
            if(jsonObj.has("accountFee")){
                userInfo.accountFee = jsonObj.getString("accountFee");
                CharSequence cs = "累计费用:"+userInfo.accountFee;
                eAccountFee.setText(cs);
            }
            if(jsonObj.has("userPackage")){
                userInfo.userPackage = jsonObj.getString("userPackage");
                CharSequence cs = "用户套餐:"+userInfo.userPackage;
                eUserPackage.setText(cs);
            }
            if(jsonObj.has("maxLeavingTime")){
                userInfo.maxLeavingTime = jsonObj.getString("maxLeavingTime");
                CharSequence cs = "剩余时间:"+userInfo.maxLeavingTime;
                eMaxLeavingTime.setText(cs);
            }
            if(jsonObj.has("notify")){
                userInfo.notify = jsonObj.getString("notify");
                CharSequence cs = "用户名:"+userInfo.userName;
            }
        } catch (JSONException e) {
            Logger.d(TAG, "InfoActivity.E0162: "+e.toString());
        }

    }

    /**
     * 处理错误消息
     * @param msg 消息
     */
    private void processErrorMessage(Message msg){
        showMessage(networkService.getLastError());
    }

    /**
     * 显示提示信息
     * @param msg
     */
    private void showMessage(String msg)
    {
        //显示提示信息
//        CharSequence cs = msg;
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置控件是否可编辑
     * @param b 是否可编辑
     */
    private void setControlsFocusable(boolean b){
        eUserName.setFocusable(b);
        eUserName.setFocusable(b);
        eUserID.setFocusable(b);
        eUserIp.setFocusable(b);
        etUserGroup.setFocusable(b);
        eAccountFee.setFocusable(b);
        eMaxLeavingTime.setFocusable(b);
        eUserPackage.setFocusable(b);
        ePortalIP.setFocusable(b);
    }

    /**
     * 设置控件可见性
     * @param visible 是否可见
     */
    private void setControlsVisible(int visible){
        if(visible == View.VISIBLE)
            progressBar.setVisibility(View.INVISIBLE);
        else
            progressBar.setVisibility(View.VISIBLE);
        eUserName.setVisibility(visible);
        eUserID.setVisibility(visible);
        eUserIp.setVisibility(visible);
        etUserGroup.setVisibility(visible);
        eAccountFee.setVisibility(visible);
        eMaxLeavingTime.setVisibility(visible);
        eUserPackage.setVisibility(visible);
        ePortalIP.setVisibility(visible);
    }

    /**
     * 用户信息类
     */
    private class UserInfo{
        public String userIndex;
        public String userName;
        public String userId;
        public String userIp;
        public String portalIp;
        public String userGroup;
        public String accountFee;
        public String userPackage;
        public String maxLeavingTime;
        public String notify;

        public String toString(){
            return "用户名:"+userName+", 用户ID"+userId+", 用户IP:"+userIp+", 服务器IP:"+portalIp+", 用户组:"+userGroup+
                    ", 费用:"+accountFee+", 用户套餐:"+userPackage+", 剩余时长:"+maxLeavingTime+", 通知:"+notify;
        }
    }
}
