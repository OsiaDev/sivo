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
                nombreMarcaText.text = nombreCompleto

                // Valores
                serialValue.text = inventario.metSerialInventario
                nucValue.text = inventario.nucInventario
                marcaValue.text = inventario.nombreMarcaInventario
                onlineValue.text = if (inventario.metOnlineInventario) "Sí" else "No"
                codigoApuestaValue.text = inventario.codigoTipoApuestaInventario

                // Expandir/Colapsar
                val isExpanded = isItemExpanded(inventario.uuidInventario)
                expandableContent.isVisible = isExpanded
                expandIcon.rotation = if (isExpanded) 180f else 0f

                // Click en el card para expandir/colapsar
                cardView.setOnClickListener {
                    onExpandClick(inventario.uuidInventario)
                }

                // Click en el botón Seleccionar
                btnSeleccionar.setOnClickListener {
                    onItemClick(inventario)
                }
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