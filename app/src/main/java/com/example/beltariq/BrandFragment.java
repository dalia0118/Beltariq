package com.example.beltariq;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;

public class BrandFragment extends Fragment {
    private FirebaseAuth mAuth;
    private ImageView imageViewBrand;
    private Uri mImageUri;

    private ActivityResultLauncher<String> selectImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_brand, container, false);

        mAuth = FirebaseAuth.getInstance();
        imageViewBrand = view.findViewById(R.id.imageViewBrand);

        Button logoutButton = view.findViewById(R.id.blogout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        Button addProdButton = view.findViewById(R.id.addprod);
        addProdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AddProductDialogFragment().show(getParentFragmentManager(), "AddProductDialogFragment");
            }
        });

        Button editProdButton = view.findViewById(R.id.editprod);
        editProdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EditProductDialogFragment().show(getParentFragmentManager(), "EditProductDialogFragment");
            }
        });

        // Initialize the ActivityResultLauncher for image selection
        selectImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                mImageUri = uri;
                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(mImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageViewBrand.setImageBitmap(bitmap);

                    // Optional: Use Glide to load image
                    // Glide.with(this).load(mImageUri).into(imageViewBrand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        imageViewBrand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        return view;
    }

    private void openFileChooser() {
        selectImageLauncher.launch("image/*");
    }
}
