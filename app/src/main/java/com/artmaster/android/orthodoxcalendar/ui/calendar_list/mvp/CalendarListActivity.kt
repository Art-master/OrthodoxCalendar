package com.artmaster.android.orthodoxcalendar.ui.calendar_list.mvp

import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.artmaster.android.orthodoxcalendar.App
import com.artmaster.android.orthodoxcalendar.R
import com.artmaster.android.orthodoxcalendar.common.*
import com.artmaster.android.orthodoxcalendar.common.OrtUtils.checkFragment
import com.artmaster.android.orthodoxcalendar.common.Settings.Name.FIRST_LOADING_TILE_CALENDAR
import com.artmaster.android.orthodoxcalendar.data.font.CustomCheckedView
import com.artmaster.android.orthodoxcalendar.databinding.ActivityCalendarBinding
import com.artmaster.android.orthodoxcalendar.databinding.FilterDrawerLayoutBinding
import com.artmaster.android.orthodoxcalendar.domain.Filter
import com.artmaster.android.orthodoxcalendar.domain.Time
import com.artmaster.android.orthodoxcalendar.impl.AppDatabase
import com.artmaster.android.orthodoxcalendar.ui.CalendarUpdateContract
import com.artmaster.android.orthodoxcalendar.ui.MessageBuilderFragment
import com.artmaster.android.orthodoxcalendar.ui.calendar_list.fragments.impl.AppInfoView
import com.artmaster.android.orthodoxcalendar.ui.calendar_list.fragments.impl.AppSettingView
import com.artmaster.android.orthodoxcalendar.ui.calendar_list.fragments.impl.ListViewDiffContract
import com.artmaster.android.orthodoxcalendar.ui.calendar_list.fragments.settings.components.CheckBoxDecorator
import com.artmaster.android.orthodoxcalendar.ui.calendar_list.fragments.shared.CalendarSharedData
import com.artmaster.android.orthodoxcalendar.ui.calendar_list.impl.CalendarListContractModel
import com.artmaster.android.orthodoxcalendar.ui.calendar_list.impl.CalendarListContractView
import com.artmaster.android.orthodoxcalendar.ui.tile_pager.impl.ContractTileView
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import moxy.MvpAppCompatActivity
import moxy.MvpView
import moxy.presenter.InjectPresenter
import javax.inject.Inject

class CalendarListActivity : MvpAppCompatActivity(), HasAndroidInjector, CalendarListContractView, MvpView {

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var model: CalendarListContractModel

    @InjectPresenter(tag = "ListPresenter")
    lateinit var presenter: CalendarListPresenter

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var listHolidayFragment: ListViewDiffContract.ViewListPager

    @Inject
    lateinit var tileCalendarFragment: ContractTileView

    @Inject
    lateinit var appInfoFragment: AppInfoView

    @Inject
    lateinit var appSettingsFragment: AppSettingView

    private val viewModel: CalendarSharedData by viewModels()

    private val preferences = App.appComponent.getPreferences()

    private val isFirstLoadTileCalendar = preferences.get(FIRST_LOADING_TILE_CALENDAR).toBoolean()

    private var toolbarMenu: Menu? = null

    private lateinit var calendarFragment: Fragment

    private enum class Tags { CALENDAR }

    private var fragment: Fragment = Fragment()

    private var _binding: ActivityCalendarBinding? = null
    private val binding get() = _binding!!

    private var _filtersBinding: FilterDrawerLayoutBinding? = null
    private val filtersBinding get() = _filtersBinding!!

    override fun androidInjector(): AndroidInjector<Any> {
        return fragmentInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        _binding = ActivityCalendarBinding.inflate(layoutInflater)
        _filtersBinding = FilterDrawerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        if (savedInstanceState == null) {
            initTypeOfCalendarFragment()
        }

        listHolidayFragment.onChangePageListener { prepareSpinner(it) }

        if (!presenter.isInRestoreState(this) && savedInstanceState == null) {
            presenter.attachView(this)
            presenter.viewIsReady()
        }

        initBarSpinner()
        initFilters()
        initFiltersListeners()
    }

