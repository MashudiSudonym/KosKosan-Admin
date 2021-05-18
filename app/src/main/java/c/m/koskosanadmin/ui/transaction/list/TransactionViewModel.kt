package c.m.koskosanadmin.ui.transaction.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import c.m.koskosanadmin.data.model.OrderResponse
import c.m.koskosanadmin.data.repository.AuthRepository
import c.m.koskosanadmin.data.repository.FirebaseRepository
import c.m.koskosanadmin.vo.ResponseState

class TransactionViewModel(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    // get admin user uid
    private val userUID: LiveData<String> = authRepository.getUserUid()

    // get transaction list by location owner uid / user uid
    fun getTransactionByLocationOwnerUID(): LiveData<ResponseState<List<OrderResponse>>> =
        firebaseRepository.readOrderByLocationOwnerUid(userUID.value.toString())
}