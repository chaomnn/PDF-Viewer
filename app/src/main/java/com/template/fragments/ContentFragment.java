package com.template.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.template.R;
import com.template.databinding.ButtonRowItemBinding;
import com.template.databinding.FragmentContentBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ContentFragment extends Fragment {

    private static final String EXTRA_CURRENT_PAGE = "currentPage";
    private static final String KEY_BOOKMARKS = "bookmarks";

    private FragmentContentBinding binding;
    private ActionBarDrawerToggle barDrawerToggle;
    private int currentPage;

    public ContentFragment() {
        super(R.layout.fragment_content);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPage = savedInstanceState != null ? savedInstanceState.getInt(EXTRA_CURRENT_PAGE) : 0;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentContentBinding.bind(view);
        TreeSet<String> bookmarks = new TreeSet<>((b1, b2) ->
                Integer.compare(Integer.parseInt(b1), Integer.parseInt(b2)));
        Set<String> initialBookmarks = getPreferences().getStringSet(KEY_BOOKMARKS, null);
        if (initialBookmarks != null) {
            bookmarks.addAll(initialBookmarks);
        }
        barDrawerToggle = new ActionBarDrawerToggle(null, binding.drawerLayout, binding.toolbar, 0, 0);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);
        final Adapter adapter = new Adapter(new AdapterCallback() {
            @Override
            public void onClick(int index) {
                binding.pdfView.jumpTo(index, true);
                binding.drawerLayout.closeDrawers();
            }

            @Override
            public void onDelete(int index) {
                bookmarks.remove(String.valueOf(index + 1));
                getPreferences().edit().putStringSet(KEY_BOOKMARKS, new HashSet<>(bookmarks)).apply();
                final Adapter adapter = Objects.requireNonNull((Adapter) binding.recyclerView.getAdapter());
                adapter.updateDataSet(bookmarks);
                if (currentPage == index) {
                    updateBookmarkIcon(false);
                }
            }
        });
        adapter.updateDataSet(bookmarks);

        binding.drawerLayout.addDrawerListener(barDrawerToggle);
        setHasOptionsMenu(true);
        barDrawerToggle.syncState();

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);

        String assetName = null;
        try {
            for (String name : getResources().getAssets().list("")) {
                if (name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                    assetName = name;
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        binding.pdfView.fromAsset(Objects.requireNonNull(assetName))
                .enableSwipe(true).swipeHorizontal(true)
                .defaultPage(currentPage)
                .onPageChange((page, pageCount) -> {
                    currentPage = page;
                    binding.progressBar.setText(getString(R.string.progress, page + 1, pageCount));
                    updateBookmarkIcon(bookmarks.contains(String.valueOf(page + 1)));
                    binding.backButton.setEnabled(page > 0);
                    binding.forwardButton.setEnabled(page < binding.pdfView.getPageCount() - 1);
                })
                .load();

        binding.forwardButton.setOnClickListener(v ->
                binding.pdfView.jumpTo(binding.pdfView.getCurrentPage() + 1, true));
        binding.backButton.setOnClickListener(v ->
                binding.pdfView.jumpTo(binding.pdfView.getCurrentPage() - 1, true));
        binding.bookmarkButton.setOnClickListener(v -> {
            String page = String.valueOf(binding.pdfView.getCurrentPage() + 1);
            boolean isItemAdded = !bookmarks.contains(page);
            if (isItemAdded) {
                bookmarks.add(page);
            } else {
                bookmarks.remove(page);
            }
            getPreferences().edit().putStringSet(KEY_BOOKMARKS, new HashSet<>(bookmarks)).apply();
            adapter.updateDataSet(bookmarks);
            updateBookmarkIcon(isItemAdded);
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_CURRENT_PAGE, currentPage);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (barDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private SharedPreferences getPreferences() {
        Context context = requireContext();
        return context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
    }

    private void updateBookmarkIcon(boolean isBookmark) {
        binding.bookmarkButton.setImageResource(isBookmark ? R.drawable.ic_bookmark_fill : R.drawable.ic_bookmark);
    }

    private interface AdapterCallback {
        void onClick(int index);
        void onDelete(int index);
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final AdapterCallback callback;
        private List<String> localDataSet = Collections.emptyList();

        public Adapter(AdapterCallback callback) {
            this.callback = callback;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final String index = localDataSet.get(position);
            holder.binding.button.setText(getString(R.string.page, index));
        }

        @Override
        public int getItemCount() {
            return localDataSet.size();
        }

        public void updateDataSet(SortedSet<String> bookmarks) {
            List<String> newDataSet = new ArrayList<>(bookmarks);
            DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return localDataSet.size();
                }

                @Override
                public int getNewListSize() {
                    return newDataSet.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    String oldIndex = localDataSet.get(oldItemPosition);
                    String newIndex = newDataSet.get(newItemPosition);
                    return oldIndex.equals(newIndex);
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return true;
                }
            }).dispatchUpdatesTo(this);
            localDataSet = Collections.unmodifiableList(newDataSet);
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            public final ButtonRowItemBinding binding;

            public ViewHolder(ViewGroup parent) {
                this(ButtonRowItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }

            private ViewHolder(ButtonRowItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                binding.getRoot().setOnClickListener(view -> {
                    final String index = localDataSet.get(getBindingAdapterPosition());
                    callback.onClick(Integer.parseInt(index) - 1);
                });
                binding.remove.setOnClickListener(view -> {
                    final String index = localDataSet.get(getBindingAdapterPosition());
                    callback.onDelete(Integer.parseInt(index) - 1);
                });
            }
        }
    }
}
