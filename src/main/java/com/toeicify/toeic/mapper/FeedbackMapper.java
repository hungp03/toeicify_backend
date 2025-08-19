package com.toeicify.toeic.mapper;

/**
 * Created by hungpham on 8/19/2025
 */
import com.toeicify.toeic.dto.response.feedback.FeedbackResponse;
import com.toeicify.toeic.entity.Feedback;
import com.toeicify.toeic.entity.FeedbackAttachment;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(source = "user.fullName", target = "userName")
    @Mapping(target = "attachments", expression = "java(mapAttachments(feedback))")
    FeedbackResponse toResponse(Feedback feedback);

    List<FeedbackResponse> toResponseList(List<Feedback> feedbackList);

    default List<String> mapAttachments(Feedback feedback) {
        if (feedback.getAttachments() == null) {
            return List.of();
        }
        return feedback.getAttachments().stream()
                .map(FeedbackAttachment::getUrl)
                .toList();
    }
}

