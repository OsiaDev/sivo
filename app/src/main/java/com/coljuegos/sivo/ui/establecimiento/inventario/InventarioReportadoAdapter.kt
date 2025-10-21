package com.coljuegos.sivo.ui.establecimiento.inventario

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coljuegos.sivo.data.entity.InventarioEntity
import com.coljuegos.sivo.databinding.ItemInventarioReportadoBinding
import java.util.UUID

class InventarioReportadoAdapter(
    private val onItemClick: (InventarioEntity) -> Unit,
    private val onExpandClick: (UUID) -> Unit,
    private val isItemExpanded: (UUID) -> Boolean
) : ListAdapter<InventarioEntity, InventarioReportadoAdapter.InventarioViewHolder>(InventarioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {
        val binding = ItemInventarioReportadoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InventarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InventarioViewHolder(
        private val binding: ItemInventarioReportadoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(inventario: InventarioEntity) {
            with(binding) {
                // Nombre y Serial
                val nombreCompleto = "${inventario.nombreMarcaInventario} - ${inventario.metSerialInventario}"

            }
        }
    }

    class InventarioDiffCallback : DiffUtil.ItemCallback<InventarioEntity>() {
        override fun areItemsTheSame(oldItem: InventarioEntity, newItem: InventarioEntity): Boolean {
            return oldItem.uuidInventario == newItem.uuidInventario
        }

        override fun areContentsTheSame(oldItem: InventarioEntity, newItem: InventarioEntity): Boolean {
            return oldItem == newItem
        }
    }

}