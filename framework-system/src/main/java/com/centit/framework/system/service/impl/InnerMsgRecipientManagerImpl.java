package com.centit.framework.system.service.impl;

import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.core.common.ObjectException;
import com.centit.framework.core.dao.PageDesc;
import com.centit.framework.core.dao.QueryParameterPrepare;
import com.centit.framework.model.adapter.MessageSender;
import com.centit.framework.model.basedata.IUnitInfo;
import com.centit.framework.model.basedata.IUserInfo;
import com.centit.framework.system.dao.InnerMsgDao;
import com.centit.framework.system.dao.InnerMsgRecipientDao;
import com.centit.framework.system.po.InnerMsg;
import com.centit.framework.system.po.InnerMsgRecipient;
import com.centit.framework.system.service.InnerMsgRecipientManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;

@Service("innerMsgRecipientManager")
public class InnerMsgRecipientManagerImpl implements InnerMsgRecipientManager, MessageSender {

    @Resource
    @NotNull
    protected InnerMsgRecipientDao innerMsgRecipientDao;

    @Resource
    private InnerMsgDao innerMsgDao;

    /*
     * 更新接受者信息
     * 
     */
    @Override
    @Transactional
    public void mergeRecipient(InnerMsgRecipient recipientCopy,
                               InnerMsgRecipient recipient) {
        innerMsgRecipientDao.deleteObject(recipientCopy);
        innerMsgRecipientDao.mergeObject(recipient);

    }

    /*
     * msg为消息主题，recipient为接收人
     * 添加消息接受者,群发(receipient.receive为数组，但是保存到数据库是挨个保存)
     * 
     */
    @Override
    @Transactional
    public void sendMsg(InnerMsgRecipient recipient, String sysUserCode) {
        String receive = recipient.getReceive();
        String receives[] = StringUtils.split(receive, ",");
        InnerMsg msg = recipient.getMInnerMsg();
        //拆分recieve   
        if (!StringUtils.isNotBlank(msg.getSender())) {
            msg.setSender(sysUserCode);
            //msg.setSenderName(CodeRepositoryUtil.getUserInfoByCode(sysUserCode).getUserName());
            if (null == msg.getSendDate()) {
                msg.setSendDate(new Date());
            }
        }
        sendToMany(receives, msg, recipient);
    }

    public void sendToMany(String[] receives, InnerMsg msg, InnerMsgRecipient recipient) {
        if (null != receives && receives.length > 0) {
            String receiveName = "";
            int bo = 0;
            for (String userCode : receives) {
                if (bo > 0) {
                    receiveName += ",";
                }
                receiveName += CodeRepositoryUtil.getUserInfoByCode(userCode).getUserName();
                bo++;
            }
            msg.setReceiveName(receiveName);
            innerMsgDao.saveNewObject(msg);
            recipient.setMInnerMsg(msg);
            //DataPushSocketServer.pushMessage(msg.getSender(), "你发送邮件："+ msg.getMsgTitle());
            for (String userCode : receives) {
                recipient.setReceive(userCode);
                innerMsgRecipientDao.saveNewObject(recipient);
                //DataPushSocketServer.pushMessage(userCode, "你有新邮件：" + recipient.getMsgTitle());
            }
        }
    }


    /*
     * 获取两者间来往消息列表
     * 
     */
    @Override
    @Transactional
    public List<InnerMsgRecipient> getExchangeMsgs(String sender, String receiver) {
        Map<String, String> map = new HashMap<>();
        map.put("sender", sender);
        map.put("receiver", receiver);
        return innerMsgRecipientDao.getExchangeMsgs(map);
    }

    /*
     * 给部门成员，所有直属或间接下级部门成员发消息
     */
    @Override
    @Transactional
    public void noticeByUnitCode(String unitCode, InnerMsg msg) throws ObjectException {

        List<IUnitInfo> unitList = CodeRepositoryUtil.getSubUnits(unitCode);
        //(ArrayList<UnitInfo>) unitDao.listAllSubUnits(unitCode);
        List<IUserInfo> userList = new ArrayList<>();
        for (IUnitInfo ui : unitList) {
            userList.addAll(CodeRepositoryUtil.getUnitUsers(ui.getUnitCode()));
        }

        if (userList.size() > 0) {
            String receiveName = CodeRepositoryUtil.getUnitName(unitCode);
            msg.setReceiveName(receiveName);
            msg.setMsgType("A");
            innerMsgDao.saveNewObject(msg);

            InnerMsgRecipient recipient = new InnerMsgRecipient();
            recipient.setMsgState(msg.getMsgState());
            recipient.setMailType(msg.getMailType());
            recipient.setMInnerMsg(msg);
            for (IUserInfo ui : userList) {
                recipient.setReceive(ui.getUserCode());
                innerMsgRecipientDao.saveNewObject(recipient);
                //DataPushSocketServer.pushMessage(ui.getUserCode(), "你有新邮件：" + recipient.getMsgTitle());
            }
        } else {
            throw new ObjectException("该机构中暂无用户");
        }
    }

    @Override
    //返回消息持有者数量
    @Transactional
    public void deleteOneRecipientById(String id) {
        InnerMsgRecipient re = innerMsgRecipientDao.getObjectById(id);
        re.setMsgState("D");
        innerMsgRecipientDao.saveObject(re);
    }


    @Override
    @Transactional
    public long getUnreadMessageCount(String userCode) {
        return innerMsgRecipientDao.getUnreadMessageCount(userCode);
    }

    @Override
    @Transactional
    public List<InnerMsgRecipient> listUnreadMessage(String userCode) {
        return innerMsgRecipientDao.listUnreadMessage(userCode);
    }


    /**
     * 发送消息
     */
    @Override
    @Transactional
    public String sendMessage(String sender, String receiver,
                              String msgSubject, String msgContent, String optId,
                              String optMethod, String optTag) {
        InnerMsg msg = new InnerMsg(sender, msgSubject, msgContent);
        msg.setSendDate(new Date());
        msg.setMsgType("P");
        msg.setMailType("O");
        msg.setMsgState("U");
        msg.setOptId(optId);
        msg.setReceiveName(CodeRepositoryUtil.getUserInfoByCode(receiver).getUserName());
        msg.setOptMethod(optMethod);
        msg.setOptTag(optTag);
        InnerMsgRecipient recipient = new InnerMsgRecipient();
        recipient.setMInnerMsg(msg);
        recipient.setReplyMsgCode(0);
        recipient.setReceiveType("P");
        recipient.setMailType("T");
        recipient.setMsgState("U");
        String[] receives = new String[]{receiver};
        sendToMany(receives, msg, recipient);
        return "OK";
    }

    @Override
    @Transactional
    public String sendMessage(String sender, String receiver,
                              String msgSubject, String msgContent) {
        return sendMessage(sender, receiver,
                msgSubject, msgContent, "msg",
                "sender", "");
    }

    @Override
    public List<InnerMsgRecipient> listObjects(Map<String, Object> filterMap) {
        return innerMsgRecipientDao.listObjects(filterMap);
    }

    @Override
    public List<InnerMsgRecipient> listObjects(Map<String, Object> filterMap, PageDesc pageDesc) {
        return innerMsgRecipientDao.pageQuery(QueryParameterPrepare.prepPageParmers(filterMap, pageDesc, innerMsgRecipientDao.pageCount(filterMap)));
    }

    @Override
    public InnerMsgRecipient getObjectById(String id) {
        return innerMsgRecipientDao.getObjectById(id);
    }
}
