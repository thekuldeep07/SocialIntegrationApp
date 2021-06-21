package com.example.basicbankingapp

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.basicbankingapp.Activities.UserDAO
import com.example.basicbankingapp.databinding.FragmentDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class Details : Fragment() {
     private lateinit var binding: FragmentDetailsBinding
     private lateinit var auth: FirebaseAuth
     private lateinit var imageuri:String
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         binding = DataBindingUtil.inflate(inflater, R.layout.fragment_details, container, false)
         auth= FirebaseAuth.getInstance()
         binding.logoutBtn.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_details_to_loginFragment)
        }

       val firebaseuser = FirebaseAuth.getInstance().currentUser!!
        setDetails(firebaseuser)
        setImageView(firebaseuser.photoUrl)






    return binding.root
    }

    private fun setDetails(firebaseuser: FirebaseUser) {
        binding.detailsView.visibility=View.GONE
        binding.progressBarDetail.visibility=View.VISIBLE
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val userDao = UserDAO()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            withContext(Dispatchers.Main) {
                binding.userEmailTV.text=user.displayEmail
                binding.userNameTV.text=user.displayName
                binding.detailsView.visibility=View.VISIBLE
                binding.progressBarDetail.visibility=View.GONE


            }
        }

        }

    private fun setImageView(photoUrl: Uri?) {
        if (photoUrl != null) {
            binding.detailsView.visibility=View.GONE
            binding.progressBarDetail.visibility=View.VISIBLE
            imageuri = photoUrl.toString() // Getting uri from facebook
            imageuri += "?type=large"
//            Log.d("mytag", imageuri)

            Glide.with(this)
                    .load(imageuri).listener(object :RequestListener<Drawable?>{
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable?>?, isFirstResource: Boolean): Boolean {
                            binding.detailsView.visibility=View.VISIBLE
                            binding.progressBarDetail.visibility=View.GONE
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            binding.detailsView.visibility=View.VISIBLE
                            binding.progressBarDetail.visibility=View.GONE
                            return  false
                        }
                    }).circleCrop().into(binding.userIV)
        }

        else{
            binding.userIV.setImageResource(R.drawable.userprofile)
        }
    }


}