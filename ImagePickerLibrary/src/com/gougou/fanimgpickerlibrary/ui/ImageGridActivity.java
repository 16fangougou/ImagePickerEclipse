package com.gougou.fanimgpickerlibrary.ui;

import java.util.ArrayList;
import java.util.List;

import com.gougou.fanimgpickerlibrary.DataHolder;
import com.gougou.fanimgpickerlibrary.ImageDataSource;
import com.gougou.fanimgpickerlibrary.ImagePicker;
import com.gougou.fanimgpickerlibrary.R;
import com.gougou.fanimgpickerlibrary.adapter.ImageFolderAdapter;
import com.gougou.fanimgpickerlibrary.adapter.ImageGridAdapter;
import com.gougou.fanimgpickerlibrary.bean.ImageFolder;
import com.gougou.fanimgpickerlibrary.bean.ImageItem;
import com.gougou.fanimgpickerlibrary.utils.CompressPhotoUtils;
import com.gougou.fanimgpickerlibrary.utils.CompressPhotoUtils.CompressCallBack;
import com.gougou.fanimgpickerlibrary.view.FolderPopUpWindow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImageGridActivity extends ImageBaseActivity implements ImageDataSource.OnImagesLoadedListener, ImageGridAdapter.OnImageItemClickListener, ImagePicker.OnImageSelectedListener, View.OnClickListener {

    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    public static final int REQUEST_PERMISSION_CAMERA = 0x02;

    private ImagePicker imagePicker;
    private Context context;

    private boolean isOrigin = false;  //是否选中原图
    private GridView mGridView;  //图片展示控件
    private View mFooterBar;     //底部栏
    private Button mBtnOk;       //确定按钮
    private Button mBtnDir;      //文件夹切换按钮
    private Button mBtnPre;      //预览按钮
    private ImageFolderAdapter mImageFolderAdapter;    //图片文件夹的适配器
    private FolderPopUpWindow mFolderPopupWindow;  //ImageSet的PopupWindow
    private List<ImageFolder> mImageFolders;   //所有的图片文件夹
    private ImageGridAdapter mImageGridAdapter;  //图片九宫格展示的适配器
	private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);

        context = this;
        imagePicker = ImagePicker.getInstance();
        imagePicker.clear();
        imagePicker.addOnImageSelectedListener(this);

        findViewById(R.id.btn_back).setOnClickListener(this);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);
        mBtnDir = (Button) findViewById(R.id.btn_dir);
        mBtnDir.setOnClickListener(this);
        mBtnPre = (Button) findViewById(R.id.btn_preview);
        mBtnPre.setOnClickListener(this);
        mGridView = (GridView) findViewById(R.id.gridview);
        mFooterBar = findViewById(R.id.footer_bar);
        if (imagePicker.isMultiMode()) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnPre.setVisibility(View.VISIBLE);
        } else {
            mBtnOk.setVisibility(View.GONE);
            mBtnPre.setVisibility(View.GONE);
        }

        mImageGridAdapter = new ImageGridAdapter(this, null);
        mImageFolderAdapter = new ImageFolderAdapter(this, null);

        onImageSelected(0, null, false);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new ImageDataSource(this, null, this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }
        }
        
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
//        progressDialog.setTitle("压缩");
        progressDialog.setMessage("正在压缩，请稍后...");
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
    }

    @SuppressLint("Override")
	@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ImageDataSource(this, null, this);
            } else {
                showToast("权限被禁止，无法选择本地图片");
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imagePicker.takePicture(this, ImagePicker.REQUEST_CODE_TAKE);
            } else {
                showToast("权限被禁止，无法打开相机");
            }
        }
    }

    @Override
    protected void onDestroy() {
        imagePicker.removeOnImageSelectedListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            Intent intent = new Intent();
            intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent);  //多选不允许裁剪裁剪，返回数据
            // 需要压缩
            int size = imagePicker.getSelectedImages().size();
            Log.i("imagepicker", "id == R.id.btn_ok size = " + size);
            List<String> imgList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
            	imgList.add(imagePicker.getSelectedImages().get(i).path);
			}
