package com.example.gymmanagementusingsqlite.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gymmanagementusingsqlite.R
import com.example.gymmanagementusingsqlite.databinding.FragmentFeePendingBinding


class FragmentFeePending : Fragment() {
    private lateinit var binding: FragmentFeePendingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentFeePendingBinding.inflate(inflater,container,false)
        return binding.root
    }


}