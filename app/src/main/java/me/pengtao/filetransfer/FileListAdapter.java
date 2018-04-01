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
package me.pengtao.filetransfer;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.hwangjr.rxbus.RxBus;

import java.io.File;
import java.util.List;

import me.pengtao.filetransfer.databinding.LayoutFileItemBinding;
import me.pengtao.filetransfer.util.FileType;
import me.pengtao.filetransfer.util.FileUtils;

/**
 * @author CPPAlien
 */
class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<FileModel> mFileModelList;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public FileListAdapter(Context context, List<FileModel> fileModelList) {
        mContext = context;
        mFileModelList = fileModelList;
        viewBinderHelper.setOpenOnlyOne(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 1) {
            View view = inflater.inflate(R.layout.empty_view, parent, false);
            return new EmptyViewHolder(view);
        } else {
            return new MyViewHolder(LayoutInflater.from(
                    mContext).inflate(R.layout.layout_file_item, parent,
                    false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            ((MyViewHolder) holder).onBindViewHolder(position);
        }
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        return mFileModelList.size() > 0 ? mFileModelList.size() : 1;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        LayoutFileItemBinding mBinding;

        public MyViewHolder(View view) {
            super(view);
            mBinding = DataBindingUtil.bind(view);
        }

        public void onBindViewHolder(int position) {
            FileModel infoModel = mFileModelList.get(position);
            if (infoModel.getFileType() == FileType.TYPE_APK) {
                mBinding.tvName.setText(mContext.getString(R.string.app_name_format,
                        infoModel.getName(), infoModel.getVersion()));
                mBinding.tvDelete.setVisibility(infoModel.isInstalled() ? View.VISIBLE : View.GONE);
                mBinding.tvDelete.setOnClickListener(v ->
                        delete(mContext, infoModel.getPackageName()));
            } else {
                mBinding.tvDelete.setVisibility(View.GONE);
                mBinding.tvName.setText(infoModel.getName());
            }
            viewBinderHelper.bind(mBinding.swipeRevealLayout, infoModel.getPath());

            mBinding.tvSize.setText(infoModel.getSize());
            mBinding.tvPath.setText(infoModel.getPath());
            if (infoModel.getFileType() == FileType.TYPE_IMAGE) {
                Glide.with(mContext).load(new File(infoModel.getPath())).into(mBinding.ivIcon);
            } else {
                mBinding.ivIcon.setImageDrawable(infoModel.getIcon());
            }

            mBinding.mainLayout.setOnClickListener(v ->
                    FileUtils.openFile(infoModel.getPath(), mContext));

            mBinding.delete.setOnClickListener(view -> {
                File file = new File(infoModel.getPath());
                if (file.exists() && file.isFile() && file.delete()) {
                    RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0);
                }
            });
            mBinding.share.setOnClickListener(view -> share(mContext, infoModel.getPath()));
            mBinding.executePendingBindings();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mFileModelList.size() == 0) {
            return 1;
        }
        return super.getItemViewType(position);
    }

    private void delete(Context context, String packageName) {
        Uri uri = Uri.fromParts("package", packageName, null);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        context.startActivity(intent);
    }

    private void share(Context context, String filePath) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, FileUtils.getFileUri(context, filePath));
        shareIntent.setType(FileUtils.getShareType(filePath));
        context.startActivity(Intent.createChooser(shareIntent, ""));
    }
}