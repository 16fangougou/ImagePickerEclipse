package com.gougou.fanimgpickerlibrary.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gougou.fanimgpickerlibrary.ImagePicker;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author SherlockHolmes
 * 压缩图片，尺寸压缩和质量压缩，可以设置文件大小和尺寸大小限制
 */
public class CompressPhotoUtils {

	public static String cachePath = "";
	private static Context context;
	private List<String> fileList = new ArrayList<>();
//	private ProgressDialog progressDialog;
	private static ImagePicker imagePicker = ImagePicker.getInstance();

	public void CompressPhoto(Context context, List<String> list, CompressCallBack callBack) {
		CompressTask task = new CompressTask(context, list, callBack);
		this.context = context;
		task.execute();
	}

	class CompressTask extends AsyncTask<Void, Integer, Integer> {
		private List<String> list;
		private CompressCallBack callBack;
		private Context context;

		CompressTask(Context context, List<String> list, CompressCallBack callBack) {
			this.context = context;
			this.list = list;
			this.callBack = callBack;
			Log.i("imagepicker", "本次压缩的数量为--->" + list.size());
		}

		/**
		 * 运行在UI线程中，在调用doInBackground()之前执行
		 */
		@Override
		protected void onPreExecute() {
//			progressDialog = ProgressDialog.show(context, null, "压缩处理中...");
		}

		/**
		 * 后台运行的方法，可以运行非UI线程，可以执行耗时的方法
		 */
		@Override
		protected Integer doInBackground(Void... params) {
			for (int i = 0; i < list.size(); i++) {
				Bitmap bitmap = compressBitmap(list.get(i));
				String path = saveBitmap(bitmap, i);
				fileList.add(path);
			}
			return null;
		}

		/**
		 * 运行在ui线程中，在doInBackground()执行完毕后执行
		 */
		@Override
		protected void onPostExecute(Integer integer) {
//			progressDialog.dismiss();
			callBack.success(fileList);
		}

		/**
		 * 在publishProgress()被调用以后执行，publishProgress()用于更新进度
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			
		}
	}

	/**
	 * 从sd卡获取压缩图片bitmap
	 */
	public static Bitmap compressBitmap(String srcPath) {
		if (TextUtils.isEmpty(srcPath)){
			return null;
		}

		File file = new File(srcPath);
		if (!file.exists()){
			return null;
		}

		if (file.length() < 1){
			return null;
		}

		int degree = getBitmapDegree(srcPath); 
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(srcPath, options);
		options.inSampleSize = calculateInSampleSize(options, imagePicker.getMaxWid(),
				imagePicker.getMaxHei());
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int options_ = 100;

		int baosLength = baos.toByteArray().length;
		Long a = System.currentTimeMillis();
		while (baosLength / 1024 > imagePicker.getMaxSize()) {
			baos.reset();
			options_ = Math.max(0, options_ - 10);
			bitmap.compress(Bitmap.CompressFormat.JPEG, options_, baos);
			baosLength = baos.toByteArray().length;
			if (options_ == 0) {
				break;
			}
		}
		/**
		 * 最好在压缩完之后进行旋转操作，否则操作较大的bitmap，很容易oom
		 */
		if (degree > 0) {
			bitmap = rotateBitmapByDegree(bitmap, degree);
		}
		return bitmap;
	}

	/**
	 * 保存bitmap到内存卡
	 */
	public static String saveBitmap(Bitmap bmp, int num) {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			cachePath = Environment.getExternalStorageDirectory().getPath() + "/.十六番";
		} else {
			cachePath = context.getCacheDir().getPath() + "/.十六番";
		}
		File file = new File(cachePath);
		if (!file.exists() && !file.isDirectory()) {
			try {
				file.mkdirs();
			} catch (Exception e) {
				cachePath = context.getCacheDir().getPath();
			}
		}
		
		String path = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String picName = formatter.format(new Date());
			path = file.getPath() + "/" + picName + "-" + num + ".jpg";
			FileOutputStream fileOutputStream = new FileOutputStream(path);
			bmp.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return path;
	}
	
	/**
	 * 得到图片的exif信息
	 * @param path
	 * @return
	 */
	public static int getBitmapDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}
	
	/**
	 * 旋转bitmap
	 * 针对三星等机型的拍照图片旋转问题	
	 * @param bm
	 * @param degree
	 * @return
	 */
	public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
		Bitmap returnBm = null;
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		try {
			returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			while (returnBm == null) {
				System.gc();
				System.runFinalization();
				returnBm = rotateBitmapByDegree(bm, degree);
			}
		}
		if (returnBm == null) {
			returnBm = bm;
		}
		if (bm != returnBm) {
			bm.recycle();
		}
		return returnBm;
	}
	
	/**
	 * 计算压缩比
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int picheight = options.outHeight;
		final int picwidth = options.outWidth;

		int targetheight = picheight;
		int targetwidth = picwidth;
		int inSampleSize = 1;

		if (targetheight > reqHeight || targetwidth > reqWidth) {
			while (targetheight >= reqHeight && targetwidth >= reqWidth) {
				inSampleSize += 1;
				targetheight = picheight / inSampleSize;
				targetwidth = picwidth / inSampleSize;
			}
		}
		return inSampleSize;
	}
	
	public interface CompressCallBack {
		void success(List<String> list);
	}

}