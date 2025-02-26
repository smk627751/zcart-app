package com.smk627751.zcart.Repository

import android.app.Activity
import android.net.Uri
import android.util.Log
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.storage
import com.smk627751.zcart.Utility
import com.smk627751.zcart.dto.Customer
import com.smk627751.zcart.dto.Notification
import com.smk627751.zcart.dto.Order
import com.smk627751.zcart.dto.OrderNotification
import com.smk627751.zcart.dto.Product
import com.smk627751.zcart.dto.RequestBody
import com.smk627751.zcart.dto.ReviewNotification
import com.smk627751.zcart.dto.User
import com.smk627751.zcart.dto.Vendor
import com.smk627751.zcart.dto.specificCategories
import com.smk627751.zcart.dto.genericCategories
import com.smk627751.zcart.sharedpreferences.Session
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


object Repository {
    private lateinit var userSession: Session
    lateinit var currentUserId : String
    private set
    var user: User? = null
    private set
    private val auth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    const val IMAGE_PATH = "images/"
    val genericCategory = genericCategories
    val category = specificCategories
    var resendToken : ForceResendingToken? = null
    fun setSession(session: Session)
    {
        userSession = session
    }
    fun isLoggedIn() : Boolean
    {
        val id = userSession.getSession()
        if (!id.isNullOrEmpty())
        {
            currentUserId = id
            getFCMToken(id)
            return true
        }
        return false
    }
    private fun getFCMToken(id: String)
    {
        Firebase.messaging.token.addOnCompleteListener {
            if (it.isSuccessful)
            {
                Log.i("uuid", "FCM token: ${it.result}")
                when(user)
                {
                    is Vendor -> {
                        val vendor = user as Vendor
                        vendor.fcmToken = it.result
                        updateUserData(id,vendor) {}
                    }
                    is Customer -> {
                        val customer = user as Customer
                        customer.fcmToken = it.result
                        updateUserData(id,customer) {}
                    }
                }
            }
            else Log.e("uuid", "Error getting FCM token: ${it.exception}")
        }
    }
    fun signUp(email : String,password : String, success : () -> Unit, failure : (e : Exception) -> Unit)
    {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                currentUserId = it.user?.uid ?: ""
                sendVerificationEmail(it.user!!)
                success()
            }
            .addOnFailureListener {
                Log.e("uuid", "Error signing up: ${it.message}")
                if (it.message?.contains("network") == true)
                {
                    failure(Exception("No Internet connection"))
                }
                else failure(it)
            }
    }
    private fun sendVerificationEmail(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("SendEmail", "Verification email sent to ${user.email}")
                } else {
                    Log.e("SendEmail", "Failed to send verification email: ${task.exception?.message}")
                }
            }
    }
    fun signIn(email: String,password: String,  success : (user : String) -> Unit, failure : (e : Exception) -> Unit)
    {
        auth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                it.user?.let { user ->
                    if (user.isEmailVerified)
                    {
                        success(user.uid)
                        currentUserId = user.uid
                        getFCMToken(user.uid)
                        userSession.saveSession(currentUserId)
                    }
                    else failure(Exception("Email not verified"))
                }
            }
            .addOnFailureListener {
                Log.e("uuid", "Error signing in: ${it.message}")
                if (it.message?.contains("network") == true)
                {
                    failure(Exception("No Internet connection"))
                }
                else failure(Exception("Invalid email or password"))
            }
    }
    fun addUserToDb(user : User)
    {
        db.collection("users")
            .document(currentUserId)
            .set(user)
    }
    fun getUserData(callBack : (user : User?) -> Unit)
    {
        if (currentUserId.isNotEmpty() && user != null)
        {
            callBack(user)
        }
        else
        {
            db.collection("users")
                .document(currentUserId)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        getFCMToken(it.result.id)
                        if (it.result["accountType"] == "Vendor")
                        {
                            user = it.result.toObject(Vendor::class.java)!!
                        }
                        else if (it.result["accountType"] == "Customer")
                        {
                            user = it.result.toObject(Customer::class.java)!!
                        }
                        else user = null
                        callBack(user)
                    }
                }
        }
    }
    fun getUserDataById(userId : String,callBack : (user : User?) -> Unit)
    {
        var user : User?
        db.collection("users")
            .document(userId)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful)
                {
                    if (it.result["accountType"] == "Vendor")
                    {
                        user = it.result.toObject(Vendor::class.java)!!
                    }
                    else if (it.result["accountType"] == "Customer")
                    {
                        user = it.result.toObject(Customer::class.java)!!
                    }
                    else user = null
                    callBack(user)
                }
            }
    }
    fun updateUserData(user : User, callBack: () -> Unit)
    {
        db.collection("users")
            .document(currentUserId)
            .update(
                when(user)
                {
                    is Vendor -> mapOf(
                        "image" to user.image,
                        "name" to user.name,
                        "email" to user.email,
                        "phone" to user.phone,
                        "address" to user.address,
                        "zipcode" to user.zipcode,
                        "products" to user.products,
                        "orders" to user.orders
                    )
                    is Customer -> mapOf(
                        "image" to user.image,
                        "name" to user.name,
                        "email" to user.email,
                        "phone" to user.phone,
                        "address" to user.address,
                        "zipcode" to user.zipcode,
                        "cartItems" to user.cartItems,
                        "myOrders" to user.myOrders
                    )

                    else -> mapOf()
                }
            )
            .addOnCompleteListener {
                if (it.isSuccessful)
                {
                    Log.i("uuid", "User data updated successfully $user")
                    this.user.apply {
                        this?.image = user.image
                        this?.name = user.name
                        this?.email = user.email
                        this?.phone = user.phone
                        this?.address = user.address
                        this?.zipcode = user.zipcode
                    }
                    callBack()
                }
                else
                {
                    Log.e("uuid", "Error updating user data: ${it.exception}")
                }
            }
    }
    fun updateUserData(id : String,user : User, callBack: () -> Unit)
    {
        db.collection("users")
            .document(id)
            .set(user)
            .addOnCompleteListener {
                if (it.isSuccessful)
                {
                    Log.i("uuid", "User data updated successfully $user")
                    callBack()
                }
                else
                {
                    Log.e("uuid", "Error updating user data: ${it.exception}")
                }
            }
    }
    fun isVendor(callBack: (result : Boolean) -> Unit)
    {
        getUserData {
            callBack(it is Vendor)
        }
    }
    fun isOwnProduct(productId : String, callBack: (result : Boolean) -> Unit)
    {
        getUserData {
            if (it is Vendor) {
                callBack(it.products.contains(productId))
            }
        }
    }
    fun addProductToDb(product : Product, callBack: () -> Unit)
    {
        Log.i("uuid", "Product data: $product")
        db.collection("products")
            .document(product.id)
            .set(product)
            .addOnCompleteListener {
                if (it.isSuccessful)
                {
                    when(user)
                    {
                        is Vendor -> {
                                val vendor = user as Vendor
                                vendor.products.add(product.id)
//                                updateUserData(vendor) {}
                                addUserToDb(user as Vendor)
                            }
                        is Customer -> {

                        }
                    }
                    callBack()
                }
                else Log.e("uuid", "Error adding product: ${it.exception}")
            }
    }
    fun updateProductToDb(product : Product, callBack: () -> Unit)
    {
        db.collection("products")
            .document(product.id)
            .update(
                mapOf(
                    "name" to product.name,
                    "price" to product.price,
                    "description" to product.description,
                    "category" to product.category,
                    "reviews" to product.reviews,
                    "image" to product.image
                )
            )
            .addOnCompleteListener {
                if (it.isSuccessful)
                {
                    when(user)
                    {
                        is Vendor -> {
                            val vendor = user as Vendor
                            vendor.products.add(product.id)
                            updateUserData(vendor) {}
                        }
                        is Customer -> {

                        }
                    }
                    callBack()
                }
                else Log.e("uuid", "Error updating product: ${it.exception}")
            }
    }
    fun uploadImage(path : String, uri : Uri?, callBack : (url : String) -> Unit)
    {
        if (uri == null)
        {
            callBack("")
            return
        }
        val imageRef = storageRef.child(path)
        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener {
                    callBack(it.toString())
                }
            }
    }
    fun getProductsName(callBack: (List<String>) -> Unit)
    {
        db.collection("products")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val products = it.result.toObjects(Product::class.java)
                    val names = products.map { it.name }
                    callBack(names)
                }
            }
    }
    fun listenProducts(
        searchQuery: String,
        type: String = "all",
        callBack: (itemsize : Int) -> Unit,
    ): FirestoreRecyclerOptions<Product>
    {
        val query = if (searchQuery.isNotEmpty())
                    {
                        when (type) {
                            "name" -> db.collection("products")
                                .whereGreaterThanOrEqualTo("searchName", searchQuery.trim().lowercase())
                                .whereLessThanOrEqualTo("searchName", searchQuery.trim().lowercase() + "\uf8ff")
                            "category" -> {
                                var query = db.collection("products")
                                    .orderBy("name")
                                val categories = searchQuery.split(",").map { it.trim() }
                                for (category in categories) {
                                    query = query.whereArrayContains("category", category)
                                }
                                query
                            }
                            else -> db.collection("products")
                        }
                    }
                    else
                    {
                        if (type == "myProducts")
                        {
                            if (user is Vendor) {
                                val vendor = user as Vendor
                                if (vendor.products.isNotEmpty())
                                {
                                    db.collection("products")
                                        .whereEqualTo("vendorId", currentUserId)
                                }
                                else
                                {
                                    return FirestoreRecyclerOptions.Builder<Product>().build()
                                }

                            } else {
                                db.collection("products")
                            }
                        }
                        else db.collection("products")
                    }.orderBy("price",Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<Product>()
            .setQuery(query, Product::class.java)
            .build()
        query.get().addOnCompleteListener {
            if (it.isSuccessful)
            {
                callBack(it.result.size())
            }
        }
        return options
    }
    fun addToCart(productId : String, callBack : () -> Unit)
    {
        getUserData {
            if (it is Customer)
            {
                it.cartItems.add(productId)
                updateUserData(it) {
                    callBack()
                }
            }
        }
    }
    fun getCartItems(callBack : (items : List<Product>) -> Unit)
    {
        getUserData {
            if (it is Customer)
            {
                val items = it.cartItems
                if (items.isEmpty()) {
                    callBack(listOf())
                    return@getUserData
                }
                db.collection("products")
                    .whereIn("id",items)
                    .get()
                    .addOnSuccessListener {
                        val products = it.toObjects(Product::class.java)
                        callBack(products)
                    }
                    .addOnFailureListener {
                        Log.e("uuid", "Error getting cart items: ${it.message}")
                    }
            }
        }
    }
    fun getProducts(ids : List<String>,callBack : (products : List<Product>) -> Unit)
    {
        db.collection("products")
            .whereIn("id",ids)
            .get()
            .addOnSuccessListener {
                val products = it.toObjects(Product::class.java)
                callBack(products)
            }
    }
    fun deleteProduct(id: String, callback: () -> Unit) {
        getUserData {
            if (it is Vendor) {
                it.products.remove(id)
                updateUserData(it) {
                    db.collection("products")
                        .document(id)
                        .delete()
                        .addOnSuccessListener {
                            callback()
                        }
                }
            }
        }
    }
    fun placeOrder(order: Order, products : Array<Product>, callBack: () -> Unit)
    {
        db.collection("orders")
            .document(order.id)
            .set(order)
            .addOnSuccessListener {
                if (user is Customer)
                {
                    val customer = user as Customer
                    customer.myOrders.add(order.id)
                    products.forEach {
                        getUserDataById(it.vendorId){ user ->
                            val vendor = user as Vendor
                            vendor.orders.add(order.id)
                            updateUserData(it.vendorId,vendor) {
                                customer.cartItems.clear()
                                updateUserData(customer) {
                                    callBack()
                                }
                            }
                        }
                    }
                }
            }
    }
    fun getOrders(searchQuery: String,callBack : (itemsize : Int,options: FirestoreRecyclerOptions<Order>) -> Unit)
    {
        when(searchQuery)
        {
            "all" ->{
                getUserData {
                    if (it is Customer)
                    {
                        val orders = it.myOrders
                        val query = db.collection("orders")
                                .whereEqualTo("customerId", currentUserId)
                                .whereNotEqualTo("status","Order cancelled")
                                .orderBy("timestamp",Query.Direction.DESCENDING)
                        val options = FirestoreRecyclerOptions.Builder<Order>()
                            .setQuery(query, Order::class.java)
                            .build()
                        query.get().addOnCompleteListener {
                            if (it.isSuccessful)
                            {
                                callBack(it.result.size(),options)
                            }
                        }
                    }
                    else if (it is Vendor)
                    {
                        val query = db.collection("orders")
                            .whereEqualTo("vendorIds", currentUserId)
                            .orderBy("timestamp",Query.Direction.DESCENDING)
                        val options = FirestoreRecyclerOptions.Builder<Order>()
                            .setQuery(query, Order::class.java)
                            .build()
                        query.get().addOnCompleteListener {
                            if (it.isSuccessful)
                            {
                                callBack(it.result.size(),options)
                            }
                        }
                    }
                }
            }
            "Order placed","Order shipped","Out for delivery","Order delivered","Order cancelled" ->{
                val query = db.collection("orders")
                    .whereEqualTo("vendorIds", currentUserId)
                    .orderBy("timestamp",Query.Direction.DESCENDING)
                    .whereEqualTo("status", searchQuery)
                val options = FirestoreRecyclerOptions.Builder<Order>()
                    .setQuery(query, Order::class.java)
                    .build()
                query.get().addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        callBack(it.result.size(),options)
                    }
                }
            }
            else -> {
                val query = db.collection("orders")
                    .whereEqualTo("vendorIds", currentUserId)
                    .orderBy("timestamp",Query.Direction.DESCENDING)
                    .whereEqualTo("id", searchQuery)
                val options = FirestoreRecyclerOptions.Builder<Order>()
                    .setQuery(query, Order::class.java)
                    .build()
                query.get().addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        callBack(it.result.size(),options)
                    }
                }
            }
        }
    }
    fun getOrderById(orderId : String, callBack : (order : Order?) -> Unit)
    {
        db.collection("orders")
            .document(orderId)
            .get()
            .addOnSuccessListener {
                val order = it.toObject(Order::class.java)
                callBack(order)
            }
            .addOnFailureListener {
                callBack(null)
            }
    }
    fun updateOrderStatus(orderId : String, status : String, callBack: () -> Unit)
    {
        db.collection("orders")
            .document(orderId)
            .update("status",status)
            .addOnSuccessListener {
                callBack()
            }
    }
    fun sendNotification(token : String, status : String)
    {
        CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler{_,e ->
            Log.e("uuid", "Error sending notification: ${e.message}")
        }).launch {
            RetrofitInstance.sendNotification(RequestBody(status, token))
        }
    }
    fun addNotification(notification: Notification)
    {
        db.collection("notifications")
            .document(notification.id)
            .set(notification)
    }
    fun getNotifications(callBack: (notifications : MutableList<Notification>) -> Unit)
    {
        val notifications = mutableListOf<Notification>()
        db.collection("notifications")
            .whereEqualTo("vendorId",currentUserId)
            .orderBy("timestamp",Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null)
                {
                    Log.e("uuid", "Error getting notifications: ${error.message}")
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    for (doc in value.documentChanges) {
                        val type = doc.document.getString("type")
                        val notification: Notification? = when (type) {
                            "Order" -> doc.document.toObject(OrderNotification::class.java)
                            "Review" -> doc.document.toObject(ReviewNotification::class.java)
                            else -> null
                        }
                        notification?.let {
                            when(doc.type)
                            {
                                DocumentChange.Type.ADDED -> notifications.add(0,it)
                                DocumentChange.Type.MODIFIED -> notifications.find {
                                    it.id == notification.id
                                }?.let {
                                    notifications[notifications.indexOf(it)] = notification
                                }
                                DocumentChange.Type.REMOVED -> notifications.remove(it)
                            }
                        }
                    }
                    callBack(notifications)
                }
            }
    }
    fun updateNotification(notification: Notification)
    {
        db.collection("notifications")
            .document(notification.id)
            .set(notification)
    }
    fun sendOTP(phone : String, activity: Activity,success: (otp : String) -> Unit,failure: (e: Exception) -> Unit)
    {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setActivity(activity)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(
                    verificationId: String,
                    forceResendingToken: ForceResendingToken
                ) {
                    success(verificationId)
                    resendToken = forceResendingToken
                }

                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(e: FirebaseException) {
                    failure(e)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    fun verifyOTP(verificationId: String, otp: String, success: () -> Unit, failure: (e: Exception) -> Unit)
    {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                success()
            }
            .addOnFailureListener {
                failure(it)
                Log.e("uuid", "Error verifying OTP: ${it.message}")
            }
    }
    fun resendOTP(
        phone: String,
        activity: Activity,
        success: (otp: String) -> Unit,
        failure: (e: Exception) -> Unit
    ) {
        if (resendToken != null) {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setActivity(activity)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                    override fun onCodeSent(
                        verificationId: String,
                        forceResendingToken: ForceResendingToken
                    ) {
                        resendToken = forceResendingToken
                        success(verificationId)
                    }

                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        failure(e)
                    }
                })
                .setForceResendingToken(resendToken!!) // Use the saved token to force resend
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            failure(Exception("Resend token is null"))
        }
    }
    fun logout()
    {
        Firebase.messaging.deleteToken().addOnCompleteListener {
            if (it.isSuccessful)
            {
                if (user != null)
                {
                    user?.fcmToken  = ""
                    updateUserData(user!!) {}
                }
            }
            else Log.e("uuid", "Error deleting FCM token: ${it.exception}")
        }
        user = null
        userSession.saveSession("")
    }

    fun forgotPassword(email: String, function: () -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful)
            {
                function()
            }
        }
    }

    fun getProductById(id: String, function: (product: Product?) -> Unit) {
        db.collection("products")
            .document(id)
            .get()
            .addOnSuccessListener {
                function(it.toObject(Product::class.java))
            }
    }

    fun removeFromCart(product: Product, function: () -> Unit) {
        getUserData {
            if (it is Customer) {
                it.cartItems.remove(product.id)
                updateUserData(it) {
                    function()
                }
            }
        }
    }

    fun isPurchased(id: String, callBack: (Boolean) -> Unit) {
        val orders = (user as Customer).myOrders
        orders.forEach { getOrderById(it){ order ->
            if (order != null) {
                val product = order.products.find { p -> p == id }
                if (product != null)
                {
                    callBack(order.status == "Order delivered")
                }
                return@getOrderById
            }
        } }
    }
}