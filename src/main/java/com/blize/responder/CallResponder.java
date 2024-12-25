package com.blize.responder;

import com.blize.document.Call;
import com.blize.dto.response.CallResponseDTO;
import com.blize.entity.User;
import com.blize.repository.UserRepository;
import com.blize.service.MongoCallService;
import com.blize.service.SocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CallResponder {

    @Autowired
    UserRepository userRepository;
    @Autowired
    MongoCallService mongoCallService;
    @Autowired
    SocketService socketService;

    public CallResponseDTO call(User user, int otherUserId, boolean video) {
        var otherUser = userRepository.findById(otherUserId);
        Call call = mongoCallService.call(user, otherUser, video);
        if (call == null) {
            return null;
        }

        var responseDTO = new CallResponseDTO();
        responseDTO.setId(call.getId());
        responseDTO.setFrom(user);
        responseDTO.setTo(otherUser);
        responseDTO.setVideo(call.isVideo());
        responseDTO.setDate(call.getDate());

        socketService.sendCall(otherUser, responseDTO);

        return responseDTO;
    }

    public CallResponseDTO accept(User user,  int otherUserId, String _id) {
        var otherUser = userRepository.findById(otherUserId);
        Call call = mongoCallService.accept(user, _id);
        if (call == null) {
            return null;
        }

        var responseDTO = new CallResponseDTO();
        responseDTO.setId(call.getId());
        responseDTO.setFrom(otherUser);
        responseDTO.setTo(user);
        responseDTO.setVideo(call.isVideo());
        responseDTO.setDate(call.getDate());
        responseDTO.setStartDate(call.getStartDate());

        socketService.sendAnswer(otherUser, responseDTO);

        return responseDTO;
    }

    public CallResponseDTO close(User user,  int otherUserId, String _id) {
        var otherUser = userRepository.findById(otherUserId);
        Call call = mongoCallService.close(user, otherUser,_id);
        if (call == null) {
            return null;
        }

        var responseDTO = new CallResponseDTO();
        responseDTO.setId(call.getId());
        responseDTO.setFrom(otherUser.getId() == call.getFrom() ? otherUser : user);
        responseDTO.setTo(user.getId() == call.getTo() ? user : otherUser);
        responseDTO.setVideo(call.isVideo());
        responseDTO.setDate(call.getDate());
        responseDTO.setStartDate(call.getStartDate());
        responseDTO.setEndDate(call.getEndDate());

        socketService.sendCallEnd(otherUser, responseDTO);

        return responseDTO;
    }

}
