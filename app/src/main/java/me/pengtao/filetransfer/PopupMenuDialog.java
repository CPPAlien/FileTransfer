package me.pengtao.filetransfer;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.pengtao.filetransfer.util.WifiUtils;

/**
 * @author chris
 */
public class PopupMenuDialog {
    private Unbinder mUnbinder;
    @BindView(R.id.popup_menu_title)
    TextView mTxtTitle;
    @BindView(R.id.popup_menu_subtitle)
    TextView mTxtSubTitle;
    @BindView(R.id.shared_wifi_state)
    ImageView mImgLanState;
    @BindView(R.id.shared_wifi_state_hint)
    TextView mTxtStateHint;
    @BindView(R.id.shared_wifi_address)
    TextView mTxtAddress;
    @BindView(R.id.shared_wifi_settings)
    Button mBtnWifiSettings;
    @BindView(R.id.shared_wifi_button_split_line)
    View mButtonSplitLine;
    private Context context;
    private Dialog dialog;
    private Display display;

    public PopupMenuDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        RxBus.get().register(this);
    }

    public PopupMenuDialog builder() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.layout_popup_menu_dialog, null);

        view.setMinimumWidth(display.getWidth());

        dialog = new Dialog(context, R.style.PopupMenuDialogStyle);
        dialog.setContentView(view);
        mUnbinder = ButterKnife.bind(this, dialog);
        dialog.setOnDismissListener(this::onDialogDismiss);

        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);

        return this;
    }

    public PopupMenuDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    public PopupMenuDialog setCanceledOnTouchOutside(boolean cancel) {
        dialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public void show() {
        dialog.show();
        String ip = WifiUtils.getDeviceIpAddress();
        onWifiConnected(ip);
        WebService.start(context);
    }

    @OnClick({R.id.shared_wifi_cancel, R.id.shared_wifi_settings})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.shared_wifi_cancel:
                dialog.dismiss();
                break;
            case R.id.shared_wifi_settings:
                context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;
            default:
                break;
        }
    }

    void onWifiConnected(String ipAddr) {
        mTxtTitle.setText(R.string.wlan_enabled);
        mTxtTitle.setTextColor(context.getResources().getColor(R.color.colorWifiConnected));
        mImgLanState.setImageResource(R.drawable.shared_wifi_enable);
        mTxtStateHint.setText(R.string.pls_input_the_following_address_in_pc_browser);
        String address = String.format(context.getString(R.string.http_address), ipAddr, Constants.HTTP_PORT);
        mTxtAddress.setText(address);
        mBtnWifiSettings.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", address);
            cm.setPrimaryClip(mClipData);
            Toast.makeText(context, context.getString(R.string.copy_toast), Toast.LENGTH_LONG).show();
        });
    }

    void onDialogDismiss(DialogInterface dialog) {
        if (mUnbinder != null) {
            mUnbinder.unbind();
            RxBus.get().post(Constants.RxBusEventType.POPUP_MENU_DIALOG_SHOW_DISMISS, Constants.MSG_DIALOG_DISMISS);
            //unregisterWifiConnectChangedReceiver();
            RxBus.get().unregister(PopupMenuDialog.this);
        }
    }
}
