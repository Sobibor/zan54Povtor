package com.example.zan54povtor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private PaintArea paintArea;
    private LinearLayout textInputContainer;
    private LinearLayout colorPanel;
    private EditText floatingEditText;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 101;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация элементов интерфейса
        textInputContainer = findViewById(R.id.textInputContainer);
        floatingEditText = findViewById(R.id.floatingEditText);
        paintArea = findViewById(R.id.paintArea);
        colorPanel = findViewById(R.id.colorPanel);

        // Настройка кнопок
        initButtons();
        initCameraButton();
        initSaveButton();
        initColorButtons();
        setupLongClickListeners();
    }

    // Настройка долгого нажатия для панели цветов
    private void setupLongClickListeners() {
        ImageButton btnDraw = findViewById(R.id.btnDraw);
        btnDraw.setOnLongClickListener(v -> {
            colorPanel.setVisibility(colorPanel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            return true;
        });
    }

    // Инициализация кнопок выбора цвета
    private void initColorButtons() {
        ImageButton btnBlack = findViewById(R.id.btnColorBlack);
        ImageButton btnRed = findViewById(R.id.btnColorRed);
        ImageButton btnBlue = findViewById(R.id.btnColorBlue);
        ImageButton btnGreen = findViewById(R.id.btnColorGreen);

        btnBlack.setOnClickListener(v -> updatePaintColor(Color.BLACK));
        btnRed.setOnClickListener(v -> updatePaintColor(Color.RED));
        btnBlue.setOnClickListener(v -> updatePaintColor(Color.BLUE));
        btnGreen.setOnClickListener(v -> updatePaintColor(Color.GREEN));
    }

    // Обновление цвета кисти
    private void updatePaintColor(int color) {
        paintArea.setPaintColor(color);
        colorPanel.setVisibility(View.GONE);
    }

    // Инициализация основных кнопок
    private void initButtons() {
        ImageButton btnDraw = findViewById(R.id.btnDraw);
        ImageButton btnText = findViewById(R.id.btnText);
        ImageButton btnClear = findViewById(R.id.btnClear);

        btnDraw.setOnClickListener(v -> {
            textInputContainer.setVisibility(View.GONE);
            colorPanel.setVisibility(View.GONE);
            hideKeyboard();
        });

        btnText.setOnClickListener(v -> {
            textInputContainer.setVisibility(View.VISIBLE);
            colorPanel.setVisibility(View.GONE);
            floatingEditText.requestFocus();
            showKeyboard();
        });

        btnClear.setOnClickListener(v -> {
            paintArea.clearCanvas();
            colorPanel.setVisibility(View.GONE);
        });
    }

    // Работа с камерой
    private void initCameraButton() {
        ImageButton btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST);
            } else {
                dispatchTakePictureIntent();
            }
        });
    }

    // Сохранение рисунка
    private void initSaveButton() {
        ImageButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST);
            } else {
                saveDrawing();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else if (requestCode == STORAGE_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveDrawing();
        }
    }

    // Создание Intent для камеры
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.zan54povtor.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    // Создание файла для фото
    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Обработка результата фото
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                if (bitmap != null) {
                    paintArea.setBackgroundImage(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка загрузки фото", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Сохранение рисунка в галерею
    private void saveDrawing() {
        if (paintArea.getWidth() == 0 || paintArea.getHeight() == 0) {
            Toast.makeText(this, "Холст пуст!", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(paintArea.getWidth(), paintArea.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        paintArea.draw(canvas);

        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String fileName = "Sketch_" + System.currentTimeMillis() + ".png";
        File file = new File(picturesDir, fileName);

        try {
            if (!picturesDir.exists()) picturesDir.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                Toast.makeText(this, "Сохранено в: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Обработка текста
    public void onTextDoneClick(View view) {
        String text = floatingEditText.getText().toString().trim();
        if (!text.isEmpty()) {
            float x = textInputContainer.getX() + 50;
            float y = textInputContainer.getY() + 100;
            paintArea.addTextElement(x, y, text);
        }
        textInputContainer.setVisibility(View.GONE);
        floatingEditText.setText("");
        hideKeyboard();
    }

    // Управление клавиатурой
    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(floatingEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(floatingEditText.getWindowToken(), 0);
    }
}