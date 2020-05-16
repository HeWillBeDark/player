package com.example.player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.example.player.base.OnItemClickListener;
import com.example.player.filesystem.FileSystemAdapter;
import com.example.player.filesystem.FileTreeStack;
import com.example.player.filesystem.FileWrapper;
import com.example.player.filesystem.SystemFileFilter;
import com.example.player.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("avcodec");
        System.loadLibrary("avfilter");
        System.loadLibrary("avformat");
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("swscale");
    }

    private static final String TAG = MainActivity.class.getSimpleName();

    private View emptyView;
    private RecyclerView mRvFiles;
    private FileSystemAdapter mAdapter;

    File mFileParent;
    List<FileWrapper> mFiles;
    FileTreeStack mFileTreeStack;
    List<File> mSelectedFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
        LoadFileTask LoadFileTask = new LoadFileTask();
        LoadFileTask.execute(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    private void initData() {

        mFileTreeStack = new FileTreeStack();
    }

    private void initView() {
        emptyView = findViewById(R.id.tv_empty);
        mAdapter = new FileSystemAdapter(this, null);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                FileWrapper fileWrapper = mAdapter.getItem(position);
                File file = fileWrapper.file;
                if (file.isDirectory()) {
                    storeSnapshot();
                    LoadFileTask loadFileTask = new LoadFileTask();
                    loadFileTask.execute(file.getAbsolutePath());
                } else {
                    if (FileUtils.isVideo(file)) {
                        VideoAct.actionStart(MainActivity.this, file.getPath());
                    } else {
                        Toast.makeText(MainActivity.this, R.string.not_video, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mRvFiles = findViewById(R.id.rv_files);
        mRvFiles.setLayoutManager(new LinearLayoutManager(this));
        mRvFiles.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mFileTreeStack.size() == 0) {
            super.onBackPressed();
        } else {
            restoreSnapshot(mFileTreeStack.pop());
        }
    }

    // FileTreeSnapshot

    private void storeSnapshot() {
        FileTreeStack.FileTreeSnapshot snapshot = new FileTreeStack.FileTreeSnapshot();
        snapshot.parent = mFileParent;
        snapshot.files = mFiles;
        snapshot.scrollOffset = mRvFiles.computeVerticalScrollOffset();
        mFileTreeStack.push(snapshot);
    }

    private void restoreSnapshot(FileTreeStack.FileTreeSnapshot snapshot) {
        final File parent = snapshot.parent;
        final List<FileWrapper> files = snapshot.files;
        final int scrollOffset = snapshot.scrollOffset;

        mFileParent = parent;
        mFiles = files;

        final int oldScrollOffset = mRvFiles.computeVerticalScrollOffset();

//        toolbar.setTitle(getToolbarTitle(parent));
        mAdapter.setData(files);
        mAdapter.notifyDataSetChanged();
        toggleEmptyViewVisibility();

        mRvFiles.scrollBy(0, scrollOffset - oldScrollOffset);
    }

    @SuppressLint("StaticFieldLeak")
    //需要手动给存储权限
    private class LoadFileTask extends AsyncTask<String, Integer, List<FileWrapper>> {

        private File parent;

        @Override
        protected List<FileWrapper> doInBackground(String... params) {
            parent = new File(params[0]);
            List<File> files = Arrays.asList(parent.listFiles(SystemFileFilter.DEFAULT_INSTANCE));
            Collections.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    if (f1.isDirectory() && f2.isFile()) {
                        return -1;
                    }
                    if (f2.isDirectory() && f1.isFile()) {
                        return 1;
                    }
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });
            // Wrap files
            List<FileWrapper> fileWrappers = new ArrayList<>(files.size());
            for (File file : files) {
                fileWrappers.add(new FileWrapper(file));
            }
            return fileWrappers;
        }

        @Override
        protected void onPostExecute(List<FileWrapper> files) {
            onFilesLoaded(parent, files);
            toggleEmptyViewVisibility();
        }
    }


    private void onFilesLoaded(File parent, List<FileWrapper> files) {
        mFileParent = parent;
        mFiles = files;
        mAdapter.setData(files);
        mAdapter.notifyDataSetChanged();
        mRvFiles.scrollToPosition(0);
    }

    private void toggleEmptyViewVisibility() {
        emptyView.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }


}
