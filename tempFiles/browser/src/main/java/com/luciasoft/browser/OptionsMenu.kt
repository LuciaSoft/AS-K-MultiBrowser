package com.luciasoft.browser

import android.content.DialogInterface
import android.view.Menu
import android.view.MenuItem
import com.luciasoft.browser.MyInputDialog.DoSomethingWithResult
import com.luciasoft.browser.MyMessageBox.Companion.show
import java.io.File

class OptionsMenu
{
    fun onMenuOpened(act: MultiBrowserActivity, menu: Menu)
    {
        var newFolderOptionVisible = false
        var listViewOptionVisible = false
        var tilesViewOptionVisible = false
        var galleryViewOptionVisible = false
        var columnCountOptionVisible = false
        var resetDirectoryOptionVisible = false
        var sortOrderOptionVisible = false
        var showHideFileNamesOptionVisible = false
        if (act.OPT.browserViewType === Options.BrowserViewType.List)
        {
            newFolderOptionVisible = true
            listViewOptionVisible = false
            tilesViewOptionVisible = true
            galleryViewOptionVisible = true
            columnCountOptionVisible = false
            resetDirectoryOptionVisible = true
            sortOrderOptionVisible = true
        }
        else if (act.OPT.browserViewType === Options.BrowserViewType.Tiles)
        {
            newFolderOptionVisible = true
            listViewOptionVisible = true
            tilesViewOptionVisible = false
            galleryViewOptionVisible = true
            columnCountOptionVisible = true
            resetDirectoryOptionVisible = true
            sortOrderOptionVisible = true
        }
        else if (act.OPT.browserViewType === Options.BrowserViewType.Gallery)
        {
            newFolderOptionVisible = false
            listViewOptionVisible = true
            tilesViewOptionVisible = true
            galleryViewOptionVisible = false
            columnCountOptionVisible = true
            resetDirectoryOptionVisible = false
            showHideFileNamesOptionVisible = true
            sortOrderOptionVisible = true
        }
        if (act.OPT.browseMode === Options.BrowseMode.LoadFilesAndOrFolders)
        {
            newFolderOptionVisible = false
        }
        if (!act.ADV.menuOptionListViewEnabled) listViewOptionVisible = false
        if (!act.ADV.menuOptionTilesViewEnabled) tilesViewOptionVisible = false
        if (!act.ADV.menuOptionGalleryViewEnabled) galleryViewOptionVisible = false
        if (!act.ADV.menuOptionColumnCountEnabled) columnCountOptionVisible = false
        if (!act.ADV.menuOptionSortOrderEnabled) sortOrderOptionVisible = false
        if (!act.ADV.menuOptionResetDirectoryEnabled) resetDirectoryOptionVisible = false
        if (!act.ADV.menuOptionShowHideFileNamesEnabled) showHideFileNamesOptionVisible = false
        if (!act.ADV.menuOptionNewFolderEnabled) newFolderOptionVisible = false
        menu.findItem(R.id.menuItemNewFolder).isVisible = newFolderOptionVisible
        menu.findItem(R.id.menuItemListView).isVisible = listViewOptionVisible
        menu.findItem(R.id.menuItemTilesView).isVisible = tilesViewOptionVisible
        menu.findItem(R.id.menuItemGalleryView).isVisible = galleryViewOptionVisible
        menu.findItem(R.id.menuItemColumnCount).isVisible = columnCountOptionVisible
        menu.findItem(R.id.menuItemResetDir).isVisible = resetDirectoryOptionVisible
        menu.findItem(R.id.menuItemSortOrder).isVisible = sortOrderOptionVisible
        menu.findItem(R.id.menuItemShowHideFileNames).isVisible = showHideFileNamesOptionVisible
    }

