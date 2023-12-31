package com.example.hamigua.loginAcount;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamigua.HomePage;
import com.example.hamigua.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private Button btnSignUp,btnLogIn, btnForgotPassword;
    private TextInputEditText edtEmail, edtPassword;
    private ProgressBar progressBar;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignUp=findViewById(R.id.btnSignUp);
        btnLogIn=findViewById(R.id.btnLogIn);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        edtEmail = findViewById(R.id.signInEmail);
        edtPassword = findViewById(R.id.signInPassword);
        progressBar = findViewById(R.id.progressBar);

        //取得authentication的連接
        firebaseAuth = FirebaseAuth.getInstance();

        //註冊的button觸發事件，跳轉至註冊頁面(SignUp.java)
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        //登入的button觸發事件，呼叫方法userLogin()
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });

        //忘記密碼的button觸發事件，跳轉至重設密碼頁面(ForgotPassword.java)
        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });
    }

    //若有使用者登入且已完成email驗證，則保持該使用者登入狀態，除非自行登出
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if((user != null)&&(user.isEmailVerified())){
            //已有使用者登入且完成email驗證，跳轉至Home_page頁面
            startActivity(new Intent(this, HomePage.class));
            finish();
        } else {
            //
        }
    }

    //使用者登入的方法
    //登入成功後跳轉頁面至App首頁(Home_page.java)
    protected void userLogin() {

        //取得使用者輸入轉成字串並用trim()去除空白換行
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

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

        //若沒有輸入密碼，則跳出此方法
        if(password.isEmpty()) {
            edtPassword.setError("密碼不得為空白");
            edtPassword.requestFocus();
            return;
        }

        //規定密碼長度必須>=6，則跳出此方法
        if(password.length() < 6) {
            edtPassword.setError("密碼長度不得小於6");
            edtPassword.requestFocus();
            return;
        }

        //顯示進度條
        progressBar.setVisibility(View.VISIBLE);

        //authentication登入驗證，透過email與password
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {

                //登入驗證成功，跳轉頁面到Home_page
                if(task.isSuccessful()){

                    //取得該登入使用者
                    FirebaseUser firebaseuser = firebaseAuth.getCurrentUser();
                    //若該使用者已完成email驗證
                    if(firebaseuser.isEmailVerified()) {
                        //隱藏進度條
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "登入成功，歡迎使用哈密瓜智慧衣櫃", Toast.LENGTH_SHORT).show();

                        //設置延遲，跳出登入訊息Toast後1000毫秒再進行跳轉頁面至App首頁(Home_page.java)
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent_login = new Intent(MainActivity.this, HomePage.class);
                                startActivity(intent_login);
                            }
                        }, 1000);
                    }
                    //若該使用者沒有完成email驗證
                    else {
                        //隱藏進度條
                        progressBar.setVisibility(View.GONE);
                        //發送驗證mail
                        firebaseuser.sendEmailVerification();
                        Toast.makeText(MainActivity.this, "您的帳號尚未通過email驗證\n已重新發送驗證至電子信箱\n" + email + "\n請驗證後重新登入", Toast.LENGTH_SHORT).show();
                    }
                }
                //登入失敗
                else {
                    //隱藏進度條
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "登入失敗，電子信箱或密碼輸入錯誤", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}