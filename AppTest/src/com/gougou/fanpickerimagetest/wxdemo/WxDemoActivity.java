package com.gougou.fanpickerimagetest.wxdemo;

import java.util.ArrayList;

import com.gougou.fanimgpickerlibrary.DataHolder;
import com.gougou.fanimgpickerlibrary.ImagePicker;
import com.gougou.fanimgpickerlibrary.bean.ImageItem;
import com.gougou.fanimgpickerlibrary.ui.ImageGridActivity2;
import com.gougou.fanimgpickerlibrary.ui.ImagePreviewDelActivity;
import com.gougou.fanpickerimagetest.R;
import com.gougou.fanpickerimagetest.imageloader.GlideImageLoader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * ================================================
 * 作    者：ikkong （ikkong@163.com），修改 jeasonlzy（廖子尧）
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：微信图片选择的Adapter, 感谢 ikkong 的提交
 * ================================================
 */
public class WxDemoActivity extends AppCompatActivity implements WxDemoAdapter.OnRecyclerViewItemClickListener {

    public static final int IMAGE_ITEM_ADD = -1;
    public static final int REQUEST_CODE_SELECT = 100;
    public static final int REQUEST_CODE_PREVIEW = 101;

    private WxDemoAdapter adapter;
    private ArrayList<ImageItem> selImageList; //当前选择的所有图片
    private int maxImgCount = 9;               //允许选择图片最大数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxdemo);

        //最好放到 Application oncreate执行
        initImagePicker();
        initWidget();
    }

    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setSelectLimit(maxImgCount);              //选中数量限制
        imagePicker.setShowCamera(true);                      //显示拍照按钮
        imagePicker.setMaxSize(512);						  //压缩之后的最大大小
        imagePicker.setMaxWid(1600);						  //压缩之后的最大宽度
        imagePicker.setMaxHei(1600);						  //压缩之后的最大高度
        imagePicker.setCompress(true);						  //是否压缩图片
        
        /**
         * 以下这些参数根据需求指定
         */
//        imagePicker.setCrop(false);                           //允许裁剪（单选才有效）
//        imagePicker.setSaveRectangle(true);                   //是否按矩形区域保存
//        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
//        imagePicker.setFocusWidth(800);                       //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
//        imagePicker.setFocusHeight(800);                      //裁剪框的高度。单位像素（圆形自动取宽高最小值）
//        imagePicker.setOutPutX(1000);                         //保存文件的宽度。单位像素
//        imagePicker.setOutPutY(1000);                         //保存文件的高度。单位像素
    }

    private void initWidget() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        selImageList = new ArrayList<>();
        adapter = new WxDemoAdapter(this, selImageList, maxImgCount);
        adapter.setOnItemClickListener(this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (position) {
            case IMAGE_ITEM_ADD:
                //打开选择,本次允许选择的数量
                ImagePicker.getInstance().setSelectLimit(maxImgCount - selImageList.size());
//                Intent intent = new Intent(this, ImageGridActivity.class);
                Intent intent = new Intent(this, ImageGridActivity2.class);
                startActivityForResult(intent, REQUEST_CODE_SELECT);
                break;
            default:
                //打开预览
                Intent intentPreview = new Intent(this, ImagePreviewDelActivity.class);
                ArrayList<ImageItem> selectedList = (ArrayList<ImageItem>) adapter.getImages();
                Log.i("imagepicker", "打开预览selectedList--->" + selectedList.size());
                intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, selectedList);
                DataHolder.getInstance().save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS, selectedList);
                intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
                startActivityForResult(intentPreview, REQUEST_CODE_PREVIEW);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            //添加图片返回
            if (data != null && requestCode == REQUEST_CODE_SELECT) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                selImageList.addAll(images);
                Log.i("imagepicker", "添加图片返回selImageList--->" + selImageList.size());
                adapter.setImages(selImageList);
            }
        } else if (resultCode == ImagePicker.RESULT_CODE_BACK) {
            //预览图片返回
            if (data != null && requestCode == REQUEST_CODE_PREVIEW) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS);
                selImageList.clear();
                selImageList.addAll(images);
                Log.i("imagepicker", "预览图片返回selImageList--->" + selImageList.size());
                adapter.setImages(selImageList);
            }
        }
    }
}
