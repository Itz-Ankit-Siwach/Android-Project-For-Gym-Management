package com.example.gymmanagementusingsqlite.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymmanagementusingsqlite.R
import com.example.gymmanagementusingsqlite.adapter.AdapterLoadMember
import com.example.gymmanagementusingsqlite.databinding.FragmentAllMembersBinding
import com.example.gymmanagementusingsqlite.global.DB
import com.example.gymmanagementusingsqlite.global.MyFunction
import com.example.gymmanagementusingsqlite.model.AllMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentAllMembers : BaseFragment() {
    private val TAG = "FragmentAllMembers"
    private var db: DB? = null
    private var adapter: AdapterLoadMember? = null
    private var arrayList: ArrayList<AllMember> = ArrayList()
    private lateinit var binding: FragmentAllMembersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAllMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = activity?.let { DB(it) }

        binding.radioGroupMember.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.rdActiveMember -> {
                    loadData("A") // Load active members
                }
                R.id.rdInActiveMember -> {
                    loadData("I") // Load inactive members
                }
            }
        }

        // Initialize RecyclerView
        binding.recyclerViewMember.layoutManager = LinearLayoutManager(activity)
    }

    override fun onResume() {
        super.onResume()
        loadData("A") // Load active members by default
    }

    private fun <R> CoroutineScope.executeAsyncTask(
        onPreExecute: () -> Unit,
        doInBackground: () -> R,
        onPostExecute: (R) -> Unit
    ) = launch {
        onPreExecute()
        val result = withContext(Dispatchers.IO) {
            doInBackground()
        }
        onPostExecute(result)
    }

    private fun loadData(memberStatus: String) {
        // Clear the array list to avoid duplication of data
        arrayList.clear()

        lifecycleScope.executeAsyncTask(onPreExecute = {
            showDialog("Loading...")
        }, doInBackground = {
            val sqlQuery = "SELECT * FROM MEMBER WHERE STATUS='$memberStatus'"
            db?.fireQuery(sqlQuery)?.use {
                if (it.count > 0) {
                    it.moveToFirst()
                    do {
                        val member = AllMember(
                            id = MyFunction.getValue(it, "ID"),
                            firstName = MyFunction.getValue(it, "FIRST_NAME"),
                            lastName = MyFunction.getValue(it, "LAST_NAME"),
                            age = MyFunction.getValue(it, "AGE"),
                            gender = MyFunction.getValue(it, "GENDER"),
                            weight = MyFunction.getValue(it, "WEIGHT"),
                            mobile = MyFunction.getValue(it, "MOBILE"),
                            address = MyFunction.getValue(it, "ADDRESS"),
                            image = MyFunction.getValue(it, "IMAGE_PATH"),
                            dateOfJoining = MyFunction.returnUserDataFormat(MyFunction.getValue(it, "DATE_OF_JOINING")),
                            expiryDate = MyFunction.returnUserDataFormat(MyFunction.getValue(it, "EXPIRE_ON"))
                        )
                        arrayList.add(member)
                    } while (it.moveToNext())
                }
            }
        }, onPostExecute = {
            CloseDialog()

            if (arrayList.isNotEmpty()) {
                binding.recyclerViewMember.visibility = View.VISIBLE
                binding.txtAllMemberNDF.visibility = View.GONE

                adapter = AdapterLoadMember(arrayList)
                binding.recyclerViewMember.adapter = adapter
//                adapter?.notifyDataSetChanged()


                adapter?.onClick {
                    loadFragment(it)

                }


            } else {
                binding.recyclerViewMember.visibility = View.GONE
                binding.txtAllMemberNDF.visibility = View.VISIBLE
            }
        })
        CloseDialog()
    }

    private fun loadFragment(id:String){
        val fragment=FragmentAddMember()
        val args=Bundle()
        args.putString("ID",id)
        fragment.arguments=args
        val fragmentManager:FragmentManager?=fragmentManager
        fragmentManager!!.beginTransaction().replace(R.id.frame_container,fragment,"FragmentAdd").commit()
    }
}
