package cn.edu.scu.creator.networkclient;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.R.attr.button;
import static android.R.attr.data;

public class CropActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.TAG;
    private CropImageView mCropView ;
    private Button btOK;
    private Button btCancel;
    private Button btRotateLeft;
    private Button btRotateRight;

    private String srcPicPath;
    private String dstPicPath;

    /**
     * 创建本Activity需要的Intent
     * @param activity 调用本Activity的Activity
     * @param srcPicPath 原图路径
     * @param dstPicPath 保存路径
     * @return Intent
     */
    public static Intent createIntent(AppCompatActivity activity,String srcPicPath, String dstPicPath){
        Intent intent = new Intent(activity,CropActivity.class);
        intent.putExtra("srcPicPath", srcPicPath);
        intent.putExtra("dstPicPath", dstPicPath);
        return intent;
    }

    /**
     * 初始按钮和mCropView组件
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        mCropView = (CropImageView)findViewById(R.id.cropImageView);
        mCropView.setInitialFrameScale(0.8f);                      //设置裁剪框大小
        mCropView.setCropMode(CropImageView.CropMode.RATIO_9_16);  //设置裁剪框比例
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {      //设置裁剪框颜色
            mCropView.setFrameColor(getResources().getColor(R.color.frame,this.getTheme()));
            mCropView.setHandleColor(getResources().getColor(R.color.handle,this.getTheme()));
            mCropView.setGuideColor(getResources().getColor(R.color.guide,this.getTheme()));
        } else {
            mCropView.setFrameColor(getResources().getColor(R.color.frame));
            mCropView.setHandleColor(getResources().getColor(R.color.handle));
            mCropView.setGuideColor(getResources().getColor(R.color.guide));
        }

        btOK = (Button)findViewById(R.id.btOK);
        btOK.setOnClickListener(onClickListener);
        btCancel = (Button)findViewById(R.id.btCancel);
        btCancel.setOnClickListener(onClickListener);
        btRotateLeft = (Button)findViewById(R.id.btRotateLeft);
        btRotateLeft.setOnClickListener(onClickListener);
        btRotateRight = (Button)findViewById(R.id.btRotateRight);
        btRotateRight.setOnClickListener(onClickListener);

        initCropView();
    }

    /**
     * 从Intent获取原图路径和保存图片路径，并载入到mCropView控件中
     */
    private void initCropView(){
        try {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                srcPicPath = bundle.getString("srcPicPath"); // 原图路径
                dstPicPath = bundle.getString("dstPicPath"); // 存储路径
                BitmapFactory.Options option = new BitmapFactory.Options();
                File file = new File(srcPicPath);
                int maxSize = (int)(1.0*1024*1024);
                if(file.length()>maxSize){ //如果原图大于1M，压缩，避免程序崩溃和加快载入速度
                    int inSampleSize = (int)(file.length()/maxSize);
                    if(inSampleSize <= 0){
                        inSampleSize = 1;
                    }
                    option.inSampleSize = inSampleSize;
                    Log.d(TAG, "initCropView: "+option.inSampleSize);
                }
                Bitmap bitmap = BitmapFactory.decodeFile(srcPicPath,option);
                mCropView.setImageBitmap(bitmap);
                if(bitmap.isRecycled()){
                    bitmap.recycle();
                }
            }
        }catch (Exception e){
            Logger.d(TAG, "CropActivity.E0104: "+e.toString());
            showMessage(e.toString());
            returnToMainActivity("",RESULT_CANCELED);
        }
    }

    /**
     * 按钮监听器
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btOK:
                    saveBitmapToPath(mCropView.getCroppedBitmap(),dstPicPath);
                    break;
                case R.id.btCancel:
                    returnToMainActivity("",RESULT_CANCELED);
                    break;
                case R.id.btRotateLeft:
                    rotateImage(true);
                    break;
                case R.id.btRotateRight:
                    rotateImage(false);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 保存图像
     * @param bitmap 要保存的图像
     * @param path 保存路径
     */
    private void saveBitmapToPath(Bitmap bitmap,String path){
//        mCropView.setImageBitmap(bitmap);
        try {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            if(bitmap.isRecycled()){
                bitmap.recycle();
            }
        } catch (IOException e) {
            Logger.d(TAG, "CropActivity.E0148: "+e.toString());
            showMessage(e.toString());
            returnToMainActivity("",RESULT_CANCELED);
        }
        returnToMainActivity(path,RESULT_OK);
    }

    /**
     * 返回上一个Activity，并将保存路径放在Intent中返回
     * @param dstPath 保存路径
     * @param result 结果
     */
    private void returnToMainActivity(String dstPath,int result){
        Intent intent = new Intent();
        intent.putExtra("dstPath", dstPath);
        setResult(result, intent);
        finish();
    }

    /**
     * 旋转图片
     */
    private void rotateImage(boolean rotateLeft){
        if(rotateLeft)
            mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
        else
            mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
    }

    private void showMessage(String msg)
    {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
}
