package com.example.hamigua.shop.seller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
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
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hamigua.LoadingDialog;
import com.example.hamigua.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AddNewProducts extends AppCompatActivity {

    private ImageView product_image; //商品封面圖片
    private Button btn_put_on_market, btn_save_product; //button-上架、儲存
    private TextInputEditText product_name, product_brand, product_detail, product_size, product_price, product_quantity; //商品參數輸入
    private Spinner clothesCategory, accessoriesCategory; //商品類別、配件下拉式選單
    private RadioGroup clothesGender;
    private RadioButton men_clothes, women_clothes, kid_clothes;
    String genderSelect = "男裝";
    String[] category = {"短袖上衣", "長袖上衣", "背心", "短褲", "長褲", "裙子", "外套", "套裝", "配件飾品"}; //商品類別下拉式選單內容陣列
    String[] accessories = {"帽子", "眼鏡", "耳環", "項鍊", "手環", "戒指", "皮/腰包", "包包", "鞋子", "襪子"}; //配件下拉式選單內容陣列
    String categorySelect, accessoriesSelect = "null"; //所選擇的衣服類別 及 配件飾品類別
    String categoryIndex; //用在查詢語法的order by中

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageRef;
    private String productID = null; //商品在firebase上的文件ID

    //從輸入規格數量(ChooseProductQuantity)傳回來的規格參數
    ArrayList<String> arrProductColor, arrProductSize, arrProductQuantity, arrProductUrl;

    private Uri imageUri; //圖片在本地的uri地址

    private String mPath = "";//設置照片路徑
    public static final int CAMERA_PERMISSION = 100;//檢測相機權限用
    public static final int REQUEST_HIGH_IMAGE = 101;//檢測相機回傳

    LoadingDialog loadingDialog; //自定義對話窗(dialog)的物件

    //標題列ActionBar的左上角返回鍵監聽事件
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_add_new_products);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        product_image = findViewById(R.id.product_image);
        btn_put_on_market = findViewById(R.id.btn_put_on_market);
        btn_save_product = findViewById(R.id.btn_save_product);
        product_name = findViewById(R.id.product_name);
        product_brand = findViewById(R.id.product_brand);
        product_detail = findViewById(R.id.product_detail);
        product_size = findViewById(R.id.product_size);
        product_price = findViewById(R.id.product_price);
        product_quantity = findViewById(R.id.product_quantity);
        clothesCategory = findViewById(R.id.spinnerClothesCategory);
        accessoriesCategory = findViewById(R.id.spinnerAccessoriesCategory);
        clothesGender = findViewById(R.id.clothesGender);
        clothesGender.setOnCheckedChangeListener(genderListener);
        men_clothes = findViewById(R.id.men_clothes);
        women_clothes = findViewById(R.id.women_clothes);
        kid_clothes = findViewById(R.id.kid_clothes);

        //選擇衣服類別spinner的adapter
        ArrayAdapter<String> adapter_ClothesCategory = new ArrayAdapter<String>(AddNewProducts.this, android.R.layout.simple_spinner_dropdown_item, category);
        adapter_ClothesCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clothesCategory.setAdapter(adapter_ClothesCategory);
        //選擇衣服類別spinner的觸發事件
        clothesCategory.setOnItemSelectedListener(clothesCategory_Listener);

        //取得firestore、authentication、storage的連接
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        btn_put_on_market.setOnClickListener(put_product_on_market);
        btn_save_product.setOnClickListener(save_product_on_myMarket);
        product_image.setOnClickListener(choose_product_image);
        product_size.setOnClickListener(choose_product_size);
        product_size.setFocusable(false); //使用者無法手動輸入

        loadingDialog = new LoadingDialog(AddNewProducts.this);

    }

    //上架商品button的觸發事件
    private final View.OnClickListener put_product_on_market = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //如果圖片在本地的uri地址不為null
            if(imageUri != null){
                //上傳圖片到Storage，呼叫方法uploadToFirebase並傳入圖片的本地uri地址
                uploadToFirebase(imageUri, "on_the_market");
            } else{
                //如果圖片在本地的uri為null，提醒使用者先選擇圖片
                Toast.makeText(AddNewProducts.this, "請先選擇衣服圖片", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //儲存商品button的觸發事件
    private final View.OnClickListener save_product_on_myMarket = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //如果圖片在本地的uri地址不為null
            if(imageUri != null){
                //上傳圖片到Storage，呼叫方法uploadToFirebase並傳入圖片的本地uri地址
                uploadToFirebase(imageUri, "save_in_my_market");
            } else{
                //如果圖片在本地的uri為null，提醒使用者先選擇圖片
                Toast.makeText(AddNewProducts.this, "請先選擇衣服圖片", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //顯示自定義dialog
    private void open_Progress_Dialog() {
        loadingDialog.start_Loading_Dialog();
    }

    //關閉自定義dialog
    private void dismiss_Progress_Dialog() {
        loadingDialog.dismiss_Dialog();
    }

    //點選上傳照片imageview
    private final View.OnClickListener choose_product_image = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //檢查是否取得相機權限，若沒有則再詢問一次
            if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
            }

            //設置確認上傳方式的對話視窗
            AlertDialog.Builder dialog = new AlertDialog.Builder(AddNewProducts.this);
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
                        Toast.makeText(AddNewProducts.this, "請先開啟相機權限", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                    //取得相片檔案的uri位址及設定檔案名稱
                    File imageFile = getImageFile();
                    if (imageFile == null) return;
                    //取得相片檔案的本地uri地址
                    imageUri = FileProvider.getUriForFile(
                            AddNewProducts.this,
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

    //點擊到商品規格的輸入欄時跳轉activity
    private final View.OnClickListener choose_product_size = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(AddNewProducts.this, ChooseProductSize.class);
            startActivity(intent);
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
            mPath = imageFile.getAbsolutePath();
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
                            .into(product_image);//將處理完的照片顯示在imageView
                });
            }).start();
        }
        //若是得到用相簿所選擇的照片回傳
        else if(requestCode == 2 && resultCode == RESULT_OK && data != null){
            //取得圖片在本地的uri
            imageUri = data.getData();
            //剛剛選擇的圖片放在imageView
            product_image.setImageURI(imageUri);
            //將剛剛選擇的圖片路徑指定給mPath
            mPath = data.getData().getPath();
        }
        else{
            Toast.makeText(this, "未作任何拍攝或選取圖片", Toast.LENGTH_SHORT).show();
        }
    }

    protected RadioGroup.OnCheckedChangeListener genderListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.men_clothes:
                    genderSelect = "男裝";
                    Log.d("aaaaaaaaa", genderSelect);
                    break;
                case R.id.women_clothes:
                    genderSelect = "女裝";
                    Log.d("aaaaaaaaa", genderSelect);
                    break;
                case R.id.kid_clothes:
                    genderSelect = "童裝";
                    Log.d("aaaaaaaaa", genderSelect);
                    break;
            }
        }
    };

    //接收從ChooseProductQuantity回傳的規格參數
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        arrProductColor = intent.getStringArrayListExtra("productColor");
        arrProductSize = intent.getStringArrayListExtra("productSize");
        arrProductQuantity = intent.getStringArrayListExtra("productQuantity");
        arrProductUrl = intent.getStringArrayListExtra("productUrl");

        StringBuilder sizeResult = new StringBuilder(); //顯示所選規格字串
        int quantityResult = 0; //顯示所選規格數量總和

        //處理所選規格字串
        for(int i = 0; i < arrProductColor.size(); i++) {
            for(int j = 0; j < arrProductSize.size(); j++) {
                sizeResult.append(arrProductColor.get(i)).append(",").append(arrProductSize.get(j));
                if(j != arrProductSize.size() - 1)
                    sizeResult.append(" ; ");
            }
            if(i < arrProductColor.size() - 1)
                sizeResult.append(" ; ");
        }

        //處理所選規格數量總和
        for (String quantity : arrProductQuantity) {
            quantityResult += Integer.parseInt(quantity);
        }

        //設定顯示所選規格結果
        product_size.setText(sizeResult);
        product_quantity.setText(String.valueOf(quantityResult));
    }

    //將圖片上傳到firestore及storage的方法
    private void uploadToFirebase(Uri uri, String productStatus){

        //取得該帳號的登入使用者
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //取得使用者輸入的衣服名稱(限制不得為空白)，若為空白則跳出此上傳衣服方法
        String productName = product_name.getText().toString().trim();
        String productBrand = product_brand.getText().toString().trim();
        String productDetail = product_detail.getText().toString().trim();
        String productSize = product_size.getText().toString().trim();
        String productPrice = product_price.getText().toString().trim();
        String productQuantity = product_quantity.getText().toString().trim();

        if(productName.isEmpty()){
            product_name.setError("商品名稱不得為空白");
            product_name.requestFocus();
            return;
        }
        if(productDetail.isEmpty()){
            product_detail.setError("商品描述不得為空白");
            product_detail.requestFocus();
            return;
        }
        if(productSize.isEmpty()){
            product_size.setError("商品尺寸不得為空白");
            product_size.requestFocus();
            return;
        }
        if(productPrice.isEmpty()){
            product_price.setError("商品價格不得為空白");
            product_price.requestFocus();
            return;
        }
        if(productQuantity.isEmpty()){
            product_quantity.setError("商品數量不得為空白");
            product_quantity.requestFocus();
            return;
        }

        open_Progress_Dialog(); //顯示自定義dialog

        //設定存放圖片的Storage路徑，"User/使用者的Uid/Clothes_Image/"，後面為檔案名稱：取得系統絕對時間 + getFileExtension(uri)
        StorageReference imagePathRef = storageRef.child("Products_Image/" + System.currentTimeMillis() + "." + getFileExtension(uri));
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
                        String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());

                        //取得該登入帳號的使用者，即為賣家(Seller)
                        firebaseFirestore.collection("user_Information").document(firebaseAuth.getCurrentUser().getUid())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if(document.exists()) {

                                                //處理顏色資料
                                                StringBuilder colorResult = new StringBuilder();
                                                for (int i = 0; i < arrProductColor.size(); i++)
                                                    colorResult.append(arrProductColor.get(i)).append(",").append(arrProductUrl.get(i)).append(";");
                                                //處理數量資料
                                                StringBuilder quantityResult = new StringBuilder();
                                                int quantityIndex = 0;
                                                for (int i = 0; i < arrProductColor.size(); i++) {
                                                    for(int j = 0; j < arrProductSize.size(); j++) {
                                                        quantityResult.append(arrProductColor.get(i)).append(",").append(arrProductSize.get(j))
                                                                .append(",").append(arrProductQuantity.get(quantityIndex)).append(";");
                                                        quantityIndex += 1;
                                                    }
                                                }

                                                //將string型別的圖片輸入資料放進hashmap
                                                Map<String, Object> productData = new HashMap<>();
                                                productData.put("product_Name", productName);

                                                if(productBrand.isEmpty())
                                                    productData.put("product_Brand", "");
                                                else
                                                    productData.put("product_Brand", productBrand);

                                                productData.put("product_Gender", genderSelect);
                                                productData.put("product_Detail", productDetail);
                                                productData.put("product_Size", productSize);
                                                productData.put("product_Price", productPrice);
                                                productData.put("product_Quantity", productQuantity);
                                                productData.put("product_Category", categorySelect);
                                                productData.put("accessories_Category", accessoriesSelect);
                                                productData.put("seller_Name", document.getString("userName"));
                                                productData.put("seller_ID", firebaseUser.getUid());
                                                productData.put("product_Image_URL", uri.toString());
                                                productData.put("product_status", productStatus);
                                                productData.put("set_Data_Time", timeStamp);
                                                productData.put("colorString", colorResult.toString());
                                                productData.put("quantityString", quantityResult.toString());

                                                //圖片除了上傳到Storage，也要將url存進firestore資料庫，之後才能撈圖片資料
                                                firebaseFirestore.collection("products_Information")
                                                        .add(productData) //在firebase新增商品文件
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                if(task.isSuccessful()) {
                                                                    if(productStatus.equals("on_the_market"))
                                                                        Toast.makeText(AddNewProducts.this, "上架商品成功", Toast.LENGTH_SHORT).show();
                                                                    else
                                                                        Toast.makeText(AddNewProducts.this, "儲存商品成功", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(AddNewProducts.this, "新增圖片URL至firestore失敗", Toast.LENGTH_SHORT).show();
                                                                }
                                                                dismiss_Progress_Dialog(); //關閉自定義dialog
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                });

                        //將圖片的使用者輸入清空
                        product_name.setText("");
                        product_brand.setText("");
                        product_detail.setText("");
                        product_size.setText("");
                        product_price.setText("");
                        product_quantity.setText("");
                        //將衣服imageView設置為原先的icon
                        product_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_add_photo_alternate_24));
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //此為正在執行上傳圖片及新增文件時的方法
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //若圖片上傳失敗
                dismiss_Progress_Dialog(); //關閉自定義dialog
                //將衣服imageView設置為原先的icon
                product_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_add_photo_alternate_24));
                Toast.makeText(AddNewProducts.this, "上傳圖片失敗", Toast.LENGTH_SHORT).show();
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
                ArrayAdapter<String> adapter_AccessoriesCategory = new ArrayAdapter<String>(AddNewProducts.this, android.R.layout.simple_spinner_dropdown_item, accessories);
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
}