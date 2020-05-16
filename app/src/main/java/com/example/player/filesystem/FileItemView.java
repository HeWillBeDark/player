package com.example.player.filesystem;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.player.R;
import com.example.player.base.IAdapterView;
import com.example.player.utils.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/3/16
 * Time: 11:03 PM
 * Desc: FilItemView
 */
public class FileItemView extends RelativeLayout implements IAdapterView<FileWrapper> {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static SimpleDateFormat DATE_FORMATTER;

    ImageView imageViewFile;
    TextView textViewName;
    TextView textViewInfo;
    TextView textViewDate;

    static {
        DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    }

    public FileItemView(Context context) {
        super(context);
        View view = View.inflate(context, R.layout.item_local_file, this);
        imageViewFile = view.findViewById(R.id.image_view_file);
        textViewName  = view.findViewById(R.id.text_view_name);
        textViewInfo  = view.findViewById(R.id.text_view_info);
        textViewDate  = view.findViewById(R.id.text_view_date);
    }

    @Override
    public void bind(FileWrapper fileWrapper, int position) {
        final File file = fileWrapper.file;
        final boolean isItemSelected = fileWrapper.selected;
        if (file.isDirectory()) {
            setSelected(isItemSelected);
            imageViewFile.setImageResource(isItemSelected ? R.drawable.ic_folder_selected : R.drawable.ic_folder);
            File[] files = file.listFiles(SystemFileFilter.DEFAULT_INSTANCE);
            int itemCount = files == null ? 0 : files.length;
            textViewInfo.setText(getContext().getResources().getQuantityString(
                    R.plurals.mp_directory_items_formatter,
                    itemCount, // one
                    itemCount  // other
            ));
        } else {
            setSelected(false);
            if (FileUtils.isVideo(file)) {
                imageViewFile.setImageResource(R.drawable.ic_file_music);
            } else if (FileUtils.isLyric(file)) {
                imageViewFile.setImageResource(R.drawable.ic_file_lyric);
            } else {
                imageViewFile.setImageResource(R.drawable.ic_file);
            }
            textViewInfo.setText(FileUtils.readableFileSize(file.length()));
        }
        textViewName.setText(file.getName());
        Date lastModified = new Date(file.lastModified());
        textViewDate.setText(DATE_FORMATTER.format(lastModified));
    }
}
