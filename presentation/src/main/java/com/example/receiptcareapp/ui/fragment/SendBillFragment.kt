package com.example.receiptcareapp.ui.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.domain.model.BottomSheetData
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.domain.model.local.DomainRoomData
import com.example.domain.model.receive.CardData
import com.example.domain.model.receive.CardSpinnerData
import com.example.domain.model.receive.DateData
import com.example.domain.model.receive.DomainReceiveCardData
import com.example.receiptcareapp.R
import com.example.receiptcareapp.base.BaseFragment
import com.example.receiptcareapp.databinding.FragmentSendBillBinding
import com.example.receiptcareapp.ui.adapter.SpinnerAdapter
import com.example.receiptcareapp.ui.botteomSheet.SendCheckBottomSheet
import com.example.receiptcareapp.util.FetchStateHandler
import com.example.receiptcareapp.util.ResponseState
import com.example.receiptcareapp.viewModel.activityViewmodel.MainActivityViewModel
import com.example.receiptcareapp.viewModel.fragmentViewModel.SendBillViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


@AndroidEntryPoint
class SendBillFragment : BaseFragment<FragmentSendBillBinding>(FragmentSendBillBinding::inflate, "ShowPictureFragment") {
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val viewModel : SendBillViewModel by viewModels()
    private var cardDataList: MutableList<CardSpinnerData> = mutableListOf()
    private var cardName = ""
    private var cardAmount = ""
    private var todayDate : LocalDate? = null
    private var selectedDate : LocalDate ? = null
    private lateinit var dateData : DateData
    private lateinit var callback: OnBackPressedCallback

    override fun initData() {
        todayDate = viewModel.dateNow()
        selectedDate = viewModel.dateNow()

        dateData = DateData(
            year = todayDate!!.year,
            month = todayDate!!.monthValue,
            day = todayDate!!.dayOfMonth
        )

        viewModel.getServerCardData()
    }

    override fun initUI() {
        with(binding){
            //글라이드
            Glide.with(pictureView)
                .load(activityViewModel.image.value!!)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                .into(pictureView)
            val formatterDate = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            btnDate.text = "${viewModel.dateNow().format(formatterDate)}"
        }
    }

    override fun initListener() {

        //TODO 여기 왜있지?
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction().add(this, "showPictureFragment").commit()

        with(binding){
            /** Date Button -> DatePickerDialog 생성 **/
            btnDate.setOnClickListener {
                val cal = Calendar.getInstance()
                val data = DatePickerDialog.OnDateSetListener { view, year, month, day ->
                    dateData = DateData(
                        year = year,
                        month = month + 1,
                        day = day
                    )
                    btnDate.text = "${year}/${viewModel.datePickerMonth(month)}/${viewModel.datePickerDay(day)}"
                    selectedDate = LocalDate.of(year, month + 1, day)
                }
                val dataDialog = DatePickerDialog(requireContext(), data,
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                dataDialog.show()
                dataDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(Color.RED)
                dataDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(Color.BLACK)
            }

            btnPrice.setOnEditorActionListener { v, actionId, event ->
                Log.e("TAG", "btnPrice.setOnEditorActionListener", )
                var handled = false
                if (actionId == EditorInfo.IME_ACTION_DONE && btnPrice.text.isNotEmpty()) {
                    btnPrice.setText(viewModel.PriceFormat(btnPrice.text.toString()))
                }
                handled
            }

            btnPrice.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    if (btnPrice.text.contains(",")) {
                        btnPrice.setText(viewModel.CommaReplaceSpace(btnPrice.text.toString()))
                        btnPrice.setSelection(btnPrice.text.length)
                    }
                }
                else { btnPrice.setText(viewModel.PriceFormat(btnPrice.text.toString())) }
            }

            btnStore.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s!!.length > 15) {
                        showShortToast("15자 이내로 입력해주세요.")
                    }
                }
            })
            btnPrice.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s!!.length > 10) {
                        showShortToast("10자 이내로 입력해주세요.")
                    }
                }
            })

            /** 완료 Button **/
            completeBtn.setOnClickListener {
                Log.e("TAG", "onViewCreated: iinin")
                when {
                    cardName == "" -> {
                        showShortToast("카드를 입력하세요.")
                    }
                    btnStore.text!!.isEmpty() -> {
                        showShortToast("가게 이름을 입력하세요.")
                    }
                    btnPrice.text.isEmpty() -> {
                        showShortToast("금액을 입력하세요.")
                    }
                    btnDate.text.isEmpty() -> {
                        showShortToast("날짜를 입력하세요.")
                    }
                    selectedDate!!.isAfter(todayDate) -> {
                        showShortToast("오늘보다 미래 날짜는 불가능합니다.")
                        Log.e("TAG", "SendBillFragment: 오늘보다 미래 날짜는 불가능합니다.", )
                    }
                    activityViewModel.image.value == null -> {
                        showShortToast("사진이 비었습니다.\n초기화면으로 돌아갑니다.")
                        NavHostFragment.findNavController(this@SendBillFragment).navigate(R.id.action_showFragment_to_homeFragment)
                    }
                    else -> {
                        if(!viewModel.amountCheck(btnPrice.text.toString(), cardAmount)) {
                            showShortToast("보유금액보다 많은 비용입니다.")
                            return@setOnClickListener
                        }
                        val myLocalDateTime = viewModel.myLocalDateTimeFuntion(dateData.year, dateData.month, dateData.month)
                        SendCheckBottomSheet(
                            viewModel,
                            BottomSheetData(
                                cardName = cardName,
                                amount = btnPrice.text.toString(),
                                cardAmount = cardAmount,
                                storeName = binding.btnStore.text.toString(),
                                date = myLocalDateTime.toString(),
                                picture = activityViewModel.image.value!!
                            )
                        ).show(parentFragmentManager, "tag")
                    }

                }
            }

            /** 취소 Button **/
            cancleBtn.setOnClickListener {
                findNavController().navigate(R.id.action_showFragment_to_homeFragment)
            }
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCardData = cardDataList[position]
                cardName = selectedCardData.name
                cardAmount = selectedCardData.amount
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

    }

    override fun initObserver() {
        /** 프로그래스바 컨트롤 **/
        viewModel.loading.observe(viewLifecycleOwner){
            if(it) binding.layoutLoadingProgress.root.visibility = View.VISIBLE
            else binding.layoutLoadingProgress.root.visibility = View.INVISIBLE
        }

        viewModel.response.observe(viewLifecycleOwner){
            Log.e("TAG", "initObserver: com", )
            when(it?.status){
                "200" -> {
                    viewModel.insertRoomData(it.uid.toString())
                    findNavController().navigate(R.id.action_showFragment_to_homeFragment)
                    showShortToast("전송 성공")
                }
                else -> {showShortToast("전송 실패")}
            }
        }

        viewModel.cardList.observe(viewLifecycleOwner){
            //myArray.clear()
            it.body?.forEach { cardDataList.add(it) }
            val cardArrayList = ArrayList<CardSpinnerData>(cardDataList)
            Log.e("TAG", "cardList.observe : ${cardDataList}", )
            binding.spinner.adapter = SpinnerAdapter(requireContext(), cardArrayList)
        }

        // Err관리
        viewModel.fetchState.observe(this) {
            showShortToast(FetchStateHandler(it))
        }
    }

    /** Fragment 뒤로가기 **/
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_showFragment_to_homeFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }
}