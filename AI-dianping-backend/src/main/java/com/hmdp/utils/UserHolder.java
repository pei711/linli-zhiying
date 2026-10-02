package com.hmdp.utils;

import com.hmdp.dto.UserDTO;

public class UserHolder {
//    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();
//
//    public static void saveUser(UserDTO user){
//        tl.set(user);
//    }
//
//    public static UserDTO getUser(){
//        return tl.get();
//    }
//
//    public static void removeUser(){
//        tl.remove();
//    }

    // 将 ThreadLocal 修改为 InheritableThreadLocal
    private static final ThreadLocal<UserDTO> tl = new InheritableThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
