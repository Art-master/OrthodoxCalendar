package com.artmaster.android.orthodoxcalendar.ui.review.mvp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.artmaster.android.orthodoxcalendar.R
import com.artmaster.android.orthodoxcalendar.common.Constants
import com.artmaster.android.orthodoxcalendar.common.Message
import com.artmaster.android.orthodoxcalendar.common.OrtUtils
import com.artmaster.android.orthodoxcalendar.data.font.CustomFont
import com.artmaster.android.orthodoxcalendar.data.font.CustomLeadingMarginSpan2
import com.artmaster.android.orthodoxcalendar.data.font.JustifiedTextView
import com.artmaster.android.orthodoxcalendar.databinding.FragmentHolidayBinding
import com.artmaster.android.orthodoxcalendar.domain.Holiday
import com.artmaster.android.orthodoxcalendar.ui.review.impl.HolidayReviewContract
import com.squareup.picasso.Picasso
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class HolidayFragment : Fragment(), HolidayReviewContract.View {

    @Inject
    lateinit var presenter: HolidayReviewContract.Presenter

    lateinit var holiday: Holiday

    private var _binding: FragmentHolidayBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(holiday: Holiday): HolidayFragment {
            val intent = Intent()
            intent.putExtra(Constants.Keys.HOLIDAY.value, holiday)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

            val fragment = HolidayFragment()
            fragment.arguments = intent.extras
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        holiday = requireArguments().get(Constants.Keys.HOLIDAY.value) as Holiday
        presenter.init(holiday)
    }

    override fun onCreateView(inflater: LayoutInflater, groupContainer: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHolidayBinding.inflate(inflater, groupContainer, false)
        presenter.attachView(this)
        presenter.viewIsReady()
        return binding.root
    }

    override fun showHolidayName(name: String) {
        binding.holidayNameInPage.text = name
    }

    override fun showDescription(initialLater: String, description: String) {
        if (description.isEmpty()) return

        buildInitialLater(initialLater)
        val desc = prepareDescription(description)
        binding.relativeLayout.addView(desc)
    }

    private fun prepareDescription(description: String): JustifiedTextView {
        return JustifiedTextView(requireContext()).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.size_text_basic).toFloat())
            setTypeface(CustomFont.getFont(requireContext(), getString(R.string.font_basic)))
            setLeadingMargin(3, 8)
            val strWithoutSpaces = deleteSpaces(description)
            setText(getTextByPadding(strWithoutSpaces))
            setTextColor(binding.newDateStyleTextView.textColors)
        }
    }

    private fun deleteSpaces(string: String): String {
        val deleteSpaces = Regex.fromLiteral("[\\s&&[^\t?\n]]+")
        return string.replace(deleteSpaces, " ")
    }

    private fun buildInitialLater(later: String) {
        binding.initialLaterTextView.apply {
            val textName = resources.getString(R.string.font_for_bukvica)
            val size = resources.getDimensionPixelSize(R.dimen.size_text_bukvica).toFloat()
            setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
            setTextColor(Color.RED)
            typeface = CustomFont.getFont(requireContext(), textName)
            scaleX = 2F
            text = later
        }
    }

    private fun getTextByPadding(string: String): SpannableString {
        val finalStr = SpannableString(string)
        val size = OrtUtils.convertDpToPixels(requireContext(), 25f)
        finalStr.setSpan(CustomLeadingMarginSpan2(3, size), 0, finalStr.length, 0)
        return finalStr
    }

    override fun showNewStyleDate(date: String) {
        if (date.isEmpty()) return

        val str = getString(R.string.new_style_date_string, date)
        binding.newDateStyleTextView.text = str
    }

    override fun showOldStyleDate(date: String) {
        if (date.isEmpty()) return

        val str = getString(R.string.old_style_date_string, date)
        binding.oldDateStyleTextView.text = str
    }

    override fun showImageHoliday(resId: Int, placeholderId: Int) {
        Picasso.with(context)
                .load(resId)
                .placeholder(placeholderId)
                .error(placeholderId)
                .into(binding.imageHoliday)
    }

    override fun showErrorMessage(msgType: Message.ERROR) {
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
