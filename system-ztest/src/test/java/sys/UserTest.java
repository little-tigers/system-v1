package sys;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;
import cn.v1.system.pojo.SysUser;
import cn.v1.system.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * @Auther: wr
 * @Date: 2018/11/1
 * @Description:
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:META-INF/spring/*.xml")
public class UserTest {

    @Autowired
    private SysUserService sysUserService;

    @Test
    public void getById(){
        SysUser User = sysUserService.getById("1009");
        System.out.printf(User.toString());

    }

    @Test
    public void update(){
        SysUser User = sysUserService.getById("1009");
        sysUserService.save(User);
    }

    @Test
    public void getList(){
      List<SysUser> list =   sysUserService.getList();
      for(SysUser User : list){
      }
    }

    @Test
    public void getPage(){
        Page<SysUser> page = sysUserService.getPage(new SysUser(), new PageBounds(0,20));
        System.out.printf(page.getResult().size()+"");
    }

}
