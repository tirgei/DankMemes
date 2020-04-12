package com.gelostech.dankmemes.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Report
import com.gelostech.dankmemes.databinding.ItemReportBinding
import com.gelostech.dankmemes.ui.callbacks.ReportsCallback
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.utils.inflate
import com.gelostech.dankmemes.utils.setDrawable
import com.mikepenz.ionicons_typeface_library.Ionicons


class ReportsAdapter(private val callback: ReportsCallback): PagedListAdapter<Report,
        ReportsAdapter.ReportHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Report>() {
            override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportHolder {
        return ReportHolder(parent.inflate(R.layout.item_report), callback)
    }

    override fun onBindViewHolder(holder: ReportHolder, position: Int) {
        val report = getItem(position)
        holder.bind(report!!)
    }

    inner class ReportHolder(private val binding: ItemReportBinding, private val callback: ReportsCallback):
            RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            binding.report = report
            binding.callback = callback

            binding.time.setDrawable(AppUtils.getDrawable(binding.root.context, Ionicons.Icon.ion_clock, R.color.color_text_secondary, 12))
        }
    }
}