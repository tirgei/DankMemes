package com.gelostech.dankmemes.ui.activities

import am.appwise.components.ni.NoInternetDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.ui.fragments.AllFragment
import com.gelostech.dankmemes.ui.fragments.FavesFragment
import com.gelostech.dankmemes.ui.fragments.NotificationsFragment
import com.gelostech.dankmemes.ui.fragments.ProfileFragment
import com.gelostech.dankmemes.ui.viewmodels.UsersViewModel
import com.gelostech.dankmemes.utils.*
import com.gelostech.dankmemes.utils.AppUtils.setDrawable
import com.gelostech.pageradapter.PagerAdapter
import com.google.firebase.messaging.FirebaseMessaging
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.wooplr.spotlight.SpotlightView
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_layout.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class MainActivity : BaseActivity(), AHBottomNavigation.OnTabSelectedListener,
        AHBottomNavigation.OnNavigationPositionListener, ViewPager.OnPageChangeListener {

    private var doubleBackToExit = false
    private var newMeme: MenuItem? = null
    private var editProfile: MenuItem? = null
    private lateinit var slidingDrawer: SlidingRootNav
    private lateinit var favesFragment: FavesFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var allFragment: AllFragment
    private lateinit var notifFragment: NotificationsFragment
    private lateinit var noInternetDialog: NoInternetDialog
    private val usersViewModel: UsersViewModel by viewModel()

    private lateinit var APP_NAME: String
    private lateinit var HOME: String
    private lateinit var FAVES: String
    private lateinit var PROFILE: String
    private lateinit var NOTIFICATIONS: String
    private lateinit var PLAY_STORE_LINK: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationUtils(this).clearNotifications()

        profileFragment = ProfileFragment()
        favesFragment = FavesFragment()
        allFragment = AllFragment()
        notifFragment = NotificationsFragment()

        APP_NAME = getString(R.string.app_name)
        HOME = getString(R.string.fragment_home)
        FAVES = getString(R.string.fragment_faves)
        PROFILE = getString(R.string.fragment_profile)
        NOTIFICATIONS = getString(R.string.fragment_notifications)
        PLAY_STORE_LINK = getString(R.string.label_play_store_link) + this.packageName

        setupToolbar()
        setupBottomNav()
        setupViewPager()
        setupDrawer()

        initNoInternet()
        initLogoutObserver()
    }

    //Setup the main toolbar
    private fun setupToolbar() {
        setSupportActionBar(mainToolbar)
        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    //Setup the bottom navigation bar
    private fun setupBottomNav() {
        val homeIcon = setDrawable(this, Ionicons.Icon.ion_fireball, R.color.secondaryText, 18)
        val collectionsIcon = setDrawable(this, Ionicons.Icon.ion_ios_heart, R.color.secondaryText, 18)
        val notificationsIcon = setDrawable(this, Ionicons.Icon.ion_ios_bell, R.color.secondaryText, 18)
        val profileIcon = setDrawable(this, FontAwesome.Icon.faw_user2, R.color.secondaryText, 18)

        bottomNav.addItem(AHBottomNavigationItem(HOME, homeIcon))
        bottomNav.addItem(AHBottomNavigationItem(FAVES, collectionsIcon))
        bottomNav.addItem(AHBottomNavigationItem(NOTIFICATIONS, notificationsIcon))
        bottomNav.addItem(AHBottomNavigationItem(PROFILE, profileIcon))

        bottomNav.defaultBackgroundColor = ContextCompat.getColor(this, R.color.white)
        bottomNav.inactiveColor = ContextCompat.getColor(this, R.color.inactiveColor)
        bottomNav.accentColor = ContextCompat.getColor(this, R.color.colorAccent)
        bottomNav.isBehaviorTranslationEnabled = false
        bottomNav.titleState = AHBottomNavigation.TitleState.ALWAYS_HIDE
        bottomNav.setUseElevation(true, 5f)

        bottomNav.setOnTabSelectedListener(this)
        bottomNav.setOnNavigationPositionListener(this)
    }

    //Setup the main view pager
    private fun setupViewPager() {
        val adapter = PagerAdapter(supportFragmentManager, this)

        adapter.addAllFragments(allFragment, favesFragment, notifFragment, profileFragment)
        adapter.addAllTitles(HOME, FAVES, NOTIFICATIONS, PROFILE)

        mainViewPager.adapter = adapter
        mainViewPager.addOnPageChangeListener(this)
        mainViewPager.offscreenPageLimit = 3
    }

    //Setup drawer
    private fun setupDrawer() {
        slidingDrawer = SlidingRootNavBuilder(this)
                .withMenuLayout(R.layout.drawer_layout)
                .withDragDistance(150)
                .withToolbarMenuToggle(mainToolbar)
                .inject()

        mainRoot.setOnClickListener {
            if (slidingDrawer.isMenuOpened) slidingDrawer.closeMenu(true)
        }

        setupDrawerIcons()
        drawerClickListeners()

        drawerName.text = sessionManager.getUsername()
        drawerEmail.text = sessionManager.getEmail()
    }

    private fun setupDrawerIcons() {
        drawerRate.setDrawable(setDrawable(this, Ionicons.Icon.ion_ios_star, R.color.white, 18))
        drawerShare.setDrawable(setDrawable(this, Ionicons.Icon.ion_android_share, R.color.white, 18))
        drawerFeedback.setDrawable(setDrawable(this, Ionicons.Icon.ion_ios_email, R.color.white, 18))
        drawerTerms.setDrawable(setDrawable(this, Ionicons.Icon.ion_clipboard, R.color.white, 18))
        drawerPolicy.setDrawable(setDrawable(this, Ionicons.Icon.ion_ios_list, R.color.white, 18))
        drawerLogout.setDrawable(setDrawable(this, Ionicons.Icon.ion_log_out, R.color.white, 18))
        drawerMoreApps.setDrawable(setDrawable(this, FontAwesome.Icon.faw_google_play, R.color.white, 18))
    }

    private fun drawerClickListeners() {
        drawerRate.setOnClickListener {
            slidingDrawer.closeMenu(true)

            Handler().postDelayed({
                val uri = Uri.parse(PLAY_STORE_LINK)
                val goToMarket = Intent(Intent.ACTION_VIEW, uri)

                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToMarket)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_LINK)))
                }
            }, 300)
        }

        drawerShare.setOnClickListener {
            slidingDrawer.closeMenu(true)

            Handler().postDelayed({
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, APP_NAME)
                val message = getString(R.string.label_invite_body) + "\n\n" + PLAY_STORE_LINK
                intent.putExtra(Intent.EXTRA_TEXT, message)
                startActivity(Intent.createChooser(intent, getString(R.string.intent_invite_pals)))
            }, 300)
        }

        drawerFeedback.setOnClickListener {
            slidingDrawer.closeMenu(true)

            Handler().postDelayed({
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = Uri.parse(getString(R.string.intent_dev_email))
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, APP_NAME)
                startActivity(Intent.createChooser(emailIntent, getString(R.string.intent_send_feedback)))
            }, 300)
        }

        drawerTerms.setOnClickListener {
            slidingDrawer.closeMenu(true)

            Handler().postDelayed({
                openLink(getString(R.string.link_terms))
            }, 300)
        }

        drawerPolicy.setOnClickListener {
            slidingDrawer.closeMenu(true)

            Handler().postDelayed({
                openLink(getString(R.string.link_privacy))
            }, 300)
        }

        drawerMoreApps.setOnClickListener {
            slidingDrawer.closeMenu(true)

            Handler().postDelayed({
                val uri = Uri.parse(getString(R.string.label_dev_id))
                val devAccount = Intent(Intent.ACTION_VIEW, uri)

                devAccount.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(devAccount)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.label_dev_id))))
                }
            }, 300)
        }

        drawerLogout.setOnClickListener {
            slidingDrawer.closeMenu(true)

            Handler().postDelayed({
                alert("Are you sure you want to log out?") {
                    title = "Log out"
                    positiveButton("Log Out") { usersViewModel.logout() }
                    negativeButton("Cancel") {}
                }.show()
            }, 300)
        }
    }

    private fun openLink(url: String) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun initNoInternet() {
        noInternetDialog = NoInternetDialog.Builder(this)
                .setCancelable(true)
                .build()
    }

    private fun initLogoutObserver() {
        usersViewModel.genericResponseLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> showLoading("Logging out...")

                Status.SUCCESS -> {
                    runDelayed(500) {
                        hideLoading()
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.TOPIC_GLOBAL)

                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        overridePendingTransition(R.anim.enter_a, R.anim.exit_b)
                        finish()
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    longToast("${it.error}. Please try again")
                }
            }
        })
    }

    override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {
        mainViewPager.setCurrentItem(position, true)
        newMeme?.isVisible = position == 0
        editProfile?.isVisible = position == 3

        when(position) {
            0 -> supportActionBar?.title = APP_NAME
            1 -> supportActionBar?.title = FAVES
            2 -> supportActionBar?.title = NOTIFICATIONS
            3 -> supportActionBar?.title = PROFILE
        }

        try {
            if (position == 0 && wasSelected) allFragment.getRecyclerView().smoothScrollToPosition(0)
        } catch (e: Exception) {
            Timber.e("Error scrolling to top: ${e.localizedMessage}")
        }

        return true
    }

    override fun onPositionChange(y: Int) {
        mainViewPager?.setCurrentItem(y, true)
    }

    override fun onPageSelected(position: Int) {
        bottomNav.currentItem = position
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        newMeme = menu?.findItem(R.id.menu_add_meme)
        editProfile = menu?.findItem(R.id.menu_edit_profile)

        editProfile?.icon = setDrawable(this, Ionicons.Icon.ion_edit, R.color.textGray, 16)
        newMeme?.icon = setDrawable(this, Ionicons.Icon.ion_image, R.color.textGray, 22)

        Handler().post {
            val view: View = findViewById(R.id.menu_add_meme)
            showPostMemeTip(view)
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun showPostMemeTip(v: View) {
        SpotlightView.Builder(this)
                .introAnimationDuration(400)
                .enableRevealAnimation(true)
                .performClick(true)
                .fadeinTextDuration(400)
                .headingTvColor(Color.parseColor("#eb273f"))
                .headingTvSize(32)
                .headingTvText("Post Meme")
                .subHeadingTvColor(Color.parseColor("#ffffff"))
                .subHeadingTvSize(16)
                .subHeadingTvText("Tap here to share your meme collection :)")
                .maskColor(Color.parseColor("#dc000000"))
                .target(v)
                .lineAnimDuration(400)
                .lineAndArcColor(Color.parseColor("#eb273f"))
                .dismissOnTouch(true)
                .dismissOnBackPress(true)
                .enableDismissAfterShown(true)
                .usageId("POST_MEME") //UNIQUE ID
                .show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.menu_add_meme -> {
                startActivity(Intent(this, PostActivity::class.java))
                overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
            }

            R.id.menu_edit_profile -> {
                startActivity(Intent(this, EditProfileActivity::class.java))
                overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
            }
        }

        return true
    }

    override fun onResume() {
        super.onResume()
        drawerName.text = sessionManager.getUsername()
        //refreshToken()
    }

    override fun onBackPressed() {
        if (slidingDrawer.isMenuOpened) {
            slidingDrawer.closeMenu(true)

        } else {
            if (doubleBackToExit) {
                finishAffinity()

            } else {
                toast("Tap back again to exit")
                doubleBackToExit = true

                Handler().postDelayed({doubleBackToExit = false}, 1500)
            }
        }
    }

    override fun onDestroy() {
        noInternetDialog.onDestroy()
        super.onDestroy()
    }


}
