package com.example.gymmanagementusingsqlite.fragment

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.example.gymmanagementusingsqlite.R
import com.example.gymmanagementusingsqlite.databinding.FragmentAddMemberBinding
import com.example.gymmanagementusingsqlite.global.DB
import com.example.gymmanagementusingsqlite.global.MyFunction
import java.text.SimpleDateFormat
import java.util.Locale


class FragmentAddMember : Fragment() {
    var db:DB?=null
    var oneMonth:String?=""
    var threeMonth:String?=""
    var sixMonth:String?=""
    var oneYear:String?=""
    var threeYear:String?=""

    private lateinit var binding:FragmentAddMemberBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding= FragmentAddMemberBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db=activity?.let { DB(it) }

        val cal=Calendar.getInstance()
        val dateSetListener=DatePickerDialog.OnDateSetListener{ view1,year,monthOfYear,dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            val myFormat="dd/MM/yyyy"
            val sdf=SimpleDateFormat(myFormat, Locale.US)
            binding.edtJoining.setText(sdf.format(cal.time))


        }
        binding.spMembership.onItemSelectedListener=object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val value=binding.spMembership.selectedItem.toString().trim()

                if (value=="Select"){
                    binding.edtExpire.setText("")
                    calculateTotal(binding.spMembership,binding.edtDiscount,binding.edtAmount)
                }else{
                    if (binding.edtJoining.text.toString().trim().isNotEmpty()){
                        if (value=="1 Month"){
                            calculateExpireDate(1,binding.edtExpire)
                            calculateTotal(binding.spMembership,binding.edtDiscount,binding.edtAmount)
                        }else if (value=="3 Month"){
                            calculateExpireDate(3,binding.edtExpire)
                            calculateTotal(binding.spMembership,binding.edtDiscount,binding.edtAmount)
                        }else if (value=="6 Month"){
                            calculateExpireDate(6,binding.edtExpire)
                            calculateTotal(binding.spMembership,binding.edtDiscount,binding.edtAmount)
                        }else if (value=="1 Year"){
                            calculateExpireDate(12,binding.edtExpire)
                            calculateTotal(binding.spMembership,binding.edtDiscount,binding.edtAmount)
                        }else if (value=="3 Year"){
                            calculateExpireDate(36,binding.edtExpire)
                            calculateTotal(binding.spMembership,binding.edtDiscount,binding.edtAmount)
                        }

                    }else{
                        showToast("Select Joining Date First")
                        binding.spMembership.setSelection(0)
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        binding.edtDiscount.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!=null){
                    calculateTotal(binding.spMembership,binding.edtDiscount,binding.edtAmount)
                }
            }

        })

        binding.imgPicDate.setOnClickListener {
            activity?.let { it1 -> DatePickerDialog(it1,dateSetListener,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show() }
        }
        getFee()
    }

    private fun getFee(){
        try {
            val sqlQuery="SELECT * FROM FEE WHERE ID = '1'"
            db?.fireQuery(sqlQuery)?.use {
                oneMonth=MyFunction.getValue(it,"ONE_MONTH")
                threeMonth=MyFunction.getValue(it,"THREE_MONTH")
                sixMonth=MyFunction.getValue(it,"SIX_MONTH")
                oneYear=MyFunction.getValue(it,"ONE_YEAR")
                threeYear=MyFunction.getValue(it,"THREE_YEAR")
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun calculateTotal(spMember: Spinner, edtDis: EditText, edtAmt: EditText) {
        val month = spMember.selectedItem.toString().trim()
        var discount = edtDis.text.toString().trim()
        if (discount.isEmpty()) {
            discount = "0"
        }

        when (month) {
            "Select" -> {
                edtAmt.setText("")
            }
            "1 Month" -> {
                if (oneMonth!!.trim().isNotEmpty()) {
                    val discountAmount = (oneMonth!!.toDouble() * discount.toDouble()) / 100
                    val total = oneMonth!!.toDouble() - discountAmount
                    edtAmt.setText(total.toString())
                }
            }
            "3 Month" -> {
                if (threeMonth!!.trim().isNotEmpty()) {
                    val discountAmount = (threeMonth!!.toDouble() * discount.toDouble()) / 100
                    val total = threeMonth!!.toDouble() - discountAmount
                    edtAmt.setText(total.toString())
                }
            }
            "6 Month" -> {
                if (sixMonth!!.trim().isNotEmpty()) {
                    val discountAmount = (sixMonth!!.toDouble() * discount.toDouble()) / 100
                    val total = sixMonth!!.toDouble() - discountAmount
                    edtAmt.setText(total.toString())
                }
            }
            "1 Year" -> {
                if (oneYear!!.trim().isNotEmpty()) {
                    val discountAmount = (oneYear!!.toDouble() * discount.toDouble()) / 100
                    val total = oneYear!!.toDouble() - discountAmount
                    edtAmt.setText(total.toString())
                }
            }
            "3 Year" -> {
                if (threeYear!!.trim().isNotEmpty()) {
                    val discountAmount = (threeYear!!.toDouble() * discount.toDouble()) / 100
                    val total = threeYear!!.toDouble() - discountAmount
                    edtAmt.setText(total.toString())
                }
            }
        }
    }

    private fun calculateExpireDate(month: Int, edtExpire: EditText) {
        val dtStart = binding.edtJoining.text.toString().trim()
        if (dtStart.isNotEmpty()) {  // Check if the joining date is not empty
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val date1 = format.parse(dtStart)
            val cal = Calendar.getInstance()
            cal.time = date1
            cal.add(Calendar.MONTH, month)

            val myFormat = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            edtExpire.setText(sdf.format(cal.time))
        } else {
            showToast("Please select a valid joining date")
        }
    }

    private fun showToast(msg:String){
        Toast.makeText(activity,msg, Toast.LENGTH_LONG).show()
    }



}