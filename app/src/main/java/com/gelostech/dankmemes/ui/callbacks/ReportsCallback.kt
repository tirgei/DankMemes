package com.gelostech.dankmemes.ui.callbacks

import com.gelostech.dankmemes.data.models.Report

interface ReportsCallback {
    fun onReportClicked(report: Report)
}