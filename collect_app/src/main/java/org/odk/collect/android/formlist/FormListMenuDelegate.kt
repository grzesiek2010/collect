package org.odk.collect.android.formlist

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.SearchView
import org.odk.collect.android.R
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.android.utilities.MenuDelegate
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

class FormListMenuDelegate(
    private val activity: ComponentActivity,
    private val viewModel: FormListViewModel,
    private val networkStateProvider: NetworkStateProvider
) : MenuDelegate {
    private var outOfSync = false
    private var syncing = false

    override fun onCreateOptionsMenu(menuInflater: MenuInflater, menu: Menu) {
        menuInflater.inflate(R.menu.list_menu, menu)

        menu.findItem(R.id.menu_filter).apply {
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                    menu.findItem(R.id.menu_refresh).isVisible = false
                    menu.findItem(R.id.menu_sort).isVisible = false
                    return true
                }

                override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                    menu.findItem(R.id.menu_refresh).isVisible = viewModel.isMatchExactlyEnabled()
                    menu.findItem(R.id.menu_sort).isVisible = true
                    return true
                }
            })

            (actionView as SearchView).apply {
                queryHint = activity.resources.getString(R.string.search)
                maxWidth = Int.MAX_VALUE
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String) = false

                    override fun onQueryTextChange(newText: String): Boolean {
                        viewModel.filterText.value = newText
                        return false
                    }
                })
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val refreshItem = menu.findItem(R.id.menu_refresh)
        refreshItem.isVisible = viewModel.isMatchExactlyEnabled()
        refreshItem.isEnabled = !syncing
        if (outOfSync) {
            refreshItem.setIcon(R.drawable.ic_baseline_refresh_error_24)
        } else {
            refreshItem.setIcon(R.drawable.ic_baseline_refresh_24)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!MultiClickGuard.allowClick(javaClass.name)) {
            return true
        }

        return when (item.itemId) {
            R.id.menu_refresh -> {
                if (networkStateProvider.isDeviceOnline) {
                    viewModel.syncWithServer().observe(activity) { success: Boolean ->
                        if (success) {
                            ToastUtils.showShortToast(activity, R.string.form_update_succeeded)
                        }
                    }
                } else {
                    ToastUtils.showShortToast(activity, R.string.no_connection)
                }
                true
            }
            R.id.menu_sort -> {
                ListSortingDialog(
                    activity,
                    intArrayOf(
                        R.string.sort_by_name_asc,
                        R.string.sort_by_name_desc,
                        R.string.sort_by_date_asc,
                        R.string.sort_by_date_desc
                    ),
                    viewModel.sortingOrder.value
                ) { newSortingOrder ->
                    viewModel.sortingOrder.value = newSortingOrder
                }.show()

                true
            }
            else -> false
        }
    }

    init {
        viewModel.isSyncingWithServer().observe(activity) { syncing: Boolean ->
            this.syncing = syncing
            activity.invalidateOptionsMenu()
        }

        viewModel.isOutOfSyncWithServer().observe(activity) { outOfSync: Boolean ->
            this.outOfSync = outOfSync
            activity.invalidateOptionsMenu()
        }
    }
}
