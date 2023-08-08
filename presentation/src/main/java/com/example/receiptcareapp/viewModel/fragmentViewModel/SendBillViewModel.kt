package com.example.receiptcareapp.viewModel.fragmentViewModel

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.domain.model.local.DomainRoomData
import com.example.domain.model.send.AppSendData
import com.example.domain.model.send.DomainSendData
import com.example.domain.usecase.data.InsertDataUseCase
import com.example.domain.usecase.room.InsertDataRoomUseCase
import com.example.receiptcareapp.base.BaseViewModel
import com.example.receiptcareapp.util.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SendBillViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val insertDataUseCase: InsertDataUseCase,
    private val insertDataRoomUseCase: InsertDataRoomUseCase
) : BaseViewModel() {

    val loading: LiveData<Boolean> get() = isLoading

    private var _response = MutableLiveData<ResponseState>()
    val response : LiveData<ResponseState> get() = _response

    init {
        Log.e("TAG", "ShowPictureViewModel")
    }

    //insertData 분리해야함
    fun insertBillData(sendData: AppSendData) {
        CoroutineScope(exceptionHandler).launch {
            isLoading.postValue(true)
            Log.e("TAG", "insertBillData isloading: ${isLoading.value}", )
            withTimeoutOrNull(waitTime) {
                updateResponse(
                    insertDataUseCase(
                        DomainSendData(
                            cardName = MultipartBody.Part.createFormData(
                                "cardName",
                                sendData.cardName
                            ),
                            storeName = MultipartBody.Part.createFormData(
                                "storeName",
                                sendData.storeName
                            ),
                            date = MultipartBody.Part.createFormData(
                                "billSubmitTime",
                                sendData.billSubmitTime
                            ),
                            amount = MultipartBody.Part.createFormData(
                                "amount",
                                sendData.amount.replace(",", "")
                            ),
                            picture = compressEncodePicture(sendData.picture)
                        )
                    ),
                    sendData
                )
                isLoading.postValue(false)
            } ?: throw SocketTimeoutException()
        }
    }

    private fun updateResponse(response: String, data: AppSendData) {
        Log.e("TAG", "updateResponse: $response", )
        //uid로 넘어옴
        when (response) {
            "0" -> {} // 실패
            else -> {
                /// TODO 여기는 임시방편
                _response.postValue(ResponseState.SUCCESS)
                ///
                if (data.billSubmitTime.contains("-") && data.billSubmitTime.contains("T") && data.billSubmitTime.contains(":")) {
                    data.billSubmitTime = dateTimeToString(data.billSubmitTime)
                }
                insertRoomData(
                    DomainRoomData(
                        cardName = data.cardName,
                        amount = data.amount,
                        storeName = data.storeName,
                        billSubmitTime = data.billSubmitTime,
                        file = data.picture.toString(),
                        uid = response
                    )
                )
            }
        }
    }

    // 이 기능을 따로 빼야할듯
    private fun insertRoomData(domainRoomData: DomainRoomData) {
        CoroutineScope(exceptionHandler).launch {
            insertDataRoomUseCase(domainRoomData)
            _response.postValue(ResponseState.SUCCESS)
        }
    }


//    fun insertBillData(sendData: AppSendData) {
//        Log.e("TAG", "sendData: $sendData", )
//        CoroutineScope(exceptionHandler).launch {
//            withTimeoutOrNull(waitTime) {
//                val result = insertDataUseCase(
//                    DomainSendData(
//                        cardName = MultipartBody.Part.createFormData("cardName", sendData.cardName),
//                        storeName = MultipartBody.Part.createFormData(
//                            "storeName",
//                            sendData.storeName
//                        ),
//                        date = MultipartBody.Part.createFormData("billSubmitTime", sendData.billSubmitTime),
//                        amount = MultipartBody.Part.createFormData("amount", sendData.amount.replace(",","")),
//                        picture = compressEncodePicture(sendData.picture)
//                    )
//                )
//                Log.e("TAG", "sendData 응답 : $result ")
//
//                if (result != "0") { //TODO uid != "0"이 아니라 실패했을 때 서버에서 받아오는 메세지로 조건식 바꾸자!!
//                    var splitData =""
//                    if(sendData.billSubmitTime.contains("-") && sendData.billSubmitTime.contains("T") && sendData.billSubmitTime.contains(":")){
//                        splitData = dateTimeToString(sendData.billSubmitTime)
//                    }
//                    insertRoomData(
//                        DomainRoomData(
//                            cardName = sendData.cardName,
//                            amount = sendData.amount,
//                            storeName = sendData.storeName,
//                            billSubmitTime = splitData,
//                            file = sendData.picture.toString(),
//                            uid = result
//                        )
//                    )
//                } else {
//                    Log.e("TAG", "sendData: 실패입니다!")
//                    Exception("오류! 전송 실패.")
//                }
//            } ?: throw SocketTimeoutException()
//        }
//    }


    fun compressEncodePicture(uri: Uri): MultipartBody.Part{
        val file = File(absolutelyPath(uri, application))
        val compressFile = compressImageFile(file)
        val requestFile = compressFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }

    fun compressImageFile(file: File): File {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        BitmapFactory.decodeFile(file.absolutePath, options)

        val resize = if(file.length() > 1000000) 5 else 1

        options.inJustDecodeBounds = false
        options.inSampleSize = resize

        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

        val rotatedBitmap = rotateImageIfRequired(bitmap, file.absolutePath)

        val outputFile = File.createTempFile("compressed_", ".jpg")
        try {
            val outputStream = FileOutputStream(outputFile)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return outputFile
    }

    private fun dateTimeToString(date:String): String{
        val myList = date.split("-","T",":")
        return "${myList[0]}년 ${myList[1]}월 ${myList[2]}일 ${myList[3]}시 ${myList[4]}분"
    }

    fun rotateImageIfRequired(bitmap: Bitmap, imagePath: String): Bitmap {
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    //절대경로로 변환
    fun absolutelyPath(path: Uri?, context: Context?): String {
        Log.e("TAG", "absolutelyPath: $path", )
        val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val c: Cursor? = context?.contentResolver?.query(path!!, proj, null, null, null)
        val index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()
        val result = c?.getString(index!!)
        Log.e("TAG", "absolutelyPath proj: $proj", )
        Log.e("TAG", "absolutelyPath c: $c", )
        Log.e("TAG", "absolutelyPath index: $index", )
        Log.e("TAG", "absolutelyPath result: $result", )
        return result!!
    }

    fun commaReplaceSpace(text: String): String {
        return text.replace(",", "")
    }

    fun dateNow(): LocalDate {
        return LocalDate.now()
    }

    fun datePickerMonth(month: Int): String {
        var myMonth: String
        if (month < 10) myMonth = "0${month + 1}"
        else myMonth = "${month + 1}"
        return myMonth
    }

    fun datePickerDay(day: Int): String {
        var myDay: String
        if (day < 10) myDay = "0${day}"
        else myDay = "${day}"
        return myDay
    }

    fun myLocalDateTimeFuntion(myYear: Int, myMonth: Int, myDay: Int): LocalDateTime? {
        return LocalDateTime.of(
            myYear, myMonth, myDay,
            LocalDateTime.now().hour, LocalDateTime.now().minute, LocalDateTime.now().second
        )
    }




}