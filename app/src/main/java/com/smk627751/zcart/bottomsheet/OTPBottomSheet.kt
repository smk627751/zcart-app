package com.smk627751.zcart.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.smk627751.zcart.R
import com.smk627751.zcart.Utility
import com.smk627751.zcart.viewmodel.OTPViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OTPBottomSheet(private val phoneNumber : String, val callBack : () -> Unit) : BottomSheetDialogFragment() {
    lateinit var viewModel: OTPViewModel
    lateinit var phone : TextView
    lateinit var resendTimer : TextView
    lateinit var otpLayout : LinearLayout
    lateinit var resendOtp : TextView
    lateinit var timerLayout: LinearLayout
    lateinit var verify : Button
    lateinit var progress : ProgressBar
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_otp_activity, container, false)
        phone = view.findViewById(R.id.phone)
        resendOtp = view.findViewById(R.id.resend_otp)
        timerLayout = view.findViewById(R.id.timer_layout)
        resendTimer = view.findViewById(R.id.resend_timer)
        viewModel = ViewModelProvider(this).get(OTPViewModel::class.java)
        otpLayout = view.findViewById(R.id.otp_layout)
        verify = view.findViewById(R.id.verify_button)
        progress = view.findViewById(R.id.progress)
        val ets = findAllEditText(otpLayout)
        phone.text = phoneNumber
        viewModel.setPhone(phoneNumber)
        lifecycleScope.launch{
            updateTimer()
        }
        resendOtp.setOnClickListener {
            resendOtp.text = "Resend OTP in"
            val ctx = requireContext()
            timerLayout.visibility = LinearLayout.VISIBLE
            resendOtp.isEnabled = false
            activity?.let { it1 ->
                viewModel.resendOTP(it1){
                    Utility.makeToast(ctx, it.message.toString())
                    Log.e("uuid",it.message.toString())
                }
            }
            lifecycleScope.launch{
                updateTimer()
            }
        }
        activity?.let { activity ->
            viewModel.sendOTP(activity){
                Utility.makeToast(activity, it.message.toString())
                Log.e("uuid",it.message.toString())
            }
        }
        verify.setOnClickListener {
            setProgress(true)
            val otp = ets.joinToString("") { it.text.toString() }
            val ctx = requireActivity()
            viewModel.verifyOTP(otp,
                {
                    setProgress(false)
                    callBack().also {
                        dismiss()
                    }
                }
                ,{
                    Utility.makeToast(ctx, "Invalid OTP")
                })
        }
        return view
    }
    private fun findAllEditText(otpLayout: LinearLayout?): List<EditText> {
        val ets = mutableListOf<EditText>()
        for (i in 0 until otpLayout!!.childCount) {
            val child = otpLayout.getChildAt(i)
            if (child is EditText) {
                child.addTextChangedListener {
                    if (child.text.length == 1) {
                        if (i + 1 < otpLayout.childCount && otpLayout.getChildAt(i + 1) != null) {
                            otpLayout.getChildAt(i + 1).requestFocus()
                        }
                    }
                }
                ets.add(child)
            }
        }
        return ets
    }
    fun setProgress(inProgress : Boolean)
    {
        if(inProgress)
        {
            verify.visibility = View.GONE
            progress.visibility = View.VISIBLE
        }
        else
        {
            verify.visibility = View.VISIBLE
            progress.visibility = View.GONE
        }
    }
    private suspend fun updateTimer()
    {
        var time = 60
        while (time > 0)
        {
            resendTimer.text = " 00:${time--}"
            delay(1000)
        }
        timerLayout.visibility = LinearLayout.GONE
        resendOtp.isEnabled = true
        resendOtp.text = "Resend OTP"
    }
}