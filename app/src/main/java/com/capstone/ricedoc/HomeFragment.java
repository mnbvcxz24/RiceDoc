package com.capstone.ricedoc;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.capstone.ricedoc.ml.Densenet121adam60;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final int REQUEST_CODE_PERMISSION = 11;
    private static final int REQUEST_CODE_CAMERA = 12;
    private static final int REQUEST_CODE_GALLERY = 13;
    int IMAGE_SIZE = 224;
    Button camera, gallery;
    ImageButton btnLanguage;
    private static final String PREF_SELECTED_LANGUAGE = "selected_language";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //TOOLBAR
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        btnLanguage = view.findViewById(R.id.btnLanguage);

        //CLICK LISTENER FOR THE LANGUAGE BUTTON
        btnLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageMenu(v);
            }
        });

        //PERMISSIONS AND CAMERA/GALLERY BUTTON
        getPermission();
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
        SharedPreferences preferences = requireContext().getSharedPreferences(
                requireContext().getPackageName(), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_SELECTED_LANGUAGE, languageCode);
        editor.apply();
    }
    private String loadSelectedLanguage() {
        SharedPreferences preferences = requireContext().getSharedPreferences(
                requireContext().getPackageName(), Context.MODE_PRIVATE);

        return preferences.getString(PREF_SELECTED_LANGUAGE, "en");
    }
    private void restartActivity() {
        Intent intent = requireActivity().getIntent();
        requireActivity().finish();
        requireActivity().startActivity(intent);
    }
    private boolean getPermission() {
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
                    this.getPermission();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // Handle the selected image from the gallery
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                    int maxDimension = 300;
                    int dimension = Math.min(maxDimension, Math.min(originalBitmap.getWidth(), originalBitmap.getHeight()));
                    Bitmap thumbnailBitmap = ThumbnailUtils.extractThumbnail(originalBitmap, dimension, dimension);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, IMAGE_SIZE, IMAGE_SIZE, false);

                    classifyImage(resizedBitmap, thumbnailBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(requireContext(), "Image selection canceled", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_CAMERA) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                Bitmap originalBitmap = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(originalBitmap.getWidth(), originalBitmap.getHeight());
                Bitmap thumbnailBitmap = ThumbnailUtils.extractThumbnail(originalBitmap, dimension, dimension);
                Bitmap resizedBitmap = originalBitmap.createScaledBitmap(originalBitmap, IMAGE_SIZE, IMAGE_SIZE, false);

                classifyImage(resizedBitmap, thumbnailBitmap);
            } else {
                Toast.makeText(requireContext(), "Image capture canceled", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        byte[] byteArray = stream.toByteArray();

        Intent loadingIntent = new Intent(requireContext(), LoadingScreen.class);

        loadingIntent.putExtra("imageByteArray", byteArray);
        loadingIntent.putExtra("text", result);
        loadingIntent.putExtra("confident_key", conPercentage);
        startActivity(loadingIntent);
    }
}
