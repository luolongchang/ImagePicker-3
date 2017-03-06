package io.github.changjiashuai.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import io.github.changjiashuai.ImagePicker;
import io.github.changjiashuai.R;
import io.github.changjiashuai.bean.ImageItem;
import io.github.changjiashuai.widget.CropImageView;

public class ImageCropActivity extends AppCompatActivity implements View.OnClickListener, CropImageView.OnBitmapSaveCompleteListener {

    private CropImageView mCropImageView;
    private Bitmap mBitmap;
    private boolean mIsSaveRectangle;
    private int mOutputX;
    private int mOutputY;
    private ArrayList<ImageItem> mImageItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        //初始化View
        initView();
        //获取需要的参数
        initData();
    }

    private void initView() {
        findViewById(R.id.btn_back).setOnClickListener(this);
        Button btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_ok.setText(getString(R.string.image_picker_complete));
        btn_ok.setOnClickListener(this);
        TextView tv_des = (TextView) findViewById(R.id.tv_des);
        tv_des.setText(getString(R.string.image_picker_photo_crop));
        mCropImageView = (CropImageView) findViewById(R.id.cv_crop_image);
        mCropImageView.setOnBitmapSaveCompleteListener(this);
    }

    private void initData() {
        mOutputX = ImagePicker.getInstance().getOutPutX();
        mOutputY = ImagePicker.getInstance().getOutPutY();
        mIsSaveRectangle = ImagePicker.getInstance().isSaveRectangle();
        mImageItems = ImagePicker.getInstance().getSelectedImages();
        String imagePath = mImageItems.get(0).path;

        mCropImageView.setFocusStyle(ImagePicker.getInstance().getStyle());
        mCropImageView.setFocusWidth(ImagePicker.getInstance().getFocusWidth());
        mCropImageView.setFocusHeight(ImagePicker.getInstance().getFocusHeight());

        //缩放图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        options.inSampleSize = calculateInSampleSize(options,
                displayMetrics.widthPixels,
                displayMetrics.heightPixels);
        options.inJustDecodeBounds = false;
        mBitmap = BitmapFactory.decodeFile(imagePath, options);
        mCropImageView.setImageBitmap(mBitmap);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = width / reqWidth;
            } else {
                inSampleSize = height / reqHeight;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_back) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (id == R.id.btn_ok) {
            mCropImageView.saveBitmapToFile(ImagePicker.getInstance().getCropCacheFolder(this),
                    mOutputX, mOutputY, mIsSaveRectangle);
        }
    }

    @Override
    public void onBitmapSaveSuccess(File file) {
        mImageItems.remove(0);
        ImageItem imageItem = new ImageItem();

        imageItem.name = file.getName();
        imageItem.path = file.getAbsolutePath();

        mImageItems.add(imageItem);

        ImagePicker.getInstance().setSelectedImages(mImageItems);
        Intent intent = new Intent();
        intent.putExtra(ImagePicker.EXTRA_IS_ORIGIN, true);
        setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
        finish();
    }

    @Override
    public void onBitmapSaveError(File file) {

    }

    @Override
    protected void onDestroy() {
        if (null != mBitmap && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        super.onDestroy();
    }
}