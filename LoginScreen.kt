package com.rushkar.pingzz.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.messaging.FirebaseMessaging
import com.rushkar.pingzz.R
import com.rushkar.pingzz.R.color
import com.rushkar.pingzz.databinding.ActivityLoginScreenBinding
import com.rushkar.pingzz.model.request_model.SendOTPRequestModel
import com.rushkar.pingzz.model.request_model.SendTokenRequestModel
import com.rushkar.pingzz.model.response_model.SendTokenResponseModel
import com.rushkar.pingzz.model.response_model.UserData
import com.rushkar.pingzz.repository.OrganizationUsersV1Repository
import com.rushkar.pingzz.ui.base.BaseActivity
import com.rushkar.pingzz.utils.otp.OtpView
import com.rushkar.pingzz.utils.toast
import com.rushkar.pingzz.viewmodel.OrganizationUsersV1ViewModel
import com.rushkar.pingzz.viewmodel.UserViewModelFactory
import com.yudoo.api.WebAPIServiceFactory
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit


class LoginScreen : BaseActivity() {
    val TAG = "LoginScreen"
    private lateinit var binding: ActivityLoginScreenBinding
    private var fcmToken: String? = ""

    private val decimalFormat = DecimalFormat("00")
    private val mResendTime: Long = TimeUnit.SECONDS.toMillis(30)
    private var mCountDownTimer: CountDownTimer? = null

    private lateinit var tvResend: TextView
    private lateinit var tvTimer: TextView
    private lateinit var phoneNumber: TextInputLayout
    private lateinit var otpLayout: LinearLayout
    private lateinit var userInfoLayout: LinearLayout
    private lateinit var title: TextView
    private lateinit var send: MaterialButton
    private lateinit var verify: MaterialButton
    private lateinit var otpView: OtpView
    private lateinit var signup: LinearLayout
    private var userData: UserData? = null
    private var tokenData: SendTokenResponseModel? = null

