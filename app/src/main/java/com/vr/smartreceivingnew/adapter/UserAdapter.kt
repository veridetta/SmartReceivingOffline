package com.vr.smartreceivingnew.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vr.smartreceivingnew.R
import com.vr.smartreceivingnew.model.UserModel
import java.util.Locale


class UserAdapter(
    private var userList: MutableList<UserModel>,
    val context: Context,
    private val onEditClickListener: (UserModel) -> Unit,
    private val onHapusClickListener: (UserModel) -> Unit,
) : RecyclerView.Adapter<UserAdapter.ProductViewHolder>() {
    public var filteredBarangList: MutableList<UserModel> = mutableListOf()
    init {
        filteredBarangList.addAll(userList)
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && filteredBarangList.isEmpty()) {
            1 // Return 1 for empty state view
        } else {
            0 // Return 0 for regular product view
        }
    }

    fun updateUsers(newUsers: List<UserModel>) {
        userList.clear()
        userList.addAll(newUsers)
        filteredBarangList.clear()
        filteredBarangList.addAll(newUsers)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredBarangList.clear()
        if (!query.isNullOrEmpty()) {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            for (user in userList) {
                val name = user.nama?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                if (name == true) {
                    filteredBarangList.add(user)
                }
            }
        } else {
            filteredBarangList.addAll(userList)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredBarangList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentBarang = filteredBarangList[position]

        holder.tvNama.text = currentBarang.nama
        holder.tvUsername.text = currentBarang.username
        holder.tvPassword.text = currentBarang.password
        holder.tvNoHp.text = currentBarang.noHp
        holder.btnUbah.setOnClickListener { onEditClickListener(currentBarang) }
        holder.btnHapus.setOnClickListener { onHapusClickListener(currentBarang) }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvPassword: TextView = itemView.findViewById(R.id.tvPassword)
        val tvNoHp: TextView = itemView.findViewById(R.id.tvNoHp)
        val btnUbah: LinearLayout = itemView.findViewById(R.id.btnUbah)
        val btnHapus: LinearLayout = itemView.findViewById(R.id.btnHapus)
    }
}
