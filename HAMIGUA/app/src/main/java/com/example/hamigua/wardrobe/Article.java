package com.example.hamigua.wardrobe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamigua.R;

public class Article extends AppCompatActivity {

    TextView tvRes;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //requestCode : 是requestPermissions()方法中後面的那個數字
        //permissions : 回傳申請的內容
        //grantResults : 權限許可狀態,回傳0代表已取得，-1代表未取得
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        StringBuffer word = new StringBuffer();
        switch (permissions.length) {
            case 1:
                if (permissions[0].equals(Manifest.permission.CAMERA)) word.append("相機權限");
                else word.append("儲存權限");
                if (grantResults[0] == 0) word.append("已取得");
                else word.append("未取得");
                word.append("\n");
                if (permissions[0].equals(Manifest.permission.CAMERA)) word.append("儲存權限");
                else word.append("相機權限");
                word.append("已取得");

                break;
            case 2:
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.CAMERA)) word.append("相機權限");
                    else word.append("儲存權限");
                    if (grantResults[i] == 0) word.append("已取得");
                    else word.append("未取得");
                    if (i < permissions.length - 1) word.append("\n");
                }
                break;
        }
        tvRes.setText(word.toString());
    }

    //標題列ActionBar的左上角返回鍵監聽事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        tvRes = findViewById(R.id.tvRes);

        boolean cameraHasGone = checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        boolean externalHasGone = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions;
            if (!cameraHasGone && !externalHasGone) {//如果兩個權限都未取得
                permissions = new String[2];
                permissions[0] = Manifest.permission.CAMERA;
                permissions[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            } else if (!cameraHasGone) {//如果只有相機權限未取得
                permissions = new String[1];
                permissions[0] = Manifest.permission.CAMERA;
            } else if (!externalHasGone) {//如果只有存取權限未取得
                permissions = new String[1];
                permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            } else {
                tvRes.setText("相機權限已取得\n儲存權限已取得");
                return;
            }
            requestPermissions(permissions, 100);
        }
    }
}