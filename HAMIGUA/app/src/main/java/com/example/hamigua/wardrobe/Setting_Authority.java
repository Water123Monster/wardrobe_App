package com.example.hamigua.wardrobe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamigua.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Setting_Authority extends AppCompatActivity {

    private String[] setting_authority_column = {"用戶名稱","性別","模型身高","模型身形","生日","電子信箱","密碼","手機號碼"};
    private String[] setting_authority_data = new String[8];
    private ListView setting_authority;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

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
        setContentView(R.layout.activity_setting_authority);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        setting_authority = findViewById(R.id.setting_authority);
        MyListviewAdapter adapter = new MyListviewAdapter(Setting_Authority.this);
        setting_authority.setAdapter(adapter);
        setting_authority.setOnItemClickListener(onClickListView);

    }

    //自訂的adapter類別
    class MyListviewAdapter extends BaseAdapter {
        private LayoutInflater myInflater;
        public MyListviewAdapter(Context c){
            myInflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return setting_authority_column.length;
        }

        @Override
        public Object getItem(int position) {
            return setting_authority_column[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //設定ListView使用自訂的Layout版面配置，並設定變數及對應的元件id，及從陣列裡取出資料放進TextView裡
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = myInflater.inflate(R.layout.setting_authority_layout,null);

            TextView setting_column = (convertView.findViewById(R.id.setting_column));
            TextView setting_data = (convertView.findViewById(R.id.setting_data));

            setting_column.setText(setting_authority_column[position]);

            firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()) {
                                    setting_authority_data[0] = document.getString("userName");
                                    setting_authority_data[1] = document.getString("gender");
                                    setting_authority_data[2] = document.getString("height");
                                    setting_authority_data[3] = document.getString("bodyType");
                                    setting_authority_data[4] = document.getString("birthday");
                                    setting_authority_data[5] = document.getString("emailAddress");
                                    setting_authority_data[6] = "********";
                                    setting_authority_data[7] = document.getString("phoneNumber");

                                }
                                setting_data.setText(setting_authority_data[position]);
                            }
                        }
                    });

            return convertView;
        }
    }

    private ListView.OnItemClickListener onClickListView = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch(position){
                case 0:
                    Intent intent_Setting_Authority_Name = new Intent(Setting_Authority.this, Setting_Authority_Name.class);
                    startActivity(intent_Setting_Authority_Name);
                    break;
                case 1:
                    Intent intent_Setting_Authority_Gender = new Intent(Setting_Authority.this, Setting_Authority_Gender.class);
                    startActivity(intent_Setting_Authority_Gender);
                    break;
                case 2:
                    Intent intent_Setting_Authority_Height = new Intent(Setting_Authority.this, Setting_Authority_Height.class);
                    startActivity(intent_Setting_Authority_Height);
                    break;
                case 3:
                    Intent intent_Setting_Authority_BodyType = new Intent(Setting_Authority.this, Setting_Authority_BodyType.class);
                    startActivity(intent_Setting_Authority_BodyType);
                    break;
                case 4:
                    Intent intent_Setting_Authority_Birthday = new Intent(Setting_Authority.this, Setting_Authority_Birthday.class);
                    startActivity(intent_Setting_Authority_Birthday);
                    break;
                case 5:
                    Intent intent_Setting_Authority_Account = new Intent(Setting_Authority.this, Setting_Authority_Account.class);
                    startActivity(intent_Setting_Authority_Account);
                    break;
                case 6:
                    Intent intent_Setting_Authority_Password = new Intent(Setting_Authority.this, Setting_Authority_Password.class);
                    startActivity(intent_Setting_Authority_Password);
                    break;
                case 7:
                    Intent intent_Setting_Authority_PhoneNumber = new Intent(Setting_Authority.this, Setting_Authority_PhoneNumber.class);
                    startActivity(intent_Setting_Authority_PhoneNumber);
                    break;
            }
        }
    };
}