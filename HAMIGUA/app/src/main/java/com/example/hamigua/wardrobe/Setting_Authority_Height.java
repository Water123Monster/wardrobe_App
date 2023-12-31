package com.example.hamigua.wardrobe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.hamigua.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Setting_Authority_Height extends AppCompatActivity {

    private TextInputEditText edtHeight;
    private String userHeight;
    private Button btnResetHeight;
    private ProgressBar progressBar;
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
        setContentView(R.layout.activity_setting_authority_height);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        edtHeight = findViewById(R.id.resetHeight);
        btnResetHeight = findViewById(R.id.btnResetHeight);
        progressBar = findViewById(R.id.progressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        btnResetHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userHeight = edtHeight.getText().toString().trim();

                //顯示進度條
                progressBar.setVisibility(View.VISIBLE);

                if(userHeight.isEmpty()) {
                    edtHeight.setError("使用者名稱不得為空白");
                    edtHeight.requestFocus();
                    //隱藏進度條
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                            .update("height", userHeight)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //隱藏進度條
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Setting_Authority_Height.this, "重新設置模型身高成功", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //隱藏進度條
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Setting_Authority_Height.this, "重新設置模型身高失敗", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }
}