package com.vr.smartreceivingnew.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.model.BarangModel
import java.util.Locale


class BarangAdapter(
    var barangList: MutableList<BarangModel>,
    val context: Context,
    private val onEditClickListener: (BarangModel) -> Unit,
    private val onHapusClickListener: (BarangModel) -> Unit,
) : RecyclerView.Adapter<BarangAdapter.ProductViewHolder>() {
    public var filteredBarangList: MutableList<BarangModel> = mutableListOf()
    init {
        filteredBarangList.addAll(barangList)
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && filteredBarangList.isEmpty()) {
            1 // Return 1 for empty state view
        } else {
            0 // Return 0 for regular product view
        }
    }
    fun filter(query: String) {
        filteredBarangList.clear()
        if (query.isEmpty()) {
            filteredBarangList.addAll(barangList)
        } else {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            for (product in barangList) {
                val nam = product.nama?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                if (nam == true) {
                    filteredBarangList.add(product)
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_barang, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredBarangList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentBarang = filteredBarangList[position]

        holder.tvNama.text = currentBarang.nama
        holder.tvMerek.text = "("+currentBarang.merek+")"
        if(currentBarang.jenis == ""){
            holder.tvJenis.text = ""
        }else{
            holder.tvJenis.text = " - "+currentBarang.jenis
        }
        holder.tvQr.text = currentBarang.kode1+" "+currentBarang.kode2
        holder.tvPerdus.text = currentBarang.perRak+" "+currentBarang.satuan+" /Rack"
        holder.btnUbah.setOnClickListener { onEditClickListener(currentBarang) }
        holder.btnHapus.setOnClickListener { onHapusClickListener(currentBarang) }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvMerek: TextView = itemView.findViewById(R.id.tvMerek)
        val tvJenis: TextView = itemView.findViewById(R.id.tvJenis)
        val tvQr: TextView = itemView.findViewById(R.id.tvQr)
        val tvPerdus: TextView = itemView.findViewById(R.id.tvPerdus)
        val btnUbah: LinearLayout = itemView.findViewById(R.id.btnUbah)
        val btnHapus: LinearLayout = itemView.findViewById(R.id.btnHapus)
    }
}
