package com.botob.ulisten2.fragments

import android.app.ActionBar
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.botob.ulisten2.R

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the [
 * design guidelines](https://developer.android.com/design/patterns/navigation-drawer.html#Interaction) for a complete explanation of the behaviors implemented here.
 */
class NavigationDrawerFragment : Fragment() {
    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private var mCallbacks: NavigationDrawerCallbacks? = null

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerListView: ListView
    private lateinit var mFragmentContainerView: View
    private var mCurrentSelectedPosition = 0
    private var mFromSavedInstanceState = false
    private var mUserLearnedDrawer = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        val sp = PreferenceManager.getDefaultSharedPreferences(activity)
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false)
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION)
            mFromSavedInstanceState = true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mDrawerListView = inflater.inflate(R.layout.drawer_main, container, false) as ListView
        mDrawerListView.onItemClickListener = OnItemClickListener { _, _, position, _ -> selectItem(position) }
        mDrawerListView.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1, arrayOf(
                getString(R.string.drawer_title_section1),
                getString(R.string.drawer_title_section2)))
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true)
        return mDrawerListView
    }

    val isDrawerOpen: Boolean
        get() = mDrawerLayout.isDrawerOpen(mFragmentContainerView)

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    fun setUp(fragmentId: Int, drawerLayout: DrawerLayout) {
        mFragmentContainerView = requireActivity().findViewById(fragmentId)
        mDrawerLayout = drawerLayout

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
        // set up the drawer's list view with items and click listener
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = object : ActionBarDrawerToggle(
                activity,  /* host Activity */
                mDrawerLayout,  /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close /* "close drawer" description for accessibility */
        ) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                if (!isAdded) {
                    return
                }
                requireActivity().invalidateOptionsMenu() // calls onPrepareOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                if (!isAdded) {
                    return
                }
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true
                    val sp = PreferenceManager
                            .getDefaultSharedPreferences(activity)
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply()
                }
                requireActivity().invalidateOptionsMenu() // calls onPrepareOptionsMenu()
            }
        }

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView)
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post { mDrawerToggle.syncState() }
        mDrawerLayout.addDrawerListener(mDrawerToggle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return if (mDrawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun selectItem(position: Int) {
        mCurrentSelectedPosition = position
        mDrawerListView.setItemChecked(position, true)
        mDrawerLayout.closeDrawer(mFragmentContainerView)
        mCallbacks?.onNavigationDrawerItemSelected(position)
    }

    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        mCallbacks = try {
            activity as NavigationDrawerCallbacks
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement NavigationDrawerCallbacks.")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mCallbacks = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig)
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private fun showGlobalContextActionBar() {
        val actionBar = actionBar
        actionBar!!.setDisplayShowTitleEnabled(true)
        actionBar.navigationMode = ActionBar.NAVIGATION_MODE_STANDARD
        actionBar.setTitle(R.string.app_name)
    }

    private val actionBar: ActionBar?
        get() = requireActivity().actionBar

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        fun onNavigationDrawerItemSelected(position: Int)
    }

    companion object {
        /**
         * Remember the position of the selected item.
         */
        private const val STATE_SELECTED_POSITION = "selected_navigation_drawer_position"

        /**
         * Per the design guidelines, you should show the drawer on launch until the user manually
         * expands it. This shared preference tracks this.
         */
        private const val PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned"
    }
}