package com.example.luzonfinalproject;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bulletin_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Bind text data
        holder.textPostTitle.setText(post.getTitle());
        holder.textPostDescription.setText(post.getDescription());
        holder.textPostAuthor.setText(String.format("By: %s", post.getAuthorName()));
        holder.textPostContact.setText(String.format("Contact: %s", post.getContactInfo()));
        holder.textPostTimestamp.setText(post.getTimestamp());
        holder.textPostCategory.setText(post.getCategory());


        loadPostImage(holder, post);
    }

    private void loadPostImage(PostViewHolder holder, Post post) {
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.imagePost.setVisibility(View.VISIBLE);

            Glide.with(holder.itemView.getContext())
                    .load(post.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_broken_image)
                    .override(800, 600)
                    .centerCrop()
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            holder.imagePost.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.imagePost);
        } else {
            holder.imagePost.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textPostTitle, textPostDescription, textPostAuthor,
                textPostContact, textPostTimestamp, textPostCategory;
        ImageView imagePost;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textPostTitle = itemView.findViewById(R.id.textPostTitle);
            textPostDescription = itemView.findViewById(R.id.textPostDescription);
            textPostAuthor = itemView.findViewById(R.id.textPostAuthor);
            textPostContact = itemView.findViewById(R.id.textPostContact);
            textPostTimestamp = itemView.findViewById(R.id.textPostTimestamp);
            textPostCategory = itemView.findViewById(R.id.textPostCategory);
            imagePost = itemView.findViewById(R.id.imagePost);
            imagePost.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    public void updateData(List<Post> newPostList) {
        this.postList = newPostList;
        notifyDataSetChanged();
    }

    public void clearData() {
        if (postList != null) {
            postList.clear();
            notifyDataSetChanged();
        }
    }
}