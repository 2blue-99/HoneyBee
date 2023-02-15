package com.example.receiptcareapp.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.receiptcareapp.R
import com.example.receiptcareapp.fragment.viewModel.FragmentViewModel
import java.io.File
import kotlin.math.log

class GalleryFragment : Fragment() {
    private val GALLERY = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    private val GALLERY_CODE = 101

    private val viewModel : FragmentViewModel by viewModels({ requireActivity() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("TAG", "onCreate: GalleryFragment", )
        CallGallery()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }


    fun CallGallery() {
        Log.e("TAG", "CallGallery 실행", )

        if(checkPermission(GALLERY)){
            Log.e("TAG", "파일 권한 있음", )

            val intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_CODE)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {   // startActivityForResult에서 불림, (but! startActivityForResult() + onActivityResult() 는 이제 사용 안한다고 함. 수정하자!
        if (resultCode == Activity.RESULT_OK) {
            Log.e("TAG", "onActivityResult: if 진입",)
            when (requestCode) {
                GALLERY_CODE -> {
                    data?.data?.let { uri ->
                        val imageUri: Uri? = data?.data
                        if(imageUri != null){
                            Log.e("TAG", "onActivityResult: imageUri NULL이 아님!", )

                            viewModel.takeImage(imageUri)
                            viewModel.takePage(2)
                            NavHostFragment.findNavController(this).navigate(R.id.action_galleryFragment_to_showFragment)
                        }
                    }
                }
            }
        } else {
            Log.e("TAG", "onActivityResult: else 진입",)
        }
    }


    /*** 권한 관련 코드 ***/
    fun checkPermission(permissions : Array<out String>) : Boolean{         // 실제 권한을 확인하는 곳
        Log.e("TAG", "checkPermission 실행", )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), permissions, GALLERY_CODE)
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
            GALLERY_CODE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(), "파일 권한을 승인해 주세요.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    }