package com.blize.service;

import com.blize.dto.response.CallResponseDTO;
import com.blize.entity.User;
import com.blize.dto.response.MessageResponseDTO;
import de.smartsquare.socketio.emitter.Emitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SocketService {
    @Autowired
    private Emitter emitter;

    public void sendMessage(User user, MessageResponseDTO message){

        this.emitter.broadcast("message", message, List.of(user.getUsername()));
    }

    public void sendRead(User user, User sessionUser){

        this.emitter.broadcast("read", Map.of("from", sessionUser), List.of(user.getUsername()));
    }

    public void sendCall(User user, CallResponseDTO callResponseDTO){

        this.emitter.broadcast("call", callResponseDTO, List.of(user.getUsername()));
    }

    public void sendAnswer(User user, CallResponseDTO callResponseDTO){

        this.emitter.broadcast("accept", callResponseDTO, List.of(user.getUsername()));
    }

    public void sendCallEnd(User user, CallResponseDTO callResponseDTO){

        this.emitter.broadcast("call-end", callResponseDTO, List.of(user.getUsername()));
    }
}
