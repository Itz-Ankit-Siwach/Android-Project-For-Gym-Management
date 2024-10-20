package com.example.gymmanagementusingsqlite.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymmanagementusingsqlite.R
import com.example.gymmanagementusingsqlite.adapter.AdapterLoadMember
import com.example.gymmanagementusingsqlite.databinding.FragmentFeePendingBinding
import com.example.gymmanagementusingsqlite.global.DB
import com.example.gymmanagementusingsqlite.global.MyFunction
import com.example.gymmanagementusingsqlite.model.AllMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


class FragmentFeePending : BaseFragment() {
    private lateinit var binding: FragmentFeePendingBinding
    private var db:DB?=null
    private var adapter: AdapterLoadMember? = null
    private var arrayList: ArrayList<AllMember> = ArrayList()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding=FragmentFeePendingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title="Fee Pending"
        db=activity?.let { DB(it) }

        binding.edtPendingSearch.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                myFilter(p0.toString())
            }

        })
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        // Clear the array list to avoid duplication of data
        arrayList.clear()

        lifecycleScope.executeAsyncTask(onPreExecute = {
            showDialog("Loading...")
        }, doInBackground = {
            arrayList.clear()
            val sqlQuery = "SELECT * FROM MEMBER WHERE STATUS='A' AND EXPIRE_ON <= '${MyFunction.getCurrentDate()}' ORDER BY FIRST_NAME"
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
                binding.recyclerViewPending.visibility = View.VISIBLE
                binding.txtPendingNDF.visibility = View.GONE

                adapter = AdapterLoadMember(arrayList)
                binding.recyclerViewPending.layoutManager=LinearLayoutManager(activity)
                binding.recyclerViewPending.adapter = adapter
//                adapter?.notifyDataSetChanged()


                adapter?.onClick {
                    loadFragment(it)

                }


            } else {
                binding.recyclerViewPending.visibility = View.GONE
                binding.txtPendingNDF.visibility = View.VISIBLE
            }
        })
        CloseDialog()
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

    private fun loadFragment(id:String){
        val fragment=FragmentAddMember()
        val args=Bundle()
        args.putString("ID",id)
        fragment.arguments=args
        val fragmentManager: FragmentManager?=fragmentManager
        fragmentManager!!.beginTransaction().replace(R.id.frame_container,fragment,"FragmentAdd").commit()
    }

    private fun myFilter(search:String){
        val temp:ArrayList<AllMember> = ArrayList()

        if (arrayList.size>0){
            for (list in arrayList){
                if(list.firstName.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))||
                    list.lastName.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)) ){
                    temp.add(list)
                }
            }
        }
        adapter?.updateList(temp)
    }

}