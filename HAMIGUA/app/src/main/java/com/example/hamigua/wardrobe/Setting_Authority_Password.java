package com.example.hamigua.wardrobe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamigua.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class Setting_Authority_Password extends AppCompatActivity {

    private TextInputEditText edtPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    //標題列ActionBar的左上角返回鍵監聽事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_authority_password);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        edtPassword = findViewById(R.id.resetPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);
        firebaseAuth = FirebaseAuth.getInstance();

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
                Intent intent_Setting_Authority = new Intent(Setting_Authority_Password.this, Setting_Authority.class);
                startActivity(intent_Setting_Authority);
            }
        });
    }
    private void resetPassword() {

        //取得使用者輸入轉成字串並用trim()去除空白換行
        String email = edtPassword.getText().toString().trim();

        //若沒有輸入電子信箱，則跳出此方法
        if(email.isEmpty()) {
            edtPassword.setError("電子信箱不得為空白");
            edtPassword.requestFocus();
            return;
        }

        //若沒有輸入有效的電子信箱，則跳出此方法
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtPassword.setError("請輸入有效的電子信箱");
            edtPassword.requestFocus();
            return;
        }

        //顯示進度條
        progressBar.setVisibility(View.VISIBLE);

        //透過authentication發送重設密碼的mail
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {

                if(task.isSuccessful()){
                    //隱藏進度條
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Setting_Authority_Password.this, "已發送電子郵件至\n" + email + "\n請查看並設置新密碼", Toast.LENGTH_SHORT).show();
                } else {
                    //隱藏進度條
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Setting_Authority_Password.this, "該E-mail尚未被註冊，請重新嘗試", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}