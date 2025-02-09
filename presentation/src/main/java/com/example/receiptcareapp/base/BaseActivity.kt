package com.example.receiptcareapp.base

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * 2023-06-16
 * pureum
 */
abstract class BaseActivity<VB:ViewBinding>(
    private val inflate: (LayoutInflater) -> VB,
    private val TAG: String
) : AppCompatActivity(){

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e(TAG, "onCreate", )
        super.onCreate(savedInstanceState)
        _binding = inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initUI()
        initObserver()
        initListener()
    }

    override fun onStart() {
        Log.e(TAG, "onStart", )
        super.onStart()
    }

    override fun onResume() {
        Log.e(TAG, "onResume", )
        super.onResume()
    }

    override fun onPause() {
        Log.e(TAG, "onPause", )
        super.onPause()
    }

    override fun onRestart() {
        Log.e(TAG, "onRestart", )
        super.onRestart()
    }

    override fun onStop() {
        Log.e(TAG, "onStop", )
        super.onStop()
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy", )
        super.onDestroy()
    }

    abstract fun initData()
    abstract fun initUI()
    abstract fun initListener()
    abstract fun initObserver()

    protected fun showShortToast(message: String?) =
        Toast.makeText(this, message ?: "", Toast.LENGTH_SHORT).show()

    protected fun showLongToast(message: String?) =
        Toast.makeText(this, message ?: "", Toast.LENGTH_LONG).show()
}