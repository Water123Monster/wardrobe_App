package com.example.hamigua.wardrobe;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamigua.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Setting_Authority_PhoneNumber extends AppCompatActivity {

    private TextInputEditText edtPhoneNumber;
    private Button btnResetPhoneNumber;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String PhoneNumber;

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
        setContentView(R.layout.activity_setting_authority_phonenumber);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        edtPhoneNumber = findViewById(R.id.resetPhoneNumber);
        btnResetPhoneNumber = findViewById(R.id.btnResetPhoneNumber);
        progressBar = findViewById(R.id.progressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        btnResetPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PhoneNumber = edtPhoneNumber.getText().toString().trim();

                //顯示進度條
                progressBar.setVisibility(View.VISIBLE);

                if(PhoneNumber.isEmpty()) {
                    edtPhoneNumber.setError("手機號碼不得為空白");
                    edtPhoneNumber.requestFocus();
                    //隱藏進度條
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                            .update("phoneNumber", PhoneNumber)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //隱藏進度條
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Setting_Authority_PhoneNumber.this, "重新設置手機號碼成功", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //隱藏進度條
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Setting_Authority_PhoneNumber.this, "重新設置手機號碼失敗", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }
}