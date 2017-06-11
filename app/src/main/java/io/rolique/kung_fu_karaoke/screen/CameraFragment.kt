package io.rolique.kung_fu_karaoke.screen

import android.Manifest
import android.app.Fragment
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v13.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.*
import android.widget.Toast
import io.rolique.kung_fu_karaoke.R
import java.util.*


/**
 * Created by Victor Artemyev on 11/06/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

class CameraFragment : Fragment() {

    val RC_CAMERA_PERMISSION = 101

    val MSG_CAMERA_OPENED = 1
    val MSG_SURFACE_READY = 2

    var mSurfaceCreated = true
    var mIsCameraConfigured = false

    val mHandlerCallback = object : Handler.Callback {
        override fun handleMessage(msg: Message?): Boolean {
            when (msg!!.what) {
                MSG_CAMERA_OPENED, MSG_SURFACE_READY ->
                    if (mSurfaceCreated && (mCameraDevice != null)
                            && !mIsCameraConfigured) {
                        configureCamera()
                    }
            }
            return true
        }
    }

    val mHandler = Handler(mHandlerCallback)

    lateinit var mSurfaceView: SurfaceView
    lateinit var mSurfaceHolder: SurfaceHolder
    lateinit var mCameraManager: CameraManager
    lateinit var mCameraIds: Array<String>

    var mCameraCaptureSession: CameraCaptureSession? = null
    var mCameraDevice: CameraDevice? = null
    var mCameraSurface: Surface? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater!!.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        mSurfaceView = view?.findViewById(R.id.surface_view) as SurfaceView
        mSurfaceHolder = mSurfaceView.holder
        mSurfaceHolder.addCallback(mSurfaceCallback)
        mCameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            mCameraIds = mCameraManager.cameraIdList
            for (id in mCameraIds) {
                Log.v("", "CameraID: " + id)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()
        val permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), RC_CAMERA_PERMISSION)
                Toast.makeText(activity, "request permission", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, "PERMISSION_ALREADY_GRANTED", Toast.LENGTH_SHORT).show()
            try {
                mCameraManager.openCamera(mCameraIds[1], mCameraStateCallback, Handler())
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RC_CAMERA_PERMISSION ->
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED)
                    try {
                        mCameraManager.openCamera(mCameraIds[1], mCameraStateCallback, Handler())
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
        }
    }

    val mCameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice?) {
            mCameraDevice = camera
            mHandler.sendEmptyMessage(MSG_CAMERA_OPENED)
        }

        override fun onDisconnected(camera: CameraDevice?) {
            //ignored
        }

        override fun onError(camera: CameraDevice?, error: Int) {
            //ignored
        }
    }

    val mSurfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            mCameraSurface = holder?.surface
            mSurfaceCreated = true
            mHandler.sendEmptyMessage(MSG_SURFACE_READY)
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            mSurfaceCreated = false
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            mCameraSurface = holder?.surface
        }
    }

    fun configureCamera() {
        // prepare list of surfaces to be used in capture requests
        val sfl = ArrayList<Surface>()
        sfl.add(mCameraSurface!!) // surface for viewfinder preview

        // configure camera with all the surfaces to be ever used
        try {
            mCameraDevice?.createCaptureSession(sfl, mCaptureSessionCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        mIsCameraConfigured = true
    }

    val mCaptureSessionCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession?) {
            mCameraCaptureSession = session
            try {
                val previewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                previewRequestBuilder?.addTarget(mCameraSurface)
                mCameraCaptureSession?.setRepeatingRequest(previewRequestBuilder?.build(), null, null)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onConfigureFailed(session: CameraCaptureSession?) {

        }
    }

    override fun onStop() {
        super.onStop()
        try {
            mCameraCaptureSession?.stopRepeating()
            mCameraCaptureSession?.close()
            mCameraCaptureSession = null
            mIsCameraConfigured = false
        } catch (e: CameraAccessException) {
            // Doesn't matter, cloising device anyway
            e.printStackTrace()
        } catch (e2: IllegalStateException) {
            // Doesn't matter, cloising device anyway
            e2.printStackTrace()
        } finally {
            mCameraDevice?.close()
            mCameraDevice = null
            mCameraCaptureSession = null
        }
    }
}