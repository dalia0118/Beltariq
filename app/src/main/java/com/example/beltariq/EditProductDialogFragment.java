package com.example.beltariq;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProductDialogFragment extends DialogFragment {

    private static final String TAG = "EditProductDialogFragment";
    private DatabaseReference mDatabase;
    private ListView productListView;
    private ArrayAdapter<String> adapter;
    private List<String> productNames;
    private Map<String, String> productIdMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_product, container, false);

        productListView = view.findViewById(R.id.productListView);
        Button closeButton = view.findViewById(R.id.closeButton);

        productNames = new ArrayList<>();
        productIdMap = new HashMap<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, productNames);
        productListView.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            mDatabase = FirebaseDatabase.getInstance("https://beltariqproject-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("products").child(currentUser.getUid());
            loadProductNames();
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        productListView.setOnItemClickListener((parent, view1, position, id) -> {
            String productName = productNames.get(position);
            String productId = productIdMap.get(productName);
            showEditProductDialog(productId, productName);
        });

        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void loadProductNames() {
        mDatabase.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                productNames.clear();
                productIdMap.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String productId = snapshot.getKey();
                    String productName = snapshot.child("productName").getValue(String.class);
                    Log.d(TAG, "Product ID: " + productId + ", Product Name: " + productName);
                    if (productName != null && productId != null) {
                        productNames.add(productName);
                        productIdMap.put(productName, productId);
                    }
                }
                if (productNames.isEmpty()) {
                    Log.d(TAG, "No products found for the current user.");
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "Failed to load products", task.getException());
                Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditProductDialog(String productId, String productName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogue_edit_product, null);
        builder.setView(dialogView);

        EditText editTextProductName = dialogView.findViewById(R.id.editTextProductName);
        EditText editTextProductPrice = dialogView.findViewById(R.id.editTextProductPrice);
        EditText editTextProductDescription = dialogView.findViewById(R.id.editTextProductDescription);

        // Set existing values
        mDatabase.child(productId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                editTextProductName.setText(task.getResult().child("productName").getValue(String.class));
                editTextProductPrice.setText(task.getResult().child("productPrice").getValue(String.class));
                editTextProductDescription.setText(task.getResult().child("productDescription").getValue(String.class));
            }
        });

        builder.setTitle("Edit Product")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newProductName = editTextProductName.getText().toString().trim();
                    String newProductPrice = editTextProductPrice.getText().toString().trim();
                    String newProductDescription = editTextProductDescription.getText().toString().trim();

                    if (!newProductName.isEmpty() && !newProductPrice.isEmpty() && !newProductDescription.isEmpty()) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("productName", newProductName);
                        updates.put("productPrice", newProductPrice);
                        updates.put("productDescription", newProductDescription);

                        mDatabase.child(productId).updateChildren(updates)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(getContext(), "Product updated", Toast.LENGTH_SHORT).show();
                                        loadProductNames();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update product", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Delete", (dialog, which) -> {
                    mDatabase.child(productId).removeValue()
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(getContext(), "Product deleted", Toast.LENGTH_SHORT).show();
                                    loadProductNames();
                                } else {
                                    Toast.makeText(getContext(), "Failed to delete product", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
