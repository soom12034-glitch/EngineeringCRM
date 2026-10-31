package com.amalaei.engineering;

import android.app.AlertDialog;
import android.content.Context;

final class FluentDialog extends AlertDialog.Builder {
    FluentDialog(Context context) {
        super(context);
    }

    @Override
    public AlertDialog create() {
        AlertDialog alertDialogCreate = super.create();
        VisualPolish.dialog(alertDialogCreate);
        return alertDialogCreate;
    }
}
