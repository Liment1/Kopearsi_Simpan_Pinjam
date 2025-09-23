package com.example.project_map.ui.savings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.project_map.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TarikBottomSheet(private val listener: OnNominalEntered) : BottomSheetDialogFragment() {

    interface OnNominalEntered {
        fun onNominalEntered(nominal: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.bottomsheet_tarik, container, false)

        val etNominal = v.findViewById<EditText>(R.id.etNominal)
        val btnKirim = v.findViewById<Button>(R.id.btnKirim)

        btnKirim.setOnClickListener {
            val nominalText = etNominal.text.toString()
            if (nominalText.isNotEmpty()) {
                val nominal = nominalText.toInt()
                listener.onNominalEntered(nominal) // otomatis Setoran Sukarela
            }
            dismiss()
        }

        return v
    }
}
