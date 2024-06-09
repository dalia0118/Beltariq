package com.example.beltariq;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;

public class AddProductDialogFragment extends DialogFragment {

    private static final String TAG = "AddProductDialogFragment";
    private DatabaseReference mDatabase;
    private StorageReference mStorageReference;
    private Uri mImageUri;
    private ImageView imageViewProduct;
    private EditText editTextProductName, editTextProductPrice, editTextDescription;
    private Spinner spinnerCategory;

    private ActivityResultLauncher<String> selectImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_addproduct, container, false);

        // Initialize database and storage references
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Handle the case where the user is not logged in
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            dismiss();
            return view;
        }
        String userId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance("https://beltariqproject-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users").child(userId).child("products");
        mStorageReference = FirebaseStorage.getInstance().getReference("product_images");

        editTextProductName = view.findViewById(R.id.editTextProductName);
        editTextProductPrice = view.findViewById(R.id.editTextProductPrice);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        imageViewProduct = view.findViewById(R.id.imageViewProduct);
        spinnerCategory = view.findViewById(R.id.spinner);
        Button buttonSelectImage = view.findViewById(R.id.buttonSelectImage);
        Button buttonSave = view.findViewById(R.id.buttonSave);

        // Set up the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.spinner_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Initialize the ActivityResultLauncher for image selection
        selectImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                mImageUri = uri;
                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(mImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageViewProduct.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productName = editTextProductName.getText().toString();
                String productPrice = editTextProductPrice.getText().toString();
                String description = editTextDescription.getText().toString();
                String category = spinnerCategory.getSelectedItem().toString();

                if (productName.isEmpty() || productPrice.isEmpty() || description.isEmpty() || category.isEmpty() || mImageUri == null) {
                    Toast.makeText(getContext(), "Please fill out all fields and select an image", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveProductToDatabase(userId, productName, productPrice, description, category);
            }
        });

        return view;
    }

    private void openFileChooser() {
        selectImageLauncher.launch("image/*");
    }

    private void saveProductToDatabase(String userId, String productName, String productPrice, String description, String category) {
        if (mImageUri != null) {
            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis() + "_" + mImageUri.getLastPathSegment());
            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        String productId = mDatabase.push().getKey();
                        Product product = new Product(productId, productName, productPrice, description, category, imageUrl);

                        if (productId != null) {
                            mDatabase.child(productId).setValue(product).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Product added to database: " + productId);
                                    Toast.makeText(getContext(), "Product added", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                } else {
                                    Log.e(TAG, "Failed to add product to database: " + task.getException().getMessage());
                                    Toast.makeText(getContext(), "Failed to add product", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e(TAG, "Failed to get database reference for product.");
                            Toast.makeText(getContext(), "Failed to add product", Toast.LENGTH_SHORT).show();
                        }
                    }))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to upload image: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public static class Product {
        public String id;
        public String name;
        public String price;
        public String description;
        public String category;
        public String imageUrl;

        public Product() {
            // Default constructor required for calls to DataSnapshot.getValue(Product.class)
        }

        public Product(String id, String name, String price, String description, String category, String imageUrl) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.description = description;
            this.category = category;
            this.imageUrl = imageUrl;
        }
    }
}
