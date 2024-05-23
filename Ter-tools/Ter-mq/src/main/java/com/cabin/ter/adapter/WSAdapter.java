package com.cabin.ter.adapter;

import com.cabin.ter.constants.dto.EmailBindingDTO;
import com.cabin.ter.constants.enums.WSRespTypeEnum;
import com.cabin.ter.constants.vo.response.WSBaseResp;
import com.cabin.ter.constants.vo.response.WSLoginSuccess;
import com.cabin.ter.constants.vo.response.WsLoginUrl;
import com.cabin.ter.vo.UserPrincipal;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;

/**
 * <p>
 *     消息适配器
 * </p>
 * @author xiaoye
 * @date Created in 2024-05-10 10:16
 */
public class WSAdapter {
    public static WSBaseResp<Object> buildLoginResp(WxMpQrCodeTicket wxMpQrCodeTicket ){
        WSBaseResp<Object> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WSRespTypeEnum.LOGIN_URL.getType());
        wsBaseResp.setData(WsLoginUrl.builder().loginUrl(wxMpQrCodeTicket.getUrl()).build());
        return wsBaseResp;
    }

    public static WSBaseResp buildScanSuccessResp() {
        WSBaseResp wsBaseResp = new WSBaseResp();
        wsBaseResp.setType(WSRespTypeEnum.LOGIN_SCAN_SUCCESS.getType());
        return wsBaseResp;
    }

    public static WSBaseResp buildEmailBindingResp(EmailBindingDTO emailBindingDTO){
        WSBaseResp wsBaseResp = new WSBaseResp();
        wsBaseResp.setType(WSRespTypeEnum.LOGIN_EMAIL_BINDING.getType());
        wsBaseResp.setData(emailBindingDTO);
        return wsBaseResp;
    }

    public static WSBaseResp buildLoginSuccessResp(UserPrincipal userPrincipal, String token){
        WSBaseResp<WSLoginSuccess> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WSRespTypeEnum.LOGIN_SUCCESS.getType());
        WSLoginSuccess wsLoginSuccess = WSLoginSuccess.builder()
                .avatar(userPrincipal.getUserAvatar())
                .name(userPrincipal.getUsername())
                .token(token)
                .uid(userPrincipal.getUserId())
                .build();
        wsBaseResp.setData(wsLoginSuccess);
        return wsBaseResp;
    }
}
