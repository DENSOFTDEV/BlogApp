package com.densoft.blogapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.densoft.blogapp.Adapters.PostsAdapter;
import com.densoft.blogapp.Constant;
import com.densoft.blogapp.HomeActivity;
import com.densoft.blogapp.R;
import com.densoft.blogapp.model.Post;
import com.densoft.blogapp.model.User;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    public View view;
    public static RecyclerView recyclerView;
    public static ArrayList<Post> arrayList;
    private SwipeRefreshLayout refreshLayout;
    private PostsAdapter postsAdapter;
    private MaterialToolbar toolbar;
    private SharedPreferences sharedPreferences;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        init();
        return  view;
    }

    private void init() {
        sharedPreferences = getContext().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.recyclerHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout = view.findViewById(R.id.swipeHome);
        toolbar = view.findViewById(R.id.toolbarHome);
        ((HomeActivity)getContext()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        getPosts();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPosts();
            }
        });
    }

    private void getPosts() {

        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);

        StringRequest request = new StringRequest(Request.Method.GET, Constant.Posts, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getBoolean("success")){
                        JSONArray array = new JSONArray(object.getString("posts"));
                        for (int i = 0 ; i < array.length(); i++){
                            JSONObject postObject = array.getJSONObject(i);
                            JSONObject userObject = postObject.getJSONObject("user");

                            User user = new User();
                            user.setId(userObject.getInt("id"));
                            user.setUserName(userObject.getString("name"+" "+userObject.getString("lastname")));
                            user.setPhoto(userObject.getString("photo"));

                            Post post = new Post();
                            post.setId(userObject.getInt("id"));
                            post.setUser(user);
                            post.setLikes(postObject.getInt("likesCount"));
                            post.setComments(postObject.getInt("commentsCount"));
                            post.setDate(postObject.getString("created_at"));
                            post.setDesc(postObject.getString("desc"));
                            post.setPhoto(postObject.getString("photo"));
                            post.setSelfLike(postObject.getBoolean("selfLike"));

                            arrayList.add(post);
                        }

                        postsAdapter = new PostsAdapter(getContext(),arrayList);
                        recyclerView.setAdapter(postsAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                refreshLayout.setRefreshing(false);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                refreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            //provide token in header

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token","");
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }
        };

        RequestQueue  queue = Volley.newRequestQueue(getContext());
        queue.add(request);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search,menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                postsAdapter.getFilter().filter(s);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
}
