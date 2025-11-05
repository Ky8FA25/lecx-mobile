package com.example.pe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pe.R;
import com.example.pe.models.Cart;
import com.example.pe.models.Product;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private List<Cart> cartList;
    private List<Product> productList; // để lấy thông tin sản phẩm

    public interface OnCartActionListener {
        void onQuantityChanged(Cart cart, int newQuantity);
        void onItemRemoved(Cart cart);
    }

    private OnCartActionListener listener;

    public CartAdapter(Context context, List<Cart> cartList, List<Product> productList, OnCartActionListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cart cart = cartList.get(position);
        Product product = null;
        for (Product p : productList) {
            if (p.getId() == cart.getProductId()) {
                product = p;
                break;
            }
        }

        if (product != null) {
            holder.name.setText(product.getName());
            holder.price.setText(String.format("$%.2f", product.getPrice()));
            holder.quantity.setText(String.valueOf(cart.getQuantity()));

            Glide.with(context)
                    .load(product.getImage())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.image);
        }

        // tăng giảm số lượng
        holder.btnIncrease.setOnClickListener(v -> {
            int qty = cart.getQuantity() + 1;
            cart.setQuantity(qty);
            holder.quantity.setText(String.valueOf(qty));
            listener.onQuantityChanged(cart, qty);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (cart.getQuantity() > 1) {
                int qty = cart.getQuantity() - 1;
                cart.setQuantity(qty);
                holder.quantity.setText(String.valueOf(qty));
                listener.onQuantityChanged(cart, qty);
            }
        });

        holder.btnRemove.setOnClickListener(v -> listener.onItemRemoved(cart));
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price, quantity;
        ImageButton btnIncrease, btnDecrease, btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cart_img);
            name = itemView.findViewById(R.id.cart_name);
            price = itemView.findViewById(R.id.cart_price);
            quantity = itemView.findViewById(R.id.cart_quantity);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
