package com.example.receiptcareapp.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.receiptcareapp.R
import com.example.receiptcareapp.databinding.FragmentCameraBinding
import com.example.receiptcareapp.base.BaseFragment
import com.example.receiptcareapp.viewModel.fragmentViewModel.CameraViewModel
import com.example.receiptcareapp.viewModel.activityViewmodel.MainActivityViewModel

class CameraFragment : BaseFragment<FragmentCameraBinding>(FragmentCameraBinding::inflate, "CameraFragment") {
    private var photoURI : Uri? = null
    private val activityViewModel : MainActivityViewModel by activityViewModels()
    private val viewModel : CameraViewModel by viewModels()

    override fun initData() {}

    override fun initUI() {}

    override fun initListener() { CallCamera() }

    override fun initObserver() {}

    /** 카메라 관련 코드 **/
    /* 카메라 호출 */
    fun CallCamera() {
        Log.e("TAG", "CallCamera", )
        dispatchTakePictureIntentEx()
    }

    private fun dispatchTakePictureIntentEx() {
        Log.e("TAG", "dispatchTakePictureIntentEx: ", )
        activityResult.launch(viewModel.dispatchTakePictureIntentExViewModel(requireActivity()))
    }

    //카메라 촬영 후 확인 시
    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.e("TAG", "activityResult: ", )
        if (it.resultCode == Activity.RESULT_OK){
            Log.e("TAG", "onActivityResult: if 진입", )
            photoURI = viewModel.photoUri.value
            if(photoURI != null) {
                Log.e("TAG", "data 있음", )
                activityViewModel.takeImage(photoURI!!)
                photoURI = null
                findNavController().navigate(R.id.action_cameraFragment_to_showFragment)
            }
        }
        else {
            Log.e("TAG", "RESULT_OK if: else 진입", )
            findNavController().navigate(R.id.action_cameraFragment_to_homeFragment)
        }
    }
}
