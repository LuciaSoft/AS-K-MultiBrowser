package com.luciasoft.browserjavatokotlin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luciasoft.browserjavatokotlin.Utils.getShortName
import java.io.File

internal class MyListAdapter(
    private val act: MultiBrowserActivity,
    private val itemList: ArrayList<DirectoryItem>)
    : RecyclerView.Adapter<MyViewHolder>()
{
    private val mIdMap = HashMap<DirectoryItem, Int>()

    init
    {
        for (i in itemList.indices) mIdMap[itemList[i]] = i
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long
    {
        val item = itemList[position]
        return mIdMap[item]!!.toLong()
    }

    override fun getItemCount(): Int
    {
        return itemList.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder
    {
        val view: View
        if (act.isListView)
        {
            view = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.list_item_list_view, viewGroup, false
            )
        }
        else
        {
            view = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.list_item_tiles_view, viewGroup, false
            )
            if (act.isGalleryView && !act.OPT.showFileNamesInGalleryView) view.findViewById<View>(
                R.id.listItemText
            ).visibility = View.GONE
            else view.findViewById<View>(R.id.listItemText).visibility = View.VISIBLE
        }
        return MyViewHolder(view, act)
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, i: Int)
    {
        val listItem = viewHolder.listItem
        val item = itemList[i]
        val path = item.path
        val name = item.name
        viewHolder.title.text = name
        val image = viewHolder.image
        var exists: Boolean
        run {
            exists = try
            {
                File(path).exists()
            }
            catch (ex: Exception)
            {
                false
            }
        }
        if (!exists)
        {
            val infoType: String
            val iconId: Int
            if (item is FileItem)
            {
                infoType = "file"
                iconId = R.mipmap.ic_file_x
            }
            else  // if (item instanceof MultiBrowserDirectoryItem.FolderItem)
            {
                infoType = "folder"
                iconId = R.mipmap.ic_folder_x
            }
            image.scaleType = ImageView.ScaleType.FIT_CENTER
            image.setImageBitmap(BitmapFactory.decodeResource(act.resources, iconId))
            if (act.isListView)
            {
                val str = "$infoType not found"
                viewHolder.info!!.text = str
            }
            listItem.isClickable = false
            listItem.isLongClickable = false
            listItem.setOnClickListener(null)
            listItem.setOnLongClickListener(null)
            return
        }
        val isFile = item is FileItem
        if (isFile)
        {
            var thumb: Bitmap? = null
            if (act.isGalleryView && act.OPT.showImagesWhileBrowsingGallery || !act.isGalleryView && act.OPT.showImagesWhileBrowsingNormal)
            {
                val fileItem = item as FileItem
                val imageId = fileItem.imageId
                if (imageId != null)
                {
                    thumb = try
                    {
                        MediaStore.Images.Thumbnails.getThumbnail(
                            act.contentResolver, imageId.toLong(),
                            MediaStore.Images.Thumbnails.MINI_KIND, null
                        )
                    }
                    catch (ex: Exception)
                    {
                        null
                    }
                    if (thumb != null)
                    {
                        image.scaleType = ImageView.ScaleType.CENTER_CROP
                        image.setImageBitmap(thumb)
                    }
                }
            }
            if (thumb == null)
            {
                image.scaleType = ImageView.ScaleType.FIT_CENTER
                image.setImageBitmap(BitmapFactory.decodeResource(act.resources, R.mipmap.ic_file))
            }
        }
        else  // item instanceof MultiBrowserDirectoryItem.FolderItem
        {
            image.scaleType = ImageView.ScaleType.FIT_CENTER
            image.setImageBitmap(BitmapFactory.decodeResource(act.resources, R.mipmap.ic_folder))
        }
        var hidden: Boolean
        run {
            hidden = try
            {
                File(path).isHidden
            }
            catch (ex: Exception)
            {
                false
            }
        }
        if (hidden) image.imageAlpha = 127 else image.imageAlpha = 255
        if (act.isListView) viewHolder.info!!.text =
            item.info
        val loadFilesFolders =
            act.OPT.browseMode == BrowseMode.LoadFilesAndOrFolders
        val saveFilesFolders =
            act.OPT.browseMode == BrowseMode.SaveFilesAndOrFolders
        val loadFolders = act.OPT.browseMode == BrowseMode.LoadFolders
        val saveFolders = act.OPT.browseMode == BrowseMode.SaveFolders
        val load = isFile && loadFilesFolders || !isFile && (loadFolders || loadFilesFolders)
        val save = isFile && saveFilesFolders || !isFile && (saveFolders || saveFilesFolders)
        val saveBoxVisible = act.findViewById<View>(R.id.saveFileLayout).visibility != View.GONE
        val sendToSaveBoxShortClick =
            save && isFile && saveBoxVisible && act.ADV.allowShortClickFileForSave &&
                act.ADV.debugMode && act.ADV.shortClickSaveFileBehavior != SaveFileBehavior.SaveFile
        val sendToSaveBoxLongClick =
            save && isFile && saveBoxVisible && act.ADV.allowLongClickFileForSave &&
                act.ADV.debugMode && act.ADV.longClickSaveFileBehavior != SaveFileBehavior.SaveFile
        val shortClickable =
            act.ADV.debugMode || !isFile || sendToSaveBoxShortClick || load && act.ADV.allowShortClickFileForLoad || save && act.ADV.allowShortClickFileForSave
        val longClickable =
            act.ADV.debugMode || sendToSaveBoxLongClick || isFile && (load && act.ADV.allowLongClickFileForLoad || save && act.ADV.allowLongClickFileForSave) || !isFile && (load && act.ADV.allowLongClickFolderForLoad || save && act.ADV.allowLongClickFolderForSave)
        listItem.isClickable = shortClickable
        if (!shortClickable) listItem.setOnClickListener(null)
        else
        {
            listItem.setOnClickListener(View.OnClickListener {
                if (!isFile)
                {
                    act.refreshView(path, false, false)
                    return@OnClickListener
                }

                // item istanceof FileItem
                if (load)
                {
                    act.onSelect(true, true, false, false, path)
                }
                else
                {
                    val saveFile =
                        !saveBoxVisible || act.ADV.shortClickSaveFileBehavior != SaveFileBehavior.SendNameToSaveBoxOrSaveFile
                    if (sendToSaveBoxShortClick) act.setEditTextSaveFileName(
                        getShortName(path)
                    )
                    if (saveFile) act.onSelect(true, false, false, false, path)
                }
            })
        }
        listItem.isLongClickable = longClickable
        if (!longClickable) listItem.setOnLongClickListener(null)
        else
        {
            listItem.setOnLongClickListener(OnLongClickListener {
                if (!isFile)
                {
                    if (load) act.onSelect(false, true, true, false, path)
                    else act.onSelect(
                        false,
                        false,
                        true,
                        false,
                        path
                    )
                    return@OnLongClickListener true
                }

                // item istanceof FileItem
                if (load)
                {
                    act.onSelect(true, true, true, false, path)
                }
                else
                {
                    val saveFile =
                        !saveBoxVisible || act.ADV.longClickSaveFileBehavior != SaveFileBehavior.SendNameToSaveBoxOrSaveFile
                    if (sendToSaveBoxLongClick) act.setEditTextSaveFileName(
                        getShortName(path)
                    )
                    if (saveFile) act.onSelect(true, false, true, false, path)
                }
                true
            })
        }
    }
}

