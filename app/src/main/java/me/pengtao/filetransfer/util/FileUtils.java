/*
 * Copyright (c) 2018 CPPAlien
 *
 * Licensed under the GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://github.com/CPPAlien/FileTransfer/blob/master/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.pengtao.filetransfer.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import me.pengtao.filetransfer.R;

/**
 * @author CPPAlien
 */
public class FileUtils {
    /**
     * 获得文件的 uri
     *
     * @param context  context
     * @param filePath 文件路径
     * @return uri
     */
    public static Uri getFileUri(Context context, String filePath) {
        File file = new File(filePath);
        return FileProvider.getUriForFile(context, context.getPackageName() + "" +
                ".fileprovider", file);
    }

    public static boolean openFile(String filePath, Context context) {
        int fileType = getFileType(filePath);
        File file = new File(filePath);

        if (file.isFile()) {
            Intent intent = null;
            Uri contentUri = getFileUri(context, filePath);
            switch (fileType) {
                case FileType.TYPE_IMAGE:
                    intent = getImageFileIntent(contentUri);
                    break;
                case FileType.TYPE_AUDIO:
                    intent = getAudioFileIntent(contentUri);
                    break;
                case FileType.TYPE_VIDEO:
                    intent = getVideoFileIntent(contentUri);
                    break;
                case FileType.TYPE_WEB:
                    intent = getHtmlFileIntent(contentUri);
                    break;
                case FileType.TYPE_TEXT:
                    intent = getTextFileIntent(contentUri);
                    break;
                case FileType.TYPE_EXCEL:
                    intent = getExcelFileIntent(contentUri);
                    break;
                case FileType.TYPE_WORD:
                    intent = getWordFileIntent(contentUri);
                    break;
                case FileType.TYPE_PPT:
                    intent = getPPTFileIntent(contentUri);
                    break;
                case FileType.TYPE_PDF:
                    intent = getPdfFileIntent(contentUri);
                    break;
                case FileType.TYPE_PACKAGE:
                case FileType.TYPE_APK:
                    intent = getApkFileIntent(context, file);
                    break;
                default:
                    new AlertDialog.Builder(context)
                            .setMessage(R.string.no_program_open_it)
                            .setPositiveButton(R.string.ok, null)
                            .show();
                    break;
            }
            if (intent != null) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
                return true;
            }
        }
        return false;
    }

    public static int getFileType(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();

        for (int i = 0; i < FileType.FileTypes.length; i++) {
            int j = checkStringEnds(fileName, FileType.FileTypes[i]);
            if (j == -1) {
                continue;
            }
            return FileType.TypeStart[i];
        }
        return FileType.TYPE_UNKNOWN;
    }

    private static int checkStringEnds(String item, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (item.endsWith(array[i])) {
                return i;
            }
        }

        return -1;
    }

    public static int getFileTypeIcon(String path) {
        int fileType = getFileType(path);
        switch (fileType) {
            case FileType.TYPE_IMAGE:
                return R.drawable.image;
            case FileType.TYPE_AUDIO:
                return R.drawable.audio;
            case FileType.TYPE_VIDEO:
                return R.drawable.vedio;
            case FileType.TYPE_WEB:
                return R.drawable.web;
            case FileType.TYPE_TEXT:
                return R.drawable.text;
            case FileType.TYPE_EXCEL:
                return R.drawable.excel;
            case FileType.TYPE_WORD:
                return R.drawable.doc;
            case FileType.TYPE_PPT:
                return R.drawable.ppt;
            case FileType.TYPE_PDF:
                return R.drawable.pdf;
            case FileType.TYPE_PACKAGE:
                return R.drawable.zip;
            default:
                return R.drawable.other_file;
        }
    }

    public static String getShareType(String path) {
        int fileType = getFileType(path);
        switch (fileType) {
            case FileType.TYPE_IMAGE:
                return "image/*";
            case FileType.TYPE_AUDIO:
                return "audio/*";
            case FileType.TYPE_VIDEO:
                return "video/*";
            case FileType.TYPE_WEB:
            case FileType.TYPE_TEXT:
                return "text/*";
            case FileType.TYPE_EXCEL:
            case FileType.TYPE_WORD:
            case FileType.TYPE_PPT:
            case FileType.TYPE_PDF:
            case FileType.TYPE_PACKAGE:
            case FileType.TYPE_APK:
                return "application/*";
            default:
                return "*/*";
        }
    }

    public static void copyFile(InputStream in, String targetLocation) throws IOException {
        OutputStream out = new FileOutputStream(targetLocation);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private static Intent getHtmlFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    private static Intent getImageFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    private static Intent getPdfFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/pdf");
        return intent;
    }

    private static Intent getTextFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "text/plain");
        return intent;
    }

    private static Intent getAudioFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        intent.setDataAndType(uri, "audio/*");
        return intent;
    }

    private static Intent getVideoFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        intent.setDataAndType(uri, "video/*");
        return intent;
    }

    private static Intent getWordFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    private static Intent getExcelFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    private static Intent getPPTFileIntent(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    private static Intent getApkFileIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //兼容7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + "" +
                    ".fileprovider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    public static void sortWithLastModified(File[] files) {
        Arrays.sort(files, (f1, f2) -> {
            long diff = f1.lastModified() - f2.lastModified();
            if (diff > 0) {
                return -1;
            } else if (diff == 0) {
                return 0;
            } else {
                return 1;
            }
        });
    }

    /**
     * Writes a byte array to a file creating the file if it does not exist.
     *
     * @param file  the file to write to
     * @param data  the content to write to the file
     * @param append if {@code true}, then bytes will be added to the
     * end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since IO 2.1
     */
    public static void writeByteArrayToFile(File file, byte[] data, boolean append) throws IOException {
        OutputStream out = null;
        try {
            out = openOutputStream(file, append);
            out.write(data);
            out.close(); // don't swallow close Exception if copy completes normally
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }
}
