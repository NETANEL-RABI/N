package com.offlinestore.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(
    private val items: List<AppInfo>,
    private val onInstallClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.imgIcon)
        val title: TextView = view.findViewById(R.id.txtLabel)
        val subtitle: TextView = view.findViewById(R.id.txtPackage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = items[position]
        holder.title.text = app.label
        holder.subtitle.text = "${app.packageName}  •  v${app.versionName ?: "?"}"
        if (app.icon != null) holder.icon.setImageDrawable(app.icon)
        else holder.icon.setImageResource(android.R.drawable.sym_def_app_icon)

        holder.itemView.setOnClickListener { onInstallClick(app) }
    }

    override fun getItemCount(): Int = items.size
}
