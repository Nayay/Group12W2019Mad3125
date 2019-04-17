package com.adb.group12w2019mad3125;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.adb.group12w2019mad3125.Model.Cart;
import com.adb.group12w2019mad3125.Prevalent.Prevalent;
import com.adb.group12w2019mad3125.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CartActivity extends AppCompatActivity {
    private static final String TAG ="Cart List Details" ;
    private RecyclerView rcCartView;
    private RecyclerView.LayoutManager rcCartLayoutManager;
    private TextView txtPrice;
    private Button btnCheckout;
    private  Double totalPrice = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        rcCartView = findViewById(R.id.rcCart);
        rcCartView.setHasFixedSize(true);
        rcCartLayoutManager =new LinearLayoutManager(this);
        rcCartView.setLayoutManager(rcCartLayoutManager);

        btnCheckout = findViewById(R.id.btnCheckoutP);
        txtPrice = findViewById(R.id.txtPrice);

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtPrice.setText("Total Price = "+totalPrice+"$");
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("totalPrice",totalPrice+"");
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        Log.e(TAG,Prevalent.currentOnlineUser.getEmail());
        FirebaseRecyclerOptions<Cart> options = new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef.child("User View")
                        .child(Prevalent.currentOnlineUser.getEmail())
                        .child("Products"), Cart.class).build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final Cart model) {
                holder.txtPName.setText(model.getPname());
                holder.txtPPrice.setText("Price = " + model.getPrice() + "$");
                holder.txtPQuantity.setText("Quantity = " + model.getQuantity());
                Double singleProductPrice = ((Double.parseDouble(model.getPrice()))*Double.parseDouble(model.getQuantity()));
                totalPrice = totalPrice+singleProductPrice;
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options[] = new CharSequence[]{
                                "Edit",
                                "Delete"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    Intent intent = new Intent(CartActivity.this,ProductDetailsActivity.class);
                                    intent.putExtra("pid",model.getPid());
                                    startActivity(intent);
                                }
                                if(which == 1){
                                    cartListRef.child("User View")
                                            .child(Prevalent.currentOnlineUser.getEmail())
                                            .child("Products").child(model.getPid()).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(CartActivity.this,"Item Removed Successfully",Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(CartActivity.this,HomeActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        builder.show();
                    }
                });

            }
            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cart_items_layout,viewGroup,false);
                CartViewHolder holder= new CartViewHolder(view);
                return holder;
            }
        };
        rcCartView.setAdapter(adapter);
        adapter.startListening();
    }
}