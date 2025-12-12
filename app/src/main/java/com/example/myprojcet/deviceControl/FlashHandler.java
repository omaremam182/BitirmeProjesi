package com.example.myprojcet.deviceControl;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.widget.Toast;

public class FlashHandler {
    private Context context;
    private CameraManager cameraManager;
    private String cameraId;
    public FlashHandler(Context context) {
        this.context = context;
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraId = cameraManager.getCameraIdList()[0]; // back camera
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void turnFlashlightOn() {
        try {
            if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, true);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void turnFlashlightOff() {
        try {
            if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, false);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
