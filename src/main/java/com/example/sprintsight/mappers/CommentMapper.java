package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.CommentRequest;
import com.example.sprintsight.dtos.responses.CommentResponse;
import com.example.sprintsight.entities.Comment;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = UserMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CommentMapper {
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "issue",     ignore = true)
    @Mapping(target = "author",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CommentRequest request);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "issue",     ignore = true)
    @Mapping(target = "author",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCommentFromRequest(CommentRequest request, @MappingTarget Comment comment);

    CommentResponse toCommentResponse(Comment comment);
}
