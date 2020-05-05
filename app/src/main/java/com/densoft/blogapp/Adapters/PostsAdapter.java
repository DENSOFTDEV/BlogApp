package com.densoft.blogapp.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.densoft.blogapp.CommentActivity;
import com.densoft.blogapp.Constant;
import com.densoft.blogapp.EditPostActivity;
import com.densoft.blogapp.HomeActivity;
import com.densoft.blogapp.R;
import com.densoft.blogapp.model.Post;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostHolder> {

    private Context context;
    private ArrayList<Post> list;
    private ArrayList<Post> listAll;
    private SharedPreferences preferences;

    public PostsAdapter(Context context, ArrayList<Post> list) {
        this.context = context;
        this.list = list;
        this.listAll = new ArrayList<>(list);
        preferences = context.getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post,parent,false);
        return  new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostHolder holder, final int position) {

        final Post post = list.get(position);
        Picasso.get().load(Constant.url+"storage/profiles/"+post.getUser().getPhoto()).into(holder.imgProfile);
        Picasso.get().load(Constant.url+"storage/posts/"+post.getPhoto()).into(holder.imgPost);
        holder.txtName.setText(post.getUser().getUserName());
        holder.txtComments.setText("View all "+post.getComments());
        holder.txtLikes.setText(post.getLikes()+" Likes");
        holder.txtDate.setText(post.getDate());
        holder.txtDesc.setText(post.getDesc());

        holder.btnLike.setImageResource(
                post.isSelfLike()? R.drawable.ic_favorite_red_24dp : R.drawable.ic_favorite_outline_24dp
        );

        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.btnLike.setImageResource(
                        post.isSelfLike()? R.drawable.ic_favorite_outline_24dp : R.drawable.ic_favorite_red_24dp
                );

                StringRequest request = new StringRequest(Request.Method.POST, Constant.Like_post, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Post mPost = list.get(position);

                        try {
                            JSONObject object = new JSONObject(response);
                            if (object.getBoolean("success")){
                                mPost.setSelfLike(post.isSelfLike());
                                mPost.setLikes(mPost.isSelfLike()?post.getLikes()+1:post.getLikes()-1);
                                list.set(position,mPost);
                                notifyItemChanged(position);
                                notifyDataSetChanged();
                            }else{
                                holder.btnLike.setImageResource(
                                        post.isSelfLike()? R.drawable.ic_favorite_red_24dp : R.drawable.ic_favorite_outline_24dp
                                );
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }){

                    //token
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        String token = preferences.getString("token","");
                        HashMap<String,String> map = new HashMap<>();
                        map.put("Authorization","Bearer "+token);
                        return map;
                    }

                    //params

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String,String> map = new HashMap<>();
                        map.put("id",post.getId()+"");
                        return map;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
            }

        });

        if (post.getUser().getId() == preferences.getInt("id",0)){
            holder.btnPostOption.setVisibility(View.VISIBLE);
        }else{
            holder.btnPostOption.setVisibility(View.GONE);
        }



        holder.btnPostOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context,holder.btnPostOption);
                popupMenu.inflate(R.menu.menu_post_options);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){
                            case R.id.item_edit: {

                                Intent i = new Intent(((HomeActivity)context), EditPostActivity.class);
                                i.putExtra("postId",post.getId());
                                i.putExtra("position",position);
                                i.putExtra("text",post.getDesc());
                                context.startActivity(i);
                                return  true;

                            }
                            case R.id.item_delete: {

                                deletePost(post.getId(),position);
                                return  true;

                            }
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        holder.txtComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(((HomeActivity)context),CommentActivity.class);
                i.putExtra("postId",post.getId());
                i.putExtra("postPosition",position);
                context.startActivity(i);
            }
        });

        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(((HomeActivity)context), CommentActivity.class);
                i.putExtra("postId",post.getId());
                i.putExtra("postPosition",position);
                context.startActivity(i);
            }
        });

    }

    private void deletePost(final int id, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage("Delete post");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                StringRequest request = new StringRequest(Request.Method.POST, Constant.Delete_post, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object = new JSONObject(response);
                            if (object.getBoolean("success")){
                                list.remove(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                                listAll.clear();
                                listAll.addAll(list);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }){
                    //add token
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        String token = preferences.getString("token","");
                        HashMap<String,String> map = new HashMap<>();
                        map.put("Authorization","Bearer "+token);
                        return map;
                    }

                    //delete
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String,String> map = new HashMap<>();
                        map.put("id",id+"");
                        return  map;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Post> filteredList = new ArrayList<>();
            if (charSequence.toString().isEmpty()){
                filteredList.addAll(listAll);
            }else{
                for (Post post: listAll){
                    if (post.getDesc().toLowerCase().contains(charSequence.toString().toLowerCase())
                    || post.getUser().getUserName().toLowerCase().contains(charSequence.toString().toLowerCase())){
                        filteredList.add(post);
                    }
                }

            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            list.clear();
            list.addAll((Collection<? extends Post>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public Filter getFilter(){
        return  filter;
    }

    class PostHolder extends RecyclerView.ViewHolder{

        private TextView txtName, txtDate, txtDesc, txtLikes, txtComments;
        private CircleImageView imgProfile;
        private ImageView imgPost;
        private ImageButton btnPostOption,btnLike,btnComment;

        public PostHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPostName);
            txtDate = itemView.findViewById(R.id.txtPostDate);
            txtDesc = itemView.findViewById(R.id.txtPostDesc);
            txtLikes = itemView.findViewById(R.id.txtPostLikes);
            txtComments = itemView.findViewById(R.id.txtPostComments);
            imgProfile = itemView.findViewById(R.id.imgPostProfile);
            imgPost = itemView.findViewById(R.id.imgPostPhoto);
            btnPostOption = itemView.findViewById(R.id.btnPostOption);
            btnLike = itemView.findViewById(R.id.btnPostLike);
            btnComment = itemView.findViewById(R.id.btnPostComment);
            btnPostOption.setVisibility(View.GONE);
        }
    }

}
