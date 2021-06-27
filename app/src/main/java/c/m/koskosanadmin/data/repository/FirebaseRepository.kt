package c.m.koskosanadmin.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import c.m.koskosanadmin.data.model.LocationResponse
import c.m.koskosanadmin.data.model.OrderResponse
import c.m.koskosanadmin.data.model.UserResponse
import c.m.koskosanadmin.vo.ResponseState
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
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
        userUID: String,
        name: String,
        imageProfilePath: Uri?,
        phoneNumber: String,
        address: String,
        email: String,
    ): LiveData<ResponseState<Double>> {
        val progressUploadingData: MutableLiveData<ResponseState<Double>> = MutableLiveData()
        val imageReference: StorageReference = userProfileStorageReference.child("$userUID/profile")
        val progressDone = 100.0

        if (imageProfilePath != null) {
            imageReference.putFile(imageProfilePath)
                .addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                        val mapUserResponseData = UserResponse(
                            userUID,
                            name,
                            (uri?.toString() ?: "-"),
                            phoneNumber,
                            address,
                            email
                        )

                        userProfileCollection.document(userUID).set(mapUserResponseData)
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
        userUID: String,
        name: String,
        address: String,
        email: String
    ): LiveData<ResponseState<Double>> {
        val progressUploadingData: MutableLiveData<ResponseState<Double>> = MutableLiveData()
        val progressDone = 100.0
        val mapUserProfileData = mapOf(
            "uid" to userUID,
            "name" to name,
            "address" to address,
            "email" to email
        )

        // Loading State
        progressUploadingData.value = ResponseState.Loading(0.0)

        userProfileCollection.document(userUID)
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

                if (!users.isNullOrEmpty()) {
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

    // get all locations by owner uid
    fun readLocationByOwnerUid(ownerUID: String): LiveData<ResponseState<List<LocationResponse>>> {
        val locations: MutableLiveData<ResponseState<List<LocationResponse>>> = MutableLiveData()

        // loading state
        locations.value = ResponseState.Loading(null)

        locationCollection.whereEqualTo("ownerUID", ownerUID).get()
            .addOnSuccessListener { snapshot ->
                val locationSnapshot = snapshot?.toObjects(LocationResponse::class.java)

                if (!locationSnapshot.isNullOrEmpty()) {
                    // success state
                    locations.value = ResponseState.Success(locationSnapshot)
                } else {
                    // error state
                    locations.value = ResponseState.Error("No Data", null)
                }
            }
            .addOnFailureListener { exception ->
                // error state
                locations.value = ResponseState.Error(exception.localizedMessage, null)
            }

        return locations
    }

    // post new location data to firestore and location photo to storage
    fun createNewLocationData(
        locationName: String,
        locationAddress: String,
        locationGooglePlace: String,
        locationOwnerUID: String,
        locationPhone: String,
        locationType: String,
        locationPhoto: Uri?,
        locationCoordinate: GeoPoint?
    ): LiveData<ResponseState<Double>> {
        val progressUploadingData: MutableLiveData<ResponseState<Double>> = MutableLiveData()
        val locationUID: String = locationCollection.document().id
        val imageReference: StorageReference = userProfileStorageReference.child("$locationOwnerUID/$locationUID/data")
        val progressDone = 100.0

        if (locationPhoto != null) {
            imageReference.putFile(locationPhoto)
                .addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                        val listOfUri: List<String> = listOf((uri?.toString() ?: "-"))
                        val mapLocationResponseData = LocationResponse(
                            locationUID,
                            locationName,
                            locationAddress,
                            locationPhone,
                            locationCoordinate,
                            listOfUri,
                            locationType,
                            locationGooglePlace,
                            locationOwnerUID,
                        )

                        locationCollection.document(locationUID).set(mapLocationResponseData)
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
        }

        return progressUploadingData
    }

    // get location detail by location uid
    fun readLocationDetailByLocationUid(locationUID: String): LiveData<ResponseState<LocationResponse>> {
        val location: MutableLiveData<ResponseState<LocationResponse>> = MutableLiveData()

        // show loading state
        location.value = ResponseState.Loading(null)

        locationCollection.whereEqualTo("uid", locationUID).get()
            .addOnSuccessListener { snapshot ->
                val locationSnapshot = snapshot?.toObjects(LocationResponse::class.java)

                if (!locationSnapshot.isNullOrEmpty()) // error state
                {
                    locationSnapshot.forEach { data ->
                        // success state
                        location.value = ResponseState.Success(data)
                    }
                } else {
                    // error state
                    location.value = ResponseState.Error("No Data", null)
                }
            }.addOnFailureListener { exception ->
                // error state
                location.value = ResponseState.Error(exception.localizedMessage, null)
            }

        return location
    }

    // TODO: update location data by location uid

    // get user order by location owner uid
    fun readOrderByLocationOwnerUid(locationOwnerUID: String): LiveData<ResponseState<List<OrderResponse>>> {
        val orders: MutableLiveData<ResponseState<List<OrderResponse>>> = MutableLiveData()

        // show loading state
        orders.value = ResponseState.Loading(null)

        orderCollection.whereEqualTo("locationOwnerUID", locationOwnerUID)
            .orderBy("orderCreated", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { snapshot ->
                val orderSnapshot = snapshot?.toObjects(OrderResponse::class.java)

                if (!orderSnapshot.isNullOrEmpty()) {
                    // success state
                    orders.value = ResponseState.Success(orderSnapshot)
                } else {
                    // error state
                    orders.value = ResponseState.Error("No Data", null)
                }
            }
            .addOnFailureListener { exception ->
                // error state
                orders.value = ResponseState.Error(exception.localizedMessage, null)
            }

        return orders
    }

    // get user order details by order uid
    fun readOrderDetailByOrderUid(orderUID: String): LiveData<ResponseState<OrderResponse>> {
        val order: MutableLiveData<ResponseState<OrderResponse>> = MutableLiveData()

        // show loading state
        order.value = ResponseState.Loading(null)

        orderCollection.whereEqualTo("uid", orderUID).get()
            .addOnSuccessListener { snapshot ->
                val orderSnapshot = snapshot?.toObjects(OrderResponse::class.java)

                // success state
                if (!orderSnapshot.isNullOrEmpty()) {
                    orderSnapshot.forEach { data ->
                        // success state
                        order.value = ResponseState.Success(data)
                    }
                } else {
                    // error state
                    order.value = ResponseState.Error("No data", null)
                }
            }
            .addOnFailureListener { exception ->
                // error state
                order.value = ResponseState.Error(exception.localizedMessage, null)
            }

        return order
    }

    fun updateOrderStatusByOrderUID(
        orderUID: String,
        orderStatus: Int
    ): LiveData<ResponseState<OrderResponse>> {
        val order: MutableLiveData<ResponseState<OrderResponse>> = MutableLiveData()

        // show loading state
        order.value = ResponseState.Loading(null)

        orderCollection.document(orderUID).update("orderStatus", orderStatus)
            .addOnSuccessListener {
                order.value = ResponseState.Success(null)
            }
            .addOnFailureListener { exception ->
                // error state
                order.value = ResponseState.Error(exception.localizedMessage, null)
            }
        return order
    }
}