package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {

        int count = userMapper.checkUserName(username);
        if (count==0)
            return ServerResponse.createByErrorMessage("用户名不存在");


        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.login(username, md5Password);
        if (user == null)
            return ServerResponse.createByErrorMessage("密码错误");


        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
          if (StringUtils.isNoneBlank(type)) {
              if (Const.USERNAME.equals(type)) {
                  int resultCount = userMapper.checkUserName(str);
                  if (resultCount>0){
                      return ServerResponse.createByErrorMessage("用户名已经存在");
                  }
              }
              if (Const.EMAIL.equals(type)){
                  int resultCount = userMapper.checkEmail(str);
                  if (resultCount>0){
                      return ServerResponse.createByErrorMessage("邮箱已存在");
                  }
              }
          }else{
              return ServerResponse.createByErrorMessage("参数错误");
          }

        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse<String> register(User user) {

        ServerResponse response = this.checkValid(user.getUsername(),Const.USERNAME);
        if (!response.isSuccess()){
            return response;
        }
        response = this.checkValid(user.getEmail(),Const.EMAIL);
        if (!response.isSuccess()){
            return response;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccess("注册成功");

    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {

        ServerResponse response = this.checkValid(username,Const.USERNAME);
        if (response.isSuccess()){
            return  ServerResponse.createByErrorMessage("用户名不存在");
        }
        String getQuestion = userMapper.getQuestionByUsername(username);

        if (StringUtils.isNotBlank(getQuestion)){
            return ServerResponse.createBySuccess(getQuestion);
        }

        return ServerResponse.createByErrorMessage("没有获取到您设置的问题");
    }

    @Override
    public ServerResponse<String> checkedQuestion(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount>0){
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题回答错误");
    }


    @Override
    public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
        if (StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("无法获取token的值");
        }

        ServerResponse response = this.checkValid(username,Const.USERNAME);
        if (response.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)){
           return ServerResponse.createByErrorMessage("Token过期或无效,请重新获取");
        }

        if(StringUtils.equals(forgetToken,token)){

            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("重置密码失败");
    }

    @Override
    public ServerResponse restPassword(User user,String passwordOld,String passwordNew){
        int resultCount = userMapper.ValidPassword(user.getId(),MD5Util.MD5EncodeUtf8(passwordOld));
        if (resultCount==0){
            return ServerResponse.createByErrorMessage("输入的旧密码错误");
        }
        User user1 = new User();
        user1.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        user1.setId(user.getId());
        int result = userMapper.updateByPrimaryKeySelective(user1);
        if (result>0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    public ServerResponse<User> updateInformation(User user){
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());

        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("email已存在,请更换email再尝试更新");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    @Override
    public ServerResponse<User> getUserInformation(Integer userId) {

        User user = userMapper.selectByPrimaryKey(userId);
        if (user==null){
            return ServerResponse.createByErrorMessage("该用户不存在");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }



    /**
     * 校验是否是管理员
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
