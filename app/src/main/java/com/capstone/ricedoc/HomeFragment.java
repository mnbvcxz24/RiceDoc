package com.capstone.ricedoc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.capstone.ricedoc.ml.Densenet121adam60;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final int REQUEST_CODE_PERMISSION = 11;
    private static final int REQUEST_CODE_CAMERA = 12;
    private static final int REQUEST_CODE_GALLERY = 13;
    private static final int REQUEST_CODE_STORAGE = 14;
    private static final int REQUEST_CODE_CROP = 15;
    int IMAGE_SIZE = 224;
    Button camera, gallery;
    ImageButton btnLanguage;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnLanguage = view.findViewById(R.id.btnLanguage);
        //CLICK LISTENER FOR THE LANGUAGE BUTTON
        btnLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageMenu(v);
            }
        });

        //PERMISSIONS AND CAMERA/GALLERY BUTTON
        getCameraPermission();
        camera = view.findViewById(R.id.camera);
        Drawable icon = getResources().getDrawable(R.drawable.baseline_photo_camera_24);
        camera.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        gallery = view.findViewById(R.id.image);

        //CLICK LISTENER FOR CAMERA/GALLERY BUTTON
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
        });

        setLocale(loadSelectedLanguage());
        return view;
    }
    private void showLanguageMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.inflate(R.menu.language_menu);

        // Set item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_item_english) {
                    setLocale("en");
                    return true;
                } else if (itemId == R.id.menu_item_cebuano) {
                    setLocale("ceb");
                    return true;
                }
                return false;
            }
        });
        // Show the popup menu
        popupMenu.show();
    }
    private void setLocale(String languageCode) {
        Locale newLocale = new Locale(languageCode);
        Locale currentLocale = getResources().getConfiguration().locale;

        if (!currentLocale.equals(newLocale)) {
            Locale.setDefault(newLocale);

            Configuration config = new Configuration();
            config.locale = newLocale;

            Context context = requireContext();
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

            saveSelectedLanguage(languageCode);

            restartActivity();
        }
    }
    private void saveSelectedLanguage(String languageCode) {
        SharedPreferences preferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("selected_language", languageCode);
        editor.apply();
    }
    private String loadSelectedLanguage() {
        SharedPreferences preferences = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE);

        return preferences.getString("selected_language", "en");
    }
    private void restartActivity() {
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        requireActivity().startActivity(intent);
    }
    private boolean getCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Camera permission not granted, request it
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // Camera permission not granted, request it again
                    getCameraPermission();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri selectedImageUri = data.getData();

                startImageCropper(selectedImageUri);
            } else {
                Toast.makeText(requireContext(), "Image selection canceled", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_CAMERA) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                Bitmap originalBitmap = (Bitmap) data.getExtras().get("data");
                Uri tempUri = getImageUri(requireContext(), originalBitmap);

                startImageCropper(tempUri);
            } else {
                Toast.makeText(requireContext(), "Image capture canceled", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
        if (result.isSuccessful()) {
            Bitmap croppedBitmap = BitmapFactory.decodeFile(result.getUriFilePath(getActivity().getApplicationContext(), true));

            int dimension = Math.min(croppedBitmap.getWidth(), croppedBitmap.getHeight());
            Bitmap thumbnailBitmap = ThumbnailUtils.extractThumbnail(croppedBitmap, dimension, dimension);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, IMAGE_SIZE, IMAGE_SIZE, false);

            classifyImage(resizedBitmap, thumbnailBitmap);
        } else {
            Toast.makeText(requireContext(), "Image cropping canceled or failed", Toast.LENGTH_LONG).show();
        }
    });
    private void startImageCropper(Uri imageUri) {
        CropImageOptions cropImageOptions = new CropImageOptions();
        cropImageOptions.allowRotation = true;
        cropImageOptions.imageSourceIncludeCamera = true;
        cropImageOptions.imageSourceIncludeGallery = true;
        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(imageUri, cropImageOptions);
        cropImage.launch(cropImageContractOptions);
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }


    private void classifyImage(Bitmap resizedBitmap, Bitmap thumbnailBitmap) {
        try {
            Densenet121adam60 model = Densenet121adam60.newInstance(requireActivity().getApplication());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
            resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());
            int pixel = 0;
            for(int i = 0; i < IMAGE_SIZE; i++){
                for(int j = 0; j < IMAGE_SIZE;  j++){
                    int val = intValues[pixel++]; //RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Densenet121adam60.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            //find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            // Log the results
            //Log.d("InferenceResult", "Confidences: " + Arrays.toString(confidences));
            //Log.d("InferenceResult", "Max Confidence: " + maxConfidence);
            //Log.d("InferenceResult", "Max Position: " + maxPos);

            String[] classes = {"Brown Spot","Healthy","Leaf Blast","Leaf Folder","Sheath Blight","Stem Borer","Tungro Virus","Unknown"};
            String result = classes[maxPos];
            String conPercentage = String.format("%.2f%%", maxConfidence * 100);

            navigateToResultPage(thumbnailBitmap, result, conPercentage);
            model.close();

        } catch (IOException e) {
            // TODO Handle the exception
        }
    }


    private void navigateToResultPage(Bitmap thumbnailBitmap, String result, String conPercentage) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnailBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageByteArray = stream.toByteArray();

        Intent loadingIntent = new Intent(requireContext(), LoadingScreen.class);

        loadingIntent.putExtra("imageByteArray", imageByteArray);
        loadingIntent.putExtra("disease", result);
        loadingIntent.putExtra("confident_key", conPercentage);
        startActivity(loadingIntent);
    }
}