//            progressDialog = ProgressDialog.show(context, null, "压缩处理中...");
            progressDialog.show();
            new CompressPhotoUtils().CompressPhoto(context, imgList, new CompressCallBack() {
				@Override
				public void success(List<String> list) {
					for (int i = 0; i < list.size(); i++) {
						imagePicker.getSelectedImages().get(i).path = list.get(i);
					}
					progressDialog.dismiss();
					finish();
				}
			});
            
        } else if (id == R.id.btn_dir) {
            if (mImageFolders == null) {
                Log.i("imagepicker", "ImageGridActivity 您的手机没有图片");
                return;
            }
            //点击文件夹按钮
            createPopupFolderList();
            mImageFolderAdapter.refreshData(mImageFolders);  //刷新数据
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.showAtLocation(mFooterBar, Gravity.NO_GRAVITY, 0, 0);
                //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                int index = mImageFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }
        } else if (id == R.id.btn_preview) {
            Intent intent = new Intent(ImageGridActivity.this, ImagePreviewActivity.class);
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0);
//            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getSelectedImages());
            DataHolder.getInstance().save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS, imagePicker.getSelectedImages());
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);
        } else if (id == R.id.btn_back) {
            //点击返回按钮
        	Log.i("imagepicker", "//点击返回按钮");
        	// 不需要压缩
            finish();
        }
    }

    /** 创建弹出的ListView */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mImageFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mImageFolderAdapter.setSelectIndex(position);
                imagePicker.setCurrentImageFolderPosition(position);
                mFolderPopupWindow.dismiss();
                ImageFolder imageFolder = (ImageFolder) adapterView.getAdapter().getItem(position);
                if (null != imageFolder) {
                    mImageGridAdapter.refreshData(imageFolder.images);
                    mBtnDir.setText(imageFolder.name);
                }
                mGridView.smoothScrollToPosition(0);//滑动到顶部
            }
        });
        mFolderPopupWindow.setMargin(mFooterBar.getHeight());
    }

    @Override
    public void onImagesLoaded(List<ImageFolder> imageFolders) {
        this.mImageFolders = imageFolders;
        imagePicker.setImageFolders(imageFolders);
        if (imageFolders.size() == 0) mImageGridAdapter.refreshData(null);
        else mImageGridAdapter.refreshData(imageFolders.get(0).images);
        mImageGridAdapter.setOnImageItemClickListener(this);
        mGridView.setAdapter(mImageGridAdapter);
        mImageFolderAdapter.refreshData(imageFolders);
    }

    @Override
    public void onImageItemClick(View view, ImageItem imageItem, int position) {
        //根据是否有相机按钮确定位置
        position = imagePicker.isShowCamera() ? position - 1 : position;
        if (imagePicker.isMultiMode()) {
            Intent intent = new Intent(ImageGridActivity.this, ImagePreviewActivity.class);
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
            DataHolder.getInstance().save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS, imagePicker.getCurrentImageFolderItems());
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);  //如果是多选，点击图片进入预览界面
        } else {
            imagePicker.clearSelectedImages();
            imagePicker.addSelectedImageItem(position, imagePicker.getCurrentImageFolderItems().get(position), true);
            if (imagePicker.isCrop()) {
                Intent intent = new Intent(ImageGridActivity.this, ImageCropActivity.class);
                startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
            } else {
                Intent intent = new Intent();
                intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
                setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
                Log.i("imagepicker", "//单选不需要裁剪，返回数据");
                // 需要压缩
                int size = imagePicker.getSelectedImages().size();
                Log.i("imagepicker", "id == R.id.btn_ok size = " + size);
                List<String> imgList = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                	imgList.add(imagePicker.getSelectedImages().get(i).path);
    			}
//                progressDialog = ProgressDialog.show(context, null, "压缩处理中...");
                progressDialog.show();
                new CompressPhotoUtils().CompressPhoto(context, imgList, new CompressCallBack() {
    				@Override
    				public void success(List<String> list) {
    					for (int i = 0; i < list.size(); i++) {
    						imagePicker.getSelectedImages().get(i).path = list.get(i);
    					}
    					progressDialog.dismiss();
    					finish();
    				}
    			});
            }
        }
    }

    @Override
    public void onImageSelected(int position, ImageItem item, boolean isAdd) {
        if (imagePicker.getSelectImageCount() > 0) {
            mBtnOk.setText(getString(R.string.select_complete, imagePicker.getSelectImageCount(), imagePicker.getSelectLimit()));
            mBtnOk.setEnabled(true);
            mBtnPre.setEnabled(true);
        } else {
            mBtnOk.setText(getString(R.string.complete));
            mBtnOk.setEnabled(false);
            mBtnPre.setEnabled(false);
        }
        mBtnPre.setText(getResources().getString(R.string.preview_count, imagePicker.getSelectImageCount()));
        /**
         * 此处，选中图片后，在某些加载框架下，可能会出现闪烁情况，例如picasso。
         */
        mImageGridAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
        	/**
        	 * 使用系统自带相机data ！= null
        	 */
            if (resultCode == ImagePicker.RESULT_CODE_BACK) {
                isOrigin = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
            }else {
                //从拍照界面返回
                //点击 X , 没有选择照片
                if (data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS) == null) {
                    //什么都不做
                	
                	// 对于部分相机，可能指定了路径之后，data还是不为空。在此直接取值
                	if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_TAKE) {
                        //发送广播通知图片增加了
                        ImagePicker.galleryAddPic(this, imagePicker.getTakeImageFile());
                        ImageItem imageItem = new ImageItem();
                        imageItem.path = imagePicker.getTakeImageFile().getAbsolutePath();
                        imagePicker.clearSelectedImages();
                        imagePicker.addSelectedImageItem(0, imageItem, true);
                        if (imagePicker.isCrop()) {
                            Intent intent = new Intent(ImageGridActivity.this, ImageCropActivity.class);
                            startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
                            setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
                            Log.i("imagepicker", "//单选不需要裁剪，返回数据");
                            // 需要压缩
                            int size = imagePicker.getSelectedImages().size();
                            Log.i("imagepicker", "id == R.id.btn_ok size = " + size);
                            List<String> imgList = new ArrayList<>();
                            for (int i = 0; i < size; i++) {
                            	imgList.add(imagePicker.getSelectedImages().get(i).path);
                			}
//                            progressDialog = ProgressDialog.show(context, null, "压缩处理中...");
                            progressDialog.show();
                            new CompressPhotoUtils().CompressPhoto(context, imgList, new CompressCallBack() {
                				@Override
                				public void success(List<String> list) {
                					for (int i = 0; i < list.size(); i++) {
                						imagePicker.getSelectedImages().get(i).path = list.get(i);
                					}
                					progressDialog.dismiss();
                					finish();
                				}
                			});
                        }
                    }
                	
                } else {
                    //说明是从裁剪页面过来的数据，直接返回就可以
                    setResult(ImagePicker.RESULT_CODE_ITEMS, data);
                    Log.i("imagepicker", "//说明是从裁剪页面过来的数据，直接返回就可以");
                    // 需要压缩
                    int size = imagePicker.getSelectedImages().size();
                    Log.i("imagepicker", "id == R.id.btn_ok size = " + size);
                    List<String> imgList = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                    	imgList.add(imagePicker.getSelectedImages().get(i).path);
        			}
