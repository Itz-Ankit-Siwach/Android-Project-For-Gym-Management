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
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.database.DatabaseUtils
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.example.gymmanagementusingsqlite.R
import com.example.gymmanagementusingsqlite.databinding.FragmentAddMemberBinding
import com.example.gymmanagementusingsqlite.databinding.RenewDialogueBinding
import com.example.gymmanagementusingsqlite.global.CaptureImage
import com.example.gymmanagementusingsqlite.global.DB
import com.example.gymmanagementusingsqlite.global.MyFunction
import java.text.SimpleDateFormat
import java.util.Date
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
    private var ID=""

    private lateinit var binding: FragmentAddMemberBinding
    private lateinit var bindingDialog:RenewDialogueBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMemberBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title="Add New Member"

        db = activity?.let { DB(it) }
        captureImage = CaptureImage(activity)

        ID=arguments!!.getString("ID").toString()

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


        binding.btnActiveInactive.setOnClickListener {
            try {
                if (getStatus()=="A"){
                    val sqlQuery="UPDATE MEMBER SET STATUS='I' WHERE ID='$ID'"
                    db?.executeQuery(sqlQuery)
                    showToast("Member is Inactive now")
                } else{
                    val sqlQuery="UPDATE MEMBER SET STATUS='A' WHERE ID='$ID'"
                    db?.executeQuery(sqlQuery)
                    showToast("Member is Active now")
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }


        if (ID.trim().isNotEmpty()){
            if (getStatus()=="A"){
                binding.btnActiveInactive.text="Inactive"
                binding.btnActiveInactive.visibility=View.VISIBLE
            }else{
                binding.btnActiveInactive.text="Active"
                binding.btnActiveInactive.visibility=View.VISIBLE
            }
            loadData()
        }else{
            binding.btnActiveInactive.visibility=View.GONE
        }


        binding.btnRenewalSave.setOnClickListener {

            if (ID.trim().isNotEmpty()){
                openRenewalDialog()
            }

        }


    }

    private fun getStatus():String{
        var status=""
        try {
            val sqlQuery="SELECT STATUS FROM MEMBER WHERE ID='$ID'"
            db?.fireQuery(sqlQuery)?.use {
                if (it.count>0){
                    status=MyFunction.getValue(it,"STATUS")
                }
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }
        return status
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


        binding.radioGroup.setOnCheckedChangeListener { radioGroup, id ->
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

    private fun validate(): Boolean {
        if (binding.edtFirstName.text.toString().trim().isEmpty()) {
            showToast("Enter First Name")
            return false
        } else if (binding.edtLastName.text.toString().trim().isEmpty()) {
            showToast("Enter Last Name")
            return false
        } else if (binding.edtAge.text.toString().trim().isEmpty()) {
            showToast("Enter Age")
            return false
        } else if (binding.edtMobile.text.toString().trim().isEmpty()) {
            showToast("Enter Mobile Number")
            return false
        } else if (binding.edtJoining.text.toString().trim().isEmpty() || !isValidDate(binding.edtJoining.text.toString().trim())) {
            showToast("Enter a valid Joining Date (dd/MM/yyyy)")
            return false
        }
        return true
    }

    private fun saveData(){
        try {
            var myIncrementId=""
            if (ID.trim().isEmpty()){
                myIncrementId=getIncrementId()
            }else{
                myIncrementId=ID
            }

            val sqlQuery = "INSERT OR REPLACE INTO MEMBER(ID, FIRST_NAME, LAST_NAME, GENDER, AGE, WEIGHT, MOBILE, ADDRESS, " +
                    "DATE_OF_JOINING, MEMBERSHIP, EXPIRE_ON, DISCOUNT, TOTAL, IMAGE_PATH, STATUS) VALUES (" +
                    "'${myIncrementId}', " +
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

            if (ID.trim().isEmpty()){
                clearData()
            }

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
    private fun isValidDate(dateString: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            dateFormat.parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }


    private fun loadData(){
        try {
            val sqlQuery="SELECT * FROM MEMBER WHERE ID='$ID'"
            db?.fireQuery(sqlQuery)?.use {
                if (it.count>0){
                    val firstName=MyFunction.getValue(it,"FIRST_NAME")
                    val lastName=MyFunction.getValue(it,"LAST_NAME")
                    val age=MyFunction.getValue(it,"AGE")
                    val gender=MyFunction.getValue(it,"GENDER")
                    val weight=MyFunction.getValue(it,"WEIGHT")
                    val mobileNo=MyFunction.getValue(it,"MOBILE")
                    val address=MyFunction.getValue(it,"ADDRESS")
                    val dateOfJoin=MyFunction.getValue(it,"DATE_OF_JOINING")
                    val membership=MyFunction.getValue(it,"MEMBERSHIP")
                    val expiry=MyFunction.getValue(it,"EXPIRE_ON")
                    val discount=MyFunction.getValue(it,"DISCOUNT")
                    val total=MyFunction.getValue(it,"TOTAL")
                    actualImagePath=MyFunction.getValue(it,"IMAGE_PATH")

                    binding.edtFirstName.setText(firstName)
                    binding.edtLastName.setText(lastName)
                    binding.edtAge.setText(age)
                    binding.edtWeight.setText(weight)
                    binding.edtMobile.setText(mobileNo)
                    binding.edtAddress.setText(address)
                    binding.edtJoining.setText(MyFunction.returnUserDataFormat(dateOfJoin))


                    if (actualImagePath.isNotEmpty()){
                        Glide.with(this)
                            .load(actualImagePath)
                            .into(binding.imgPic)
                    }else{
                        if (gender=="Male"){
                            Glide.with(this)
                                .load(R.drawable.boy)
                                .into(binding.imgPic)
                        }else{
                            Glide.with(this)
                                .load(R.drawable.girl)
                                .into(binding.imgPic)
                        }
                    }

                    if (membership.trim().isNotEmpty()){
                        when(membership){
                            "1 Month" -> {
                                binding.spMembership.setSelection(1)
                            }
                            "3 Month" -> {
                                binding.spMembership.setSelection(2)
                            }
                            "6 Month" -> {
                                binding.spMembership.setSelection(3)
                            }
                            "1 Year" -> {
                                binding.spMembership.setSelection(4)
                            }
                            "3 Year" -> {
                                binding.spMembership.setSelection(5)
                            }
                            else ->{
                                binding.spMembership.setSelection(0)
                            }
                        }
                    }

                    if (gender=="Male"){
                        binding.radioGroup.check(R.id.rdMale)
                    }else{
                        binding.radioGroup.check(R.id.rdFemale)
                    }

                    binding.edtExpire.setText(MyFunction.returnUserDataFormat(expiry))
                    binding.edtAmount.setText(total)
                    binding.edtDiscount.setText(discount)

                    val sdf=SimpleDateFormat("yyyy-MM-dd",Locale.US)
                    val eDate=sdf.parse(expiry)
                    if (eDate!!.after(Date())){
                        //if expiry>current date
                        binding.btnRenewalSave.visibility=View.GONE
                    }else{
                        if (getStatus()=="A"){
                            binding.btnRenewalSave.visibility=View.VISIBLE
                        }else{
                            binding.btnRenewalSave.visibility=View.GONE
                        }
                    }

                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun openRenewalDialog() {
        // Inflate the dialog layout
        bindingDialog = RenewDialogueBinding.inflate(LayoutInflater.from(activity))
        val dialog = Dialog(requireActivity(), R.style.AlterDialogCustom)
        dialog.setContentView(bindingDialog.root)
        dialog.setCancelable(false)
        dialog.show()

        // Set the initial joining date from expire text
        bindingDialog.edtDialogJoining.setText(binding.edtExpire.text.toString().trim())

        // Dismiss the dialog when the back button is clicked
        bindingDialog.imgDialogRenewBack.setOnClickListener {
            dialog.dismiss()
        }

        // Initialize calendar and date set listener
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            val myFormat = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            bindingDialog.edtDialogJoining.setText(sdf.format(cal.time))
        }

        // Show date picker when date image is clicked
        bindingDialog.imgDialogPicDate.setOnClickListener {
            activity?.let {
                DatePickerDialog(
                    it,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        // Set item selection listener for the membership spinner
        bindingDialog.spDialogMembership.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val value = parent.getItemAtPosition(position).toString().trim()
                if (value == "Select") {
                    bindingDialog.edtDialogExpire.setText("") // Clear expiration date if "Select" is chosen
                    bindingDialog.edtDialogAmount.setText("0.00") // Clear amount when "Select" is chosen
                } else {
                    if (bindingDialog.edtDialogJoining.text.toString().trim().isNotEmpty()) { // Check if joining date is set
                        val months = when (value) {
                            "1 Month" -> 1
                            "3 Month" -> 3
                            "6 Month" -> 6
                            "1 Year" -> 12
                            "3 Year" -> 36
                            else -> 0
                        }

                        if (months > 0) {
                            val dtStart = bindingDialog.edtDialogJoining.text.toString().trim()
                            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)

                            // Parse the joining date
                            val date1 = format.parse(dtStart)
                            if (date1 != null) {
                                val calExpire = Calendar.getInstance().apply { time = date1 }
                                calExpire.add(Calendar.MONTH, months) // Use the months calculated from spinner

                                val myFormat = "dd/MM/yyyy"
                                val sdf = SimpleDateFormat(myFormat, Locale.US)
                                bindingDialog.edtDialogExpire.setText(sdf.format(calExpire.time))
                            } else {
                                showToast("Please select a valid joining date")
                            }

                            // Calculate the total amount
                            val discount = bindingDialog.edtDialogDiscount.text.toString().toDoubleOrNull() ?: 0.0
                            val fee = fees[value]?.toDoubleOrNull() ?: 0.0

                            val total = fee - (fee * discount / 100)
                            bindingDialog.edtDialogAmount.setText(total.toString()) // Set total amount in the dialog
                        }
                    } else {
                        showToast("Select Joining Date First") // Ensure the user selects a date first
                        bindingDialog.spDialogMembership.setSelection(0) // Reset the spinner selection
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

        // Add listener for discount input to recalculate total when discount changes
        bindingDialog.edtDialogDiscount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Recalculate total whenever the discount changes
                val discount = s.toString().toDoubleOrNull() ?: 0.0
                val membershipType = bindingDialog.spDialogMembership.selectedItem?.toString()?.trim()
                if (membershipType != null) {
                    val fee = fees[membershipType]?.toDoubleOrNull() ?: 0.0
                    val total = fee - (fee * discount / 100)
                    bindingDialog.edtDialogAmount.setText(total.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        bindingDialog.btnDialogRenewalSave.setOnClickListener {
            if (bindingDialog.spDialogMembership.selectedItem.toString().trim() != "Select") {
                try {
                    val joiningDate = MyFunction.returnSQLDataFormat(bindingDialog.edtDialogJoining.text.toString().trim())
                    val membership = bindingDialog.spDialogMembership.selectedItem.toString().trim()
                    val expireDate = MyFunction.returnSQLDataFormat(bindingDialog.edtDialogExpire.text.toString().trim())
                    val discount = bindingDialog.edtDialogDiscount.text.toString().toDoubleOrNull() ?: 0.0
                    val total = bindingDialog.edtDialogAmount.text.toString().toDoubleOrNull() ?: 0.0

                    // Construct the SQL query
                    val sqlQuery = "UPDATE MEMBER SET " +
                            "DATE_OF_JOINING='$joiningDate', " +
                            "MEMBERSHIP='$membership', " +
                            "EXPIRE_ON='$expireDate', " +
                            "DISCOUNT=$discount, " +
                            "TOTAL=$total " +
                            "WHERE ID='$ID'" // Ensure ID is defined appropriately

                    db?.executeQuery(sqlQuery)
                    showToast("Membership information updated successfully.")
                    dialog.dismiss()
                    loadData()

                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("Failed to update membership information.")
                }
            } else {
                showToast("Please select a valid membership type.")
            }
        }

    }



}
