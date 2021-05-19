package c.m.koskosanadmin.ui.transaction.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import c.m.koskosanadmin.data.model.OrderResponse
import c.m.koskosanadmin.data.repository.FirebaseRepository
import c.m.koskosanadmin.vo.ResponseState
import kotlin.properties.Delegates

class DetailTransactionViewModel(private val firebaseRepository: FirebaseRepository) : ViewModel() {
    // get detail order by order transaction uid
    private lateinit var _orderUIDInput: String

    fun setOrderUID(orderUID: String) {
        this._orderUIDInput = orderUID
    }

    fun getOrderDetailByOrderUID(): LiveData<ResponseState<OrderResponse>> =
        firebaseRepository.readOrderDetailByOrderUid(_orderUIDInput)

    // get order status value
    private var _orderStatusInput by Delegates.notNull<Int>()

    fun setOrderStatus(orderStatus: Int) {
        this._orderStatusInput = orderStatus
    }

    fun updateOrderStatusByOrderUID(): LiveData<ResponseState<OrderResponse>> =
        firebaseRepository.updateOrderStatusByOrderUID(_orderUIDInput, _orderStatusInput)
}