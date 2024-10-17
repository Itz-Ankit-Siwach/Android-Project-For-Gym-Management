package com.example.gymmanagementusingsqlite.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gymmanagementusingsqlite.R
import com.example.gymmanagementusingsqlite.databinding.FragmentAllMembersBinding


class FragmentAllMembers : Fragment() {
    private lateinit var binding:FragmentAllMembersBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentAllMembersBinding.inflate(inflater,container,false)
        return binding.root
    }


}