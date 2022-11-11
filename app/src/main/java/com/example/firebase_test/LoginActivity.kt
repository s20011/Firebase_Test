package com.example.firebase_test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.firebase_test.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSingInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.loginActivityToolbar)
        supportActionBar?.let {
            it.title = "Login"
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }

        //Googleサインインを設定する
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSingInClient = GoogleSignIn.getClient(this, gso)

        //Firebase Authの初期化
        auth = Firebase.auth

        binding.loginbutton.setOnClickListener {
            signIn()
        }

        binding.btsignOut.setOnClickListener {
            signOut()
        }

        binding.updatebt.setOnClickListener {
            val name = binding.updateUsername.text.toString()
            updateProfile(name)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()

        //ユーザーがサインインしているかどうかを確認し、それに応じてUIを更新する
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //GoogleSignInApi.getSingInIntent()からIntentを起動して返された結果
        if(requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                //Googleサインインが成功したので、Firebaseで認証する。
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                //Googleサインインに失敗し、UIを適切に更新する
                Log.w(TAG, "Google sing in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //サインインに成功したときユーザーの情報をUIに反映
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    //サインインに失敗したときメッセージを送る
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSingInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    private fun signOut() {
        Firebase.auth.signOut()
        updateUI(auth.currentUser)
    }

    private fun updateUI(user: FirebaseUser?){
        if (user == null){
            binding.emailtext.text = "NULL"
            binding.usernametx.text = "NULL"
        }else {
            binding.emailtext.text = user.email.toString()
            binding.usernametx.text = user.displayName.toString()
        }
    }

    //Userの名前変更
    private fun updateProfile(name:String){
        val user = Firebase.auth.currentUser
        //ログインしていないときにボタンが押されたときの処理
        if(user == null) {
            Toast.makeText(this@LoginActivity,
                MESSAGE_NOT_LOGIN,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val profileUpdates = userProfileChangeRequest {
            displayName = when(name) {
                "" -> "====="
                else -> name
            }

        }

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful){
                binding.usernametx.text = user.displayName.toString()
                Toast.makeText(this@LoginActivity,
                    "名前を変更しました",
                    Toast.LENGTH_LONG
                ).show()
                Log.d(TAG, "--->" + binding.usernametx.text.toString())
                Log.d(TAG, "User profile updated")
            }
        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val MESSAGE_NOT_LOGIN = "ログインしてください"
        private const val RC_SIGN_IN = 100
    }


}