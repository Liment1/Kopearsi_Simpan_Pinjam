package com.example.project_map.ui.admin.withdrawal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

        holder.b.tvUserName.text = item.userName
        holder.b.tvAmount.text = format.format(item.amount)

        holder.b.btnApprove.setOnClickListener { onApprove(item) }
        holder.b.btnReject.setOnClickListener { onReject(item) }
    }

    override fun getItemCount() = list.size
}