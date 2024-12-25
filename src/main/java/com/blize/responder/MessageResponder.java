package com.blize.responder;

import com.blize.document.Message;
import com.blize.document.result.MessageGroupItem;
import com.blize.entity.User;
import com.blize.repository.UserRepository;
import com.blize.dto.response.MessageGroupItemResponseDTO;
import com.blize.dto.response.MessageResponseDTO;
import com.blize.dto.response.MessagesDetailResponseDTO;
import com.blize.service.MongoMessagesService;
import com.blize.service.SocketService;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MessageResponder {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MongoMessagesService mongoMessagesService;
    @Autowired
    private SocketService socketService;

    public List<MessageGroupItemResponseDTO> groupList(User user, int offset) {
        List<MessageGroupItem> messageGroupItems = mongoMessagesService.groupList(user, offset);
        var fromIds = messageGroupItems.stream().map(MessageGroupItem::getFrom).toList();
        var toIds = messageGroupItems.stream().map(MessageGroupItem::getTo).toList();
        var userIds = Stream.concat(fromIds.stream(), toIds.stream()).collect(Collectors.toSet()).stream().toList();
        var users = userRepository.findUsersByIdIn(userIds);
        var usersHashMap = new HashMap<Integer, User>();
        List<MessageGroupItemResponseDTO> responseItems = new ArrayList<>();
        for (User usr : users) {
            usersHashMap.put(usr.getId(), usr);
        }

        for (MessageGroupItem messageGroupItem : messageGroupItems) {
            User fromUser = usersHashMap.get(messageGroupItem.getFrom());
            User toUser = usersHashMap.get(messageGroupItem.getTo());

            var responseItem = new MessageGroupItemResponseDTO();
            responseItem.setFrom(fromUser);
            responseItem.setTo(toUser);
            responseItem.setText(messageGroupItem.getText());
            responseItem.setDate(messageGroupItem.getDate());
            responseItem.setRead(messageGroupItem.isRead());
            responseItem.setUnReadCount(messageGroupItem.getUnReadCount());
            responseItems.add(responseItem);
        }

        return responseItems;

    }

    public MessagesDetailResponseDTO detail(User user, int otherUserId, int offset) {

        var otherUser = userRepository.findById(otherUserId);
        List<Message> messageList = mongoMessagesService.detail(user, otherUser, offset);

        var messageDetailResponse = new MessagesDetailResponseDTO();
        messageDetailResponse.setTo(otherUser);
        messageDetailResponse.setMessages(new ArrayList<>());

        for (Message messageItem : messageList) {

            var messageResponse = new MessageResponseDTO();
            messageResponse.setId(messageItem.getId());
            messageResponse.setFrom(user.getId() == messageItem.getFrom() ? user : otherUser);
            messageResponse.setTo(user.getId() == messageItem.getTo() ? user : otherUser);
            messageResponse.setText(messageItem.getText());
            messageResponse.setDate(messageItem.getDate());
            messageResponse.setRead(messageItem.isRead());

            messageDetailResponse.getMessages().add(messageResponse);
        }

        return messageDetailResponse;

    }

    public MessageResponseDTO add(User user, int otherUserId, String text) {
        var otherUser = userRepository.findById(otherUserId);
        var message = mongoMessagesService.add(user, otherUser, text);

        var messageResponse = new MessageResponseDTO();
        messageResponse.setId(message.getId());
        messageResponse.setFrom(user);
        messageResponse.setTo(otherUser);
        messageResponse.setText(message.getText());
        messageResponse.setDate(message.getDate());
        messageResponse.setRead(message.isRead());

        socketService.sendMessage(otherUser, messageResponse);

        return messageResponse;
    }

    public UpdateResult read(User user, int otherUserId) {
        var otherUser = userRepository.findById(otherUserId);
        var res = mongoMessagesService.read(user, otherUser);

        socketService.sendRead(otherUser, user);

        return res;
    }
}
