package com.example.hamigua.wardrobe;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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

import java.util.Calendar;

public class Setting_Authority_Birthday extends AppCompatActivity {

    private TextInputEditText edtBirthday;
    private Button btnResetBirthday;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String userBirthday;

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
        setContentView(R.layout.activity_setting_authority_birthday);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        edtBirthday = findViewById(R.id.resetBirthday);
        btnResetBirthday = findViewById(R.id.btnResetBirthday);
        progressBar = findViewById(R.id.progressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        edtBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(java.util.Calendar.YEAR);
                int month = calendar.get(java.util.Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(Setting_Authority_Birthday.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        edtBirthday.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
                    }
                }, year, month, day);
                dialog.show();
            }
        });

        btnResetBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userBirthday = edtBirthday.getText().toString().trim();

                //顯示進度條
                progressBar.setVisibility(View.VISIBLE);

                if(userBirthday.isEmpty()) {
                    edtBirthday.setError("出生日期不得為空白");
                    edtBirthday.requestFocus();
                    //隱藏進度條
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                            .update("birthday", userBirthday)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //隱藏進度條
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Setting_Authority_Birthday.this, "重新設置生日成功", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //隱藏進度條
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Setting_Authority_Birthday.this, "重新設置生日失敗", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }
}