//                    progressDialog = ProgressDialog.show(context, null, "压缩处理中...");
                    progressDialog.show();
                    new CompressPhotoUtils().CompressPhoto(context, imgList, new CompressCallBack() {
        				@Override
        				public void success(List<String> list) {
        					for (int i = 0; i < list.size(); i++) {
        						imagePicker.getSelectedImages().get(i).path = list.get(i);
        					}
        					progressDialog.dismiss();
        					finish();
        				}
        			});
                }
            }
        } else {
        	/**
        	 * 使用第三方相机data == null
        	 */
            //如果是裁剪，因为裁剪指定了存储的Uri，所以返回的data一定为null
            if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_TAKE) {
                //发送广播通知图片增加了
                ImagePicker.galleryAddPic(this, imagePicker.getTakeImageFile());
                ImageItem imageItem = new ImageItem();
                imageItem.path = imagePicker.getTakeImageFile().getAbsolutePath();
                imagePicker.clearSelectedImages();
                imagePicker.addSelectedImageItem(0, imageItem, true);
                if (imagePicker.isCrop()) {
                    Intent intent = new Intent(ImageGridActivity.this, ImageCropActivity.class);
                    startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
                    setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
                    Log.i("imagepicker", "//单选不需要裁剪，返回数据");
                 // 需要压缩
                    int size = imagePicker.getSelectedImages().size();
                    Log.i("imagepicker", "id == R.id.btn_ok size = " + size);
                    List<String> imgList = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                    	imgList.add(imagePicker.getSelectedImages().get(i).path);
        			}
//                    progressDialog = ProgressDialog.show(context, null, "压缩处理中...");
                    progressDialog.show();
                    new CompressPhotoUtils().CompressPhoto(context, imgList, new CompressCallBack() {
        				@Override
        				public void success(List<String> list) {
        					for (int i = 0; i < list.size(); i++) {
        						imagePicker.getSelectedImages().get(i).path = list.get(i);
        					}
        					progressDialog.dismiss();
        					finish();
        				}
        			});
                }
            }
        }
    }
}