package com.roncoo.education.course.service.common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 广告信息
 *
 * @author wujing
 */
@Data
@Accessors(chain = true)
public class AdvListDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "广告信息集合")
	private List<AdvDTO> advList =  new ArrayList<>();
}
