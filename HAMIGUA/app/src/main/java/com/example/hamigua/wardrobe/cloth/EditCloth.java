package com.example.hamigua.wardrobe.cloth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hamigua.HomePage;
import com.example.hamigua.LoadingDialog;
import com.example.hamigua.R;
import com.example.hamigua.shop.eventbus.CartCountEvent;
import com.example.hamigua.wardrobe.eventbus.DeleteClothEvent;
import com.example.hamigua.wardrobe.model.ClothData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class EditCloth extends AppCompatActivity {

    ImageView clothImageView;
    Button btn_SaveCloth, btn_DeleteCloth;
    TextInputEditText edtClothImageName, edtClothImageHashtag1, edtClothImageHashtag2, edtClothImageHashtag3;

    Spinner clothesCategory, accessoriesCategory;
    String[] category = {"短袖上衣", "長袖上衣", "背心", "短褲", "長褲", "裙子", "外套", "套裝", "配件飾品"};
    String[] accessories = {"帽子", "眼鏡", "耳環", "項鍊", "手環", "戒指", "皮/腰包", "包包", "鞋子", "襪子"};
    String categorySelect, accessoriesSelect; //所選擇的衣服類別 及 配件飾品類別
    String categoryIndex; //用在查詢語法的order by中

    FirebaseUser USER;
    FirebaseFirestore DB;
    StorageReference storageRef;

    private Uri imageUri; //圖片在本地的uri地址

    private String mPath = "";//設置照片路徑
    public static final int CAMERA_PERMISSION = 100;//檢測相機權限用
    public static final int REQUEST_HIGH_IMAGE = 101;//檢測相機回傳

    LoadingDialog loadingDialog;

    ClothData clothData;
    int position;

    /** 以下暫存 */
    TextInputEditText clothImageHSV;
    Spinner spinnerClothesLength, spinnerClothesWidth;
    String[] length = {"長", "適中", "短"};
    String[] width = {"寬", "適中", "窄"};
    String lengthSelect, widthSelect;
    /** 以上暫存 */

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cloth);

        clothImageView = findViewById(R.id.clothImageView);
        btn_SaveCloth = findViewById(R.id.btn_SaveCloth);
        btn_DeleteCloth = findViewById(R.id.btn_DeleteCloth);
        edtClothImageName = findViewById(R.id.clothImageName);
        edtClothImageHashtag1 = findViewById(R.id.clothImageHashtag1);
        edtClothImageHashtag2 = findViewById(R.id.clothImageHashtag2);
        edtClothImageHashtag3 = findViewById(R.id.clothImageHashtag3);
        //progressBar = findViewById(R.id.progressBar);
        clothesCategory = findViewById(R.id.spinnerClothesCategory);
        accessoriesCategory = findViewById(R.id.spinnerAccessoriesCategory);

        btn_DeleteCloth.setOnClickListener(deleteListener);

        /** 以下暫存 */
        /*
        clothImageHSV = findViewById(R.id.clothImageHSV);
        spinnerClothesLength = findViewById(R.id.spinnerClothesLength);
        spinnerClothesWidth = findViewById(R.id.spinnerClothesWidth);

        ArrayAdapter<String> adapter_ClothesLength = new ArrayAdapter<String>(AddClothes.this, android.R.layout.simple_spinner_dropdown_item, length);
        adapter_ClothesLength.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClothesLength.setAdapter(adapter_ClothesLength);
        //選擇衣服類別spinner的觸發事件
        spinnerClothesLength.setOnItemSelectedListener(clothesLength_Listener);

        ArrayAdapter<String> adapter_ClothesWidth = new ArrayAdapter<String>(AddClothes.this, android.R.layout.simple_spinner_dropdown_item, width);
        adapter_ClothesWidth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClothesWidth.setAdapter(adapter_ClothesWidth);
        //選擇衣服類別spinner的觸發事件
        spinnerClothesWidth.setOnItemSelectedListener(clothesWidth_Listener);

         */
        /** 以上暫存 */

        //上傳衣服imageView的觸發事件
        clothImageView.setOnClickListener(clothImageListener);

        //上傳衣服button的觸發事件
        btn_SaveCloth.setOnClickListener(saveClothListener);

        init();
        getIntentData();
        show_Cloth_Data();
    }

    protected void init() {
        //選擇衣服類別spinner的adapter
        ArrayAdapter<String> adapter_ClothesCategory = new ArrayAdapter<String>(EditCloth.this, android.R.layout.simple_spinner_dropdown_item, category);
        adapter_ClothesCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clothesCategory.setAdapter(adapter_ClothesCategory);
        //選擇衣服類別spinner的觸發事件
        clothesCategory.setOnItemSelectedListener(clothesCategory_Listener);

        //隱藏進度條
        //progressBar.setVisibility(View.INVISIBLE);
        loadingDialog = new LoadingDialog(EditCloth.this);

        //取得firestore、authentication、storage的連接
        DB = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    protected void getIntentData() {
        Intent intent = getIntent();
        clothData = intent.getParcelableExtra("clothData");
        position = intent.getIntExtra("position", 0);
    }

    protected void show_Cloth_Data() {
        Glide.with(EditCloth.this).load(clothData.getImageUrl()).into(clothImageView);
        edtClothImageName.setText(clothData.getImageName());

        int tempIndex = 0;
        for(int i = 0; i < category.length; i++) {
            if(clothData.getImageCategory().equals(category[i]))
                tempIndex = i;
        }
        clothesCategory.setSelection(tempIndex);

        edtClothImageHashtag1.setText(clothData.getImageHashtag1());
        edtClothImageHashtag2.setText(clothData.getImageHashtag2());
        edtClothImageHashtag3.setText(clothData.getImageHashtag3());
    }

    protected View.OnClickListener clothImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //檢查是否取得相機權限，若沒有則再詢問一次
            if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
            }

            //設置確認上傳方式的對話視窗
            AlertDialog.Builder dialog = new AlertDialog.Builder(EditCloth.this);
            dialog.setTitle("上傳方式");
            dialog.setMessage("您想要用什麼方式上傳衣服圖片呢？");

            //若使用者按下"從相簿選擇圖片"的觸擊事件
            dialog.setPositiveButton("從相簿選擇圖片", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //開啟檔案瀏覽
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    //設定檔案型別image
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, 2);

                    //選擇完圖片會執行方法onActivityResult()
                }
            });

            //若使用者按下"用相機拍攝"的觸擊事件
            dialog.setNeutralButton("用相機拍攝", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //設置intent到相機介面
                    Intent highIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //檢查是否已取得權限
                    if (highIntent.resolveActivity(getPackageManager()) == null) {
                        Toast.makeText(EditCloth.this, "請先開啟相機權限", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                    //取得相片檔案的uri位址及設定檔案名稱
                    File imageFile = getImageFile();
                    if (imageFile == null) return;
                    //取得相片檔案的本地uri地址
                    imageUri = FileProvider.getUriForFile(
                            EditCloth.this,
                            "com.example.hamigua.CameraEx",//要跟AndroidManifest.xml中的authorities 一致
                            imageFile
                    );
                    highIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    startActivityForResult(highIntent,REQUEST_HIGH_IMAGE);//開啟相機
                }
            });

            AlertDialog alertDialog = dialog.create();
            alertDialog.show();
        }
    };

    protected View.OnClickListener saveClothListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //如果圖片在本地的uri地址不為null
            if(imageUri != null){
                //上傳圖片到Storage，呼叫方法uploadToFirebase並傳入圖片的本地uri地址
                uploadToFirebase(imageUri);
            } else{
                //如果圖片在本地的uri為null，提醒使用者先選擇圖片
                Toast.makeText(EditCloth.this, "請先選擇衣服圖片", Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected View.OnClickListener deleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DB.collection("user_Information").document(USER.getUid()).collection("Clothes")
                    .document(clothData.getImageName())
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(EditCloth.this, "已刪除該衣服", Toast.LENGTH_SHORT).show();
                            //設置延遲，1000毫秒後跳轉回Home_page.class
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    EventBus.getDefault().postSticky(new DeleteClothEvent("successful", clothData.getImageCategory(), position));
                                    finish();
                                }
                            }, 1000);
                        }
                    });
        }
    };

    /** 以下暫存 */
    private AdapterView.OnItemSelectedListener clothesLength_Listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            lengthSelect = length[position];
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private AdapterView.OnItemSelectedListener clothesWidth_Listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            widthSelect = width[position];
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
    /** 以上暫存 */

    //選擇衣服類別的spinner觸發事件
    private Spinner.OnItemSelectedListener clothesCategory_Listener = new Spinner.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //將使用者所選擇的類別指定給string變數categorySelect
            categorySelect = category[position];
            categoryIndex = Integer.toString(position + 1);

            //如果使用者選擇的衣服類別是"配件飾品"
            if(position == 8){

                //顯示選擇配件飾品的spinner
                accessoriesCategory.setVisibility(View.VISIBLE);

                //選擇配件飾品spinner的adapter
                ArrayAdapter<String> adapter_AccessoriesCategory = new ArrayAdapter<String>(EditCloth.this, android.R.layout.simple_spinner_dropdown_item, accessories);
                adapter_AccessoriesCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                accessoriesCategory.setAdapter(adapter_AccessoriesCategory);

                //選擇配件飾品spinner的觸發事件
                accessoriesCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //將使用者所選擇的配件飾品指定給string變數accessoriesSelect
                        accessoriesSelect = accessories[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        //
                    }
                });
            } else {
                //如果使用者選擇的衣服類別不是"配件飾品"，隱藏選擇配件飾品的spinner
                accessoriesCategory.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //
        }
    };

    //取得相片檔案的uri位址及設定檔案名稱
    private File getImageFile()  {
        //設定檔案名稱，取得系統絕對時間
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = time+"_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            //給予檔案命名及檔案格式
            File imageFile = File.createTempFile(fileName,".jpg",dir);
            //給予全域變數中的照片檔案位置，方便後面取得
            mPath = imageFile.getAbsolutePath(); /** to辛亞 這個是相機拍照的圖片路徑*/
            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }

    //取得照片回傳
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //若是得到用相機拍攝的照片回傳
        if (requestCode == REQUEST_HIGH_IMAGE && resultCode == -1){
            new Thread(()->{
                //在BitmapFactory中以檔案uri路徑取得相片檔案，並處理為AtomicReference<Bitmap>，方便後續旋轉圖片
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath));
                Matrix matrix = new Matrix();
                matrix.setRotate(90f);//轉90度
                getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                        ,0,0
                        ,getHighImage.get().getWidth()
                        ,getHighImage.get().getHeight()
                        ,matrix,true));
                runOnUiThread(()->{
                    //以Glide設置圖片(因為旋轉圖片屬於耗時處理，故會LAG一下，且必須使用Thread執行緒)
                    Glide.with(this)
                            .load(getHighImage.get())
                            .centerCrop()
                            .into(clothImageView);//將處理完的照片顯示在imageView
                });
            }).start();
        }
        //若是得到用相簿所選擇的照片回傳
        else if(requestCode == 2 && resultCode == RESULT_OK && data != null){
            //取得圖片在本地的uri
            imageUri = data.getData();
            //剛剛選擇的圖片放在衣服imageView
            clothImageView.setImageURI(imageUri);
            //將剛剛選擇的圖片路徑指定給mPath
            mPath = data.getData().getPath(); /** to辛亞 這個是相簿裡選擇的圖片路徑*/
        }
        else{
            Toast.makeText(this, "未作任何拍攝或選取圖片", Toast.LENGTH_SHORT).show();
        }
    }

    //將圖片上傳到firestore及storage的方法
    private void uploadToFirebase(Uri uri){

        loadingDialog.start_Loading_Dialog();

        //取得使用者輸入的衣服名稱(限制不得為空白)，若為空白則跳出此上傳衣服方法
        String clothImageName = edtClothImageName.getText().toString().trim();
        if(clothImageName.isEmpty()){
            edtClothImageName.setError("衣服名稱不得為空白");
            edtClothImageName.requestFocus();
            return;
        }

        /** 以下暫存 */
        String color_HSV = clothImageHSV.getText().toString().trim();
        /** 以上暫存 */

        //設定存放圖片的Storage路徑，"User/使用者的Uid/Clothes_Image/"，後面為檔案名稱：取得系統絕對時間 + getFileExtension(uri)
        StorageReference imagePathRef = storageRef.child("User/" + USER.getUid() + "/Clothes_Image/" + System.currentTimeMillis() + "." + getFileExtension(uri));
        //將圖片的本地uri地址上傳到Storage
        imagePathRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            //若上傳圖片成功
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //取得該已上傳圖片在Storage的url
                imagePathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    //若取得圖片url成功
                    @Override
                    public void onSuccess(Uri uri) {

                        //隱藏進度條
                        //progressBar.setVisibility(View.INVISIBLE);
                        //將衣服imageView設置為原先的icon
                        clothImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_add_photo_alternate_24));

                        //宣告要傳進firestore資料庫的string變數
                        String clothImageHashtag1, clothImageHashtag2, clothImageHashtag3;

                        //取得使用者輸入3個hashtag，轉成字串去除空白換行並指定給各個變數，若未輸入Hashtag則將變數設為null
                        if(edtClothImageHashtag1.getText().toString().trim().isEmpty()) {
                            clothImageHashtag1 = " ";
                        } else {
                            clothImageHashtag1 = edtClothImageHashtag1.getText().toString().trim();
                        }
                        if(edtClothImageHashtag2.getText().toString().trim().isEmpty()) {
                            clothImageHashtag2 = " ";
                        } else {
                            clothImageHashtag2 = edtClothImageHashtag2.getText().toString().trim();
                        }
                        if(edtClothImageHashtag3.getText().toString().trim().isEmpty()) {
                            clothImageHashtag3 = " ";
                        } else {
                            clothImageHashtag3 = edtClothImageHashtag3.getText().toString().trim();
                        }

                        String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());

                        //將string型別的圖片輸入資料放進hashmap
                        Map<String, Object> imageData = new HashMap<>();
                        imageData.put("clothes_Image_Name", clothImageName);
                        imageData.put("clothes_Category", categorySelect);
                        imageData.put("accessories_Category", accessoriesSelect);
                        imageData.put("clothes_Image_Hashtag1", clothImageHashtag1);
                        imageData.put("clothes_Image_Hashtag2", clothImageHashtag2);
                        imageData.put("clothes_Image_Hashtag3", clothImageHashtag3);
                        imageData.put("clothes_Image_URL", uri.toString());
                        imageData.put("set_Data_Time", timeStamp);
                        imageData.put("index", categoryIndex);
                        imageData.put("favorite_Status", "No");
                        imageData.put("privacy_Status", "locked");
                        /** 以下暫存 */
                        /*
                        imageData.put("color_HSV", color_HSV);
                        imageData.put("length", lengthSelect);
                        imageData.put("width", widthSelect);
                         */
                        /** 以上暫存 */

                        //圖片除了上傳到Storage，也要將url存進firestore資料庫，之後才能撈圖片資料
                        //指定要存放的firestore集合路徑，並將hashmap圖片資料.set()放進firestore，路徑為：集合user_Information -> 該登入使用者的文件(Uid) -> 集合Clothes -> 該衣服的名稱文件
                        DB.collection("user_Information").document(USER.getUid()).collection("Clothes").document(clothImageName)
                                .set(imageData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(EditCloth.this, "新增圖片URL至firestore成功", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditCloth.this, "新增圖片URL至firestore失敗\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        //將圖片的使用者輸入清空
                        edtClothImageName.setText("");
                        edtClothImageHashtag1.setText("");
                        edtClothImageHashtag2.setText("");
                        edtClothImageHashtag3.setText("");

                        loadingDialog.dismiss_Dialog();
                        Toast.makeText(EditCloth.this, "上傳圖片至storge成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //此為正在執行上傳圖片及新增文件時的方法
                //顯示進度條
                //progressBar.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialog.dismiss_Dialog();
                //若圖片上傳失敗
                //隱藏進度條
                //progressBar.setVisibility(View.INVISIBLE);
                //將衣服imageView設置為原先的icon
                clothImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_add_photo_alternate_24));
                Toast.makeText(EditCloth.this, "上傳圖片失敗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //這個方法我真的不知道是在寫什麼qq
    //我猜是可以回傳使用者所選擇的檔案的格式(例如.jpg)
    private String getFileExtension(Uri mUri){

        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(cr.getType(mUri));

    }
}