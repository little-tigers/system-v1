package sys;

import cn.v1.framework.page.Page;
import cn.v1.framework.page.PageBounds;
import cn.v1.system.pojo.SysRole;
import cn.v1.system.service.SysRoleService;
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
public class RoleTest {

    @Autowired
    private SysRoleService sysRoleService;

    @Test
    public void getById(){
        SysRole role = sysRoleService.getById("1009");
        System.out.printf(role.toString());

    }

    @Test
    public void update(){
        SysRole role = sysRoleService.getById("1009");
        sysRoleService.save(role);
    }

    @Test
    public void getList(){
      List<SysRole> list =  sysRoleService.getList();
        System.out.printf(list.get(0).getUserList().get(0).getName());
        System.out.printf(list.size()+"");
    }

    @Test
    public void getPage(){
        Page<SysRole> page = sysRoleService.getPage(new SysRole(), new PageBounds(0,20));
        System.out.printf(page.getResult().size()+"");
    }



}
