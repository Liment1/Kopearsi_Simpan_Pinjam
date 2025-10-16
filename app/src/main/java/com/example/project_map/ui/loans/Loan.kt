package com.example.project_map.ui.loans
import org.json.JSONObject

data class Loan(
    val id: Long,
    val namaPeminjam: String,
    val nominal: Double,
    val tenor: String,
    val tujuan: String,
    var status: String,             // Proses, Diterima, Ditolak, Lunas
    val bunga: Double = 0.05,
    var sisaAngsuran: Double = nominal,
    var totalDibayar: Double = 0.0,
    var alasanPenolakan: String = ""
)

fun Loan.toJson(): JSONObject {
    val obj = JSONObject()

    fun safeDouble(value: Double): Double {
        return if (value.isNaN() || value.isInfinite()) 0.0 else value
    }

    obj.put("id", id)
    obj.put("namaPeminjam", namaPeminjam)
    obj.put("nominal", safeDouble(nominal))
    obj.put("tenor", tenor)
    obj.put("tujuan", tujuan)
    obj.put("status", status)
    obj.put("bunga", safeDouble(bunga))
    obj.put("sisaAngsuran", safeDouble(sisaAngsuran))
    obj.put("totalDibayar", safeDouble(totalDibayar))
    obj.put("alasanPenolakan", alasanPenolakan)

    return obj
}





