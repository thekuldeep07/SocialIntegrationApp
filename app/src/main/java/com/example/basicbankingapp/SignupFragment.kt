package com.example.basicbankingapp

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.basicbankingapp.Activities.UserDAO
import com.example.basicbankingapp.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SignupFragment : Fragment() {
    private  lateinit var  binding:FragmentSignupBinding
    private val TAG = "SignInActivity Tag"
    private lateinit var auth : FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)


        var animationStatusSignup:Int? = arguments?.getInt("signUpAnimation")

        binding.loginTv.setOnClickListener {
            val bundle:Bundle= bundleOf(
                Pair("Animation Status login", 0), Pair(
                    "Animation Status signUp",
                    0
                )
            )
            it.findNavController().navigate(R.id.action_signupFragment_to_loginFragment, bundle)
        }

        if (animationStatusSignup!=0){
            startSignUpAnimation()
        }

        auth=Firebase.auth

        binding.signUpBtn.setOnClickListener {
              register()
        }


        return binding.root
    }


    private fun register() {
        val email = binding.signUpEmail.text.toString()
        val password = binding.signUpPass.text.toString()
        val confirmPass = binding.signUpConfrmPass.text.toString()
        val name =  binding.signUpName.text.toString()

        if (email.isEmpty()){
            Toast.makeText(requireContext(),"Email Required", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(requireContext(),"Password Required", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(requireContext(),"Name Required", Toast.LENGTH_SHORT).show()
        }
        else if(password != confirmPass){
            Toast.makeText(requireContext(),"Match password", Toast.LENGTH_SHORT).show()
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(requireContext(),"Enter Correct Email", Toast.LENGTH_SHORT).show()
        }
        else if(name.isEmpty()){
            Toast.makeText(requireContext(),"Name Required", Toast.LENGTH_SHORT).show()
        }
        else{
            binding.signUpView.visibility = View.GONE
            binding.progressBarSignUp.visibility = View.VISIBLE
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{ task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("signup", "createUserWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user,name)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("signup", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(requireContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            updateUI(null, name)
                        }
                    }



        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?, name: String) {
        if (firebaseUser!=null) {

            val user =  User(firebaseUser.uid,name,firebaseUser.email,firebaseUser.photoUrl.toString(),false)
            val userDao = UserDAO()
            userDao.addUser(user)
            findNavController().navigate(R.id.action_signupFragment_to_details)
        }
        else{
            binding.signUpView.visibility = View.VISIBLE
            binding.progressBarSignUp.visibility = View.GONE
        }
    }



    fun startSignUpAnimation(){

        binding.signUpEmail.translationX=800F
        binding.signUpPass.translationX=800F
        binding.signUpConfrmPass.translationX=800F
        binding.signUpBtn.translationX=800F
        binding.signUpName.translationX=800F



        binding.signUpEmail.alpha=0F
        binding.signUpPass.alpha=0F
        binding.signUpBtn.alpha=0F
        binding.signUpConfrmPass.alpha=0F
        binding.signUpName.alpha=0F


        binding.signUpName.animate()
                .translationX(0F).alpha(1F).setDuration(800).setStartDelay(300).start()
        binding.signUpEmail.animate()
                .translationX(0F).alpha(1F).setDuration(800).setStartDelay(500).start()
        binding.signUpPass.animate()
                .translationX(0F).alpha(1F).setDuration(800).setStartDelay(700).start()
        binding.signUpConfrmPass.animate()
                .translationX(0F).alpha(1F).setDuration(800).setStartDelay(700).start()
        binding.signUpBtn.animate()
                .translationX(0F).alpha(1F).setDuration(800).setStartDelay(900).start()


    }


}