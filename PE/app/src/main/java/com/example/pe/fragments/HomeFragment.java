        package com.example.pe.fragments;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.SearchView;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.fragment.app.Fragment;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.denzcoskun.imageslider.ImageSlider;
        import com.denzcoskun.imageslider.constants.ScaleTypes;
        import com.denzcoskun.imageslider.models.SlideModel;
        import com.example.pe.R;
        import com.example.pe.activities.ShowAllActivity;
        import com.example.pe.adapters.CategoryAdapter;
        import com.example.pe.adapters.ProductAdapter;
        import com.example.pe.data.CategoryDAO;
        import com.example.pe.data.ProductDAO;
        import com.example.pe.models.Category;
        import com.example.pe.models.Product;
        import android.widget.AdapterView;
        import android.widget.Spinner;
        import java.util.Collections;
        import java.util.Comparator;

        import java.util.ArrayList;
        import java.util.List;

        public class HomeFragment extends Fragment {

            TextView catShowAll, popularShowAll, newProductShowAll;
            RecyclerView catRecyclerView, newProductRecyclerView, popularRecyclerView;

            CategoryAdapter categoryAdapter;
            ProductAdapter newProductAdapter, popularProductAdapter;

            List<Category> categoryList;
            List<Product> allProductList;  // ds gốc
            List<Product> newProductList;
            List<Product> popularProductList;

            public HomeFragment() {
            }

            @Override
            public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState) {

                View root = inflater.inflate(R.layout.fragment_home, container, false);

                catRecyclerView = root.findViewById(R.id.rec_category);
                newProductRecyclerView = root.findViewById(R.id.new_product_rec);
                popularRecyclerView = root.findViewById(R.id.popular_rec);

                catShowAll = root.findViewById(R.id.category_see_all);
    //            newProductShowAll = root.findViewById(R.id.newProducts_see_all);
                popularShowAll = root.findViewById(R.id.popular_see_all);

                // Sự kiện See All
                View.OnClickListener seeAllClick = v -> {
                    Intent intent = new Intent(getContext(), ShowAllActivity.class);
                    startActivity(intent);
                };
                catShowAll.setOnClickListener(seeAllClick);
    //            newProductShowAll.setOnClickListener(seeAllClick);
                popularShowAll.setOnClickListener(seeAllClick);

                // Slider
                setupImageSlider(root);

                // Category
                setupCategoryRecycler();

                // New products
                setupNewProductRecycler();

                Spinner sortSpinner = root.findViewById(R.id.sort_spinner);
                sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (newProductList == null || newProductList.isEmpty()) return;

//                        if (position == 0) { // Sort ascending
//                            Collections.sort(newProductList, Comparator.comparingDouble(Product::getPrice));
//                        } else if (position == 1) { // Sort descending
//                            Collections.sort(newProductList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
//                        }

//                        newProductAdapter.notifyDataSetChanged();
                        if (position == 0) { // tăng dần
                            newProductAdapter.sortByPrice(true);
                        } else if (position == 1) { // giảm dần
                            newProductAdapter.sortByPrice(false);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                // Popular products (tạm thời dùng chung ds Product)
                setupPopularProductRecycler();

                SearchView searchView = root.findViewById(R.id.search_view);
                searchView.clearFocus(); // tránh tự focus khi mở fragment

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        filterProducts(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filterProducts(newText);
                        return true;
                    }
                });
                return root;
            }
            private void filterProducts(String query) {
                if (allProductList == null) return;

                List<Product> filteredList = new ArrayList<>();
                if (query == null || query.trim().isEmpty()) {
                    //  ô search trống -> trả về ds gốc
                    filteredList = new ArrayList<>(allProductList);
                } else {
                    for (Product p : allProductList) {
                        if (p.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredList.add(p);
                        }
                    }
                }

                newProductAdapter.updateList(filteredList);
                newProductList = filteredList;

                // Sort lại theo spinner
                Spinner sortSpinner = getView().findViewById(R.id.sort_spinner);
                int selectedPos = sortSpinner.getSelectedItemPosition();
                if (selectedPos == 0) {
                    newProductAdapter.sortByPrice(true);
                } else if (selectedPos == 1) {
                    newProductAdapter.sortByPrice(false);
                }


            }
            private void setupImageSlider(View root) {
                ImageSlider imageSlider = root.findViewById(R.id.image_slider);
                List<SlideModel> slideModels = new ArrayList<>();

                slideModels.add(new SlideModel(R.drawable.banner1, "Discount On Men Clothing", ScaleTypes.CENTER_CROP));
                slideModels.add(new SlideModel(R.drawable.banner2, "Discount On Home Applicances", ScaleTypes.CENTER_CROP));
                slideModels.add(new SlideModel(R.drawable.banner3, "70% OFF", ScaleTypes.CENTER_CROP));

                imageSlider.setImageList(slideModels);
            }

            private void setupCategoryRecycler() {
                catRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
                categoryList = new ArrayList<>();

                CategoryDAO categoryDAO = new CategoryDAO(getContext());
                categoryDAO.open();
                categoryList = categoryDAO.getAllCategories();
                categoryDAO.close();

                if (categoryList.isEmpty()) {
                    Toast.makeText(getContext(), "No categories found", Toast.LENGTH_SHORT).show();
                }

                categoryAdapter = new CategoryAdapter(getContext(), categoryList);
                catRecyclerView.setAdapter(categoryAdapter);
            }

            private void setupNewProductRecycler() {
                newProductRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
                newProductList = new ArrayList<>();

                ProductDAO productDAO = new ProductDAO(getContext());
                productDAO.open();
//                newProductList = productDAO.getAllProducts();
                allProductList = productDAO.getAllProducts();
                productDAO.close();

                newProductList = new ArrayList<>(allProductList);
                newProductAdapter = new ProductAdapter(getContext(), newProductList, product -> {
                });
                newProductRecyclerView.setAdapter(newProductAdapter);
            }

            private void setupPopularProductRecycler() {
                popularRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
                popularProductList = new ArrayList<>();

                // Hiện tại dùng cùng danh sách sản phẩm
                ProductDAO productDAO = new ProductDAO(getContext());
                productDAO.open();
                popularProductList = productDAO.getAllProducts();
                productDAO.close();

                popularProductAdapter = new ProductAdapter(getContext(), popularProductList, product -> {
                });
                popularRecyclerView.setAdapter(popularProductAdapter);
            }
        }