    private val userViewModel: OrganizationUsersV1ViewModel by viewModels {
        UserViewModelFactory(
            OrganizationUsersV1Repository(
                WebAPIServiceFactory.newInstance().makeServiceFactory()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
    }

    private fun initData() {
        fetchFirebaseToken()
        binding.infoLayout.setBackgroundResource(R.drawable.card_background)

        title = findViewById<TextView>(R.id.title)
        tvResend = findViewById<TextView>(R.id.tvResend)
        tvTimer = findViewById<TextView>(R.id.tvTimer)
        phoneNumber = findViewById<TextInputLayout>(R.id.phoneNumber)
        otpLayout = findViewById<LinearLayout>(R.id.otpLayout)
        userInfoLayout = findViewById<LinearLayout>(R.id.userInfoLayout)

        otpView = findViewById<OtpView>(R.id.otpView)

        val textView = findViewById<TextView>(R.id.signUp_txtview)
        val text = "Don't have any account? Sign Up"
        val ssb = SpannableStringBuilder(text)
        val fcsRed = ForegroundColorSpan(resources.getColor(color.colorBlack))
        ssb.setSpan(fcsRed, 24, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.setText(ssb)

        send = findViewById<MaterialButton>(R.id.send)
        verify = findViewById<MaterialButton>(R.id.verify)
        signup = findViewById<LinearLayout>(R.id.signUp_ly)

        signup.setOnClickListener {
            Intent(this, SignUpScreen::class.java).apply {
                startActivity(this)
            }
        }

        loginViewVisibility()

        send.setOnClickListener {
            if (phoneNumber.editText?.text.toString().trim().isNotEmpty()) {
                if (phoneNumber.editText?.text.toString().length == 10) {
                    sendOtpApiCall(phoneNumber.editText?.text.toString().trim())
                    otpLayout.visibility = View.VISIBLE
                    startCountDown()
                    /*otpLayout.visibility = View.VISIBLE
                    startCountDown()
                    send.text = getString(R.string.continue_text)
                    send.setOnClickListener {
                        if (otpView.text.toString().isNotEmpty()) {
                            if (otpView.text.toString().length == 6) {
                                Intent(this, MainActivity::class.java).apply {
                                    startActivity(this)
                                }
                            } else {
                                toast("Please enter VALID OTP")
                            }
                        } else {
                            toast("Please enter VALID OTP")
                        }
                    }*/
                } else {
                    toast("Please enter VALID Phone Number")
                }
            } else {
                toast("Please enter Phone Number")
            }
        }

        verify.setOnClickListener {
            if (otpView.text.toString().isNotEmpty()) {
                if (otpView.text.toString().length == 6 && otpView.text.toString().equals(userData?.Otp)) {
                    /*Intent(this, MainActivity::class.java).apply {
                        startActivity(this)
                    }*/
                    //toast("VERIFY OTP")
                    sendTokenApiCall(userData!!.Name!!,userData!!.Otp.toString(),"password","192.168.1.1")
                } else {
                    toast("Please enter VALID OTP")
                }
            } else {
                toast("Please enter VALID OTP")
            }
        }

        userViewModel.successStatus.observe(this){
            if (it) {
                otpViewVisibility()
            } else {
                otpLayout.visibility = View.GONE
                send.visibility = View.VISIBLE
            }
            hideProgressDialog()
        }

        userViewModel.otpSuccessStatus.observe(this){
            hideProgressDialog()
            if (it){

            }
        }

        userViewModel.otpData.observe(this) {
            userData = it
        }

        tvResend.setOnClickListener {
            sendOtpApiCall(phoneNumber.editText?.text.toString().trim())
            startCountDown()
        }

    }

    private fun startCountDown() {
        if (mCountDownTimer == null) {
            tvResend.visibility = View.GONE
            tvTimer.visibility = View.VISIBLE
            mCountDownTimer = object : CountDownTimer(mResendTime, 1000) {
                override fun onFinish() {
                    tvResend.visibility = View.VISIBLE
                    tvTimer.visibility = View.GONE
                    mCountDownTimer = null
                }

                override fun onTick(millisUntilFinished: Long) {
                    tvTimer.text = decimalFormat.format(millisUntilFinished / 1000)
                }
            }.start()
        }
    }

    private fun stopCountDown() {
        mCountDownTimer?.cancel()
        mCountDownTimer = null
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }


    private fun sendOtpApiCall(phoneNumber: String) {
        showProgressDialog()
        val request = SendOTPRequestModel().apply {
            mobileNumber = phoneNumber
            deviceToken = fcmToken
            deviceType = "Android"
        }
        userViewModel.sendOtpOnMobile(request)
    }

    private fun sendTokenApiCall(userName: String,Password:String,GrantType :String,ipAddress : String) {
        showProgressDialog()
        val request = SendTokenRequestModel().apply {
            username = userName
            password = Password
            granttype = GrantType
            IpAddress = ipAddress
        }
        userViewModel.sendTokenOnMobile(request)
    }

    /*To Show Login Visibility View*/
    private fun loginViewVisibility() {
        phoneNumber.visibility = View.VISIBLE
        phoneNumber.isClickable = true
        phoneNumber.isEnabled = true
        userInfoLayout.visibility = View.GONE
        otpLayout.visibility = View.GONE
        send.visibility = View.VISIBLE
        verify.visibility = View.GONE
        //register.visibility = View.GONE
        title.text = resources.getString(R.string.login)
    }

    /*To show otp views visibility*/
    private fun otpViewVisibility() {
        phoneNumber.visibility = View.VISIBLE
        phoneNumber.isClickable = false
        phoneNumber.isEnabled = false
        userInfoLayout.visibility = View.GONE
        otpLayout.visibility = View.VISIBLE
        send.visibility = View.GONE
        verify.visibility = View.VISIBLE

        //register.visibility = View.GONE
        title.text = resources.getString(R.string.otp)
    }


    private fun fetchFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                fcmToken = task.result.toString()
                Log.d(TAG, "onCreate: FCM token : $fcmToken")
            })
    }
}