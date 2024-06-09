package com.example.beltariq.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beltariq.Product;
import com.example.beltariq.ProductAdapter;
import com.example.beltariq.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        loadProducts();

        return view;
    }

    private void loadProducts() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://beltariqproject-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                Log.d(TAG, "DataSnapshot: " + dataSnapshot.toString());

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot productsSnapshot = userSnapshot.child("products");
                    for (DataSnapshot productSnapshot : productsSnapshot.getChildren()) {
                        Product product = productSnapshot.getValue(Product.class);
                        if (product != null) {
                            productList.add(product);
                            Log.d(TAG, "Product loaded: " + product.getName());
                        } else {
                            Log.d(TAG, "Product is null for snapshot: " + productSnapshot.toString());
                        }
                    }
                }
                if (productList.isEmpty()) {
                    Log.d(TAG, "No products found.");
                }
                productAdapter.notifyDataSetChanged();
                Log.d(TAG, "Total products loaded: " + productList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load products", databaseError.toException());
            }
        });
    }
}
