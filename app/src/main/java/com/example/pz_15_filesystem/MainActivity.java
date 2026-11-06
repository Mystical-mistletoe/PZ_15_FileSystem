package com.example.pz_15_filesystem;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String FILE_NAME = "document.bin";
    private static final String TXT_NAME = "document.txt";

    private int colorIndex = 0;
    private final int[] COLORS = {Color.BLACK, Color.BLUE, Color.RED, Color.GREEN};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private File getExternalPath() {
        return new File(getExternalFilesDir(null), FILE_NAME);
    }

    private File getExternalTxtPath() {
        return new File(getExternalFilesDir(null), TXT_NAME);
    }


    public void saveText(View view) {
        EditText editor = findViewById(R.id.editor);
        Editable editable = editor.getText();

        try (FileOutputStream fos = new FileOutputStream(getExternalPath())) {
            Parcel parcel = Parcel.obtain();
            try {
                TextUtils.writeToParcel(editable, parcel, 0);
                fos.write(parcel.marshall());
            } finally {
                parcel.recycle();
            }

            try (FileOutputStream txtFos = new FileOutputStream(getExternalTxtPath())) {
                txtFos.write(editable.toString().getBytes());
            }

            Toast.makeText(this, "Файл сохранен", Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public void openText(View view) {
        File file = getExternalPath();
        if (!file.exists()) {
            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        try (FileInputStream fin = new FileInputStream(file)) {
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);

            Parcel parcel = Parcel.obtain();
            Spanned restored;
            try {
                parcel.unmarshall(bytes, 0, bytes.length);
                parcel.setDataPosition(0);
                restored = (Spanned) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
            } finally {
                parcel.recycle();
            }

            TextView textView = findViewById(R.id.text);
            textView.setText(restored);

            EditText editor = findViewById(R.id.editor);
            editor.setText(restored);
            editor.setSelection(restored.length());
        } catch (IOException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int[] sel() {
        EditText ed = findViewById(R.id.editor);
        Editable e = ed.getText();
        if (e == null || e.length() == 0) return null;

        int a = ed.getSelectionStart();
        int b = ed.getSelectionEnd();
        if (a < 0 || b < 0 || a == b) return null;
        return a < b ? new int[]{a, b} : new int[]{b, a};
    }

    private void apply(Object span) {
        int[] r = sel();
        if (r == null) {
            Toast.makeText(this, "Выделите текст", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText ed = findViewById(R.id.editor);
        Editable text = ed.getText();
        if (text == null) return;

        text.setSpan(span, r[0], r[1], Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    public void changeColor(View view) {
        colorIndex = (colorIndex + 1) % COLORS.length;
        apply(new ForegroundColorSpan(COLORS[colorIndex]));
    }

    public void upSize(View view) {
        apply(new RelativeSizeSpan(1.2f));
    }

    public void downSize(View view) {
        apply(new RelativeSizeSpan(0.8f));
    }

    public void goBold(View view) {
        apply(new StyleSpan(Typeface.BOLD));
    }

    public void goItalic(View view) {
        apply(new StyleSpan(Typeface.ITALIC));
    }

    public void goUnderline(View view) {
        apply(new UnderlineSpan());
    }
}