package com.example.hamigua.shop.seller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hamigua.LoadingDialog;
import com.example.hamigua.R;
import com.example.hamigua.SetOutlinedButtonStyle;
import com.example.hamigua.shop.adapter.ShopProductColorRecyclerViewAdapter;
import com.example.hamigua.shop.model.ProductColorData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ChooseProductSize extends AppCompatActivity implements ShopProductColorRecyclerViewAdapter.OnItemClickListener{

    private BottomSheetDialog dialog; //下方對話窗，新增規格
    ChipGroup colorChipGroup, sizeChipGroup; //顏色、尺寸ChipGroup
    Chip colorBlackChip, colorWhiteChip, colorAddChip, size_S_Chip, size_M_Chip, size_L_Chip, sizeAddChip; //預設Chip

    MaterialButton nextSizePage; //下一步button

    TextView colorEdit, sizeEdit, recyclerViewHelper; //顏色編輯、尺寸編輯、個別顏色照片上傳提示文字(預設是隱藏)

    RecyclerView colorImageRecyclerView; //個別顏色照片上傳RecyclerView，預設是隱藏
    ShopProductColorRecyclerViewAdapter colorAdapter; //個別顏色照片上傳Adapter

    //productColor, productSize, productUrl - 取得的規格參數
    //colorChecked, sizeChecked - 儲存已選擇的chip文字
    ArrayList<String> productColor, productSize, productUrl, colorChecked, sizeChecked;
    ArrayList<ProductColorData> colorImageData; //商品個別顏色的資料模型物件

    private Uri imageUri; //圖片在本地的uri地址
    private int tempIndex; //取得被點擊的資料模型物件index

    private String mPath = "";//設置照片路徑
    public static final int CAMERA_PERMISSION = 100;//檢測相機權限用
    public static final int REQUEST_HIGH_IMAGE = 101;//檢測相機回傳

    private Handler handler;

    private StorageReference storageRef;
    LoadingDialog loadingDialog; //自定義對話窗(dialog)的物件
    SetOutlinedButtonStyle buttonStyle; //自定義設定button樣式的物件

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
        setContentView(R.layout.activity_seller_choose_product_size);

        //設置標題列ActionBar的左上角返回鍵
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        colorChipGroup = findViewById(R.id.colorChipGroup);
        sizeChipGroup = findViewById(R.id.sizeChipGroup);

        colorAddChip = findViewById(R.id.colorAddChip);
        colorAddChip.setOnClickListener(colorAddListener);
        sizeAddChip = findViewById(R.id.sizeAddChip);
        sizeAddChip.setOnClickListener(sizeAddListener);
        colorBlackChip = findViewById(R.id.colorBlackChip);
        colorBlackChip.setOnCheckedChangeListener(colorListener);
        colorWhiteChip = findViewById(R.id.colorWhiteChip);
        colorWhiteChip.setOnCheckedChangeListener(colorListener);
        size_S_Chip = findViewById(R.id.size_S_Chip);
        size_S_Chip.setOnCheckedChangeListener(sizeListener);
        size_M_Chip = findViewById(R.id.size_M_Chip);
        size_M_Chip.setOnCheckedChangeListener(sizeListener);
        size_L_Chip = findViewById(R.id.size_L_Chip);
        size_L_Chip.setOnCheckedChangeListener(sizeListener);

        nextSizePage = findViewById(R.id.nextSizePage);
        nextSizePage.setOnClickListener(nextPageListener);
        nextSizePage.setClickable(false); //設定下一步button無法點擊

        colorEdit = findViewById(R.id.colorEdit);
        colorEdit.setOnClickListener(colorEditListener);
        sizeEdit = findViewById(R.id.sizeEdit);
        sizeEdit.setOnClickListener(sizeEditListener);

        colorImageRecyclerView = findViewById(R.id.colorImageRecyclerView);
        recyclerViewHelper = findViewById(R.id.recyclerViewHelper);

        init();
        get_Camera_Dialog();
    }

    protected void init() {
        dialog = new BottomSheetDialog(this);
        loadingDialog = new LoadingDialog(ChooseProductSize.this);
        buttonStyle = new SetOutlinedButtonStyle(nextSizePage);

        storageRef = FirebaseStorage.getInstance().getReference();

        productColor = new ArrayList<>();
        productSize = new ArrayList<>();
        productUrl = new ArrayList<>();
        colorChecked = new ArrayList<>();
        sizeChecked = new ArrayList<>();
        colorImageData = new ArrayList<>();
    }

    //顏色chip編輯狀態
    protected View.OnClickListener colorEditListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (colorEdit.getText().equals("編輯")) {
                colorEdit.setText("完成");
                for (int i = 0; i < colorChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) colorChipGroup.getChildAt(i); //取得顏色ChipGroup裡每一個chip
                    chip.setCloseIconVisible(true); //設定刪除chip的icon顯示
                    chip.setOnCloseIconClickListener(new View.OnClickListener() { //刪除chip的icon點擊事件
                        @Override
                        public void onClick(View v) {
                            colorChipGroup.removeView(chip); //從顏色ChipGroup移除被點擊到刪除icon的chip
                        }
                    });
                }
            }
            else if (colorEdit.getText().equals("完成")) {
                colorEdit.setText("編輯");
                for (int i = 0; i < colorChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) colorChipGroup.getChildAt(i); //取得顏色ChipGroup裡每一個chip
                    chip.setCloseIconVisible(false); //設定刪除chip的icon隱藏
                }
            }

        }
    };

    //尺寸chip編輯狀態
    protected View.OnClickListener sizeEditListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (sizeEdit.getText().equals("編輯")) {
                sizeEdit.setText("完成");
                for (int i = 0; i < sizeChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) sizeChipGroup.getChildAt(i); //取得尺寸ChipGroup裡每一個chip
                    chip.setCloseIconVisible(true); //設定刪除chip的icon顯示
                    chip.setOnCloseIconClickListener(new View.OnClickListener() { //刪除chip的icon點擊事件
                        @Override
                        public void onClick(View v) {
                            sizeChipGroup.removeView(chip); //從尺寸ChipGroup移除被點擊到刪除icon的chip
                        }
                    });
                }
            }
            else if (sizeEdit.getText().equals("完成")) {
                sizeEdit.setText("編輯");
                for (int i = 0; i < sizeChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) sizeChipGroup.getChildAt(i); //取得尺寸ChipGroup裡每一個chip
                    chip.setCloseIconVisible(false); //設定刪除chip的icon隱藏
                }
            }
        }
    };

    //設定個別顏色照片上傳Adapter
    protected void setColorImageAdapter() {
        colorAdapter = new ShopProductColorRecyclerViewAdapter(ChooseProductSize.this, colorImageData, this);
        GridLayoutManager manager = new GridLayoutManager(ChooseProductSize.this, 1, GridLayoutManager.HORIZONTAL, false);
        colorImageRecyclerView.setLayoutManager(manager);
        colorImageRecyclerView.setAdapter(colorAdapter);
    }

    //黑色、白色chip的點擊事件
    protected CompoundButton.OnCheckedChangeListener colorListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                colorChecked.add(buttonView.getText().toString()); //將文字add進已選擇的顏色arraylist

                ProductColorData productColorData = new ProductColorData(buttonView.getText().toString(), null);
                colorImageData.add(productColorData); //將文字add進個別顏色的資料模型物件

                recyclerViewHelper.setVisibility(View.VISIBLE); //顯示個別顏色照片上傳提示文字
                colorImageRecyclerView.setVisibility(View.VISIBLE); //顯示個別顏色照片上傳RecyclerView
                setColorImageAdapter(); //設定個別顏色照片上傳Adapter(重整資料)
                if(sizeChecked.size() != 0) //已選擇的尺寸!=0
                    buttonStyle.setButtonClickable();
            }
            else {
                colorChecked.remove(buttonView.getText().toString()); //將文字從已選擇的顏色arraylist中移除
                colorImageData.removeIf(item -> item.product_color.equals(buttonView.getText().toString())); //將文字從個別顏色的資料模型物件中移除
                setColorImageAdapter(); //設定個別顏色照片上傳Adapter(重整資料)
                if(colorChecked.size() == 0) { //已選擇的顏色==0
                    recyclerViewHelper.setVisibility(View.GONE); //隱藏個別顏色照片上傳提示文字
                    colorImageRecyclerView.setVisibility(View.GONE); //隱藏個別顏色照片上傳RecyclerView
                }
                if(colorChecked.size() == 0 || sizeChecked.size() == 0) //已選擇的顏色==0 且 已選擇的尺寸==0
                    buttonStyle.setButtonUnClickable();
            }

        }
    };

    //尺寸S、尺寸M、尺寸L的chip的點擊事件
    protected CompoundButton.OnCheckedChangeListener sizeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                sizeChecked.add(buttonView.getText().toString()); //將文字add進已選擇的尺寸arraylist
                if(colorChecked.size() != 0) //已選擇的顏色!=0
                    buttonStyle.setButtonClickable();
            }
            else {
                sizeChecked.remove(buttonView.getText().toString()); //將文字從已選擇的尺寸arraylist中移除
                if(colorChecked.size() == 0 || sizeChecked.size() == 0) //已選擇的顏色==0 且 已選擇的尺寸==0
                    buttonStyle.setButtonUnClickable();
            }
        }
    };

    //使用者新增的chip
    protected void AddChip(String txtChip, int requestCode) {
        LayoutInflater inflater = LayoutInflater.from(this);
        if (requestCode == 1) { //新增顏色chip
            Chip chip = (Chip)inflater.inflate(R.layout.product_color_chip_item, null, false); //設定自訂chip樣式
            chip.setText(txtChip); //設定chip文字
            chip.setCheckable(true); //設定chip可勾選
            chip.setClickable(true); //設定chip點擊
            chip.setOnCheckedChangeListener(colorListener); //設定新增顏色chip的點擊事件
            colorChipGroup.addView(chip); //新增chip至顏色ChipGroup

        } else { //新增尺寸chip
            Chip chip = (Chip)inflater.inflate(R.layout.product_size_chip_item, null, false); //設定自訂chip樣式
            chip.setText(txtChip); //設定chip文字
            chip.setCheckable(true); //設定chip可勾選
            chip.setClickable(true); //設定chip點擊
            chip.setOnCheckedChangeListener(sizeListener); //設定新增尺寸chip的點擊事件
            sizeChipGroup.addView(chip); //新增chip至尺寸ChipGroup
        }
    }

    //下一步button的點擊事件
    protected View.OnClickListener nextPageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //顏色照片
            //檢查每個顏色都有上傳照片，若找到url為null會結束方法
            for(int i = 0; i < colorImageData.size(); i++) {
                if(colorImageData.get(i).product_image_url == null) {
                    Toast.makeText(ChooseProductSize.this, "商品照片尚未上傳完整\n請上傳後再儲存資料", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    productUrl.add(colorImageData.get(i).product_image_url); //將url放進arraylist
                }
            }

            //顏色
            for (int i = 0; i < colorImageData.size(); i++) {
                productColor.add(colorImageData.get(i).product_color); //將顏色放進arraylist
            }

            //尺寸
            for (int i = 0; i < sizeChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) sizeChipGroup.getChildAt(i); //取得尺寸ChipGroup的每一個chip
                if (chip.isChecked())
                    productSize.add(chip.getText().toString()); //將尺寸放進arraylist
            }

            //傳到下一個activity(ChooseProductQuantity)
            Intent intent = new Intent();
            intent.setClass(ChooseProductSize.this, ChooseProductQuantity.class);
            intent.putExtra("productColor", productColor); //放入傳入參數(顏色arraylist)
            intent.putExtra("productSize", productSize); //放入傳入參數(尺寸arraylist)
            intent.putExtra("productUrl", productUrl); //放入傳入參數(顏色照片arraylist)
            startActivity(intent);
            //清空arraylist
            productColor.clear();
            productSize.clear();
            productUrl.clear();
        }
    };

    //顏色-新增chip的點擊事件
    protected View.OnClickListener colorAddListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            createDialog(1); //開啟下方對話窗(dialog)，參數為顏色(requestCode：1)
            dialog.show(); //顯示下方對話窗(dialog)
        }
    };

    //尺寸-新增chip的點擊事件
    protected View.OnClickListener sizeAddListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            createDialog(2); //開啟下方對話窗(dialog)，參數為尺寸(requestCode：2)
            dialog.show(); //顯示下方對話窗(dialog)
        }
    };

    //設定下方對話窗(dialog)，用來讓使用者輸入新增的chip，參數requestCode代表新增顏色or尺寸
    protected void createDialog(int requestCode) {
        View view = getLayoutInflater().inflate(R.layout.product_size_bottom_dialog, null, false); //設定對話窗(dialog)的layout樣式

        //設定對話窗(dialog)的layout的物件id
        EditText edtSize = view.findViewById(R.id.edtSize); //使用者輸入欄位
        ImageView dialogFinish = view.findViewById(R.id.dialogFinish); //關閉對話窗imageView
        ImageView edtClear = view.findViewById(R.id.edtClear); //清空使用者輸入欄位imageView

        //設定使用者輸入欄位的動作事件
        edtSize.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) { //按下鍵盤的輸入(enter)後
                    InputMethodManager manager = ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE));
                    if (manager != null) {
                        manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); //關閉鍵盤
                    }
                    dialog.dismiss(); //關閉下方對話窗(dialog)
                    AddChip(edtSize.getText().toString(), requestCode); //將使用者輸入欄位的文字、requestCode傳到AddChip(新增chip方法)
                    edtSize.setText(""); //清空使用者輸入欄位
                }
                return false;
            }
        });

        //關閉對話窗imageView的點擊事件
        dialogFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); //關閉下方對話窗(dialog)
            }
        });

        //清空使用者輸入欄位imageView的點擊事件
        edtClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSize.setText(""); //清空使用者輸入欄位
            }
        });

        dialog.setContentView(view); //將設定好的layout樣式指定給此class的下方對話窗(dialog)物件
    }

    //實作adapter裡的介面的方法
    @Override
    public void onImageClick(ProductColorData productColorData, int index) {
        tempIndex = index; //將點擊到的arraylist的index指定給此class的全域變數
        showDialog(); //顯示上傳對話窗(dialog)
    }

    //詢問使用者要用哪種方式上傳照片，並執行intent
    protected void showDialog() {
        //檢查是否取得相機權限，若沒有則再詢問一次
        if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
        }

        //設置確認上傳方式的對話視窗
        AlertDialog.Builder dialog = new AlertDialog.Builder(ChooseProductSize.this);
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
                    Toast.makeText(ChooseProductSize.this, "請先開啟相機權限", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                //取得相片檔案的uri位址及設定檔案名稱
                File imageFile = getImageFile();
                if (imageFile == null) return;
                //取得相片檔案的本地uri地址
                imageUri = FileProvider.getUriForFile(
                        ChooseProductSize.this,
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

                handler.sendEmptyMessage(0); //用handler來執行後續的程式(有執行緒(Thread)問題)
                upload_Color_Image_To_Firebase(imageUri); //上傳圖片至firebase-storage
            }).start();
        }
        //若是得到用相簿所選擇的照片回傳
        else if(requestCode == 2 && resultCode == RESULT_OK && data != null){
            //取得圖片在本地的uri
            imageUri = data.getData();
            //將剛剛選擇的圖片路徑指定給mPath
            mPath = data.getData().getPath();

            loadingDialog.start_Loading_Dialog(); //顯示讀取中dialog
            upload_Color_Image_To_Firebase(imageUri); //上傳圖片至firebase-storage
        }
        else{
            Toast.makeText(this, "未作任何拍攝或選取圖片", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("HandlerLeak")
    protected void get_Camera_Dialog() {
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                loadingDialog.start_Loading_Dialog(); //顯示讀取中dialog
            }
        };
    }

    //上傳圖片至firebase-storage，參數是此class的全域變數imageUri
    protected void upload_Color_Image_To_Firebase(Uri uri) {

        //設定存放圖片的Storage路徑，"User/使用者的Uid/Clothes_Image/"，後面為檔案名稱：取得系統絕對時間 + getFileExtension(uri)
        StorageReference colorImagePathRef = storageRef.child("Products_Image/" + System.currentTimeMillis() + "." + getFileExtension(uri));
        colorImagePathRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                colorImagePathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        colorImageData.get(tempIndex).product_image_url = uri.toString(); //透過全域變數index，取得點擊到的顏色物件，將url指定給該物件
                        setColorImageAdapter(); //設定個別顏色照片上傳Adapter(重整資料)
                        loadingDialog.dismiss_Dialog(); //關閉讀取中dialog
                    }
                });
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