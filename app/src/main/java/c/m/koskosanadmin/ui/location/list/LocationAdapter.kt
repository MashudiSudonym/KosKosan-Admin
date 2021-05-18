package c.m.koskosanadmin.ui.location.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import c.m.koskosanadmin.data.model.LocationResponse
import c.m.koskosanadmin.databinding.ItemLocationBinding

class LocationAdapter(private val onClick: (LocationResponse) -> Unit) :
    ListAdapter<LocationResponse, LocationAdapter.LocationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val contents = getItem(position)
        holder.bind(contents)
    }

    class LocationViewHolder(itemView: ItemLocationBinding, onClick: (LocationResponse) -> Unit) :
        RecyclerView.ViewHolder(itemView.root) {
        private val locationName = itemView.tvLocationName
        private val locationAddress = itemView.tvLocationAddress
        private var currentLocation: LocationResponse? = null

        init {
            itemView.locationItemLayout.setOnClickListener {
                currentLocation?.let {
                    onClick(it)
                }
            }
        }

        fun bind(locationResponse: LocationResponse) {
            currentLocation = locationResponse

            // add data to widget view
            locationName.text = currentLocation?.name?.capitalize()
            locationAddress.text = currentLocation?.address
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<LocationResponse>() {
        override fun areItemsTheSame(
            oldItem: LocationResponse,
            newItem: LocationResponse
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: LocationResponse,
            newItem: LocationResponse
        ): Boolean = oldItem.uid == newItem.uid

    }
}