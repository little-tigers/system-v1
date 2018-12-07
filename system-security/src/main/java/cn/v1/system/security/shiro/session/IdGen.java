package cn.v1.system.security.shiro.session;

import cn.v1.framework.utils.IdGenUtil;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.IdGenerator;

import java.io.Serializable;
import java.util.UUID;

/**
 * @Auther: wr
 * @Date: 2018/11/5
 * @Description:
 */
@Service
@Lazy(false)
public class IdGen implements SessionIdGenerator {
    @Override
    public Serializable generateId(Session session) {
        return IdGenUtil.uuid();
    }
}
