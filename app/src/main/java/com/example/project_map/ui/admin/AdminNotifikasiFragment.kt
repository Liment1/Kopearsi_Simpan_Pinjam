package com.example.project_map.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_map.data.model.Announcement
import com.example.project_map.data.model.ProfitDistributionRecord
import com.example.project_map.databinding.FragmentAdminNotifikasiBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.util.*

class AdminNotifikasiFragment : Fragment() {

    private var _binding: FragmentAdminNotifikasiBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: AnnouncementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminNotifikasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadAnnouncementHistory()

        // 1. Manual Notification
        binding.btnSend.setOnClickListener {
            sendManualNotification()
        }

        // 2. Profit Distribution (SHU)
        binding.btnDistributeProfit.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = AnnouncementAdapter(emptyList())
        binding.rvAnnouncementHistory.layoutManager = LinearLayoutManager(context)
        binding.rvAnnouncementHistory.adapter = adapter
    }

    private fun loadAnnouncementHistory() {
        // Query ordered by 'date' as per your screenshot structure
        db.collection("announcements")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(context, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val list = value?.toObjects(Announcement::class.java) ?: emptyList()
                adapter.updateList(list)
            }
    }

    private fun sendManualNotification() {
        val title = binding.etTitle.text.toString().trim()
        val message = binding.etMessage.text.toString().trim()
        val isUrgent = binding.cbUrgent.isChecked

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(context, "Isi semua bidang", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSend.isEnabled = false

        val announcement = Announcement(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            date = Timestamp.now(), // Field name 'date' matches screenshot
            isUrgent = isUrgent // Field name 'isUrgent' matches screenshot
        )

        db.collection("announcements").document(announcement.id)
            .set(announcement)
            .addOnSuccessListener {
                Toast.makeText(context, "Pengumuman terkirim", Toast.LENGTH_SHORT).show()
                binding.etTitle.text?.clear()
                binding.etMessage.text?.clear()
                binding.cbUrgent.isChecked = false
                binding.btnSend.isEnabled = true
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal mengirim", Toast.LENGTH_SHORT).show()
                binding.btnSend.isEnabled = true
            }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Pembagian SHU")
            .setMessage("Anda yakin ingin membagikan 90% dari profit bulan ini ke semua anggota?")
            .setPositiveButton("Ya, Bagikan") { _, _ ->
                executeProfitDistribution()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun executeProfitDistribution() {
        binding.btnDistributeProfit.isEnabled = false
        Toast.makeText(context, "Memproses pembagian...", Toast.LENGTH_LONG).show()

        // Step A: Get latest Financial Report using 'generatedAt'
        db.collection("financial_reports")
            .orderBy("generatedAt", Query.Direction.DESCENDING) // Changed from 'timestamp' to 'generatedAt'
            .limit(1)
            .get()
            .addOnSuccessListener { reportSnapshot ->
                if (reportSnapshot.isEmpty) {
                    Toast.makeText(context, "Tidak ada laporan keuangan ditemukan", Toast.LENGTH_SHORT).show()
                    binding.btnDistributeProfit.isEnabled = true
                    return@addOnSuccessListener
                }

                val reportDoc = reportSnapshot.documents[0]
                val netProfit = reportDoc.getDouble("netProfit") ?: 0.0
                val totalRevenue = reportDoc.getDouble("totalRevenue") ?: 0.0
                val monthName = reportDoc.getString("month") ?: "Bulan Ini"

                if (netProfit <= 0) {
                    Toast.makeText(context, "Net Profit 0 atau negatif, tidak bisa membagikan.", Toast.LENGTH_LONG).show()
                    binding.btnDistributeProfit.isEnabled = true
                    return@addOnSuccessListener
                }

                // Step B: Get All Users
                db.collection("users").get().addOnSuccessListener { userSnapshot ->
                    val users = userSnapshot.documents
                    val totalMembers = users.size

                    if (totalMembers == 0) return@addOnSuccessListener

                    // Step C: Calculate Math
                    val totalToDistribute = netProfit * 0.90
                    val sharePerMember = totalToDistribute / totalMembers

                    // Step D: Batch Write
                    val batch = db.batch()

                    // 1. Update Users
                    for (userDoc in users) {
                        batch.update(userDoc.reference, "totalSimpanan", FieldValue.increment(sharePerMember))
                    }

                    // 2. Create Historical Record (Subcollection in financial report)
                    val historyId = UUID.randomUUID().toString()
                    val historyRef = db.collection("financial_reports")
                        .document(reportDoc.id)
                        .collection("distribution_history") // New collection inside financial reports
                        .document(historyId)

                    val record = ProfitDistributionRecord(
                        id = historyId,
                        sourceReportId = reportDoc.id,
                        sourceMonth = monthName,
                        totalRevenue = totalRevenue,
                        netProfit = netProfit,
                        distributedAmount = totalToDistribute,
                        sharePerMember = sharePerMember,
                        totalMembers = totalMembers,
                        distributedAt = Timestamp.now()
                    )
                    batch.set(historyRef, record)

                    // 3. Create Announcement
                    val announcementId = UUID.randomUUID().toString()
                    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                    val formattedShare = currencyFormat.format(sharePerMember)

                    val announcement = Announcement(
                        id = announcementId,
                        title = "Pembagian SHU ($monthName)",
                        message = "SHU sebesar 90% dari profit $monthName telah dibagikan. " +
                                "Anda menerima $formattedShare.",
                        date = Timestamp.now(),
                        isUrgent = true // Profit distribution is usually important/urgent
                    )

                    batch.set(db.collection("announcements").document(announcementId), announcement)

                    // Step E: Commit
                    batch.commit().addOnSuccessListener {
                        Toast.makeText(context, "Sukses! Profit $monthName dibagikan.", Toast.LENGTH_LONG).show()
                        binding.btnDistributeProfit.isEnabled = true
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                        binding.btnDistributeProfit.isEnabled = true
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal mengambil data laporan", Toast.LENGTH_SHORT).show()
                binding.btnDistributeProfit.isEnabled = true
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}