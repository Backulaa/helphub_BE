package com.helphub.backend.security.jwt;

import com.helphub.backend.persistence.entity.User;
import com.helphub.backend.persistence.repository.ConversationMemberRepository;
import com.helphub.backend.persistence.repository.UserRepository;
import com.helphub.backend.security.model.WebSocketUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private static final Pattern CONVERSATION_MESSAGES_TOPIC = Pattern.compile(
            "^/topic/conversations/([0-9a-fA-F-]{36})/messages$");

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!Boolean.TRUE.equals(user.getIsActive())) {
                throw new IllegalArgumentException("User account is inactive");
            }

            if (!jwtService.isTokenValid(token, user.getEmail())) {
                throw new IllegalArgumentException("Invalid JWT token");
            }

            accessor.setUser(new WebSocketUserPrincipal(user.getId(), user.getEmail()));
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validateSubscription(accessor);
        }

        return message;
    }

    private void validateSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        Matcher matcher = CONVERSATION_MESSAGES_TOPIC.matcher(destination);
        if (!matcher.matches()) {
            return;
        }

        if (!(accessor.getUser() instanceof WebSocketUserPrincipal principal)) {
            throw new IllegalArgumentException("Missing websocket user");
        }

        UUID conversationId = UUID.fromString(matcher.group(1));
        boolean isMember = conversationMemberRepository.existsByConversationIdAndUserId(
                conversationId,
                principal.getUserId());

        if (!isMember) {
            throw new IllegalArgumentException("Not authorized to subscribe to this conversation");
        }
    }
}
