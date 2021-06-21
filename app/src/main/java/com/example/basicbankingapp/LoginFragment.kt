package com.example.basicbankingapp

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.basicbankingapp.Activities.MainActivity
import com.example.basicbankingapp.Activities.UserDAO
import com.example.basicbankingapp.databinding.FragmentLoginBinding
import com.facebook.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.*


@Suppress("DEPRECATION")
class LoginFragment : Fragment() {

    private val RC_SIGN_IN: Int = 123
    private val TAG = "SignInActivity Tag"
    private lateinit var binding:FragmentLoginBinding
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var auth : FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_login, container, false)

        var animationStatusLogIn:Int? = arguments?.getInt("Animation Status login")
        var animationStatusSignup:Int? = arguments?.getInt("Animation Status signUp")


        binding.signupTv.setOnClickListener {
            if (animationStatusSignup==0){
                val bundle1:Bundle= bundleOf(Pair("signUpAnimation",0))
                it.findNavController().navigate(R.id.action_loginFragment_to_signupFragment,bundle1)
        }
            else{
                it.findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
            }

            }
        //calling animation
        if(animationStatusLogIn!=0){
            startLoginAnimation()
        }

        auth= Firebase.auth

        googleSignInClient = GoogleSignIn.getClient(requireContext(),getGSO())

        FacebookSdk.sdkInitialize(requireContext());

        callbackManager = CallbackManager.Factory.create()


        binding.fabGoogle.setOnClickListener {
            if(isOnline(requireContext())){
                googleSignIn()
            }
            else{
                displayDialog()
            }

        }

        binding.loginBtn.setOnClickListener {
            if(isOnline(requireContext())){
                logIn()
            }
            else{
                displayDialog()
            }

        }

        binding.fabFacebook.setOnClickListener {
            facebookSignIn()
        }



        return binding.root
    }

    private fun displayDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.findViewById<Button>(R.id.OKBtn).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun facebookSignIn() {

        binding.fabFacebook.setFragment(this)
        binding.fabFacebook.setReadPermissions("email", "public_profile")
        binding.fabFacebook.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })
// ...


    }


    private fun logIn() {
        val email = binding.loginEmail.text.toString()
        val password = binding.loginPass.text.toString()
        if (email.isEmpty()){
            Toast.makeText(requireContext(),"Email Required", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(requireContext(),"Password Required", Toast.LENGTH_SHORT).show()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(requireContext(),"Enter Correct Email", Toast.LENGTH_SHORT).show()
        }
        else{
            binding.logInView.visibility = View.GONE
            binding.progressBarLogIn.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener{ task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("login", "signInWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("login", "signInWithEmail:failure", task.exception)
                            Toast.makeText(requireContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                    }


        }

    }
    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }


    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        binding.logInView.visibility = View.GONE
        binding.progressBarLogIn.visibility = View.VISIBLE
        auth.signInWithCredential(credential)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's informatio
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    LoginManager.getInstance().logOut()
                    updateUI(user,1)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("face", "signInWithCredential:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication failed.dsfds",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account =
                    completedTask.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "Authentication failed.",
                Toast.LENGTH_SHORT).show()
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)

        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        binding.logInView.visibility = View.GONE
        binding.progressBarLogIn.visibility = View.VISIBLE
        auth.signInWithCredential(credential)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user,1)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(requireContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }

    }

    private fun updateUI(firebaseUser: FirebaseUser?,code:Int=0) {

        if (firebaseUser!=null) {
            if(code==0){
                findNavController().navigate(R.id.action_loginFragment_to_details)
            }
            else {
                val user = User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email, firebaseUser.photoUrl.toString(), false)
                val userDao = UserDAO()
                userDao.addUser(user)
                findNavController().navigate(R.id.action_loginFragment_to_details)
            }
        }
        else{
            binding.logInView.visibility = View.VISIBLE
            binding.progressBarLogIn.visibility = View.GONE
        }
    }



    private fun getGSO():GoogleSignInOptions {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        return  gso
    }




    private fun startLoginAnimation() {
        binding.fabFacebook.translationY= 300F
        binding.fabGoogle.translationY=300F
        binding.loginEmail.translationX=800F
        binding.loginPass.translationX=800F
        binding.loginBtn.translationX=800F


        binding.fabFacebook.alpha = 0F
        binding.fabGoogle.alpha=0F
        binding.loginEmail.alpha=0F
        binding.loginPass.alpha=0F
        binding.loginBtn.alpha=0F

        binding.fabFacebook.animate()
                .translationY(0F).alpha(1F).setDuration(1000).setStartDelay(400).start()
        binding.fabGoogle.animate()
                .translationY(0F).alpha(1F).setDuration(1000).setStartDelay(600).start()

        binding.loginEmail.animate()
                .translationX(0F).alpha(1F).setDuration(800).setStartDelay(300).start()
        binding.loginPass.animate()
                .translationX(0F).alpha(1F).setDuration(800).setStartDelay(500).start()
        binding.loginBtn.animate()
                .translationX(0F).alpha(1F).setDuration(800).setStartDelay(700).start()


    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }


}


