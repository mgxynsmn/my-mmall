package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

public interface IUserService {

    ServerResponse<User> login(String username,String password);

    ServerResponse<String> checkValid(String str,String type);

    ServerResponse<String> register(User user);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkedQuestion(String username,String question,String answer);

    ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken);

    ServerResponse<String> restPassword(User user,String passwordOld,String passwordNew);

    ServerResponse<User>   updateInformation(User user);

    ServerResponse<User> getUserInformation(Integer userId);

    ServerResponse checkAdminRole(User user);
}
