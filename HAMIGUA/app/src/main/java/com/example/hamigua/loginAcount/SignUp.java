package com.example.hamigua.loginAcount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.hamigua.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private Button btnNext;
    private TextInputEditText edtUsername, edtEmail, edtPhoneNumber, edtBirth, edtPassword;
    private ProgressBar progressBar;
    private String gender;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        btnNext=findViewById(R.id.btnNext);

        edtUsername = findViewById(R.id.Username);
        edtEmail = findViewById(R.id.email);
        edtPhoneNumber = findViewById(R.id.PhoneNumber);
        edtBirth = findViewById(R.id.Birth);
        edtPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);

        //取得firestore與authentication的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //呼叫選擇日期的小視窗，讓使用者選取生日年月日
        select_Birth();

        //確認註冊button觸擊事件，呼叫方法registerUser()
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    //建立註冊使用者的方法
    //將使用者輸入資料寫進firestore與authentication並建立新的使用者，並且跳轉至登入頁面重新登入
    protected void registerUser() {

        //取得使用者輸入轉成字串並用trim()去除空白換行
        String userName = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String birth = edtBirth.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        //若沒有輸入使用者名稱，則跳出此方法
        if(userName.isEmpty()) {
            edtUsername.setError("使用者名稱不得為空白");
            edtUsername.requestFocus();
            return;
        }

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

        //若沒有輸入電話，則跳出此方法
        if(phoneNumber.isEmpty()) {
            edtPhoneNumber.setError("電話號碼不得為空白");
            edtPhoneNumber.requestFocus();
            return;
        }

        //若沒有輸入生日，則跳出此方法
        if(birth.isEmpty()) {
            edtBirth.setError("出生日期不得為空白");
            edtBirth.requestFocus();
            return;
        }

        //若沒有輸入密碼，則跳出此方法
        if(password.isEmpty()) {
            edtPassword.setError("密碼不得為空白");
            edtPassword.requestFocus();
            return;
        }

        //規定密碼長度必須>=6
        if(password.length() < 6) {
            edtPassword.setError("密碼長度不得小於6");
            edtPassword.requestFocus();
            return;
        }

        //顯示進度條
        //progressBar.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, HAGE_Default_Setting.class);
        intent.putExtra("userName", userName);
        intent.putExtra("emailAddress", email);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("birthday", birth);
        intent.putExtra("password", password);
        startActivity(intent);


        /*
        //將使用者的e-mail&密碼存放進authentication新增使用者
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        //註冊新使用者成功
                        if(task.isSuccessful()){

                            //將string型別的使用者輸入資料放進hashmap
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("userName", userName);
                            //userData.put("gender", gender);
                            userData.put("emailAddress", email);
                            userData.put("phoneNumber", phoneNumber);
                            userData.put("birthday", birth);
                            userData.put("password", password);

                            //指定要存放的firestore集合路徑，並將hashmap使用者資料.set()放進firestore，文件名稱設為authentication的該使用者ID(Uid)
                            firebaseFirestore.collection("user_Information").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .set(userData)
                                    //新增firestore文件成功
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //隱藏進度條
                                            progressBar.setVisibility(View.GONE);

                                            //取得該註冊使用者
                                            FirebaseUser firebaseuser = firebaseAuth.getCurrentUser();
                                            //若已通過email驗證
                                            if(firebaseuser.isEmailVerified()) {
                                                //
                                            }
                                            //發送驗證mail
                                            else {
                                                firebaseuser.sendEmailVerification();
                                                Toast.makeText(SignUp.this, "註冊成功，已發送驗證至電子信箱\n" + email + "\n請驗證後重新登入", Toast.LENGTH_SHORT).show();
                                            }
                                            //設置延遲，跳出註冊訊息Toast後2500毫秒再進行跳轉頁面至(MainActivity.java)
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent intent=new Intent(SignUp.this, MainActivity.class);
                                                    startActivity(intent);
                                                }
                                            }, 2500);
                                        }
                                    })
                                    //新增firestore文件失敗
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            //隱藏進度條
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(SignUp.this, "註冊失敗\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        //註冊新使用者失敗
                        else {
                            //隱藏進度條
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignUp.this, "註冊失敗\n此電子信箱已被註冊，請重新嘗試", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

         */


    }

    //選取年月日的小工具
    protected void select_Birth() {
        edtBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(java.util.Calendar.YEAR);
                int month = calendar.get(java.util.Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(SignUp.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        edtBirth.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                    }
                }, year, month, day);
                dialog.show();
            }
        });
    }
}