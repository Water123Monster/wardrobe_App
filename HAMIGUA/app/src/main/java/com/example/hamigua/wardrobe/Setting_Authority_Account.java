package com.example.hamigua.wardrobe;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamigua.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Setting_Authority_Account extends AppCompatActivity {

    private TextInputEditText edtAccount;
    private Button btnResetAccount;
    private ProgressBar progressBar;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String email, old_email, password;

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
        setContentView(R.layout.activity_setting_authority_account);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        edtAccount = findViewById(R.id.resetAccount);
        btnResetAccount = findViewById(R.id.btnResetAccount);
        progressBar = findViewById(R.id.progressBar);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                old_email = document.getString("emailAddress");
                                password = document.getString("password");
                            }
                        }
                    }
                });

        /*
        btnResetAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = edtAccount.getText().toString().trim();

                //顯示進度條
                progressBar.setVisibility(View.VISIBLE);

                if(email.isEmpty()) {
                    edtAccount.setError("電子信箱不得為空白");
                    edtAccount.requestFocus();
                    //隱藏進度條
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    AuthCredential credential = EmailAuthProvider.getCredential(old_email, password);

                    firebaseUser.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    firebaseUser.updateEmail(email)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    //隱藏進度條
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(Setting_Authority_Account.this, "重新設置電子信箱成功", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    //隱藏進度條
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(Setting_Authority_Account.this, "重新設置電子信箱失敗", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                }
            }
        });
        */
    }
}