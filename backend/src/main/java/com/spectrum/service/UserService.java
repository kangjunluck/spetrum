package com.spectrum.service;

import com.spectrum.common.request.FollowReq;
import com.spectrum.common.request.UserRegisterPostReq;
import com.spectrum.common.request.UserUpdateReq;
import com.spectrum.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {

    void createUser(UserRegisterPostReq registerInfo) throws IOException;

    User findUserByUserId(String userId);

    User search(String userid);

    User searchNick(String usernickname);

    void updateUser(UserUpdateReq updateInfo, String userid) throws IOException;

    void deleteUser(String userid);

    void follow(FollowReq followReq);

    List<String> searchUserId(String userid);

    String followCheck(String userid, String myid);

    List<User> followList(String userid);
}