    private fun initFilters() {
        binding.showFiltersButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
            binding.showFiltersButton.hide()
            binding.addHoliday.hide()
        }

        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                binding.showFiltersButton.show()
                binding.addHoliday.show()
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                binding.showFiltersButton.hide()
                binding.addHoliday.hide()
            }
        })
    }

    private fun initFiltersListeners() {
        initCheckBoxFilter(filtersBinding.filterEaster, Filter.EASTER)
        initCheckBoxFilter(filtersBinding.filterHeadHolidays, Filter.HEAD_HOLIDAYS)
        initCheckBoxFilter(filtersBinding.filterAverageHolidays, Filter.AVERAGE_HOLIDAYS)
        initCheckBoxFilter(filtersBinding.filterCommonMemoryDays, Filter.COMMON_MEMORY_DAYS)
        initCheckBoxFilter(filtersBinding.filterMemoryDays, Filter.MEMORY_DAYS)
        initCheckBoxFilter(filtersBinding.filterNameDays, Filter.NAME_DAYS)
    }

    private fun initCheckBoxFilter(view: CustomCheckedView, filter: Filter) {
        val p = CheckBoxDecorator(view, preferences, Settings.Name.FILTER_AVERAGE_HOLIDAYS).prepare()
        p.onClick = {
            if (it) viewModel.addFilter(filter)
            else viewModel.removeFilter(filter)
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.run {
            val bundle = calendarFragment.arguments ?: return
            setCurrentState(bundle, this)
        }
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.run {
            if (calendarFragment.arguments == null) calendarFragment.arguments = Bundle()
            setCurrentState(this, calendarFragment.requireArguments())
        }
    }

    private fun initTypeOfCalendarFragment() {
        calendarFragment = if (isFirstLoadTileCalendar) {
            tileCalendarFragment as Fragment
        } else {
            listHolidayFragment as Fragment
        }
        setStartArguments(calendarFragment)
    }

    private fun prepareSpinner(position: Int) {
        binding.toolbarYearSpinner.setSelection(position)
        controlSpinnerView(position)
    }

    private fun setStartArguments(fragment: Fragment) {
        if (fragment.arguments != null) return
        fragment.arguments = Bundle().apply {
            putInt(Constants.Keys.YEAR.value, getStartYear())
            putInt(Constants.Keys.MONTH.value, getMonth())
            putInt(Constants.Keys.DAY.value, getDay())
        }
    }

    private fun setCurrentState(arguments: Bundle, state: Bundle) {
        state.apply {
            putInt(Constants.Keys.YEAR.value, arguments.getInt(Constants.Keys.YEAR.value))
            putInt(Constants.Keys.MONTH.value, arguments.getInt(Constants.Keys.MONTH.value))
            putInt(Constants.Keys.DAY.value, arguments.getInt(Constants.Keys.DAY.value))
        }
    }

    private fun controlSpinnerView(position: Int) {
        val firstPosition = 0
        val lastPosition = getYears().size - 1
        when (position) {
            lastPosition -> binding.arrowRight.visibility = View.GONE
            firstPosition -> binding.arrowLeft.visibility = View.GONE
            else -> {
                binding.arrowRight.visibility = View.VISIBLE
                binding.arrowLeft.visibility = View.VISIBLE
            }
        }
    }

    private fun getStartYear() = intent.getIntExtra(Constants.Keys.YEAR.value, Time().year)
    private fun getCurrentYear() = calendarFragment.arguments?.getInt(Constants.Keys.YEAR.value, getStartYear())
    private fun getMonth() = intent.getIntExtra(Constants.Keys.MONTH.value, Time().monthWith0)
    private fun getDay() = intent.getIntExtra(Constants.Keys.DAY.value, Time().dayOfMonth)

    private fun getYears(): ArrayList<String> {
        val size = Constants.HolidayList.PAGE_SIZE.value
        val firstYear = getStartYear() - size / 2
        val years = ArrayList<String>(size)
        for (element in firstYear until firstYear + size) {
            years.add(element.toString())
        }
        return years
    }

    override fun showActionBar() = supportActionBar!!.show()

    override fun hideActionBar() {
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.hide()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_app, menu)
        toolbarMenu = menu

        changeIconTypeCalendar()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val currentVisibleFragment = when (item!!.itemId) {
            R.id.item_about -> checkFragment(appInfoFragment)
            R.id.item_settings -> checkFragment(appSettingsFragment)
            R.id.item_reset_date -> {
                resetDateState()
                null
            }
            else -> null
        }
        changeMainFragment(item)
        if (currentVisibleFragment != null) {
            if (isExist(currentVisibleFragment)) {
                showFragment(checkFragment(calendarFragment))
                removeFragment(currentVisibleFragment)
            } else {
                this.fragment = currentVisibleFragment
                hideFragment(checkFragment(calendarFragment))
                replaceFragment(R.id.menu_fragments_container, currentVisibleFragment)
            }
        } else if (item.itemId == R.id.item_view) {
            removeFragment(this.fragment)
            showFragment(checkFragment(calendarFragment))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeMainFragment(item: MenuItem) {
        if (item.itemId != R.id.item_view) return

        val fragmentTile = tileCalendarFragment as Fragment
        val fragmentList = listHolidayFragment as Fragment

        calendarFragment = if (calendarFragment is ListViewDiffContract.ViewListPager) {
            fragmentTile.arguments = fragmentList.arguments
            fragmentTile
        } else {
            fragmentList.arguments = fragmentTile.arguments
            fragmentList
        }

        changeIconTypeCalendar()
        replaceFragment(R.id.activityCalendar, calendarFragment)
    }

    private fun updateFragmentData(fragment: CalendarUpdateContract) {
        fragment.updateYear()
        fragment.updateMonth()
        fragment.updateDay()
    }

    private fun changeIconTypeCalendar() {
        if (calendarFragment is ListViewDiffContract.ViewListPager) {
            toolbarMenu?.getItem(0)?.setIcon(R.drawable.icon_tile)
        } else {
            toolbarMenu?.getItem(0)?.setIcon(R.drawable.icon_list)
        }
    }

    override fun showHolidayList() {
        if (::calendarFragment.isInitialized.not()) {
            calendarFragment = supportFragmentManager.findFragmentByTag(Tags.CALENDAR.name)!!
        }
        val fragment = checkFragment(calendarFragment)
        if (fragment.isAdded.not()) {
            addFragment(R.id.activityCalendar, fragment, Tags.CALENDAR.name)
        }
    }

    override fun showErrorMessage(msgType: Message.ERROR) {
        val bundle = Bundle()
        bundle.putString(msgType.name, msgType.toString())

        val dialogError = MessageBuilderFragment()
        dialogError.arguments = bundle
        dialogError.show(supportFragmentManager, "dialogError")
    }

    private fun initBarSpinner() {
        val years = getYears()
        val adapter = SpinnerAdapter(this, R.layout.spinner_year_item, years.toTypedArray())
        adapter.setDropDownViewResource(R.layout.spinner_year_dropdown)
        binding.toolbarYearSpinner.adapter = adapter
        val pos = years.indexOf(getStartYear().toString())
        binding.toolbarYearSpinner.setSelection(pos)
        setOnItemSelected()
    }

    private fun showFragment(fragment: Fragment) {
        if (fragment.isHidden) {
            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(0, 0)
                    .show(fragment)
                    .commit()
        }
    }

    private fun hideFragment(fragment: Fragment) {
        if (!fragment.isHidden) {
            supportFragmentManager
                    .beginTransaction()
                    .hide(fragment)
                    .commit()
        }
    }

    private fun removeFragment(fragment: Fragment) {
        if (fragment.isAdded) {
            supportFragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commit()
        }
    }

    private fun addFragment(resId: Int, fragment: Fragment, tag: String?) {
        if (!fragment.isAdded) {
            supportFragmentManager
                    .beginTransaction()
                    .add(resId, fragment, tag)
                    .commit()
        }
    }

    private fun replaceFragment(resId: Int, fragment: Fragment, tag: String? = null) {
        addFragment(resId, fragment, tag)
        if (!fragment.isHidden) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(resId, fragment)
                    .addToBackStack(null)
                    .commit()
        }
    }

    private fun isExist(fragment: Fragment): Boolean {
        return supportFragmentManager.fragments.contains(fragment)
    }

    override fun onBackPressed() {
        when {
            isExist(checkFragment(appSettingsFragment)) -> {
                removeFragment(appSettingsFragment as Fragment)
                showFragment(checkFragment(calendarFragment))
            }

            isExist(checkFragment(appInfoFragment)) -> {
                removeFragment(appInfoFragment as Fragment)
                showFragment(checkFragment(calendarFragment))
            }

            else -> OrtUtils.exitProgram(this)
        }
    }

    private fun setOnItemSelected() {
        binding.toolbarYearSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                if (isTheSameYear(position).not()) {
                    val year = getSpinnerCurrentYear()
                    updateArgs(tileCalendarFragment as Fragment, year)
                    updateArgs(listHolidayFragment as Fragment, year)
                    controlSpinnerView(position)
                    (calendarFragment as CalendarUpdateContract).updateYear()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }
    }

    private fun updateArgs(fragment: Fragment, year: Int? = null, month: Int? = null, day: Int? = null) {
        val y = year ?: calendarFragment.arguments?.getInt(Constants.Keys.YEAR.value)
        val m = month ?: calendarFragment.arguments?.getInt(Constants.Keys.MONTH.value)
        val d = day ?: calendarFragment.arguments?.getInt(Constants.Keys.DAY.value)

        fragment.arguments = Bundle().apply {
            putInt(Constants.Keys.YEAR.value, y!!)
            putInt(Constants.Keys.MONTH.value, m!!)
            putInt(Constants.Keys.DAY.value, d!!)
        }
    }

    private fun isTheSameYear(spinnerPosition: Int): Boolean {
        return getCurrentYear() == getYears()[spinnerPosition].toInt()
    }

    private fun getSpinnerCurrentYear(): Int {
        val years = getYears()
        val currentYear = years[binding.toolbarYearSpinner.selectedItemPosition]
        return currentYear.toInt()
    }

    private fun resetDateState() {
        val fr = calendarFragment as CalendarUpdateContract
        val time = Time()

        resetArgsValues()
        if (time.year == getStartYear()) {
            val years = getYears()
            val pos = years.indexOf(getStartYear().toString())
            binding.toolbarYearSpinner.setSelection(pos)
            //year will update in spinner listener
        }
        if (time.monthWith0 == getMonth()) fr.updateMonth()
    }

    private fun resetArgsValues() {
        val time = Time()
        intent.putExtra(Constants.Keys.YEAR.value, time.year)
        intent.putExtra(Constants.Keys.MONTH.value, time.monthWith0)
        intent.putExtra(Constants.Keys.DAY.value, time.dayOfMonth)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
        fragment.onDestroy()
    }
}