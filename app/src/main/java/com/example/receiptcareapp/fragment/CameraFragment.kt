package com.example.receiptcareapp.fragment

import android.annotation.SuppressLint
import android.app.appsearch.AppSearchResult
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.receiptcareapp.R

class CameraFragment : Fragment() {
    private val CAMERA = arrayOf(android.Manifest.permission.CAMERA)
    private val CAMERA_CODE = 98

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("TAG", "onCreate: CameraFragment", )
        CallCamera()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    fun CallCamera() {  // 카메라 실행 함수
        Log.e("TAG", "CallCamera 실행", )

        if (checkPermission(CAMERA)) {  // 카메라 권한 있을 시 카메라 실행함
            Log.e("TAG", "카메라 권한 있음", )

            val itt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(itt, CAMERA_CODE)                        // startActivityForResult() + onActivityResult() 는 이제 사용 안한다고 함. 수정하자!
        }
    }

    fun checkPermission(permissions : Array<out String>) : Boolean{         // 실제 권한을 확인하는 곳
        /* .
        인수로 필요한 권한을 배열로 전달하도록 함
        이 배열을 순회하면서 각각의 권한이 있는지 확인함
        권한이 없으면 ActivityCompat.requestPermissions 호출하여 사용자에게 권한 요청!
         */
        Log.e("TAG", "checkPermission 실행", )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), permissions, CAMERA_CODE)
                    return false;
                }
            }
        }
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {  // 권한 확인 직후 바로 호출됨
        Log.e("TAG", "onRequestPermissionsResult 실행", )

        when(requestCode) {
            CAMERA_CODE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(), "카메라 권한을 승인해 주세요.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {   // startActivityForResult에서 불림, (but! startActivityForResult() + onActivityResult() 는 이제 사용 안한다고 함. 수정하자!
        Log.e("TAG", "onActivityResult 실행",)
        if (resultCode == AppSearchResult.RESULT_OK) {
            when (requestCode) {
                CAMERA_CODE -> {
                    if (data?.extras?.get("data") != null) {
                        val img = data?.extras?.get("data") as Bitmap
                        Log.e("TAG", "img : ${img}", )
                        //binding.imageView.setImageBitmap(img)
                    }
                }
            }
        }
    }


}