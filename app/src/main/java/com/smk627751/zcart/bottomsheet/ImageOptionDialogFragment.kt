package com.smk627751.zcart.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.R.id.design_bottom_sheet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.smk627751.zcart.R

class ImageOptionDialogFragment(val callBack: (option : String) -> Unit) : BottomSheetDialogFragment() {
    constructor() : this({})
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_image_option, container, false)
        view.findViewById<View>(R.id.capture_image).setOnClickListener {
            callBack("camera")
            dismiss()
        }
        view.findViewById<View>(R.id.choose_image).setOnClickListener {
            callBack("gallery")
            dismiss()
        }
        view.findViewById<View>(R.id.delete_image).setOnClickListener {
            callBack("delete")
            dismiss()
        }
        setupFullHeight()
        return view
    }
    companion object {
        fun newInstance(callBack: (option : String) -> Unit): ImageOptionDialogFragment = ImageOptionDialogFragment(callBack)
    }
    private fun setupFullHeight() {
        val bottomSheet = view?.findViewById<View>(design_bottom_sheet)
        val behavior = (dialog as BottomSheetDialog).behavior
        if (bottomSheet != null) {
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            bottomSheet.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissAllowingStateLoss()
    }
}