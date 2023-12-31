package com.example.hamigua.loginAcount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.hamigua.HomePage;
import com.example.hamigua.LoadingDialog;
import com.example.hamigua.R;
import com.example.hamigua.wardrobe.HAGE_Setting;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class HAGE_Default_Setting extends AppCompatActivity {

    RadioGroup HAGE_gender, HAGE_bodyType, HAGE_bodyRatio, HAGE_armSwing;
    RadioButton HAGE_male, HAGE_female;
    RadioButton bodyType_thin, bodyType_fit, bodyType_fat;
    RadioButton ratio_fourSix, ratio_fiveFive;
    RadioButton armSwing_high, armSwing_low;
    TextInputEditText HAGE_height;
    MaterialButton btnSave;

    //取得使用者輸入轉成字串並用trim()去除空白換行
    String userName;
    String email;
    String phoneNumber;
    String birth;
    String password;

    String gender, bodyType, bodyRatio, armSwing;

    FirebaseFirestore DB;
    FirebaseAuth USER;

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hage_default_setting);

        //radioButton
        HAGE_male = findViewById(R.id.HAGE_male);
        HAGE_female = findViewById(R.id.HAGE_female);
        bodyType_thin = findViewById(R.id.bodyType_thin);
        bodyType_fit = findViewById(R.id.bodyType_fit);
        bodyType_fat = findViewById(R.id.bodyType_fat);
        ratio_fourSix = findViewById(R.id.ratio_fourSix);
        ratio_fiveFive = findViewById(R.id.ratio_fiveFive);
        armSwing_high = findViewById(R.id.armSwing_high);
        armSwing_low = findViewById(R.id.armSwing_low);

        //radioGroup
        HAGE_gender = findViewById(R.id.HAGE_gender);
        HAGE_gender.setOnCheckedChangeListener(genderListener);
        HAGE_bodyType = findViewById(R.id.HAGE_bodyType);
        HAGE_bodyType.setOnCheckedChangeListener(bodyTypeListener);
        HAGE_bodyRatio = findViewById(R.id.HAGE_bodyRatio);
        HAGE_bodyRatio.setOnCheckedChangeListener(ratioListener);
        HAGE_armSwing = findViewById(R.id.HAGE_armSwing);
        HAGE_armSwing.setOnCheckedChangeListener(armSwingListener);

        HAGE_height = findViewById(R.id.HAGE_height);
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(saveListener);

        init();
        get_Intent();
    }

    protected void init() {
        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(HAGE_Default_Setting.this);
    }

    protected void get_Intent() {
        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        email = intent.getStringExtra("emailAddress");
        phoneNumber = intent.getStringExtra("phoneNumber");
        birth = intent.getStringExtra("birthday");
        password = intent.getStringExtra("password");
    }

    protected RadioGroup.OnCheckedChangeListener genderListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            RadioButton radioButton = findViewById(checkedId);
            gender = radioButton.getText().toString();
        }
    };

    protected RadioGroup.OnCheckedChangeListener bodyTypeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            RadioButton radioButton = findViewById(checkedId);
            bodyType = radioButton.getText().toString();
        }
    };

    protected RadioGroup.OnCheckedChangeListener ratioListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            RadioButton radioButton = findViewById(checkedId);
            bodyRatio = radioButton.getText().toString();
        }
    };

    protected RadioGroup.OnCheckedChangeListener armSwingListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            RadioButton radioButton = findViewById(checkedId);
            armSwing = radioButton.getText().toString();
        }
    };

    protected View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            HAGE_height.clearFocus();
            String height = HAGE_height.getText().toString().trim();
            if(height.isEmpty()) {
                HAGE_height.setError("人偶身高不得為空白");
                HAGE_height.requestFocus();
            } else {
                loadingDialog.start_Loading_Dialog();

                //將使用者的e-mail&密碼存放進authentication新增使用者
                USER.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                //註冊新使用者成功
                                if(task.isSuccessful()){

                                    //將string型別的使用者輸入資料放進hashmap
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("userName", userName);
                                    userData.put("emailAddress", email);
                                    userData.put("phoneNumber", phoneNumber);
                                    userData.put("birthday", birth);
                                    userData.put("password", password);
                                    userData.put("gender", gender);
                                    userData.put("height", height);
                                    userData.put("bodyType", bodyType);
                                    userData.put("bodyRatio", bodyRatio);
                                    userData.put("armSwing", armSwing);

                                    //指定要存放的firestore集合路徑，並將hashmap使用者資料.set()放進firestore，文件名稱設為authentication的該使用者ID(Uid)
                                    DB.collection("user_Information").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .set(userData)
                                            //新增firestore文件成功
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            loadingDialog.dismiss_Dialog();
                                                            //取得該註冊使用者
                                                            FirebaseUser firebaseuser = USER.getCurrentUser();
                                                            //若已通過email驗證
                                                            if(firebaseuser.isEmailVerified()) {
                                                                //
                                                            }
                                                            //發送驗證mail
                                                            else {
                                                                firebaseuser.sendEmailVerification();
                                                                Toast.makeText(HAGE_Default_Setting.this, "註冊成功，已發送驗證至電子信箱\n" + email + "\n請驗證後重新登入", Toast.LENGTH_SHORT).show();
                                                            }
                                                            //設置延遲，跳出註冊訊息Toast後2500毫秒再進行跳轉頁面至(MainActivity.java)
                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Intent intent=new Intent(HAGE_Default_Setting.this, MainActivity.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //將其他activity的生命週期結束
                                                                    startActivity(intent);
                                                                }
                                                            }, 1500);
                                                        }
                                                    }, 500);

                                                }
                                            });
                                }
                                //註冊新使用者失敗
                                else {
                                    loadingDialog.dismiss_Dialog();
                                    Toast.makeText(HAGE_Default_Setting.this, "註冊失敗\n此電子信箱已被註冊，請重新嘗試", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    };
}