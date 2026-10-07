package com.roomchatapps.Pmishra.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.roomchatapps.Pmishra.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageCropDialog extends Dialog {

    public interface OnImageCroppedListener {
        void onImageCropped(Bitmap croppedBitmap, Uri croppedUri);
    }

    private final Uri imageUri;
    private final OnImageCroppedListener listener;

    private ImageView ivPreview;
    private Bitmap workingBitmap;

    // Interactive Touch & Gesture Matrix variables
    private final Matrix matrix = new Matrix();
    private final Matrix savedMatrix = new Matrix();
    private final PointF startPoint = new PointF();
    private ScaleGestureDetector scaleDetector;

    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_ZOOM = 2;
    private int touchMode = MODE_NONE;

    public ImageCropDialog(@NonNull Context context, @NonNull Uri imageUri, @NonNull OnImageCroppedListener listener) {
        super(context);
        this.imageUri = imageUri;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_image_crop, null, false);
        setContentView(view);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setDimAmount(0.85f);
        }
        setCancelable(true);

        ivPreview = view.findViewById(R.id.ivCropPreview);
        if (ivPreview != null) {
            ivPreview.setScaleType(ImageView.ScaleType.MATRIX);
        }
        ImageView btnClose = view.findViewById(R.id.btnClose);
        MaterialButton btnRotate = view.findViewById(R.id.btnRotate);
        MaterialButton btnUseOriginal = view.findViewById(R.id.btnUseOriginal);
        MaterialButton btnCropAndApply = view.findViewById(R.id.btnCropAndApply);

        setupTouchGestures();
        loadImage();

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        if (btnRotate != null) {
            btnRotate.setOnClickListener(v -> applyRotation());
        }

        if (btnUseOriginal != null) {
            btnUseOriginal.setOnClickListener(v -> resetImageMatrix());
        }

        if (btnCropAndApply != null) {
            btnCropAndApply.setOnClickListener(v -> cropAndSave());
        }
    }

    private void setupTouchGestures() {
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                if (ivPreview != null) ivPreview.setImageMatrix(matrix);
                return true;
            }
        });

        if (ivPreview != null) {
            ivPreview.setOnTouchListener((v, event) -> {
                scaleDetector.onTouchEvent(event);

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        savedMatrix.set(matrix);
                        startPoint.set(event.getX(), event.getY());
                        touchMode = MODE_DRAG;
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        touchMode = MODE_ZOOM;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (touchMode == MODE_DRAG) {
                            float dx = event.getX() - startPoint.x;
                            float dy = event.getY() - startPoint.y;
                            matrix.set(savedMatrix);
                            matrix.postTranslate(dx, dy);
                            ivPreview.setImageMatrix(matrix);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        touchMode = MODE_NONE;
                        break;
                }
                v.performClick();
                return true;
            });
        }
    }

    private void loadImage() {
        try (InputStream is = getContext().getContentResolver().openInputStream(imageUri)) {
            if (is != null) {
                workingBitmap = BitmapFactory.decodeStream(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (workingBitmap != null) {
            try (InputStream is = getContext().getContentResolver().openInputStream(imageUri)) {
                if (is != null) {
                    ExifInterface exif = new ExifInterface(is);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int rotationDegrees = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationDegrees = 90;
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationDegrees = 180;
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationDegrees = 270;

                    if (rotationDegrees != 0) {
                        Matrix rotMatrix = new Matrix();
                        rotMatrix.postRotate(rotationDegrees);
                        workingBitmap = Bitmap.createBitmap(workingBitmap, 0, 0, workingBitmap.getWidth(), workingBitmap.getHeight(), rotMatrix, true);
                    }
                }
            } catch (Throwable ignored) {}

            ivPreview.setImageBitmap(workingBitmap);
            ivPreview.post(this::resetImageMatrix);
        } else {
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private void resetImageMatrix() {
        if (workingBitmap == null || ivPreview == null) return;

        int viewWidth = ivPreview.getWidth();
        int viewHeight = ivPreview.getHeight();
        if (viewWidth <= 0 || viewHeight <= 0) return;

        int imgWidth = workingBitmap.getWidth();
        int imgHeight = workingBitmap.getHeight();

        float scale = Math.max((float) viewWidth / imgWidth, (float) viewHeight / imgHeight);

        float dx = (viewWidth - imgWidth * scale) / 2f;
        float dy = (viewHeight - imgHeight * scale) / 2f;

        matrix.reset();
        matrix.postScale(scale, scale);
        matrix.postTranslate(dx, dy);

        ivPreview.setImageMatrix(matrix);
    }

    private void applyRotation() {
        if (workingBitmap == null) return;
        Matrix rotMatrix = new Matrix();
        rotMatrix.postRotate(90);
        workingBitmap = Bitmap.createBitmap(workingBitmap, 0, 0, workingBitmap.getWidth(), workingBitmap.getHeight(), rotMatrix, true);
        ivPreview.setImageBitmap(workingBitmap);
        resetImageMatrix();
    }

    private void cropAndSave() {
        if (workingBitmap == null || ivPreview == null) {
            dismiss();
            return;
        }

        try {
            int viewWidth = ivPreview.getWidth();
            int viewHeight = ivPreview.getHeight();
            if (viewWidth <= 0 || viewHeight <= 0) {
                viewWidth = 280;
                viewHeight = 280;
            }

            // Render current matrix transformed bitmap view into a 1:1 high quality canvas
            Bitmap rendered = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(rendered);
            canvas.drawBitmap(workingBitmap, matrix, null);

            File file = new File(getContext().getCacheDir(), "cropped_avatar_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(file)) {
                rendered.compress(Bitmap.CompressFormat.JPEG, 92, out);
                Uri croppedUri = Uri.fromFile(file);
                if (listener != null) {
                    listener.onImageCropped(rendered, croppedUri);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onImageCropped(workingBitmap, imageUri);
            }
        }
        dismiss();
    }
}
