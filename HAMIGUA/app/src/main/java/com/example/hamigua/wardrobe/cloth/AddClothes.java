package com.example.hamigua.wardrobe.cloth;

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
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.hamigua.LoadingDialog;
import com.example.hamigua.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AddClothes extends AppCompatActivity {

    private ImageView imageClothes;
    private Button btn_uploadImage;
    private TextInputEditText edtClothImageName, edtClothImageHashtag1, edtClothImageHashtag2, edtClothImageHashtag3;
    //private ProgressBar progressBar;
    private Spinner clothesCategory, accessoriesCategory;
    String[] category = {"短袖上衣", "長袖上衣", "背心", "短褲", "長褲", "裙子", "外套", "套裝", "配件飾品"};
    String[] accessories = {"帽子", "眼鏡", "耳環", "項鍊", "手環", "戒指", "皮/腰包", "包包", "鞋子", "襪子"};
    String categorySelect, accessoriesSelect; //所選擇的衣服類別 及 配件飾品類別
    String categoryIndex; //用在查詢語法的order by中

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageRef;

    private Uri imageUri; //圖片在本地的uri地址

    private String mPath = "";//設置照片路徑
    public static final int CAMERA_PERMISSION = 100;//檢測相機權限用
    public static final int REQUEST_HIGH_IMAGE = 101;//檢測相機回傳

    LoadingDialog loadingDialog;

    /** 以下暫存 */
    TextInputEditText clothImageH, clothImageS, clothImageV;
    Spinner spinnerClothesLength, spinnerClothesWidth;
    String[] length = {"長", "短"};
    String[] width = {"寬", "窄"};
    String lengthSelect, widthSelect;
    /** 以上暫存 */

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_clothes);

        imageClothes = findViewById(R.id.clothImageView);
        btn_uploadImage = findViewById(R.id.btn_UploadClothes);
        edtClothImageName = findViewById(R.id.clothImageName);
        edtClothImageHashtag1 = findViewById(R.id.clothImageHashtag1);
        edtClothImageHashtag2 = findViewById(R.id.clothImageHashtag2);
        edtClothImageHashtag3 = findViewById(R.id.clothImageHashtag3);
        //progressBar = findViewById(R.id.progressBar);
        clothesCategory = findViewById(R.id.spinnerClothesCategory);
        accessoriesCategory = findViewById(R.id.spinnerAccessoriesCategory);

        /** 以下暫存 */

        clothImageH = findViewById(R.id.clothImageH);
        clothImageS = findViewById(R.id.clothImageS);
        clothImageV = findViewById(R.id.clothImageV);
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


        /** 以上暫存 */

        //選擇衣服類別spinner的adapter
        ArrayAdapter<String> adapter_ClothesCategory = new ArrayAdapter<String>(AddClothes.this, android.R.layout.simple_spinner_dropdown_item, category);
        adapter_ClothesCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clothesCategory.setAdapter(adapter_ClothesCategory);
        //選擇衣服類別spinner的觸發事件
        clothesCategory.setOnItemSelectedListener(clothesCategory_Listener);

        //隱藏進度條
        //progressBar.setVisibility(View.INVISIBLE);
        loadingDialog = new LoadingDialog(AddClothes.this);

        //取得firestore、authentication、storage的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        //上傳衣服imageView的觸發事件
        imageClothes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //檢查是否取得相機權限，若沒有則再詢問一次
                if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
                }

                //設置確認上傳方式的對話視窗
                AlertDialog.Builder dialog = new AlertDialog.Builder(AddClothes.this);
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
                            Toast.makeText(AddClothes.this, "請先開啟相機權限", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        //取得相片檔案的uri位址及設定檔案名稱
                        File imageFile = getImageFile();
                        if (imageFile == null) return;
                        //取得相片檔案的本地uri地址
                        imageUri = FileProvider.getUriForFile(
                                AddClothes.this,
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
        });

        //上傳衣服button的觸發事件
        btn_uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果圖片在本地的uri地址不為null
                if(imageUri != null){
                    if(check_User_Input()) {
                        //上傳圖片到Storage，呼叫方法uploadToFirebase並傳入圖片的本地uri地址
                        uploadToFirebase(imageUri);
                    }
                } else{
                    //如果圖片在本地的uri為null，提醒使用者先選擇圖片
                    Toast.makeText(AddClothes.this, "請先選擇衣服圖片", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

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
                ArrayAdapter<String> adapter_AccessoriesCategory = new ArrayAdapter<String>(AddClothes.this, android.R.layout.simple_spinner_dropdown_item, accessories);
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
                            .into(imageClothes);//將處理完的照片顯示在imageView
                });
            }).start();
        }
        //若是得到用相簿所選擇的照片回傳
        else if(requestCode == 2 && resultCode == RESULT_OK && data != null){
            //取得圖片在本地的uri
            imageUri = data.getData();
            //剛剛選擇的圖片放在衣服imageView
            imageClothes.setImageURI(imageUri);
            //將剛剛選擇的圖片路徑指定給mPath
            mPath = data.getData().getPath(); /** to辛亞 這個是相簿裡選擇的圖片路徑*/
        }
        else{
            Toast.makeText(this, "未作任何拍攝或選取圖片", Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean check_User_Input() {
        //取得使用者輸入的衣服名稱(限制不得為空白)，若為空白則跳出此上傳衣服方法
        String clothImageName = edtClothImageName.getText().toString().trim();
        if(clothImageName.isEmpty()){
            edtClothImageName.setError("衣服名稱不得為空白");
            edtClothImageName.requestFocus();
            return false;
        }
        String color_H = clothImageH.getText().toString().trim();
        String color_S = clothImageS.getText().toString().trim();
        String color_V = clothImageV.getText().toString().trim();
        if(color_H.isEmpty()){
            clothImageH.setError("衣服顏色H屬性不得為空白");
            clothImageH.requestFocus();
            return false;
        }
        if(color_S.isEmpty()){
            clothImageS.setError("衣服顏色S屬性不得為空白");
            clothImageS.requestFocus();
            return false;
        }
        if(color_V.isEmpty()){
            clothImageV.setError("衣服顏色V屬性不得為空白");
            clothImageV.requestFocus();
            return false;
        }
        else {
            return true;
        }
    }

    //將圖片上傳到firestore及storage的方法
    private void uploadToFirebase(Uri uri){

        loadingDialog.start_Loading_Dialog();

        //取得該帳號的登入使用者
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        String clothImageName = edtClothImageName.getText().toString().trim();

        /** 以下暫存 */
        String color_H = clothImageH.getText().toString().trim();
        String color_S = clothImageS.getText().toString().trim();
        String color_V = clothImageV.getText().toString().trim();
        /** 以上暫存 */

        //設定存放圖片的Storage路徑，"User/使用者的Uid/Clothes_Image/"，後面為檔案名稱：取得系統絕對時間 + getFileExtension(uri)
        StorageReference imagePathRef = storageRef.child("User/" + firebaseUser.getUid() + "/Clothes_Image/" + System.currentTimeMillis() + "." + getFileExtension(uri));
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
                        imageClothes.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_add_photo_alternate_24));

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

                        imageData.put("H", color_H);
                        imageData.put("S", color_S);
                        imageData.put("V", color_V);
                        if(lengthSelect.equals("長"))
                            imageData.put("length", "L");
                        else
                            imageData.put("length", "S");
                        if(widthSelect.equals("寬"))
                            imageData.put("width", "B");
                        else
                            imageData.put("width", "T");

                        /** 以上暫存 */

                        //圖片除了上傳到Storage，也要將url存進firestore資料庫，之後才能撈圖片資料
                        //指定要存放的firestore集合路徑，並將hashmap圖片資料.set()放進firestore，路徑為：集合user_Information -> 該登入使用者的文件(Uid) -> 集合Clothes -> 該衣服的名稱文件
                        firebaseFirestore.collection("user_Information").document(firebaseUser.getUid()).collection("Clothes").document(clothImageName)
                                .set(imageData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(AddClothes.this, "新增圖片URL至firestore成功", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddClothes.this, "新增圖片URL至firestore失敗\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        //將圖片的使用者輸入清空
                        edtClothImageName.setText("");
                        edtClothImageHashtag1.setText("");
                        edtClothImageHashtag2.setText("");
                        edtClothImageHashtag3.setText("");

                        loadingDialog.dismiss_Dialog();
                        Toast.makeText(AddClothes.this, "上傳圖片至storge成功", Toast.LENGTH_SHORT).show();
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
                imageClothes.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_add_photo_alternate_24));
                Toast.makeText(AddClothes.this, "上傳圖片失敗", Toast.LENGTH_SHORT).show();
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