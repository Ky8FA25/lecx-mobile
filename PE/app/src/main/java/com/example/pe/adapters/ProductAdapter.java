package com.example.pe.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pe.R;
import com.example.pe.activities.DetailedActivity;
import com.example.pe.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnProductClickListener listener;

    private OnProductManageListener manageListener;

    private String userRole;

    // Giao diện click
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;

    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(String.format("%.2f", product.getPrice()));
        holder.itemView.setOnLongClickListener(v -> {
            // Hiện menu Edit/Delete
            PopupMenu popup = new PopupMenu(context, v);
            popup.inflate(R.menu.menu_product_item);
            popup.setOnMenuItemClickListener(item -> {
                if (manageListener != null) {
                    if (item.getItemId() == R.id.menu_edit) {
                        manageListener.onEdit(product);
                    } else if (item.getItemId() == R.id.menu_delete) {
                        manageListener.onDelete(product);
                    }
                }
                return true;
            });
            popup.show();
            return true;
        });
        // Load ảnh bằng Glide
        Glide.with(context)
                .load(product.getImage())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.imgProduct);

        // Xử lý sự kiện click item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public void setManageListener(OnProductManageListener listener) {
        this.manageListener = listener;
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    public void sortByPrice(boolean ascending) {
        if (productList == null || productList.isEmpty()) return;

        if (ascending) {
            productList.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice())); // tăng dần
        } else {
            productList.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice())); // giảm dần
        }

        notifyDataSetChanged();
    }


    // ViewHolder
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.product_img);
            txtName = itemView.findViewById(R.id.all_product_name);
            txtPrice = itemView.findViewById(R.id.product_price);
        }
    }

    public interface OnProductManageListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }
}
