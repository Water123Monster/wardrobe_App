package com.example.hamigua.wardrobe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.view.View;
import android.widget.Toast;

import com.example.hamigua.HomePage;
import com.example.hamigua.R;
import com.example.hamigua.loginAcount.HAGE_Default_Setting;
import com.example.hamigua.loginAcount.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PersonalPreferenceSetting extends AppCompatActivity {

    FirebaseFirestore DB;
    FirebaseUser USER;

    MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_preference_setting);

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(saveListener);

        init();
    }

    protected void init() {
        DB = FirebaseFirestore.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    protected View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Map<String, Object> preferenceData = new HashMap<>();
            preferenceData.put("01_Red", "5");
            preferenceData.put("02_Orange", "4");
            preferenceData.put("03_Yellow", "3");
            preferenceData.put("04_ChartreuseGreen", "4");
            preferenceData.put("05_Green", "3");
            preferenceData.put("06_SpringGreen", "3");
            preferenceData.put("07_Cyan", "2");
            preferenceData.put("08_Azure", "2");
            preferenceData.put("09_Blue", "2");
            preferenceData.put("10_Violet", "4");
            preferenceData.put("11_Magenta", "5");
            preferenceData.put("12_Rose", "5");
            preferenceData.put("13_BB", "4");
            preferenceData.put("14_BT", "5");
            preferenceData.put("15_TB", "3");
            preferenceData.put("16_TT", "3");
            preferenceData.put("17_LL", "4");
            preferenceData.put("18_LS", "3");
            preferenceData.put("19_SL", "3");
            preferenceData.put("20_SS", "2");
            preferenceData.put("21_Hs_Hv", "5");
            preferenceData.put("22_Hs_Mv", "3");
            preferenceData.put("23_Hs_Lv", "2");
            preferenceData.put("24_Ms_Hv", "3");
            preferenceData.put("25_Ms_Mv", "3");
            preferenceData.put("26_Ms_Lv", "2");
            preferenceData.put("27_Ls_Hv", "2");
            preferenceData.put("28_LS_Mv", "3");
            preferenceData.put("29_Ls_Lv", "2");
            preferenceData.put("30_Same", "5");
            preferenceData.put("31_Contrasting", "2");
            preferenceData.put("32_Achromatic", "4");
            preferenceData.put("33_Analogous", "4");
            preferenceData.put("34_importance_HSV", "1");
            preferenceData.put("35_importance_BT", "2");
            preferenceData.put("36_importance_SL", "3");

            DB.collection("personal_Preference").document(USER.getUid())
                    .set(preferenceData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(PersonalPreferenceSetting.this, "穿搭喜好設定完成", Toast.LENGTH_SHORT).show();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(PersonalPreferenceSetting.this, HomePage.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //將其他activity的生命週期結束
                                        startActivity(intent);
                                    }
                                }, 1500);
                            }
                        }
                    });
        }
    };
}