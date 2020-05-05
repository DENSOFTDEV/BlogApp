package com.densoft.blogapp;

import com.android.volley.toolbox.StringRequest;
import com.densoft.blogapp.model.Post;

public class Constant {

    public static  final String url = "https://densoftdevelopers.com/projects/blogapi/public/";
    public static  final String Home = url+"api";
    public static  final String Login = Home+"/login";
    public static  final String Register = Home+"/register";
    public static final String Save_user_info = Home+"/save_user_info";
    public static final String Posts = Home+"/posts";
    public static final String Add_post = Posts+"/create";
    public static final String Update_post = Posts+"/update";
    public static final String Delete_post = Posts+"/delete";
    public static final String Like_post = Posts+"/like";
    public static final String Comments = Posts+"/comments";
    public static final String Create_comment = Home+"/comments/create";
    public static final String Delete_comment = Home+"/comments/delete";


}
