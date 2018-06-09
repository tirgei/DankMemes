package com.gelostech.dankmemes.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem

import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.commoners.BaseActivity
import com.gelostech.dankmemes.commoners.DankMemesUtil.setDrawable
import com.gelostech.dankmemes.fragments.CollectionsFragment
import com.gelostech.dankmemes.fragments.HomeFragment
import com.gelostech.dankmemes.fragments.ProfileFragment
import com.gelostech.dankmemes.utils.PagerAdapter
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import com.gelostech.dankmemes.commoners.BottomNavigationViewBehavior
import com.gelostech.dankmemes.utils.setDrawable
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import com.yarolegovich.slidingrootnav.callback.DragStateListener
import kotlinx.android.synthetic.main.drawer_layout.*
import android.content.ActivityNotFoundException
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.alert


class MainActivity : BaseActivity(), AHBottomNavigation.OnTabSelectedListener,
        AHBottomNavigation.OnNavigationPositionListener, ViewPager.OnPageChangeListener, DragStateListener  {
    private var doubleBackToExit = false
    private var newMeme: MenuItem? = null
    private var editProfile: MenuItem? = null
    private lateinit var slidingDrawer: SlidingRootNav
    private lateinit var homeFragment: HomeFragment
    private lateinit var collectionsFragment: CollectionsFragment
    private lateinit var profileFragment: ProfileFragment

    companion object {
        private const val HOME: String = "Home"
        private const val COLLECTIONS: String = "Favorites"
        private const val PROFILE: String = "Profile"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        setupBottomNav()
        setupViewPager()
        setupDrawer()
    }

    //Setup the main toolbar
    private fun setupToolbar() {
        setSupportActionBar(mainToolbar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mainToolbarTitle.text = getString(R.string.app_name)
    }

    //Setup the bottom navigation bar
    private fun setupBottomNav() {
        val homeIcon = setDrawable(this, Ionicons.Icon.ion_ios_home, R.color.secondaryText, 18)
        val momentsIcon = setDrawable(this, FontAwesome.Icon.faw_folder_open2, R.color.secondaryText, 20)
        val growthIcon = setDrawable(this, FontAwesome.Icon.faw_user2, R.color.secondaryText, 18)

        bottomNav.addItem(AHBottomNavigationItem(HOME, homeIcon))
        bottomNav.addItem(AHBottomNavigationItem(COLLECTIONS, momentsIcon))
        bottomNav.addItem(AHBottomNavigationItem(PROFILE, growthIcon))

        bottomNav.defaultBackgroundColor = ContextCompat.getColor(this, R.color.white)
        bottomNav.inactiveColor = ContextCompat.getColor(this, R.color.inactiveColor)
        bottomNav.accentColor = ContextCompat.getColor(this, R.color.colorAccent)
        bottomNav.isBehaviorTranslationEnabled = false
        bottomNav.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        bottomNav.setUseElevation(true, 5f)
        bottomNav.setOnTabSelectedListener(this)
        bottomNav.setOnNavigationPositionListener(this)

    }

    //Setup the main view pager
    private fun setupViewPager() {
        val adapter = PagerAdapter(supportFragmentManager, this)
        homeFragment = HomeFragment()
        profileFragment = ProfileFragment()
        collectionsFragment = CollectionsFragment()

        adapter.addAllFrags(homeFragment, collectionsFragment, profileFragment)
        adapter.addAllTitles(HOME, COLLECTIONS, PROFILE)

        mainViewPager.adapter = adapter
        mainViewPager.addOnPageChangeListener(this)
        mainViewPager.offscreenPageLimit = 2
    }

    //Setup drawer
    private fun setupDrawer() {
        slidingDrawer = SlidingRootNavBuilder(this)
                .withMenuLayout(R.layout.drawer_layout)
                .withDragDistance(150)
                .withToolbarMenuToggle(mainToolbar)
                .addDragStateListener(this)
                .inject()

        mainRoot.setOnClickListener {
            if (slidingDrawer.isMenuOpened) slidingDrawer.closeMenu(true)
        }

        setupDrawerIcons()
        drawerClickListeners()

        drawerName.text = "Vincent Tirgei"
        drawerEmail.text = "tirgeic@gmail.com"

    }

    private fun setupDrawerIcons() {
        drawerRate.setDrawable(setDrawable(this, Ionicons.Icon.ion_ios_star, R.color.white, 18))
        drawerShare.setDrawable(setDrawable(this, Ionicons.Icon.ion_android_share, R.color.white, 18))
        drawerFeedback.setDrawable(setDrawable(this, Ionicons.Icon.ion_ios_email, R.color.white, 18))
        drawerTerms.setDrawable(setDrawable(this, Ionicons.Icon.ion_clipboard, R.color.white, 18))
        drawerPolicy.setDrawable(setDrawable(this, Ionicons.Icon.ion_ios_list, R.color.white, 18))
        drawerLogout.setDrawable(setDrawable(this, Ionicons.Icon.ion_log_out, R.color.white, 18))
    }

    private fun drawerClickListeners() {
        drawerRate.setOnClickListener {
            slidingDrawer.closeMenu(true)

            Handler().postDelayed({
                val uri = Uri.parse(resources.getString(R.string.play_store_link) + this.packageName)
                val goToMarket = Intent(Intent.ACTION_VIEW, uri)

                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToMarket)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse(resources.getString(R.string.play_store_link) + this.packageName)))
                }
            }, 300)
        }

        drawerShare.setOnClickListener {
            slidingDrawer.closeMenu(true)

            Handler().postDelayed({
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                val message = resources.getString(R.string.invite_body) + "\n\n" + resources.getString(R.string.play_store_link) + this.packageName
                intent.putExtra(Intent.EXTRA_TEXT, message)
                startActivity(Intent.createChooser(intent, "Invite pals..."))
            }, 300)
        }

        drawerFeedback.setOnClickListener {
            slidingDrawer.closeMenu(true)

            Handler().postDelayed({
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = Uri.parse("mailto: devtirgei@gmail.com")
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Dank Memes")
                startActivity(Intent.createChooser(emailIntent, "Send feedback"))
            }, 300)
        }

        drawerTerms.setOnClickListener {

        }

        drawerPolicy.setOnClickListener {

        }

        drawerLogout.setOnClickListener {
            slidingDrawer.closeMenu(true)

            Handler().postDelayed({
                alert("Are you sure you want to log out?") {
                    title = "Log out"
                    positiveButton("LOG OUT") {
                        val firebaseAuth = FirebaseAuth.getInstance()
                        firebaseAuth.signOut()

                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        overridePendingTransition(R.anim.enter_a, R.anim.exit_b)
                        finish()
                    }
                    negativeButton("CANCEL") {}
                }.show()
            }, 300)
        }

    }

    override fun onDragEnd(isMenuOpened: Boolean) {

    }

    override fun onDragStart() {

    }

    override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {
        mainViewPager.setCurrentItem(position, true)
        newMeme?.isVisible = position == 0
        editProfile?.isVisible = position == 2

        when(position) {
            0 -> mainToolbarTitle.text = getString(R.string.app_name)
            1 -> mainToolbarTitle.text = COLLECTIONS
            2 -> mainToolbarTitle.text = PROFILE
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

        editProfile?.icon = setDrawable(this, Ionicons.Icon.ion_edit, R.color.textGray, 14)
        newMeme?.icon = setDrawable(this, Ionicons.Icon.ion_plus, R.color.textGray, 14)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.menu_add_meme -> {
                startActivity(Intent(this, PostActivity::class.java))
                overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
            }

            R.id.menu_edit_profile -> {
                val intent = Intent(this, EditProfileActivity::class.java)
                intent.putExtra("user", profileFragment.getUser())
                startActivity(intent)
                overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
            }
        }

        return true
    }

    override fun onBackPressed() {
        if (slidingDrawer.isMenuOpened) {
            slidingDrawer.closeMenu(true)
        } else {
            if (doubleBackToExit) {
                super.onBackPressed()
            } else {
                toast("Tap back again to exit")

                doubleBackToExit = true

                Handler().postDelayed({doubleBackToExit = false}, 1500)
            }
        }
    }


}
