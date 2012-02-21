package jp.dai1741.android.portcamerasample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class PortraitCameraSampleActivity extends Activity {
    private static final String TAG = "PortraitCameraSampleActivity";

    private CameraPreviewView mCameraPreviewView;
    private ViewGroup mRootViewGroup;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mCameraPreviewView = new CameraPreviewView(this);
        mRootViewGroup = (ViewGroup) findViewById(R.id.root_layout);
        mRootViewGroup.addView(mCameraPreviewView);

        // 画面をタップしたら撮影する
        mCameraPreviewView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCameraAvailable) { // 写真の生成中は撮影しない
                    mCameraPreviewView.mCamera.takePicture(null, null, null,
                            kPictureCallback);
                    mCameraAvailable = false;
                }
            }
        });
        Toast.makeText(this, "タップして撮影", Toast.LENGTH_SHORT).show();
    }

    /** カメラが現在使用可能かどうか */
    private boolean mCameraAvailable = true;

    /**
     * カメラで撮った写真を正しい向きにして返す。
     * 返される画像は変更可能となる。
     * 
     * @param cameraBitmap
     *            カメラで撮った正しい向きでないBitmap
     * @param degrees
     *            この写真を取ったときの画面の向き。0、90、180、270のいずれか。
     * @return 正しい向きのミュータブルなBitmap
     */
    private static Bitmap getMutableRotatedCameraBitmap(Bitmap cameraBitmap, int degrees) {
        int width, height;
        if (degrees % 180 == 0) { // 画面が横向きなら
            width = cameraBitmap.getWidth();
            height = cameraBitmap.getHeight();
        }
        else {
            width = cameraBitmap.getHeight();
            height = cameraBitmap.getWidth();
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.rotate(degrees, width / 2, height / 2);

        int offset = (degrees % 180 == 0) ? 0 : (degrees == 90)
                ? (width - height) / 2
                : (height - width) / 2;

        canvas.translate(offset, -offset);
        canvas.drawBitmap(cameraBitmap, 0, 0, null);
        cameraBitmap.recycle();
        return bitmap;
    }


    /**
     * カメラで撮った写真を正しい向きにして返す。
     * 返される画像は変更不可能となる。
     * 
     * @param cameraBitmap
     *            カメラで撮った正しい向きでないBitmap
     * @param degrees
     *            この写真を取ったときの画面の向き。0、90、180、270のいずれか。
     * @return 正しい向きのイミュータブルなBitmap
     */
    @SuppressWarnings("unused")
    private static Bitmap getImmutableRotatedCameraBitmap(Bitmap cameraBitmap, int degrees) {
        Matrix m = new Matrix();
        m.postRotate(degrees);
        return Bitmap.createBitmap(cameraBitmap, 0, 0, cameraBitmap.getWidth(),
                cameraBitmap.getHeight(), m, false);
    }

    /**
     * 写真を撮ったときに呼ばれるコールバック関数
     */
    private final Camera.PictureCallback kPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "Camera.PictureCallback#onPictureTaken()");

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            int degrees = CameraPreviewView
                    .getCameraDisplayOrientation(PortraitCameraSampleActivity.this);
            Log.d(TAG, "Camera rotation degrees: " + degrees);

            // 回転された画像を得る。
            Bitmap rotatedBitmap = getMutableRotatedCameraBitmap(bitmap, degrees);
            // Bitmap rotatedBitmap = getImmutableRotatedCameraBitmap(bitmap, degrees);

            showPicture(rotatedBitmap);
        }
    };

    /**
     * 撮影した写真を画面に表示する。
     * 保存用のロジックもここに記述している。
     * 
     * @param bitmap
     *            写真
     */
    private void showPicture(final Bitmap bitmap) {

        // Inflaterを使ってxmlのレイアウトを画面上に貼り付ける
        final View pictureView = getLayoutInflater().inflate(R.layout.picture, null);
        mRootViewGroup.addView(pictureView);

        ImageView iv = (ImageView) findViewById(R.id.picture);
        iv.setImageBitmap(bitmap);
        final Button saveButton = (Button) findViewById(R.id.save_button);

        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.equals(saveButton)) {
                    // クリックされたビューが保存ボタンなら画像を保存する
                    String path = MediaStore.Images.Media.insertImage(
                            PortraitCameraSampleActivity.this.getContentResolver(),
                            bitmap, "", "");
                    Toast.makeText(PortraitCameraSampleActivity.this,
                            "画像を保存しました: " + path, Toast.LENGTH_LONG).show();
                }
                Log.d(TAG, "Picture dismissed");
                mRootViewGroup.removeView(pictureView);

                mCameraPreviewView.mCamera.startPreview(); // カメラを再起動
                mCameraAvailable = true;
            }
        };
        iv.setOnClickListener(listener);
        saveButton.setOnClickListener(listener);
    }
}