package com.cwa.server.logic.module.match.cache;

import com.cwa.ISession;

/**
 * 副本缓存类
 * 
 * @author tzy
 * 
 */
public class MatchCache {
	private int matchType;// 关卡类型
	private int matchKeyId;// 关卡id
	private long startTime;// 开始时间
	private boolean check;// 是否检查过
	private ISession session;

	public int getMatchType() {
		return matchType;
	}

	public void setMatchType(int matchType) {
		this.matchType = matchType;
	}

	public int getMatchKeyId() {
		return matchKeyId;
	}

	public void setMatchKeyId(int matchKeyId) {
		this.matchKeyId = matchKeyId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public ISession getSession() {
		return session;
	}

	public void setSession(ISession session) {
		this.session = session;
	}

}
