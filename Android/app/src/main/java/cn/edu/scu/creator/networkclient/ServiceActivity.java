package cn.edu.scu.creator.networkclient;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ServiceActivity extends AppCompatActivity {

    private Button btConfirm;
    private Button btChooseService;
    private EditText etOperatorUserId;
    private EditText etOperatorPwd;

    private String internet;
    private String chinaMobile;
    private String chinaUnicom;
    private String mobileOutput;
    private String unicomOutput;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor config;

    /**
     * 制作Intent
     * @param activity 母activity
     * @return 制作好的Intent
     */
    public static Intent createIntent(AppCompatActivity activity){
        Intent intent = new Intent(activity,ServiceActivity.class);
        return intent;
    }

    /**
     * 初始化
     * @param savedInstanceState 默认的参数
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_service);
        setTitle(R.string.choose_service);

        Resources resources = this.getResources();
        internet = resources.getString(R.string.internet);
        chinaMobile = resources.getString(R.string.china_mobile);
        chinaUnicom = resources.getString(R.string.china_unicom);
        mobileOutput = resources.getString(R.string.mobile_output);
        unicomOutput = resources.getString(R.string.unicom_output);

        initialize();
    }

    private void initialize(){
        sharedPreferences = getApplicationContext().getSharedPreferences("config",Context.MODE_PRIVATE);

        btConfirm = (Button) findViewById(R.id.btConfirm);
        btConfirm.setOnClickListener(onClickListener);

        btChooseService = (Button)findViewById(R.id.btChooseService);
        btChooseService.setOnClickListener(onClickListener);
        btChooseService.setText(sharedPreferences.getString("serviceName",internet));

        etOperatorUserId = (EditText)findViewById(R.id.etOperatorUserId);
        etOperatorUserId.setText(sharedPreferences.getString("operatorUserId",""));
        etOperatorPwd = (EditText)findViewById(R.id.etOperatorPwd);
        etOperatorPwd.setText(sharedPreferences.getString("operatorPwd",""));

        if(etOperatorUserId.getText().toString().isEmpty()){
            etOperatorUserId.requestFocus();
        }else{
            etOperatorPwd.requestFocus();
        }

        if(btChooseService.getText().toString().equals(internet)){
            etOperatorUserId.setEnabled(false);
            etOperatorPwd.setEnabled(false);
        }
    }

    /**
     * 按钮监听器
     */
    private View.OnClickListener onClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btChooseService:
                    createItems();
                    break;
                case R.id.btConfirm:
                    returnToMainActivity(RESULT_OK);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 创建服务类型项
     */
    private void createItems() {
        final String[] items = { "校园网","中国移动","中国联通"};
        AlertDialog.Builder menuDialogBuilder = new AlertDialog.Builder(ServiceActivity.this);
        menuDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(etOperatorUserId.getText().toString().trim().isEmpty()){
                    etOperatorUserId.requestFocus();
                }
                btChooseService.setText(items[which]);
                switch (which){
                    case 0:
                        etOperatorUserId.setText("");
                        etOperatorUserId.setHint(R.string.account);
                        etOperatorUserId.setEnabled(false);
                        etOperatorPwd.setText("");
                        etOperatorPwd.setEnabled(false);
                        break;
                    case 1:
                        etOperatorUserId.setEnabled(true);
                        etOperatorUserId.setHint(R.string.mobile_account);
                        etOperatorPwd.setEnabled(true);
                        break;
                    case 2:
                        etOperatorUserId.setEnabled(true);
                        etOperatorUserId.setHint(R.string.unicom_account);
                        etOperatorPwd.setEnabled(true);
                        break;
                    default:
                        break;
                }
            }
        });
        menuDialogBuilder.show();
    }

    /**
     * 将数据保存到Intent，返回到MainActivity
     * @param result 成功还是失败的标志
     */
    private void returnToMainActivity(int result){
        Intent intent = new Intent();
        String service = btChooseService.getText().toString();
        config = sharedPreferences.edit();
        config.putString("serviceName",service);
        config.apply();
        String operatorUserId = etOperatorUserId.getText().toString().trim();
        String operatorPwd = etOperatorPwd.getText().toString().trim();
        if(!service.equals(internet)) {
            //当选择的服务不是校园网时，检查账号密码是否为空
            if (operatorUserId.isEmpty()) {
                etOperatorUserId.requestFocus();
                showMessage("账号为空");
                return;
            }
            if (operatorPwd.isEmpty()) {
                etOperatorPwd.requestFocus();
                showMessage("密码为空");
                return;
            }
        }
        if(service.equals(internet)){
            intent.putExtra("service","internet");
        }else if(service.equals(chinaMobile)){
            intent.putExtra("service",mobileOutput);
        }else if(service.equals(chinaUnicom)){
            intent.putExtra("service",unicomOutput);
        }else{
            result = RESULT_CANCELED;
        }
        intent.putExtra("operatorUserId",operatorUserId);
        intent.putExtra("operatorPwd",operatorPwd);
        setResult(result, intent);
        finish();
    }

    private void showMessage(String msg)
    {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
}
