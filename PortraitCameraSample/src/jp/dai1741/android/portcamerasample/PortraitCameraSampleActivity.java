package jp.dai1741.android.portcamerasample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
        mCameraPreviewView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCameraAvailable) {
                    mCameraPreviewView.mCamera.takePicture(null, null, null,
                            kPictureCallback);
                    mCameraAvailable = false;
                }
            }
        });
        Toast.makeText(this, "タップして撮影", Toast.LENGTH_SHORT).show();
    }

    private boolean mCameraAvailable = true;

    private final Camera.PictureCallback kPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "Camera.PictureCallback#onPictureTaken()");

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            int degrees = CameraPreviewView
                    .getCameraDisplayOrientation(PortraitCameraSampleActivity.this);
            Log.d(TAG, "Camera rotation degrees: " + degrees);

            final Bitmap rotatedBitmap;
            rotatedBitmap = getMutableRotatedCameraBitmap(bitmap, degrees);
            // rotatedBitmap = getImmutableRotatedCameraBitmap(bitmap, degrees);

            ImageView iv = new ImageView(PortraitCameraSampleActivity.this);
            mRootViewGroup.addView(iv);
            iv.setImageBitmap(rotatedBitmap);
            iv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Picture dismissed");
                    mRootViewGroup.removeView(v);
                    mCameraAvailable = true;
                    mCameraPreviewView.mCamera.startPreview();
                }
            });
        }
    };

    private static Bitmap getMutableRotatedCameraBitmap(Bitmap cameraBitmap, int degrees) {
        int width, height;
        if (degrees % 180 == 0) {
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
        int offset = (height - width) / 2 * ((degrees - 180) % 180) / 90;

        canvas.translate(offset, -offset);
        canvas.drawBitmap(cameraBitmap, 0, 0, null);
        cameraBitmap.recycle();
        return bitmap;
    }

    private static Bitmap getImmutableRotatedCameraBitmap(Bitmap cameraBitmap, int degrees) {
        Matrix m = new Matrix();
        m.postRotate(degrees);
        return Bitmap.createBitmap(cameraBitmap, 0, 0, cameraBitmap.getWidth(),
                cameraBitmap.getHeight(), m, false);
    }
}