    fun onOptionsItemSelected(act: MultiBrowserActivity, item: MenuItem): Boolean
    {
        val itemId = item.itemId
        if (itemId == R.id.menuItemNewFolder)
        {
            var dir = act.OPT.currentDir ?: return true
            
            val dlg = MyInputDialog(
                act,
                "Create New Folder",
                "New Folder Name",
                object : DoSomethingWithResult
                {
                    override fun doSomething(result: String)
                    {
                        var result = result
                        result = result.trim { it <= ' ' }
                        if (result.isEmpty()) return
                        if (!dir.endsWith("/")) dir += "/"
                        dir += result
                        try
                        {
                            if (File(dir).exists())
                            {
                                show(
                                    act,
                                    "Directory Exists",
                                    "The directory already exists.",
                                    MyMessageBox.ButtonsType.Ok,
                                    null, null
                                )
                                return
                            }
                            val success = File(dir).mkdirs()
                            if (!success)
                            {
                                show(
                                    act,
                                    "Error",
                                    "Could not create directory.",
                                    MyMessageBox.ButtonsType.Ok,
                                    null, null
                                )
                                return
                            }
                            act.refreshView(dir, true, false)
                        }
                        catch (ex: Exception)
                        {
                        }
                    }
                })
            dlg.show()
            return true
        }
        if (itemId == R.id.menuItemListView)
        {
            if (act.OPT.browserViewType === Options.BrowserViewType.List) return false
            act.OPT.browserViewType = Options.BrowserViewType.List
            act.refreshView(true, true)
            return true
        }
        if (itemId == R.id.menuItemTilesView)
        {
            if (act.OPT.browserViewType === Options.BrowserViewType.Tiles) return false
            act.OPT.browserViewType = Options.BrowserViewType.Tiles
            act.refreshView(true, true)
            return true
        }
        if (itemId == R.id.menuItemGalleryView)
        {
            if (act.OPT.browserViewType === Options.BrowserViewType.Gallery) return false
            act.OPT.browserViewType = Options.BrowserViewType.Gallery
            act.refreshView(true, true)
            return true
        }
        if (itemId == R.id.menuItemColumnCount)
        {
            val counts = Array(10) { "" }
            for (i in 0..9) counts[i] = "" + (i + 1)
            val listDlg = MyListDialog()
            val galleryView = act.OPT.browserViewType === Options.BrowserViewType.Gallery
            val columnCount =
                if (galleryView) act.OPT.galleryViewColumnCount else act.OPT.normalViewColumnCount
            listDlg.show(
                act,
                "Column Count",
                counts,
                columnCount - 1,
                DialogInterface.OnClickListener { dialog, which ->
                    val count = listDlg.choice + 1
                    var refresh = false
                    if (galleryView)
                    {
                        if (count != act.OPT.galleryViewColumnCount)
                        {
                            act.OPT.galleryViewColumnCount = count
                            refresh = true
                        }
                    }
                    else
                    {
                        if (count != act.OPT.normalViewColumnCount)
                        {
                            act.OPT.normalViewColumnCount = count
                            refresh = true
                        }
                    }
                    if (refresh) act.refreshView(false, true)
                })
            return true
        }
        if (itemId == R.id.menuItemResetDir)
        {
            act.OPT.currentDir = act.OPT.defaultDir
            act.refreshView(true, false)
            return true
        }
        if (itemId == R.id.menuItemSortOrder)
        {
            val index: Int
            val sortOrder = if (act.OPT.browserViewType === Options.BrowserViewType.Gallery) act.OPT.galleryViewSortOrder else act.OPT.normalViewSortOrder
            index = when (sortOrder)
            {
                Options.SortOrder.PathAscending -> 0
                Options.SortOrder.PathDescending -> 1
                Options.SortOrder.DateAscending -> 2
                Options.SortOrder.DateDescending -> 3
                Options.SortOrder.SizeAscending -> 4
                Options.SortOrder.SizeDescending -> 5
                else -> 0
            }
            val options = arrayOf(
                "Path Ascending", "Path Descending",
                "Date Ascending", "Date Descending",
                "Size Ascending", "Size Descending"
            )
            val listDlg = MyListDialog()
            listDlg.show(act, "Sort Order", options, index) { dialog, which ->
                val option = listDlg.choice
                val order =
                when (option)
                {
                    0 -> Options.SortOrder.PathAscending
                    1 -> Options.SortOrder.PathDescending
                    2 -> Options.SortOrder.DateAscending
                    3 -> Options.SortOrder.DateDescending
                    4 -> Options.SortOrder.SizeAscending
                    5 -> Options.SortOrder.SizeDescending
                    else -> null
                }
                var refresh = false
                if (order != null)
                {
                    if (act.OPT.browserViewType === Options.BrowserViewType.Gallery)
                    {
                        if (act.OPT.galleryViewSortOrder !== order)
                        {
                            act.OPT.galleryViewSortOrder = order
                            refresh = true
                        }
                    }
                    else
                    {
                        if (act.OPT.normalViewSortOrder !== order)
                        {
                            act.OPT.normalViewSortOrder = order
                            refresh = true
                        }
                    }
                }
                if (refresh) act.refreshView(true, false)
            }
            return true
        }
        if (itemId == R.id.menuItemShowHideFileNames)
        {
            act.OPT.showFileNamesInGalleryView = !act.OPT.showFileNamesInGalleryView
            act.refreshView(false, false)
            return true
        }
        return false
    }
}