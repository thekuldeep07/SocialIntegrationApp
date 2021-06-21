package com.example.basicbankingapp.Activities

import android.util.Log
import com.example.basicbankingapp.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDAO {
    private val db = FirebaseFirestore.getInstance()
    private val userCollections = db.collection("users")

    fun addUser(user: User?){
        user?.let {
            GlobalScope.launch( Dispatchers.IO){
                userCollections.document(user.uid).set(it)
                Log.d("working","working")
            }
        }
    }

    fun getUserById(uId:String):Task<DocumentSnapshot>{
        return userCollections.document(uId).get()
    }
}