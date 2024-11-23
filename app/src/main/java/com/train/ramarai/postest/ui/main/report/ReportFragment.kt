package com.train.ramarai.postest.ui.main.report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.train.ramarai.postest.R
import com.train.ramarai.postest.databinding.FragmentReportBinding


class ReportFragment : Fragment() {
    private lateinit var binding: FragmentReportBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportBinding.inflate(inflater, container, false)
        val view = binding.root


        return view
    }


}