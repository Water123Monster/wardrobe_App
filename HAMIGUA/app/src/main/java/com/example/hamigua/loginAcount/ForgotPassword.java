package com.example.hamigua.loginAcount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.hamigua.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class ForgotPassword extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private Button resetPassword;
    private ProgressBar progressBar;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        edtEmail = findViewById(R.id.resetPasswordEmail);
        resetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);

        //取得authentication的連接
        firebaseAuth = FirebaseAuth.getInstance();

        //重設密碼的button觸發事件，呼叫方法resetPassword()
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

    }

    //重設密碼的方法
    private void resetPassword() {

        //取得使用者輸入轉成字串並用trim()去除空白換行
        String email = edtEmail.getText().toString().trim();

        //若沒有輸入電子信箱，則跳出此方法
        if(email.isEmpty()) {
            edtEmail.setError("電子信箱不得為空白");
            edtEmail.requestFocus();
            return;
        }

        //若沒有輸入有效的電子信箱，則跳出此方法
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("請輸入有效的電子信箱");
            edtEmail.requestFocus();
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
                    Toast.makeText(ForgotPassword.this, "已發送電子郵件至\n" + email + "\n請查看並設置新密碼", Toast.LENGTH_SHORT).show();
                } else {
                    //隱藏進度條
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ForgotPassword.this, "該E-mail尚未被註冊，請重新嘗試", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}