package com.example.gymmanagementusingsqlite.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.Manifest
import android.app.Activity
import android.database.DatabaseUtils
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.example.gymmanagementusingsqlite.R
import com.example.gymmanagementusingsqlite.databinding.FragmentAddMemberBinding
import com.example.gymmanagementusingsqlite.global.CaptureImage
import com.example.gymmanagementusingsqlite.global.DB
import com.example.gymmanagementusingsqlite.global.MyFunction
import java.text.SimpleDateFormat
import java.util.Locale

class FragmentAddMember : Fragment() {

    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_STORAGE_PERMISSION = 200
    private val REQUEST_CAMERA = 1234
    private val REQUEST_GALLERY = 5464
    private var actualImagePath = ""

    private var captureImage: CaptureImage? = null
    private var db: DB? = null
    private var fees: MutableMap<String, String> = mutableMapOf()
    private var gender="Male"
    private lateinit var binding: FragmentAddMemberBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMemberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = activity?.let { DB(it) }
        captureImage = CaptureImage(activity)

        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val myFormat = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            binding.edtJoining.setText(sdf.format(cal.time))
        }

        setupMembershipSpinner()
        setupDiscountTextWatcher()
        setupJoiningDatePicker(dateSetListener)
        setupImagePicker()
        getFee()
    }

    private fun setupMembershipSpinner() {
        binding.spMembership.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val value = binding.spMembership.selectedItem.toString().trim()
                if (value == "Select") {
                    binding.edtExpire.setText("")
                    calculateTotal()
                } else {
                    if (binding.edtJoining.text.toString().trim().isNotEmpty()) {
                        val months = when (value) {
                            "1 Month" -> 1
                            "3 Month" -> 3
                            "6 Month" -> 6
                            "1 Year" -> 12
                            "3 Year" -> 36
                            else -> 0
                        }
                        if (months > 0) {
                            calculateExpireDate(months)
                            calculateTotal()
                        }
                    } else {
                        showToast("Select Joining Date First")
                        binding.spMembership.setSelection(0)
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setupDiscountTextWatcher() {
        binding.edtDiscount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                calculateTotal()
            }
        })


        binding.radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when(id){
                R.id.rdMale -> {
                    gender="Male"
                }
                R.id.rdFemale -> {
                    gender="Female"
                }
            }
        }
        binding.btnAddMemberSave.setOnClickListener {
            if (validate()){
                saveData()
            }
        }

    }

    private fun setupJoiningDatePicker(dateSetListener: DatePickerDialog.OnDateSetListener) {
        binding.imgPicDate.setOnClickListener {
            activity?.let {
                DatePickerDialog(it, dateSetListener, Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show()
            }
        }
    }

    private fun setupImagePicker() {
        binding.imgTakeImage.setOnClickListener { getImage() }
    }

    private fun getFee() {
        try {
            val sqlQuery = "SELECT * FROM FEE WHERE ID = '1'"
            db?.fireQuery(sqlQuery)?.use {

                if (it.count > 0) {
                    fees["1 Month"] = MyFunction.getValue(it, "ONE_MONTH") ?: "0"
                    fees["3 Month"] = MyFunction.getValue(it, "THREE_MONTH") ?: "0"
                    fees["6 Month"] = MyFunction.getValue(it, "SIX_MONTH") ?: "0"
                    fees["1 Year"] = MyFunction.getValue(it, "ONE_YEAR") ?: "0"
                    fees["3 Year"] = MyFunction.getValue(it, "THREE_YEAR") ?: "0"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateTotal() {
        val month = binding.spMembership.selectedItem.toString().trim()
        val discount = binding.edtDiscount.text.toString().toDoubleOrNull() ?: 0.0
        val fee = fees[month]?.toDoubleOrNull() ?: 0.0

        val total = fee - (fee * discount / 100)
        binding.edtAmount.setText(total.toString())
    }

    private fun calculateExpireDate(month: Int) {
        val dtStart = binding.edtJoining.text.toString().trim()
        if (dtStart.isNotEmpty()) {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val date1 = format.parse(dtStart)
            val cal = Calendar.getInstance().apply { time = date1 }
            cal.add(Calendar.MONTH, month)

            val myFormat = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            binding.edtExpire.setText(sdf.format(cal.time))
        } else {
            showToast("Please select a valid joining date")
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
    }

    private fun getImage() {
        val items = arrayOf("Take Photo", "Choose Image", "Cancel")
        android.app.AlertDialog.Builder(requireActivity())
            .setCancelable(false)
            .setTitle("Select Image")
            .setItems(items) { _, i ->
                when (items[i]) {
                    "Take Photo" -> {
                        if (hasCameraPermissions()) {
                            takePhoto()
                        } else {
                            requestCameraPermissions()
                        }
                    }
                    "Choose Image" -> {
                        if (hasStoragePermission()) {
                            pickImage()
                        } else {
                            requestStoragePermission()
                        }
                    }
                    "Cancel" -> {}
                }
            }
            .show()
    }

    private fun hasCameraPermissions() =
        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun hasStoragePermission() =
        ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_STORAGE_PERMISSION
        )
    }

    private fun takePhoto() {
        val imageUri = captureImage?.setImageUri() ?: run {
            showToast("Unable to create image URI")
            return
        }

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        try {
            startActivityForResult(takePictureIntent, REQUEST_CAMERA)
        } catch (e: Exception) {
            showToast("Failed to open camera: ${e.message}")
            Log.e("FragmentAddMember", "Camera Error: ${e.message}", e)
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }

        try {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY)
        } catch (e: Exception) {
            showToast("Failed to open gallery: ${e.message}")
            Log.e("FragmentAddMember", "Gallery Error: ${e.message}", e)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    showToast("Camera permission denied")
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage()
                } else {
                    showToast("Storage permission denied")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            showToast("Operation cancelled or failed")
            return
        }

        when (requestCode) {
            REQUEST_CAMERA -> {
                val imagePath = captureImage?.getRightAngleImage(captureImage?.imagePath)
                imagePath?.let { loadImage(it.toString()) }
                    ?: showToast("Failed to capture image")
            }
            REQUEST_GALLERY -> {
                data?.data?.let { uri ->
                    val imagePath = captureImage?.getRightAngleImage(captureImage?.getPath(uri, context))
                    imagePath?.let { loadImage(it.toString()) }
                        ?: showToast("Failed to load image from gallery")
                } ?: showToast("Failed to get image from gallery")
            }
        }
    }

    private fun loadImage(path: String) {
        Log.d("FragmentAddMember", "ImagePath: $path")
        getImagePath(captureImage?.decodeFile(path))
    }

    private fun getImagePath(bitmap: Bitmap?) {
        bitmap?.let {
            val tempUri: Uri? = captureImage?.getImageUri(activity, bitmap)
            actualImagePath = captureImage?.getRealPathFromURI(tempUri, activity).toString()
            Log.d("FragmentAddMember", "ActualImagePath: $actualImagePath")

            Glide.with(requireActivity())
                .load(tempUri)
                .into(binding.imgPic)
        }
    }

    private fun validate():Boolean{
        if(binding.edtFirstName.text.toString().trim().isEmpty()){
            showToast("Enter First Name")
            return false
        }else if (binding.edtLastName.text.toString().trim().isEmpty()){
            showToast("Enter Last Name")
            return false
        }else if (binding.edtAge.text.toString().trim().isEmpty()){
            showToast("Enter Age")
            return false
        }else if (binding.edtMobile.text.toString().trim().isEmpty()){
            showToast("Enter Mobile Number")
            return false
        }
        return true
    }

    private fun saveData(){
        try {
            val sqlQuery = "INSERT OR REPLACE INTO MEMBER(ID, FIRST_NAME, LAST_NAME, GENDER, AGE, WEIGHT, MOBILE, ADDRESS, " +
                    "DATE_OF_JOINING, MEMBERSHIP, EXPIRE_ON, DISCOUNT, TOTAL, IMAGE_PATH, STATUS) VALUES (" +
                    "'${getIncrementId()}', " +
                    "${DatabaseUtils.sqlEscapeString(binding.edtFirstName.text.toString().trim())}, " +
                    "${DatabaseUtils.sqlEscapeString(binding.edtLastName.text.toString().trim())}, " +
                    "'$gender', " +
                    "'${binding.edtAge.text.toString().trim()}', " +
                    "'${binding.edtWeight.text.toString().trim()}', " +
                    "'${binding.edtMobile.text.toString().trim()}', " +
                    "'${binding.edtAddress.text.toString().trim()}', " +
                    "'${MyFunction.returnSQLDataFormat(binding.edtJoining.text.toString().trim())}', " +
                    "'${binding.spMembership.selectedItem.toString().trim()}', " +
                    "'${MyFunction.returnSQLDataFormat(binding.edtExpire.text.toString().trim())}', " +
                    "'${binding.edtDiscount.text.toString().trim()}', " +
                    "'${binding.edtAmount.text.toString().trim()}', " +
                    "'$actualImagePath', " +
                    "'A')"
            db?.executeQuery(sqlQuery)
            showToast("Member data saved successfully!")
            clearData()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun getIncrementId():String{
        var incrementId=""
        try {
            val sqlQuery="SELECT IFNULL(MAX(ID)+1,'1') AS ID FROM MEMBER"
            db?.fireQuery(sqlQuery)?.use {
                if (it.count > 0) {
                    incrementId = MyFunction.getValue(it, "ID")
                }
            }

        }catch (e:Exception){
            e.printStackTrace()
        }

        return incrementId
    }

    private fun clearData(){
        binding.edtFirstName.setText("")
        binding.edtLastName.setText("")
        binding.edtAge.setText("")
        binding.edtWeight.setText("")
        binding.edtMobile.setText("")
        binding.edtJoining.setText("")

        Glide.with(this)
            .load(R.drawable.boy)
            .into(binding.imgPic)
    }
}
