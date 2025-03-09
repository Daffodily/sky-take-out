package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    public static final String WX_AUTH_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return
     */
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 调用微信的接口,获取当前微信用户的openid
        String url = getUrl(userLoginDTO);

        // 使用 HttpClient 创建请求
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {

            // 获取响应内容
            String responseString = EntityUtils.toString(response.getEntity());

            JSONObject jsonObject = JSONObject.parseObject(responseString);

            String openid = jsonObject.getString("openid");
            if (openid == null) {
                throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
            }
            User user = userMapper.FindByOpenId(openid);

            if (user == null) {
                user = User.builder()
                        .openid(openid)
                        .createTime(LocalDateTime.now())
                        .build();
                userMapper.insert(user);
            }
            return user;

        } catch (Exception e) {
            // 捕获并处理异常
            log.error("调用微信 API 出现异常: {}", e.getMessage(), e);  // 使用日志框架记录异常
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
    }

    private String getUrl(UserLoginDTO userLoginDTO) {
        // 使用 weChatProperties
        return UriComponentsBuilder.fromHttpUrl(WX_AUTH_URL)
                .queryParam("appid", weChatProperties.getAppid())  // 使用 weChatProperties
                .queryParam("secret", weChatProperties.getSecret())
                .queryParam("js_code", userLoginDTO.getCode())
                .queryParam("grant_type", "authorization_code")
                .toUriString();
    }
}
