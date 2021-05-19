package c.m.koskosanadmin.ui.transaction.detail

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import c.m.koskosanadmin.R
import c.m.koskosanadmin.databinding.ActivityDetailTransactionBinding
import c.m.koskosanadmin.databinding.BottomSheetOrderStatusSelectBinding
import c.m.koskosanadmin.util.Constants.ACCEPT_STATUS
import c.m.koskosanadmin.util.Constants.CANCEL_STATUS
import c.m.koskosanadmin.util.Constants.SURVEY_STATUS
import c.m.koskosanadmin.util.Constants.UID
import c.m.koskosanadmin.util.Constants.WAITING_STATUS
import c.m.koskosanadmin.util.ViewUtilities.gone
import c.m.koskosanadmin.util.ViewUtilities.invisible
import c.m.koskosanadmin.util.ViewUtilities.snackBarBasicShort
import c.m.koskosanadmin.util.ViewUtilities.visible
import c.m.koskosanadmin.vo.ResponseState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailTransactionActivity : AppCompatActivity() {

    private val detailTransactionViewModel: DetailTransactionViewModel by viewModel()
    private lateinit var detailTransactionBinding: ActivityDetailTransactionBinding
    private lateinit var bottomSheetOrderStatusSelectBinding: BottomSheetOrderStatusSelectBinding
    private lateinit var layout: View
    private var uid: String? = ""
    private lateinit var bottomSheet: View
    private lateinit var sheetBehavior: BottomSheetBehavior<View>
    private var sheetDialog: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize view binding
        detailTransactionBinding = ActivityDetailTransactionBinding.inflate(layoutInflater)
        setContentView(detailTransactionBinding.root)

        // for handle view widget utilities
        layout = detailTransactionBinding.root

        // get parsing transaction uid
        val intent = intent
        uid = intent.getStringExtra(UID)

        // AppBar / ActionBar title setup
        setSupportActionBar(detailTransactionBinding.toolbarDetailTransaction)
        supportActionBar?.apply {
            title = getString(R.string.detail_transaction_title)
            setDisplayHomeAsUpEnabled(true)
        }

        // Bottom Sheet Initialize
        bottomSheetInitialize()

        // initialize get detail transaction
        initializeGetDetailTransactionByOrderUid()

        // initialize swipe to refresh data
        detailTransactionBinding.detailTransactionSwipeRefreshView.setOnRefreshListener {
            detailTransactionBinding.detailTransactionSwipeRefreshView.isRefreshing = false

            // get data
            initializeGetDetailTransactionByOrderUid()
        }
    }

    // Initialize setup of bottom sheet navigation
    private fun bottomSheetInitialize() {
        // initialize bottom sheet frame
        bottomSheet = detailTransactionBinding.bottomSheet
        sheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // initialize view binding for bottom sheet
        bottomSheetOrderStatusSelectBinding =
            BottomSheetOrderStatusSelectBinding.inflate(layoutInflater)
        val viewBottomSheet = bottomSheetOrderStatusSelectBinding.root

        sheetDialog = BottomSheetDialog(this)
        sheetDialog?.setContentView(viewBottomSheet)

        // change order status
        detailTransactionBinding.btnChangeStatus.setOnClickListener {
            sheetDialog?.show()

            if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    // radio button function send order status to firebase database
    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when (view.id) {
                R.id.radio_waiting_status ->
                    if (checked) {
                        detailTransactionViewModel.setOrderStatus(WAITING_STATUS)
                        successUpdateOrderStatusState(getString(R.string.waiting_status))
                    }
                R.id.radio_survey_status ->
                    if (checked) {
                        detailTransactionViewModel.setOrderStatus(SURVEY_STATUS)
                        successUpdateOrderStatusState(getString(R.string.survey_status))
                    }
                R.id.radio_accept_status ->
                    if (checked) {
                        detailTransactionViewModel.setOrderStatus(ACCEPT_STATUS)
                        successUpdateOrderStatusState(getString(R.string.accept_status))
                    }
                R.id.radio_cancel_status ->
                    if (checked) {
                        detailTransactionViewModel.setOrderStatus(CANCEL_STATUS)
                        successUpdateOrderStatusState(getString(R.string.cancel_status))
                    }
            }
        }
    }

    private fun successUpdateOrderStatusState(status: String) {
        detailTransactionViewModel.updateOrderStatusByOrderUID().observe(this, { response ->
            if (response != null) when (response) {
                is ResponseState.Error -> showErrorStateView() // error state
                is ResponseState.Loading -> showLoadingStateView() // loading state
                is ResponseState.Success -> {
                    // success state
                    showSuccessStateView()

                    // reload detail transaction data
                    initializeGetDetailTransactionByOrderUid()
                    layout.snackBarBasicShort(status)
                }
            }
        })
    }

    // initialize get detail transaction
    private fun initializeGetDetailTransactionByOrderUid() {
        detailTransactionViewModel.setOrderUID(uid.toString())
        detailTransactionViewModel.getOrderDetailByOrderUID()
            .observe(this, { response ->
                if (response != null) when (response) {
                    is ResponseState.Error -> showErrorStateView() // error state
                    is ResponseState.Loading -> showLoadingStateView() // loading state
                    is ResponseState.Success -> {
                        // success state
                        showSuccessStateView()

                        // show data to view
                        val data = response.data

                        with(detailTransactionBinding) {
                            tvOrderCreatedAt.text = data?.orderCreated
                            tvOrderStatus.text = when (data?.orderStatus) {
                                WAITING_STATUS -> getString(R.string.waiting_status)
                                SURVEY_STATUS -> getString(R.string.survey_status)
                                ACCEPT_STATUS -> getString(R.string.accept_status)
                                CANCEL_STATUS -> getString(R.string.cancel_status)
                                else -> getString(R.string.data_error_null)
                            }
                            tvSurveyDate.text = data?.surveySchedule
                            tvRentStartDate.text = data?.rentStart
                            tvRentStopDate.text = data?.rentStop
                            tvNameUserTransaction.text = data?.userName
                            tvAddressUserTransaction.text = data?.userAddress
                            tvPhoneUserTransaction.text = data?.userPhone
                            tvNameLocationTransaction.text = data?.nameLocation
                            tvAddressLocationTransaction.text = data?.addressLocation
                            tvPhoneLocationTransaction.text = data?.phoneLocation

                            // default radio button checked
                            when (data?.orderStatus) {
                                WAITING_STATUS -> bottomSheetOrderStatusSelectBinding.radioWaitingStatus.isChecked =
                                    true
                                SURVEY_STATUS -> bottomSheetOrderStatusSelectBinding.radioSurveyStatus.isChecked =
                                    true
                                ACCEPT_STATUS -> bottomSheetOrderStatusSelectBinding.radioAcceptStatus.isChecked =
                                    true
                                CANCEL_STATUS -> bottomSheetOrderStatusSelectBinding.radioCancelStatus.isChecked =
                                    true
                            }
                        }
                    }
                }
            })
    }

    // handle success state of view
    private fun showSuccessStateView() {
        detailTransactionBinding.animLoading.gone()
        detailTransactionBinding.animError.gone()
        detailTransactionBinding.detailTransactionLayout.visible()
    }

    // handle error state of view
    private fun showErrorStateView() {
        detailTransactionBinding.animError.visible()
        detailTransactionBinding.animLoading.gone()
        detailTransactionBinding.detailTransactionLayout.invisible()
    }

    // handle loading state of view
    private fun showLoadingStateView() {
        detailTransactionBinding.detailTransactionLayout.invisible()
        detailTransactionBinding.animLoading.visible()
        detailTransactionBinding.animError.gone()
    }

    // activate back button arrow
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}