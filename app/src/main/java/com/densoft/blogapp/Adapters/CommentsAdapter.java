package com.densoft.blogapp.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.densoft.blogapp.Fragments.HomeFragment;
import com.densoft.blogapp.R;
import com.densoft.blogapp.model.Comment;
import com.densoft.blogapp.model.Post;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsHolder> {

    private Context context;
    private ArrayList<Comment> list;
    private SharedPreferences preferences;
    private ProgressDialog dialog;

    public CommentsAdapter(Context context, ArrayList<Comment> list) {
        this.context = context;
        this.list = list;
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        preferences = context.getSharedPreferences("user",Context.MODE_PRIVATE);
    }


    @NonNull
    @Override
    public CommentsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comment,parent,false);
        return new CommentsHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsHolder holder, final int position) {
        final Comment comment = list.get(position);
        Picasso.get().load(comment.getUser().getPhoto()).into(holder.imgProfile);
        holder.txtName.setText(comment.getUser().getUserName());
        holder.txtDate.setText(comment.getDate());
        holder.txtComment.setText(comment.getComment());

        if (preferences.getInt("id", 0 ) != comment.getUser().getId()){
            holder.btnDelete.setVisibility(View.GONE);
        }else{

            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Are you sure?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteComment(comment.getId(),position);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    builder.show();
                }
            });
        }

    }

    private void deleteComment(final int id, final int position) {
        dialog.setMessage("Deleting Comment");
        dialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, Constant.Delete_comment, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getBoolean("success")){
                        list.remove(position);
                        Post post = HomeFragment.arrayList.get(CommentActivity.postposition);
                        post.setComments(post.getComments()-1);
                        HomeFragment.arrayList.set(CommentActivity.postposition,post);
                        HomeFragment.recyclerView.getAdapter().notifyDataSetChanged();
                        notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }){
            //add token to
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = preferences.getString("token", "");
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }

            //params

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("id", id + "");
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class CommentsHolder extends RecyclerView.ViewHolder{

        private CircleImageView imgProfile;
        private TextView txtName,txtDate,txtComment;
        private ImageButton btnDelete;

        public CommentsHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.imgCommentProfile);
            txtName = itemView.findViewById(R.id.txtCommentName);
            txtDate = itemView.findViewById(R.id.txtCommentDate);
            txtComment = itemView.findViewById(R.id.txtCommentText);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);
        }
    }
}
