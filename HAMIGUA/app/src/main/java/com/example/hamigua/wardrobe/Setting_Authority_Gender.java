package com.example.hamigua.wardrobe;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamigua.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Setting_Authority_Gender extends AppCompatActivity {

    private Spinner spnGender;
    String[] Strgender = new String[]{"男性", "女性"};
    private Button btnResetGender;
    private ProgressBar progressBar;
    protected String gender;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

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
        setContentView(R.layout.activity_setting_authority_gender);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        spnGender = findViewById(R.id.spnGender);
        btnResetGender = findViewById(R.id.btnResetGender);
        progressBar = findViewById(R.id.progressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, Strgender);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnGender.setAdapter(adapter);
        spnGender.setOnItemSelectedListener(spnGenderListener);

        btnResetGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //顯示進度條
                progressBar.setVisibility(View.VISIBLE);

                firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                        .update("gender", gender)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //隱藏進度條
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(Setting_Authority_Gender.this, "重新設置性別成功", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //隱藏進度條
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(Setting_Authority_Gender.this, "重新設置性別失敗", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private Spinner.OnItemSelectedListener spnGenderListener = new Spinner.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
            gender = parent.getSelectedItem().toString().trim();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //
        }
    };
}