package com.roncoo.education.user.service.controller.biz;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.roncoo.education.user.common.bean.qo.LecturerAuditQO;
import com.roncoo.education.user.common.bean.vo.LecturerAuditVO;
import com.roncoo.education.user.common.bean.vo.LecturerExtVO;
import com.roncoo.education.user.service.dao.LecturerAuditDao;
import com.roncoo.education.user.service.dao.LecturerDao;
import com.roncoo.education.user.service.dao.LecturerExtDao;
import com.roncoo.education.user.service.dao.UserExtDao;
import com.roncoo.education.user.service.dao.impl.mapper.entity.Lecturer;
import com.roncoo.education.user.service.dao.impl.mapper.entity.LecturerAudit;
import com.roncoo.education.user.service.dao.impl.mapper.entity.LecturerAuditExample;
import com.roncoo.education.user.service.dao.impl.mapper.entity.LecturerAuditExample.Criteria;
import com.roncoo.education.user.service.dao.impl.mapper.entity.LecturerExt;
import com.roncoo.education.user.service.dao.impl.mapper.entity.UserExt;
import com.roncoo.education.util.base.BaseBiz;
import com.roncoo.education.util.base.BaseException;
import com.roncoo.education.util.base.Page;
import com.roncoo.education.util.base.PageUtil;
import com.roncoo.education.util.enums.AuditStatusEnum;
import com.roncoo.education.util.enums.UserTypeEnum;
import com.roncoo.education.util.tools.BeanUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;

/**
 * 讲师信息-审核
 *
 * @author wujing
 */
@Component
public class BossLecturerAuditBiz extends BaseBiz {

	@Autowired
	private LecturerAuditDao dao;
	@Autowired
	private LecturerDao lecturerDao;
	@Autowired
	private LecturerExtDao lecturerExtDao;
	@Autowired
	private UserExtDao userExtDao;

	public Page<LecturerAuditVO> listForPage(LecturerAuditQO qo) {
		LecturerAuditExample example = new LecturerAuditExample();
		Criteria c = example.createCriteria();
		Page<LecturerAudit> page = dao.listForPage(qo.getPageCurrent(), qo.getPageSize(), example);
		return PageUtil.transform(page, LecturerAuditVO.class);
	}

	public int save(LecturerAuditQO qo) {
		LecturerAudit record = BeanUtil.copyProperties(qo, LecturerAudit.class);
		return dao.save(record);
	}

	public int deleteById(Long id) {
		return dao.deleteById(id);
	}

	public LecturerAuditVO getById(Long id) {
		LecturerAudit record = dao.getById(id);
		LecturerAuditVO vo = BeanUtil.copyProperties(record, LecturerAuditVO.class);
		// 查找讲师账户信息
		LecturerExt lecturerExt = lecturerExtDao.getByLecturerUserNo(vo.getLecturerUserNo());
		vo.setLecturerExtVO(BeanUtil.copyProperties(lecturerExt, LecturerExtVO.class));
		return vo;
	}

	public int updateById(LecturerAuditQO qo) {
		LecturerAudit record = BeanUtil.copyProperties(qo, LecturerAudit.class);
		return dao.updateById(record);
	}

	/**
	 * 讲师审核
	 * 
	 * @param qo
	 * @return
	 * @author wuyun
	 */
	@Transactional
	public int audit(LecturerAuditQO qo) {
		LecturerAudit lecturerAudit = dao.getById(qo.getId());
		if (AuditStatusEnum.SUCCESS.getCode().equals(qo.getAuditStatus())) {
			// 查找讲师信息表，是否存在该讲师
			Lecturer lecturer = lecturerDao.getByLecturerUserNo(lecturerAudit.getLecturerUserNo());
			if (ObjectUtil.isNull(lecturer)) {
				// 插入
				lecturer = BeanUtil.copyProperties(lecturerAudit, Lecturer.class);
				lecturer.setGmtCreate(null);
				lecturer.setGmtModified(null);
				lecturerDao.save(lecturer);
			} else {
				// 更新
				lecturer = BeanUtil.copyProperties(lecturerAudit, Lecturer.class);
				lecturer.setGmtCreate(null);
				lecturer.setGmtModified(null);
				lecturerDao.updateById(lecturer);
			}
			// 查找用户信息是否存在
			UserExt userExt = userExtDao.getByUserNo(lecturer.getLecturerUserNo());
			if (ObjectUtil.isNull(userExt)) {
				throw new BaseException("获取不到用户信息");
			}
			// 存在更新为讲师类型
			userExt.setUserType(UserTypeEnum.LECTURER.getCode());
			userExtDao.updateById(userExt);
		}

		LecturerAudit record = BeanUtil.copyProperties(qo, LecturerAudit.class);
		return dao.updateById(record);
	}

	/**
	 * 手机号码校验用户信息表是否存在
	 * 
	 * @param qo
	 * @return
	 * @author wuyun
	 */
	public LecturerAuditVO checkUserAndLecturer(LecturerAuditQO qo) {
		// 手机号码校验
		if (!Pattern.compile(REGEX_MOBILE).matcher(qo.getLecturerMobile()).matches()) {
			throw new BaseException("手机号码格式不正确");
		}
		// 根据传入手机号获取用户信息(讲师的用户信息)
		UserExt userExt = userExtDao.getByMobile(qo.getLecturerMobile());
		LecturerAuditVO vo = new LecturerAuditVO();

		if (ObjectUtil.isNull(userExt)) {
			// 用户不存在
			vo.setCheckUserAndLecturer(1);
		} else {
			// 根据用户编号获取讲师信息
			LecturerAudit record = dao.getByLecturerUserNo(userExt.getUserNo());
			if (ObjectUtil.isNull(record)) {
				vo.setCheckUserAndLecturer(2);
			} else {
				vo.setCheckUserAndLecturer(3);
			}
		}
		return vo;
	}
}
