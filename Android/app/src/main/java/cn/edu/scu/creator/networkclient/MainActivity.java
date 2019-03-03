package cn.edu.scu.creator.networkclient;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cn.edu.scu.creator.networkclient.ConstValues.*;

public class MainActivity extends AppCompatActivity {

    public final static String TAG = STRINGS.TAG;
    private final static boolean isHeigerThanAnroidM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    private EditText etUserId;
    private EditText etPassword;
    private Button btLogin;
    private Button btSettings;
    private CheckBox cbSavePassword;

    private NetworkService networkService;
    private Handler handler;
    private String userIndex;
    private int buttonColor;
    private boolean autoLogin;
    private ConstraintLayout layout;

    private SharedPreferences sp;
    private Editor config;

    private boolean hasWriteStoragePermission;
    private boolean hasReadStoragePermission;

    private String service = "internet";
    private String operatorUserId = "";
    private String operatorPwd = "";

    private DrawerLayout menuDrawerLayout;
    private ListView menuListView;
    private boolean isMenuDrawerLayoutOpened;
    private List<String> menuList;
    private ArrayAdapter<String> menuArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        this.initialize();
    }

    private void initialize(){
        isMenuDrawerLayoutOpened = false;
        userIndex = "";
        sp = getApplicationContext().getSharedPreferences("config",Context.MODE_PRIVATE);
        autoLogin = sp.getBoolean("autoLogin",false);
        menuList = getMenuList();
        menuArrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.layout_item,menuList);

        buttonColor = Color.argb(60,60,60,100);

        etUserId = (EditText)findViewById(R.id.etUserId);
        etPassword = (EditText)findViewById(R.id.etPassword);
        btLogin = (Button)findViewById(R.id.btLogin);
        btSettings = (Button)findViewById(R.id.btSettings);
        cbSavePassword = (CheckBox)findViewById(R.id.cbSavePassword);
        btLogin.setBackgroundColor(buttonColor);
        layout = (ConstraintLayout)findViewById(R.id.main_layout);
        menuDrawerLayout = (DrawerLayout)findViewById(R.id.menuDrawerLayout);
        menuDrawerLayout.addDrawerListener(drawerListener);
        menuListView = (ListView)findViewById(R.id.menuListView);
        menuListView.setAdapter(menuArrayAdapter);
        menuListView.setOnItemClickListener(onItemClickListener);

        readUserInfo(getApplicationContext());
        hasReadStoragePermission = false;
        hasWriteStoragePermission = false;
        if(isHeigerThanAnroidM)
            checkPermissions();
        else{
            hasWriteStoragePermission = true;
            hasReadStoragePermission = true;
        }

        //设置监听器
        etUserId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                config = sp.edit();
                config.putString("userId",s.toString());
                config.apply();
            }
        });
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(cbSavePassword.isChecked()) {
                    config = sp.edit();
                    config.putString("password", s.toString());
                    config.apply();
                }
            }
        });
        cbSavePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                config = sp.edit();
                config.putString("userId", etUserId.getText().toString().trim());
                config.putBoolean("savePassword",isChecked);
                if(isChecked) {
                    config.putString("password", etPassword.getText().toString().trim());
                }else{
                    config.putString("password", "");
                }
                config.apply();
            }
        });
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(etUserId.getText().toString().trim().isEmpty()){
                        etUserId.requestFocus();
                        showMessage(STRINGS.USER_ID_IS_EMPTY);
                    }
                    else if(etPassword.getText().toString().trim().isEmpty()){
                        etPassword.requestFocus();
                        showMessage(STRINGS.PASSWORD_IS_EMPTY);
                    }else {
                        if(!networkService.isWifiConnected(getApplicationContext())){
                            showMessage("没有连接WIFI呢");
                        }
                        else if (btLogin.getText().toString().equals(STRINGS.StrLogin)) {
                            networkService.isAlreadyOnline();
                            setEnabled(false);
                        } else {
                            networkService.logout(userIndex);
                            setEnabled(false);
                        }
                    }
                }catch (Exception e){
                    Logger.d(TAG, "onClick: "+e.toString());
                }
            }
        });
        btSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                createMenu();
                menuDrawerLayout.openDrawer(Gravity.END);
            }
        });
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_TYPES.MsgLogin:
                        processLoginMsg(msg);
                        break;
                    case MSG_TYPES.MsgLogout:
                        processLogoutMessage(msg);
                        break;
                    case MSG_TYPES.MsgIsOnline:
                        processIsOnlineMessage(msg);
                        break;
                    case MSG_TYPES.MsgLogoutByUserIdAndPass:
                        processLogoutByUserIdAndPassMessage(msg);
                        break;
                    case MSG_TYPES.MsgError:
                        processErrorMessage();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        networkService = new NetworkService(getApplicationContext(),handler);
        if(autoLogin){
            String userId = etUserId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if(userId.isEmpty()){
                etUserId.requestFocus();
                showMessage(STRINGS.USER_ID_IS_EMPTY);
                return;
            }
            if(password.isEmpty()){
                etPassword.requestFocus();
                showMessage(STRINGS.PASSWORD_IS_EMPTY);
                return;
            }
            if(!networkService.isWifiConnected(getApplicationContext())){
                showMessage("尚未连接WIFI，自动登录失败");
                return;
            }
            if (btLogin.getText().toString().equals(STRINGS.StrLogin)) {
                if(!networkService.isAlreadyOnline())
                    processErrorMessage();
                setEnabled(false);
            } else {
                if(!networkService.logout(userIndex))
                    processErrorMessage();
                setEnabled(false);
            }
        }
    }

    /**
     * 生成菜单项
     * @return 返回菜单项的List
     */
    private List<String> getMenuList(){

        List<String> data = new ArrayList<>();
        data.add("查看用户信息");
        if(!autoLogin)
            data.add("设为自动登录");
        else
            data.add("取消自动登录");
        data.add("下线所有设备");
        data.add("选择流量出口");
        data.add("更换背景图片");
        data.add("恢复默认背景");
        return data;
    }

    private int clickedItemPosition = 7;
    /**
     * DrawerLayout的监听器
     */
    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {}
        @Override
        public void onDrawerOpened(View drawerView) {
            isMenuDrawerLayoutOpened = true;
        }
        @Override
        public void onDrawerClosed(View drawerView) { //等到drawerLayout关闭后才执行操作，避免出现卡顿现象
            isMenuDrawerLayoutOpened = false;
            switch (clickedItemPosition) {
                case 0:
                    showUserInfoActivity();
                    break;
                case 1:
                    setAutoLogin();
                    break;
                case 2:
                    logoutByUserIdAndPass();
                    break;
                case 3:
                    chooseService();
                    break;
                case 4:
                    changeBackground();
                    break;
                case 5:
                    resetBackgound();
                    break;
                default:
                    break;
            }
            clickedItemPosition = 7;
        }
        @Override
        public void onDrawerStateChanged(int newState) {}
    };

    /**
     * ListView的监听器
     */
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            menuDrawerLayout.closeDrawer(Gravity.END);
            clickedItemPosition = position;
        }
    };

    /**
     * 处理强制下线的消息
     * @param msg 消息
     */
    private void processLogoutByUserIdAndPassMessage(Message msg){
        try{
            if(msg.arg1 == NETWORK_REQUEST.FAIL){
                showMessage(msg.obj.toString());
                setEnabled(true);
                return;
            }
            JSONObject jsonObj = new JSONObject(msg.obj.toString());
            String message = jsonObj.getString("message");
            showMessage(message);
        }catch (JSONException e){
            Logger.d(TAG, "MainActivity.E0227: "+e.toString());
        }
    }

    /**
     * 处理登录消息
     * @param msg 消息
     */
    private void processLoginMsg(Message msg){
        //解析Json数据
        try{
            if(msg.arg1 == NETWORK_REQUEST.FAIL){
                showMessage(msg.obj.toString());
                setEnabled(true);
                return;
            }
            JSONObject jsonObj = new JSONObject(msg.obj.toString());
            String result = jsonObj.getString("result");
            String message = jsonObj.getString("message");
            if(result.equals("success")){
                userIndex = jsonObj.getString("userIndex");
                btLogin.setEnabled(true);
                btLogin.setText(R.string.logout);
                btLogin.setBackgroundColor(Color.TRANSPARENT);
            }
            else{
                if(message.contains("您未绑定服务对应的运营商!")){
                    String userId = etUserId.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    networkService.login(userId,password,service,operatorUserId,operatorPwd);
                }else if(message.contains("验证码错误")){
                    showMessage("密码输入错误次数太多，请1分钟后重试");
                    setEnabled(true);
                    btLogin.setBackgroundColor(buttonColor);
                }else{
                    setEnabled(true);
                    btLogin.setBackgroundColor(buttonColor);
                    showMessage(message);
                }
            }
        }catch (JSONException e){
            Logger.d(TAG, "MainActivity.E0227: "+e.toString());
        }
    }

    /**
     * 处理注销消息
     * @param msg 消息
     */
    private void processLogoutMessage(Message msg){
        try{
            if(msg.arg1 == NETWORK_REQUEST.FAIL){
                showMessage(msg.obj.toString());
                setEnabled(true);
                return;
            }
            JSONObject jsonObj = new JSONObject(msg.obj.toString());
            String result = jsonObj.getString("result");
            String message = jsonObj.getString("message");
            if(result.equals("success")){
                setEnabled(true);
                btLogin.setText(R.string.login);
                btLogin.setBackgroundColor(buttonColor);
            }else{
                setEnabled(true);
                showMessage(message);
                btLogin.setText(R.string.login);
            }
        }catch (JSONException e){
            Logger.d(TAG, "MainActivity.E0250: "+e.toString());
        }
    }

    /**
     * 处理是否在线的消息
     * @param msg 消息
     */
    private  void processIsOnlineMessage(Message msg){
        try {
            if(msg.arg1 == NETWORK_REQUEST.FAIL){
                showMessage(msg.obj.toString());
                setEnabled(true);
                return;
            }
            JSONObject jsonObj = new JSONObject(msg.obj.toString());
            if(jsonObj.has("userIndex"))
                this.userIndex = jsonObj.getString("userIndex");
            if(jsonObj.has("result")){
                if(jsonObj.getString("result").equals("fail")){//用户不在线，登录
                    String userId = etUserId.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    if(!networkService.login(userId, password,service))
                        processErrorMessage();
                }
                else{
                    if(jsonObj.has("userId")){
                        if(!jsonObj.getString("userId").equals(etUserId.getText().toString().trim())){
                            setEnabled(true);
                            AlertDialog dialog = new AlertDialog.Builder(this)
                                    .setMessage("已有别的账号在此IP登录，是否强制下线？")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(!networkService.logout(userIndex))
                                                processErrorMessage();
                                        }
                                    }).setNegativeButton("取消", null).create();
                            dialog.show();
                            return;
                        }else{
                            etUserId.setEnabled(false);
                            etPassword.setEnabled(false);
                            btLogin.setEnabled(true);
                            btLogin.setText(R.string.logout);
                            btLogin.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Logger.d(TAG, "MainActivity.E0276: "+e.toString());
        }
    }

    /**
     * 处理错误消息
     */
    private void processErrorMessage(){
        showMessage(networkService.getLastError());
        setEnabled(true);
        btLogin.setText(R.string.login);
    }

    /**
     * 设置控件是否enable
     * @param enabled enable
     */
    private void setEnabled(boolean enabled){
        btLogin.setEnabled(enabled);
        etUserId.setEnabled(enabled);
        etPassword.setEnabled(enabled);
        cbSavePassword.setEnabled(enabled);
    }

    /**
     * 显示提示信息
     * @param msg 信息
     */
    private void showMessage(String msg)
    {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

//    private void showMessage(String msg,boolean isLongTime)
//    {
//        if(isLongTime)
//            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
//        else
//            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
//    }

    /**
     * 重写返回事件，当处于登录状态时实现Home键的功能，否则直接调用原来的返回键功能
     */
    @Override
    public void onBackPressed()
    {
        if(!isMenuDrawerLayoutOpened) {
            if (btLogin.getText().toString().equals("登录")) //退出
            {
                super.onBackPressed();
            } else { //只是返回桌面
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        }else{
            menuDrawerLayout.closeDrawer(Gravity.END);
        }
    }
    /**
     * 读取用户信息
     * @param context 当前应用的上下文环境
     */
    private void readUserInfo(Context context)
    {
//        SharedPreferences sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        String userId = sp.getString("userId","");
        etUserId.setText(userId);
        etPassword.setText(sp.getString("password",""));
        cbSavePassword.setChecked(sp.getBoolean("savePassword",false));
        if(!userId.isEmpty())
            etPassword.requestFocus();
        etPassword.setSelection(etPassword.getText().length());
        //读取背景路径，并设置背景
        String backgroundPath = sp.getString("backgroundPath","");
        if(!backgroundPath.isEmpty()){
            File file = new File(backgroundPath);
            if(file.exists()) {
                setBackground(backgroundPath);
            }else{
                this.setDefaultBackground();
            }
        }
        service = sp.getString("service","internet");
        operatorUserId = sp.getString("operatorUserId","");
        operatorPwd = sp.getString("operatorPwd","");
    }

    /**
     * 设置默认背景
     */
    private void setDefaultBackground(){
        layout.setBackgroundResource(R.drawable.background);
    }


//    /**
//     * 创建菜单
//     */
//    private void createMenu() {
//        String[] items = {
//                "                    查看用户信息","                    设为自动登录",
//                "                    下线所有设备","                    选择网络出口",
//                "                    更换背景图片","                    恢复默认背景"};
//        if(autoLogin)
//            items[1] = "                    取消自动登录";
//        AlertDialog.Builder menuDialogBuilder = new AlertDialog.Builder(MainActivity.this);
//        menuDialogBuilder.setItems(items, onItemClickedListener);
//        menuDialogBuilder.show();
//    }
//
//    /**
//     * 菜单选择监听器
//     */
//    private DialogInterface.OnClickListener onItemClickedListener = new DialogInterface.OnClickListener() {
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            switch (which) {
//                case 0:
//                    showUserInfoActivity();
//                    break;
//                case 1:
//                    setAutoLogin();
//                    break;
//                case 2:
//                    logoutByUserIdAndPass();
//                    break;
//                case 3:
//                    chooseService();
//                    break;
//                case 4:
//                    changeBackground();
//                    break;
//                case 5:
//                    resetBackgound();
//                    break;
//                default:
//                    break;
//            }
//        }
//    };

    /**
     * 选择服务
     */
    private void chooseService(){
        startActivityForResult(ServiceActivity.createIntent(this), REQUEST_CODES.REQUEST_CHOOSE_SERVICE);
    }

    /**
     * 设置和取消自动登录
     */
    private void setAutoLogin(){
        config = sp.edit();
        config.putBoolean("autoLogin",!autoLogin);
        config.apply();
        if(!autoLogin){
            menuList.set(1,"取消自动登录");
            showMessage("已设为自动登录");
        }else{
            showMessage("已取消自动登录");
            menuList.set(1,"设为自动登录");
        }
        menuArrayAdapter.notifyDataSetChanged();
        autoLogin = !autoLogin;
    }

    /**
     * 将背景设置为默认背景
     */
    private void resetBackgound(){
        config = sp.edit();
        config.putString("backgroundPath","");
        config.apply();
        setDefaultBackground();
    }

    /**
     * 通过用户名和密码强制下线所有在线设备
     */
    private void logoutByUserIdAndPass(){
        String userId = etUserId.getText().toString().trim();
        if(userId.isEmpty()){
            this.showMessage(STRINGS.USER_ID_IS_EMPTY);
            etUserId.requestFocus();
            return;
        }
        String pass = etPassword.getText().toString().trim();
        if(pass.isEmpty()){
            this.showMessage(STRINGS.PASSWORD_IS_EMPTY);
            etPassword.requestFocus();
            return;
        }
        if(!networkService.logoutByUserIdAndPass(userId,pass)){
            processErrorMessage();
        }
    }

    /**
     * 显示用户信息
     */
    private void showUserInfoActivity(){
        if(btLogin.getText().equals("登录")) {
            showMessage("尚未登陆，无法获取用户信息");
        }
        else {
            Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
            //用Bundle携带数据
            Bundle bundle = new Bundle();
            bundle.putString("userIndex", this.userIndex);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    /**
     * 更改背景
     */
    private void changeBackground(){
        if(hasReadStoragePermission) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODES.RESULT);
        }
    }

    /**
     * Andoid M及以上版本需要弹出窗口获取读写权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions(){
        int hasReadPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
            hasReadStoragePermission = false;
            final MainActivity activty=this;
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage("需要赋予访问存储的权限，不开启将无法更换软件背景")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activty,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODES.REQUEST_READ_PERMISSION);
                        }
                    }).setNegativeButton("取消", null).create();
            dialog.show();
        }else{
            hasReadStoragePermission = true;
        }
    }

    /**
     * 获取读写权限设置结果
     * @param requestCode 请求码
     * @param permissions 权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODES.REQUEST_READ_PERMISSION){
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                hasReadStoragePermission = true;
            }else{
                hasReadStoragePermission = false;
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage("需要赋予访问存储的权限，不开启将无法更换软件背景")
                        .setPositiveButton("确定", null).create();
                dialog.show();
            }
        }
        if (requestCode == REQUEST_CODES.REQUEST_WRITE_PERMISSION){
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //用户同意使用write
                hasWriteStoragePermission = true;
            }else{
                //用户不同意，自行处理即可
                hasWriteStoragePermission = false;
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage("需要赋予访问存储的权限，不开启将无法更换软件背景")
                        .setPositiveButton("确定", null).create();
                dialog.show();
            }
        }
    }

    /**
     * 启动别的Activity的结果
     * @param requestCode 请求标识
     * @param resultCode 结果
     * @param data 附带的Intent数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == REQUEST_CODES.RESULT && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if(cursor!=null)
            {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                {
                    cropPicture(picturePath);
                }else{
                    showMessage("无法存储图像");
                }
            }
            else
            {
                Logger.d(TAG, "MainActivity.E0481: 获取照片失败");
                showMessage("E0481: 获取照片失败");
            }
        }else if (requestCode == REQUEST_CODES.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            try {
                if (extras != null) {
                    String backgroundPath = extras.getString("dstPath");
                    setBackground(backgroundPath);
                }
            }catch (Exception e){
                Logger.d(TAG, "MainActivity.E0589: "+e.toString());
                showMessage("E0589: 裁剪照片失败"+e.toString());
            }
        }
        else if (requestCode == REQUEST_CODES.REQUEST_CHOOSE_SERVICE && resultCode == RESULT_OK && data != null){
            Bundle extras = data.getExtras();
            try {
                if (extras != null) {
                    String mService = extras.getString("service","internet");
                    mService = URLEncoder.encode(mService,"utf-8");
                    service = URLEncoder.encode(mService,"utf-8");
                    operatorUserId = extras.getString("operatorUserId","");
                    operatorPwd = extras.getString("operatorPwd","");
//                    Log.d(TAG, "onActivityResult: "+service+" "+operatorUserId+" "+operatorPwd);
                    config = sp.edit();
                    config.putString("service",service);
                    config.putString("operatorUserId",operatorUserId);
                    config.putString("operatorPwd",operatorPwd);
                    config.apply();
                }
            }catch (Exception e){
                Logger.d(TAG, "MainActivity.E0689: "+e.toString());
                showMessage("E0689: 获取服务信息失败"+e.toString());
            }
        }else{
            Logger.d(TAG, "onActivityResult: "+resultCode);
        }
    }

    /**
     * 设置背景
     * @param imPath 图像路径
     */
    public void setBackground(String imPath){
        try {
            File file = new File(imPath);
            if(file.exists()){
                Drawable drawable = Drawable.createFromPath(imPath);
                layout.setBackground(drawable);
            }else{
                setDefaultBackground();
            }
        }catch (Exception e){
            Logger.d(TAG, "MainActivity.E0600: "+e.toString());
            showMessage("0600: 设置背景失败"+e.toString());
        }
    }

    /**
     * 截图
     * @param path 原图路径
     */
    private void cropPicture(String path){
        try {
            File srcPic = new File(path);
            String toPath = Environment.getExternalStorageDirectory().getPath() + "/NetworkClient";
            File destDir = new File(toPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String savePath = toPath+"/bg.jpeg_";
            startActivityForResult(CropActivity.createIntent(this,path,savePath), REQUEST_CODES.REQUEST_CROP);
            //保存图片位置
            config = sp.edit();
            config.putString("backgroundPath",savePath);
            config.apply();
        }catch (Exception e){
            Logger.d(TAG, "MainActivity.E0620: "+e.toString());
            showMessage("E0620: 裁剪照片失败"+e.toString());
        }
    }
}
