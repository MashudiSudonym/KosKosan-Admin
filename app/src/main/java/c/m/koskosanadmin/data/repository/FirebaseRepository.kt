package c.m.koskosanadmin.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import c.m.koskosanadmin.data.model.OrderResponse
import c.m.koskosanadmin.data.model.UserResponse
import c.m.koskosanadmin.vo.ResponseState
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import timber.log.Timber
import kotlin.math.ceil

class FirebaseRepository {
    // Firebase Storage instance
    private val storage: FirebaseStorage = Firebase.storage
    private val userProfileStorageReference: StorageReference = storage.reference.child("users")

    // Firestore instance
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val userProfileCollection: CollectionReference = firestore.collection("users")
    private val locationCollection: CollectionReference = firestore.collection("locations")
    private val orderCollection: CollectionReference = firestore.collection("orders")

    // Check user data from users collection firestore
    fun checkUserProfileData(userUID: String): LiveData<Boolean> {
        val isUserProfileData: MutableLiveData<Boolean> = MutableLiveData()
        userProfileCollection.whereEqualTo("uid", userUID).limit(1).get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot?.toObjects(UserResponse::class.java)

                // this return to false if users have a data
                isUserProfileData.value = users?.isNotEmpty() == false
            }
            .addOnFailureListener { error ->
                Timber.e("$error")
            }

        return isUserProfileData
    }

    // post user profile data to firestore and user profile image to storage
    fun createUserProfileData(
        uid: String,
        name: String,
        imageProfilePath: Uri?,
        phoneNumber: String,
        address: String,
        email: String,
    ): LiveData<ResponseState<Double>> {
        val progressUploadingData: MutableLiveData<ResponseState<Double>> = MutableLiveData()
        val imageReference: StorageReference = userProfileStorageReference.child("$uid/profile")
        val progressDone = 100.0

        if (imageProfilePath != null) {
            imageReference.putFile(imageProfilePath)
                .addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                        val mapUserResponseData = UserResponse(
                            uid,
                            name,
                            (uri?.toString() ?: "-"),
                            phoneNumber,
                            address,
                            email
                        )

                        userProfileCollection.document(uid).set(mapUserResponseData)
                            .addOnSuccessListener {
                                progressUploadingData.value =
                                    ResponseState.Success(progressDone)
                            }
                            .addOnFailureListener { exception ->
                                progressUploadingData.value =
                                    ResponseState.Error(exception.localizedMessage, null)
                            }
                    }
                }
                .addOnFailureListener {
                    progressUploadingData.value = ResponseState.Error("upload image failed", null)
                }
                .addOnProgressListener { snapshot ->
                    val progressCount: Double =
                        100.0 * snapshot.bytesTransferred / snapshot.totalByteCount
                    progressUploadingData.value = ResponseState.Loading(ceil(progressCount))
                }
        } else {
            progressUploadingData.value = ResponseState.Error("image profile not found", null)
        }

        return progressUploadingData
    }

    // update user profile data
    fun updateUserProfileData(
        uid: String,
        name: String,
        address: String,
        email: String
    ): LiveData<ResponseState<Double>> {
        val progressUploadingData: MutableLiveData<ResponseState<Double>> = MutableLiveData()
        val progressDone = 100.0
        val mapUserProfileData = mapOf(
            "uid" to uid,
            "name" to name,
            "address" to address,
            "email" to email
        )

        // Loading State
        progressUploadingData.value = ResponseState.Loading(0.0)

        userProfileCollection.document(uid)
            .update(mapUserProfileData)
            .addOnSuccessListener {
                progressUploadingData.value = ResponseState.Success(progressDone)
            }
            .addOnFailureListener { exception ->
                progressUploadingData.value = ResponseState.Error(exception.localizedMessage, null)
            }

        return progressUploadingData
    }

    // get user profile data by user uid
    fun readUserProfileData(userUID: String): LiveData<ResponseState<UserResponse>> {
        val userProfileData: MutableLiveData<ResponseState<UserResponse>> = MutableLiveData()

        // Loading State
        userProfileData.value = ResponseState.Loading(null)

        userProfileCollection.whereEqualTo("uid", userUID).limit(1).get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot?.toObjects(UserResponse::class.java)

                if (users != null) {
                    users.forEach { data ->

                        userProfileData.value = ResponseState.Success(data)
                    }
                } else {
                    userProfileData.value = ResponseState.Error("No Data")
                }

            }
            .addOnFailureListener { error ->
                userProfileData.value = ResponseState.Error(error.localizedMessage)
            }

        return userProfileData
    }

    // TODO: get all locations by owner uid

    // TODO: get location detail by location uid

    // get user order by location owner uid
    fun readOrderByLocationOwnerUid(locationOwnerUID: String): LiveData<ResponseState<List<OrderResponse>>> {
        val orders: MutableLiveData<ResponseState<List<OrderResponse>>> = MutableLiveData()

        // show loading state
        orders.value = ResponseState.Loading(null)

        orderCollection.whereEqualTo("locationOwnerUID", locationOwnerUID)
            .orderBy("orderCreated", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { snapshot ->
                val orderSnapshot = snapshot?.toObjects(OrderResponse::class.java)

                // success state
                orders.value = ResponseState.Success(orderSnapshot)
            }
            .addOnFailureListener { exception ->
                // error state
                orders.value = ResponseState.Error(exception.localizedMessage, null)
            }

        return orders
    }

    // get user order details by order uid
    fun readOrderDetailByOrderUid(orderUID: String): LiveData<ResponseState<OrderResponse>> {
        val orders: MutableLiveData<ResponseState<OrderResponse>> = MutableLiveData()

        // show loading state
        orders.value = ResponseState.Loading(null)

        orderCollection.whereEqualTo("uid", orderUID).get()
            .addOnSuccessListener { snapshot ->
                val orderSnapshot = snapshot?.toObjects(OrderResponse::class.java)

                // success state
                if (orderSnapshot != null) {
                    orderSnapshot.forEach { data ->
                        // success state
                        orders.value = ResponseState.Success(data)
                    }
                } else {
                    // error state
                    orders.value = ResponseState.Error("No data", null)
                }
            }
            .addOnFailureListener { exception ->
                // error state
                orders.value = ResponseState.Error(exception.localizedMessage, null)
            }

        return orders
    }

    // TODO : update order status
}