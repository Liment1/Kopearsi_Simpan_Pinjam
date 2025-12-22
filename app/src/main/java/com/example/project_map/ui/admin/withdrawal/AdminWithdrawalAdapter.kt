package com.example.project_map.ui.admin.withdrawal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project_map.R
import com.example.project_map.data.model.WithdrawalRequest
import com.example.project_map.databinding.ItemAdminWithdrawalBinding
import java.text.NumberFormat
import java.util.Locale

class AdminWithdrawalAdapter(
    private val onApprove: (WithdrawalRequest) -> Unit,
    private val onReject: (WithdrawalRequest) -> Unit
) : RecyclerView.Adapter<AdminWithdrawalAdapter.VH>() {

    private var list = listOf<WithdrawalRequest>()

    fun submitList(newList: List<WithdrawalRequest>) {
        list = newList
        notifyDataSetChanged()
    }

    class VH(val b: ItemAdminWithdrawalBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAdminWithdrawalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0

        // 1. Name
        holder.b.tvUserName.text = if (item.userName.isNotEmpty()) item.userName else "Member #${item.userId.take(5)}"

        // 2. Amount
        holder.b.tvAmount.text = format.format(item.amount)

        // 3. Bank Details (NEW - Essential for Admin)
        // If you don't have a TextView for this in item_admin_withdrawal.xml,
        // you should add one. For now, I'll append it to the name or assume you add a tvBankDetails
        // holder.b.tvBankDetails.text = "${item.bankName} - ${item.accountNumber}"

        // 4. Profile Picture
        if (!item.userAvatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.userAvatarUrl)
                .placeholder(R.drawable.ic_default_profile_picture) // Add a default drawable
                .error(R.drawable.ic_default_profile_picture)
                .circleCrop()
                .into(holder.b.ivProfile)
        } else {
            holder.b.ivProfile.setImageResource(R.drawable.ic_default_profile_picture)
        }

        holder.b.btnApprove.setOnClickListener { onApprove(item) }
        holder.b.btnReject.setOnClickListener { onReject(item) }
    }

    override fun getItemCount() = list.size
}