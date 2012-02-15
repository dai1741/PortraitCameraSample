package jp.dai1741.android.portcamerasample;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

public class PortraitCameraSampleActivity extends Activity {
    private CameraPreviewView mCameraPreviewView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mCameraPreviewView = new CameraPreviewView(this);
        ViewGroup root = (ViewGroup)findViewById(R.id.root_layout);
        root.addView(mCameraPreviewView);
    }
}