internal class MyViewHolder(view: View, act: MultiBrowserActivity)
    : RecyclerView.ViewHolder(view)
{
    var listItem: LinearLayout
    var title: TextView
    var image: ImageView
    var info: TextView? = null

    init
    {
        listItem = view as LinearLayout
        listItem.setBackgroundColor(act.THM.colorListBackground)
        title = view.findViewById(R.id.listItemText)
        title.typeface = act.THM.getFontBdIt(act.assets)
        image = view.findViewById(R.id.listItemIcon)
        if (act.isGalleryView)
        {
            title.setTextColor(act.THM.colorGalleryItemText)
            title.setTextSize(act.THM.unitSp, act.THM.sizeGalleryViewItemText)
        }
        else
        {
            title.setTextColor(act.THM.colorListItemText)
            if (act.isListView)
            {
                title.setTextSize(act.THM.unitSp, act.THM.sizeListViewItemText)
                info = view.findViewById(R.id.listItemSubText)
                info!!.typeface = act.THM.getFontNorm(act.assets)
                info!!.setTextColor(act.THM.colorListItemSubText)
                info!!.setTextSize(act.THM.unitSp, act.THM.sizeListViewItemSubText)
                val accent = view.findViewById<View>(R.id.listItemAccent)
                accent.setBackgroundColor(act.THM.colorListAccent)
            }
            else
            {
                title.setTextSize(act.THM.unitSp, act.THM.sizeTilesViewItemText)
            }
        }
    }
}