package com.coljuegos.sivo.ui.galeria

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.ImagenEntity
import com.coljuegos.sivo.databinding.ItemImagenBinding
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class GaleriaAdapter(
    private val onImageClick: (ImagenEntity) -> Unit,
    private val onDeleteClick: (ImagenEntity) -> Unit
) : ListAdapter<ImagenEntity, GaleriaAdapter.ImagenViewHolder>(ImagenDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagenViewHolder {
        val binding = ItemImagenBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImagenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagenViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ImagenViewHolder(
        private val binding: ItemImagenBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imagen: ImagenEntity) {
            with(binding) {
                // Cargar imagen con Glide
                val file = File(imagen.rutaImagen)
                if (file.exists()) {
                    Glide.with(itemView.context)
                        .load(file)
                        .centerCrop()
                        .placeholder(R.drawable.ic_camara)
                        .error(R.drawable.ic_camara)
                        .into(imageViewFoto)
                } else {
                    imageViewFoto.setImageResource(R.drawable.ic_camara)
                }

                // Mostrar información de la imagen
                textViewNombre.text = imagen.nombreImagen

                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                textViewFecha.text = formatter.format(imagen.fechaCaptura)

                // Mostrar tamaño del archivo
                val tamanoEnKB = imagen.tamanoBytesImagen / 1024
                textViewTamano.text = "${tamanoEnKB} KB"

                // Click listeners
                root.setOnClickListener {
                    onImageClick(imagen)
                }

                buttonDelete.setOnClickListener {
                    onDeleteClick(imagen)
                }
            }
        }
    }

    private class ImagenDiffCallback : DiffUtil.ItemCallback<ImagenEntity>() {
        override fun areItemsTheSame(oldItem: ImagenEntity, newItem: ImagenEntity): Boolean {
            return oldItem.uuidImagen == newItem.uuidImagen
        }

        override fun areContentsTheSame(oldItem: ImagenEntity, newItem: ImagenEntity): Boolean {
            return oldItem == newItem
        }
    }

}