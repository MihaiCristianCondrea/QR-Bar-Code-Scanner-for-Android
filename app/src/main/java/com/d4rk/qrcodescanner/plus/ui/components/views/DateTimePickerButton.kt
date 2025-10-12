package com.d4rk.qrcodescanner.plus.ui.components.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.databinding.LayoutDateTimePickerButtonBinding
import com.d4rk.qrcodescanner.plus.utils.extension.formatOrNull
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Locale

class DateTimePickerButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutDateTimePickerButtonBinding =
        LayoutDateTimePickerButtonBinding.inflate(LayoutInflater.from(context), this, true)
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.DateTimePickerButton).apply {
            showHint(this)
            recycle()
        }
        binding.root.setOnClickListener {
            showDateTimePickerDialog()
        }
        showDateTime()
    }

    var dateTime: Long = System.currentTimeMillis()
        set(value) {
            field = value
            showDateTime()
        }

    private fun showHint(attributes: TypedArray) {
        binding.textViewHint.text =
            attributes.getString(R.styleable.DateTimePickerButton_hint).orEmpty()
    }

    private fun showDateTimePickerDialog() {
        val fragmentActivity = context as? FragmentActivity ?: return
        val fragmentManager = fragmentActivity.supportFragmentManager
        val currentCalendar = java.util.Calendar.getInstance().apply {
            timeInMillis = dateTime
        }
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(binding.textViewHint.text.toString())
            .setSelection(currentCalendar.timeInMillis).build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = java.util.Calendar.getInstance().apply {
                timeInMillis = selection
            }
            val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentCalendar.get(java.util.Calendar.HOUR_OF_DAY))
                .setMinute(currentCalendar.get(java.util.Calendar.MINUTE))
                .setTitleText(binding.textViewHint.text.toString()).build()
            timePicker.addOnPositiveButtonClickListener {
                selectedDate.set(java.util.Calendar.HOUR_OF_DAY, timePicker.hour)
                selectedDate.set(java.util.Calendar.MINUTE, timePicker.minute)
                dateTime = selectedDate.timeInMillis
            }
            timePicker.show(fragmentManager, "timePicker")
        }
        datePicker.show(fragmentManager, "datePicker")
    }

    private fun showDateTime() {
        binding.textViewDateTime.text = dateFormatter.formatOrNull(dateTime).orEmpty()
    }
}