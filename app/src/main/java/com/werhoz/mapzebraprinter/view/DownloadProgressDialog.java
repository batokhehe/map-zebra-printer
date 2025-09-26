package com.werhoz.mapzebraprinter.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.werhoz.mapzebraprinter.R;

public class DownloadProgressDialog extends DialogFragment {
    private LinearProgressIndicator progressProducts, progressPrices;
    private TextView tvTitle, tvProduct, tvPrice;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View view = requireActivity().getLayoutInflater()
                .inflate(R.layout.dialog_download_progress, null);

        tvTitle = view.findViewById(R.id.tvTitle);
        progressProducts = view.findViewById(R.id.progressProducts);
        progressPrices = view.findViewById(R.id.progressPrices);
        tvProduct = view.findViewById(R.id.tvProductPercent);
        tvPrice = view.findViewById(R.id.tvPricePercent);

        builder.setView(view);
        builder.setCancelable(false);
        return builder.create();
    }

    public void setTitle(String title) {
        if (tvTitle != null) tvTitle.setText(title);
    }

    public void setProductProgress(int percent) {
        if (progressProducts != null) {
            progressProducts.setProgressCompat(percent, true);
            tvProduct.setText(String.format("%d%%", percent));
        }
    }

    public void setPriceProgress(int percent) {
        if (progressPrices != null) {
            progressPrices.setProgressCompat(percent, true);
            tvPrice.setText(String.format("%d%%", percent));
        }
    }
}
