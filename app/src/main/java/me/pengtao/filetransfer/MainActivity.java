package me.pengtao.filetransfer;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.pengtao.filetransfer.util.FileType;
import me.pengtao.filetransfer.util.FileUtils;
import timber.log.Timber;

/**
 * @author chris
 */
public class MainActivity extends AppCompatActivity implements Animator.AnimatorListener {
    private static final int WRITE_PERMISSION_CODE = 1;
    private static final int FILE_FETCH_CODE = 2;
    private String mAlreadyWrited = "";
    Unbinder mUnbinder;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.recyclerview)
    RecyclerView mAppList;
    @BindView(R.id.content_main)
    SwipeRefreshLayout mSwipeRefreshLayout;
    List<FileModel> mFileModelList = new ArrayList<>();
    FileListAdapter mAppshelfAdapter;

    public synchronized static Drawable getIconFromPackageName(String packageName, Context
            context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            Context otherAppCtx = context.createPackageContext(packageName, Context
                    .CONTEXT_IGNORE_SECURITY);
            List<Integer> displayMetrics = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                displayMetrics.add(DisplayMetrics.DENSITY_XXXHIGH);
            }
            displayMetrics.add(DisplayMetrics.DENSITY_XXHIGH);
            displayMetrics.add(DisplayMetrics.DENSITY_XHIGH);
            displayMetrics.add(DisplayMetrics.DENSITY_HIGH);
            displayMetrics.add(DisplayMetrics.DENSITY_TV);
            for (int displayMetric : displayMetrics) {
                try {
                    Drawable d = otherAppCtx.getResources().getDrawableForDensity(pi
                            .applicationInfo.icon, displayMetric);
                    if (d != null) {
                        return d;
                    }
                } catch (Resources.NotFoundException e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            // Handle Error here
        }
        ApplicationInfo appInfo;
        try {
            appInfo = pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return appInfo.loadIcon(pm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.delete_all:
                    if (!mFileModelList.isEmpty()) {
                        showDialog();
                    }
                    break;
                case R.id.add_files:
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, FILE_FETCH_CODE);
                    break;
                default:
                    break;
            }
            return false;
        });
        Timber.plant(new Timber.DebugTree());
        RxBus.get().register(this);
        initRecyclerView();
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip() != null) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            if (item != null && item.getText() != null && item.getText().length() > 0 && !item.getText().equals(mAlreadyWrited)) {
                File file = new File(Constants.DIR, "clipboard_" + String.valueOf(System.currentTimeMillis()) + ".txt");
                try {
                    FileUtils.writeByteArrayToFile(file, item.getText().toString().getBytes(), false);
                    Toast.makeText(this, "已把剪切板中内容写入到该文件中", Toast.LENGTH_SHORT).show();
                    RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0);
                    mAlreadyWrited = item.getText().toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "文件写入失败", Toast.LENGTH_SHORT).show();
                }
            }
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_menu, menu);
        return true;
    }

    @OnClick(R.id.fab)
    public void onClick(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mFab, "translationY", 0, mFab
                    .getHeight() * 2).setDuration(200L);
            objectAnimator.setInterpolator(new AccelerateInterpolator());
            objectAnimator.addListener(this);
            objectAnimator.start();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.permission_setting);
            builder.setMessage(R.string.permission_need_des);
            builder.setPositiveButton(R.string.permission_go, (dialog, which) -> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        RxBus.get().unregister(this);
    }

    @SuppressWarnings("unused")
    @Subscribe(tags = {@Tag(Constants.RxBusEventType.POPUP_MENU_DIALOG_SHOW_DISMISS)})
    public void onPopupMenuDialogDismiss(Integer type) {
        if (type == Constants.MSG_DIALOG_DISMISS) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mFab, "translationY", mFab
                    .getHeight() * 2, 0).setDuration(200L);
            objectAnimator.setInterpolator(new AccelerateInterpolator());
            objectAnimator.start();
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sure_delete_all);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> deleteAll());
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @Override
    public void onAnimationStart(Animator animation) {
        new PopupMenuDialog(this).builder().setCancelable(false)
                .setCanceledOnTouchOutside(true).show();
    }

    @Override
    public void onAnimationEnd(Animator animation) {
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    void initRecyclerView() {
        mAppshelfAdapter = new FileListAdapter(this, mFileModelList);
        mAppList.setHasFixedSize(true);
        mAppList.setLayoutManager(new LinearLayoutManager(this));
        mAppList.setAdapter(mAppshelfAdapter);
        RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0);

        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(() ->
                RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0));
    }

    private void handleFiles(String path, long length) {
        FileModel fileModel = new FileModel();
        PackageManager pm = getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, 0);

        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = path;
            appInfo.publicSourceDir = path;
            String packageName = appInfo.packageName;
            String version = info.versionName;
            Drawable icon = pm.getApplicationIcon(appInfo);
            String appName = pm.getApplicationLabel(appInfo).toString();
            if (TextUtils.isEmpty(appName)) {
                appName = getApplicationName(packageName);
            }
            if (icon == null) {
                icon = getIconFromPackageName(packageName, this);
            }
            fileModel.setName(appName);
            fileModel.setPackageName(packageName);
            fileModel.setPath(path);
            fileModel.setSize(getFileSize(length));
            fileModel.setVersion(version);
            fileModel.setIcon(icon);
            fileModel.setFileType(FileType.TYPE_APK);
            fileModel.setInstalled(isAvailable(this, packageName));
            mFileModelList.add(fileModel);
        } else {
            fileModel.setFileType(FileUtils.getFileType(path));
            fileModel.setPath(path);
            String[] pathItems = path.split(File.separator);
            fileModel.setName(pathItems[pathItems.length - 1]);
            fileModel.setSize(getFileSize(length));
            Drawable icon = ContextCompat.getDrawable(this, FileUtils.getFileTypeIcon(path));
            fileModel.setIcon(icon);
            mFileModelList.add(fileModel);
        }
    }

    private String getFileSize(long length) {
        DecimalFormat df = new DecimalFormat("######0.0");
        if (length < 1024.f) {
            return (int) length + "B";
        } else if (length < 1024 * 1024.f) {
            return df.format(length / 1024.f) + "K";
        } else if (length < 1024 * 1024 * 1024.f) {
            return df.format((length / 1024.f / 1024.f)) + "M";
        }
        return df.format(length / 1024.f / 1024.f / 1024.f) + "G";
    }

    @SuppressWarnings("unused")
    @Subscribe(thread = EventThread.IO, tags = {@Tag(Constants.RxBusEventType.LOAD_BOOK_LIST)})
    public void loadFileList(Integer type) {
        File dir = Constants.DIR;
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }

            FileUtils.sortWithLastModified(files);
            mFileModelList.clear();
            for (File file : files) {
                handleFiles(file.getAbsolutePath(), file.length());
            }
        }
        runOnUiThread(() -> {
            mSwipeRefreshLayout.setRefreshing(false);
            mAppshelfAdapter.notifyDataSetChanged();
        });
    }

    public String getApplicationName(String packageName) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo;
        try {
            packageManager = getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        if (applicationInfo != null) {
            return (String) packageManager.getApplicationLabel(applicationInfo);
        }
        return packageName;
    }

    /**
     * 判断相对应的APP是否存在
     *
     * @param context                                                                  context
     * @param packageName(包名)(若想判断QQ，则改为com.tencent.mobileqq，若想判断微信，则改为com.tencent.mm)
     * @return
     */
    public boolean isAvailable(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        //获取手机系统的所有APP包名，然后进行一一比较
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if ((pinfo.get(i)).packageName
                    .equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }

    //删除所有文件
    private void deleteAll() {
        File dir = Constants.DIR;
        if (dir.exists() && dir.isDirectory()) {
            File[] fileNames = dir.listFiles();
            if (fileNames != null) {
                for (File fileName : fileNames) {
                    fileName.delete();
                }
            }
        }
        RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent
            data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_FETCH_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    ContentResolver content = getContentResolver();
                    FileUtils.copyFile(content.openInputStream(data.getData()), Constants.DIR
                            + File.separator + FileUtils.getFileName(this, uri));
                    Toast.makeText(this, R.string.please_refresh_web, Toast.LENGTH_LONG).show();
                    RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0);
                } catch (IOException e) {
                    Toast.makeText(this, R.string.read_file_failed, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.read_file_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
