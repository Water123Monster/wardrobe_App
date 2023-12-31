package com.example.hamigua.wardrobe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.hamigua.HomePage;
import com.example.hamigua.LoadingDialog;
import com.example.hamigua.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HAGE_Setting extends AppCompatActivity {

    RadioGroup HAGE_gender, HAGE_bodyType, HAGE_bodyRatio, HAGE_armSwing;
    RadioButton HAGE_male, HAGE_female;
    RadioButton bodyType_thin, bodyType_fit, bodyType_fat;
    RadioButton ratio_fourSix, ratio_fiveFive;
    RadioButton armSwing_high, armSwing_low;
    TextInputEditText HAGE_height;
    MaterialButton btnSave;

    String gender, bodyType, bodyRatio, armSwing;

    FirebaseFirestore DB;
    FirebaseAuth USER;

    LoadingDialog loadingDialog;

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
        setContentView(R.layout.activity_hage_setting);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                get_DB_HAGE_Data();
                loadingDialog.dismiss_Dialog();
            }
        }, 1000);
    }

    protected void init() {
        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(HAGE_Setting.this);
        loadingDialog.start_Loading_Dialog();
    }

    protected void get_DB_HAGE_Data() {
        DB.collection("user_Information").document(USER.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();

                            String gender = document.getString("gender");
                            switch (gender) {
                                case "男性" :
                                    HAGE_male.setChecked(true);
                                    break;
                                case "女性" :
                                    HAGE_female.setChecked(true);
                                    break;
                            }

                            String height = document.getString("height");
                            HAGE_height.setText(height);

                            String bodyType = document.getString("bodyType");
                            switch (bodyType) {
                                case "偏瘦" :
                                    bodyType_thin.setChecked(true);
                                    break;
                                case "適中" :
                                    bodyType_fit.setChecked(true);
                                    break;
                                case "偏胖" :
                                    bodyType_fat.setChecked(true);
                                    break;
                            }

                            String bodyRatio = document.getString("bodyRatio");
                            switch (bodyRatio) {
                                case "46身" :
                                    ratio_fourSix.setChecked(true);
                                    break;
                                case "55身" :
                                    ratio_fiveFive.setChecked(true);
                                    break;
                            }

                            String armSwing = document.getString("armSwing");
                            switch (armSwing) {
                                case "擺福較高" :
                                    armSwing_high.setChecked(true);
                                    break;
                                case "擺福較低" :
                                    armSwing_low.setChecked(true);
                                    break;
                            }
                        }
                    }
                });
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



                Map<String, Object> HAGE_data = new HashMap<>();
                HAGE_data.put("gender", gender);
                HAGE_data.put("height", height);
                HAGE_data.put("bodyType", bodyType);
                HAGE_data.put("bodyRatio", bodyRatio);
                HAGE_data.put("armSwing", armSwing);

                DB.collection("user_Information").document(USER.getCurrentUser().getUid())
                        .update(HAGE_data)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(HAGE_Setting.this, "更新人偶設定成功", Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss_Dialog();

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(HAGE_Setting.this, HomePage.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //將其他activity的生命週期結束
                                                startActivity(intent);

                                                //activity的生命週期教學 https://codertw.com/android-%E9%96%8B%E7%99%BC/333374/
                                            }
                                        }, 1000);
                                    }
                                }, 500);
                            }
                        });
            }
        }
    };
}