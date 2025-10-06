package com.d4rk.qrcodescanner.plus.ui.screens.settings.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import com.d4rk.qrcodescanner.plus.databinding.ActivityChooseSearchEngineBinding
import com.d4rk.qrcodescanner.plus.di.settings
import com.d4rk.qrcodescanner.plus.extension.applySystemWindowInsets
import com.d4rk.qrcodescanner.plus.extension.unsafeLazy
import com.d4rk.qrcodescanner.plus.model.SearchEngine
import com.d4rk.qrcodescanner.plus.ui.components.navigation.BaseActivity
import com.d4rk.qrcodescanner.plus.ui.components.views.SettingsRadioButton
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class ChooseSearchEngineActivity : BaseActivity() {
    private lateinit var binding : ActivityChooseSearchEngineBinding

    companion object {
        fun start(context : Context) {
            val intent = Intent(context , ChooseSearchEngineActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val buttons by unsafeLazy {
        listOf(binding.buttonNone , binding.buttonAskEveryTime , binding.buttonBing , binding.buttonDuckDuckGo , binding.buttonGoogle , binding.buttonQwant , binding.buttonStartpage , binding.buttonYahoo , binding.buttonYandex)
    }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChooseSearchEngineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportEdgeToEdge()
        showInitialValue()
        handleSettingsChanged()
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true , applyBottom = true)
    }

    private fun showInitialValue() {
        when (settings.searchEngine) {
            SearchEngine.NONE -> binding.buttonNone.isChecked = true
            SearchEngine.ASK_EVERY_TIME -> binding.buttonAskEveryTime.isChecked = true
            SearchEngine.STARTPAGE -> binding.buttonStartpage.isChecked = true
            SearchEngine.BING -> binding.buttonBing.isChecked = true
            SearchEngine.DUCK_DUCK_GO -> binding.buttonDuckDuckGo.isChecked = true
            SearchEngine.GOOGLE -> binding.buttonGoogle.isChecked = true
            SearchEngine.QWANT -> binding.buttonQwant.isChecked = true
            SearchEngine.YAHOO -> binding.buttonYahoo.isChecked = true
            SearchEngine.YANDEX -> binding.buttonYandex.isChecked = true
        }
    }

    private fun handleSettingsChanged() {
        binding.buttonNone.setCheckedChangedListener(SearchEngine.NONE)
        binding.buttonAskEveryTime.setCheckedChangedListener(SearchEngine.ASK_EVERY_TIME)
        binding.buttonBing.setCheckedChangedListener(SearchEngine.BING)
        binding.buttonStartpage.setCheckedChangedListener(SearchEngine.STARTPAGE)
        binding.buttonDuckDuckGo.setCheckedChangedListener(SearchEngine.DUCK_DUCK_GO)
        binding.buttonGoogle.setCheckedChangedListener(SearchEngine.GOOGLE)
        binding.buttonQwant.setCheckedChangedListener(SearchEngine.QWANT)
        binding.buttonYahoo.setCheckedChangedListener(SearchEngine.YAHOO)
        binding.buttonYandex.setCheckedChangedListener(SearchEngine.YANDEX)
    }

    private fun SettingsRadioButton.setCheckedChangedListener(searchEngine : SearchEngine) {
        setCheckedChangedListener { isChecked ->
            if (isChecked) {
                uncheckOtherButtons(this)
                settings.searchEngine = searchEngine
            }
        }
    }

    private fun uncheckOtherButtons(checkedButton : View) {
        buttons.forEach { button ->
            if (checkedButton !== button) {
                button.isChecked = false
            }
        }
    }
}