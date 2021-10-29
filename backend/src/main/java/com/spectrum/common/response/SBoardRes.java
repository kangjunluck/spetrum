package com.spectrum.common.response;

import com.spectrum.entity.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Getter
@Setter
@ApiModel("SBoardRes")
public class SBoardRes {

    @ApiModelProperty(name="id", example = "1")
    long id;

    @ApiModelProperty(name="content", example = "한줄sns내용")
    String content;

    @ApiModelProperty(name="like", example = "좋아요 수")
    int like;

    @ApiModelProperty(name="created", example = "작성 날짜")
    Date created;

    @ApiModelProperty(name="updated", example = "수정 날짜")
    Date updated;
}
